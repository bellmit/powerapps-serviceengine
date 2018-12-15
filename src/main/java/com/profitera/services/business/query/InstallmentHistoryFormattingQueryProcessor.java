package com.profitera.services.business.query;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.loan.ILoanAccountService;
import com.profitera.services.system.loan.LoanAccount;
import com.profitera.services.system.loan.impl.ProjectedInstallment;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.BigDecimalUtil;
import com.profitera.util.MapListTable;
import com.profitera.util.MapListUtil;

public class InstallmentHistoryFormattingQueryProcessor extends
    BaseListQueryProcessor {
  private static final String PROJECT_FORWARD = "PROJECT_FORWARD";
  private static final String PENALTY_HISTORY_QUERY = "PENALTY_HISTORY_QUERY";
  private static final String PRINCIPAL_HISTORY_QUERY = "PRINCIPAL_HISTORY_QUERY";
  private static final String INTEREST_HISTORY_QUERY = "INTEREST_HISTORY_QUERY";
  private static final Map DUMMY_HISTORY = new HashMap();
  static {
    DUMMY_HISTORY.put("AMOUNT", BigDecimalUtil.ZERO);
    DUMMY_HISTORY.put("DUE_AMOUNT", BigDecimalUtil.ZERO);
    DUMMY_HISTORY.put("PAID_AMOUNT", BigDecimalUtil.ZERO);
  }

  {
    addRequiredProperty(INTEREST_HISTORY_QUERY, String.class, "Interest history query", "Interest history query");
    addRequiredProperty(PRINCIPAL_HISTORY_QUERY, String.class, "Principal history query", "Principal history query");
    addRequiredProperty(PENALTY_HISTORY_QUERY, String.class, "Penalty history query", "Penalty history query");
    addProperty(PROJECT_FORWARD, Boolean.class, Boolean.FALSE, "Project current state forward for the loan", 
        "Project installment schedule based on the current loan principal as an estimate.");
  } 
  
  public List postProcessResults(Map arguments, List result, IQueryService qs)
      throws TransferObjectException {
    if (result.size() > 1){
      throw new IllegalArgumentException("Only a single input row of balances should be provided");
    } else if (result.size() == 0){
      return result;
    }
    Map input = (Map) result.get(0);
    BigDecimal interest = (BigDecimal) getRequiredResultValue("INTEREST_BALANCE", BigDecimal.class, input);
    BigDecimal pOverdue = (BigDecimal) getRequiredResultValue("PRINCIPAL_OVERDUE_BALANCE", BigDecimal.class, input);
    BigDecimal penalty = (BigDecimal) getRequiredResultValue("PENALTY_BALANCE", BigDecimal.class, input);
    final Long accountId = (Long) getRequiredResultValue("ACCOUNT_ID", Long.class, input);
    // This was going to be written to aggregate for you but I realized that 
    // between the aggr. QP that is available and SQL group by it seems like
    // a waste of effort to do so here, do it your own damn self!
    List interestHistory = executePropertyQuery(INTEREST_HISTORY_QUERY, arguments, qs);
    List principalHistory = executePropertyQuery(PRINCIPAL_HISTORY_QUERY, arguments, qs);
    List penaltyHistory = executePropertyQuery(PENALTY_HISTORY_QUERY, arguments, qs);
    distributeFunds(INTEREST_HISTORY_QUERY, interest, interestHistory);
    distributeFunds(PENALTY_HISTORY_QUERY, penalty, penaltyHistory);
    distributeFunds(PRINCIPAL_HISTORY_QUERY, pOverdue, principalHistory);
    //
    final MapListTable fullHistory = new MapListTable();
    for (int i = 0; i < principalHistory.size(); i++) {
      Map pRow = (Map) principalHistory.get(i);
      Date pDate = (Date) pRow.get("DATE");
      int index = MapListUtil.firstIndexOf("DATE", pDate, interestHistory);
      Map iRow = DUMMY_HISTORY;
      if (index != -1){
        iRow = (Map) interestHistory.get(index);
      }
      index = MapListUtil.firstIndexOf("DATE", pDate, penaltyHistory);
      Map pnRow = DUMMY_HISTORY;
      if (index != -1){
        pnRow = (Map) penaltyHistory.get(index);
      }
      fullHistory.set("DATE", i, pDate);
      fullHistory.set("PRINCIPAL_AMOUNT", i, pRow.get("AMOUNT"));
      fullHistory.set("INTEREST_AMOUNT", i, iRow.get("AMOUNT"));
      fullHistory.set("PENALTY_AMOUNT", i, pnRow.get("AMOUNT"));
      //
      fullHistory.set("PRINCIPAL_PAID_AMOUNT", i, pRow.get("PAID_AMOUNT"));
      fullHistory.set("INTEREST_PAID_AMOUNT", i, iRow.get("PAID_AMOUNT"));
      fullHistory.set("PENALTY_PAID_AMOUNT", i, pnRow.get("PAID_AMOUNT"));
      //
      fullHistory.set("PRINCIPAL_DUE_AMOUNT", i, pRow.get("DUE_AMOUNT"));
      fullHistory.set("INTEREST_DUE_AMOUNT", i, iRow.get("DUE_AMOUNT"));
      fullHistory.set("PENALTY_DUE_AMOUNT", i, pnRow.get("DUE_AMOUNT"));
      //
      fullHistory.set("PROJECTED", i, Boolean.FALSE);
    }
    Object projectInstallmentsForward = getProperty(PROJECT_FORWARD);
    if (projectInstallmentsForward.equals(Boolean.TRUE)){
      try {
        getReadWriteProvider().execute(new IRunnableTransaction(){
          public void execute(ITransaction t) throws SQLException,
              AbortTransactionException {
            LoanAccount la = getLoanAccount(accountId);
            Date maxDate = null;
            if (fullHistory.getRowCount() > 0) {
              maxDate = (Date) fullHistory.get("DATE", fullHistory.getRowCount() - 1);
            } else {
              // This should be the last immediate interest posting date,
              // then fall back to loan creation date
              maxDate = la.getLastImmediateInterestPostedDate(getReadWriteProvider(), t);
              if (maxDate == null) {
                maxDate = la.getLoanFinancialAccountsCreatedDate(getReadWriteProvider());
              }
            }
            List<ProjectedInstallment> past = new ArrayList<ProjectedInstallment>();
            if (fullHistory.getRowCount() > 0) {
              int i = fullHistory.getRowCount() - 1;
              Date d = (Date) fullHistory.get("DATE", i);
              BigDecimal p = (BigDecimal) fullHistory.get("PRINCIPAL_AMOUNT", i);
              past.add(new ProjectedInstallment(d, p, BigDecimal.ZERO, BigDecimal.ZERO, d));
            }
            List<ProjectedInstallment> projected = la.getProjectedInstallments(past, getReadWriteProvider(), t);
            // If the last installment we have in the history ends up being partial then
            // we need to merge that data in with the first projection somehow
            if (projected.size() > 0 && fullHistory.getRowCount() > 0) {
              ProjectedInstallment p = projected.get(0);
              for(int i = 1;i < projected.size() && p.getDate().before(maxDate);i++){
                p = projected.get(i);
              }
              if (p.getDate().equals(maxDate)) {
                projected.remove(p);
                int row = fullHistory.getRowCount() - 1;
                // Principal
                BigDecimal pr = (BigDecimal) fullHistory.get("PRINCIPAL_AMOUNT", row);
                fullHistory.set("PRINCIPAL_AMOUNT", row, p.getPrincipal());
                BigDecimal pDifference = p.getPrincipal().subtract(pr);
                BigDecimal prDue = (BigDecimal) fullHistory.get("PRINCIPAL_DUE_AMOUNT", row);
                fullHistory.set("PRINCIPAL_DUE_AMOUNT", row, prDue.add(pDifference));
                // Interest
                BigDecimal in = (BigDecimal) fullHistory.get("INTEREST_AMOUNT", row);
                fullHistory.set("INTEREST_AMOUNT", row, p.getInterest());
                BigDecimal diff = p.getInterest().subtract(in);
                BigDecimal due = (BigDecimal) fullHistory.get("INTEREST_DUE_AMOUNT", row);
                fullHistory.set("INTEREST_DUE_AMOUNT", row, due.add(diff));
                // If it is partially projected then it is a projection
                fullHistory.set("PROJECTED", row, true);
              }
            }
            for (int j = 0; j < projected.size(); j++) {
              ProjectedInstallment row = projected.get(j);
              int i = fullHistory.getRowCount();
              fullHistory.set("DATE", i, row.getDate());
              fullHistory.set("PRINCIPAL_AMOUNT", i, row.getPrincipal());
              fullHistory.set("INTEREST_AMOUNT", i, row.getInterest());
              fullHistory.set("PENALTY_AMOUNT", i, BigDecimal.ZERO);
              if (row.isPaid()){
                fullHistory.set("PRINCIPAL_DUE_AMOUNT", i, BigDecimal.ZERO);
                fullHistory.set("INTEREST_DUE_AMOUNT", i, BigDecimal.ZERO);
                //
                fullHistory.set("PRINCIPAL_PAID_AMOUNT", i, row.getPrincipal());
                fullHistory.set("INTEREST_PAID_AMOUNT", i, row.getInterest());
              } else {
                fullHistory.set("PRINCIPAL_DUE_AMOUNT", i, row.getPrincipal());
                fullHistory.set("INTEREST_DUE_AMOUNT", i, row.getInterest());
                //
                fullHistory.set("PRINCIPAL_PAID_AMOUNT", i, BigDecimal.ZERO);
                fullHistory.set("INTEREST_PAID_AMOUNT", i, BigDecimal.ZERO);
              }
              fullHistory.set("PENALTY_PAID_AMOUNT", i, BigDecimal.ZERO);
              fullHistory.set("PENALTY_DUE_AMOUNT", i, BigDecimal.ZERO);
              //
              fullHistory.set("PROJECTED", i, Boolean.TRUE);
            }
          }});
      
      } catch (SQLException e) {
     // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (AbortTransactionException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return MapListUtil.sort(new Object[]{"DATE"}, true, fullHistory.getData());
  }
  protected IReadWriteDataProvider getReadWriteProvider() {
    final IReadWriteDataProvider provider = (IReadWriteDataProvider) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "ReadWriteProvider");
    return provider;
  }
  
  private LoanAccount getLoanAccount(Long accountId) {
    return getLoanAccountService().getLoanAccount(accountId);
  }
  
  protected ILoanAccountService getLoanAccountService(){
    final ILoanAccountService provider = (ILoanAccountService) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "LoanAccountService");
    if (provider == null){
      throw new IllegalStateException("LoanAccountService system services is not configured");
    }
    return provider;
  }


  private void distributeFunds(String propName, BigDecimal balance, List history) {
    history = MapListUtil.sortBy(new Object[]{"DATE"}, false, history);
    for (Iterator i = history.iterator(); i.hasNext();) {
      Map row = (Map) i.next();
      Date date = (Date) row.get("DATE");
      if (date == null) {
        throw new IllegalArgumentException("Missing DATE result column value for " + getProperty(propName));
      }
      Object amt = row.get("AMOUNT");
      if (amt == null) {
        throw new IllegalArgumentException("Missing AMOUNT result column value for " + getProperty(propName));
      } else if (!(amt instanceof BigDecimal)){
        throw new IllegalArgumentException("Invalid AMOUNT result column value for " + getProperty(propName) + ", expected BigDecimal but was " + amt.getClass().getName());
      }
      BigDecimal paidAmount = BigDecimalUtil.ZERO;
      BigDecimal dueAmount = BigDecimalUtil.ZERO;
      BigDecimal hAmount = (BigDecimal) amt;
      // If the balance is over the inst amount
      if (balance.compareTo(BigDecimalUtil.ZERO) == 0){
        paidAmount = hAmount;
        dueAmount = BigDecimalUtil.ZERO;
      }else if (balance.compareTo(hAmount) > 0){
        balance = balance.subtract(hAmount);
        paidAmount = BigDecimalUtil.ZERO;
        dueAmount = hAmount;
      } else {
        dueAmount = balance;
        paidAmount = hAmount.subtract(dueAmount);
        balance = BigDecimalUtil.ZERO;
      }
      row.put("PAID_AMOUNT", paidAmount);
      row.put("DUE_AMOUNT", dueAmount);
    }
  }

  private List executePropertyQuery(String queryProp, Map arguments,
      IQueryService qs) throws TransferObjectException {
    String query = (String) getProperty(queryProp);
    TransferObject resultTO = (TransferObject)qs.getQueryList(query, arguments);
    if (resultTO.isFailed()){
      throw new TransferObjectException(resultTO);
    }
    List result = (List) resultTO.getBeanHolder();
    return result;
  }
  
  private Object getRequiredResultValue(String col, Class c, Map row){
    Object v = row.get(col);
    if (v == null){
      throw new IllegalArgumentException("Required result column " + col + " not provided for " + getQueryName());
    } else if (!v.getClass().equals(c)) {
      throw new IllegalArgumentException("Required result column " + col + " of the wrong type for " + getQueryName());
    }
    return v;
    
  }

  protected String getDocumentation() {
    return getSummary();
  }

  protected String getSummary() {
    return "Formats the installment history of a loan";
  }

}
