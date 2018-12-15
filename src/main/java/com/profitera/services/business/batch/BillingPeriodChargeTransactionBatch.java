package com.profitera.services.business.batch;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.QuerySpec;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.AccountType;
import com.profitera.services.system.financial.Split;
import com.profitera.services.system.financial.Transaction;
import com.profitera.services.system.loan.IAccountTypes;
import com.profitera.services.system.loan.LoanAccount;
import com.profitera.services.system.loan.impl.IScheduleManager;
import com.profitera.services.system.loan.impl.PostingSchedule.PostingType;
import com.profitera.util.BigDecimalUtil;
import com.profitera.util.DateParser;

public class BillingPeriodChargeTransactionBatch extends AbstractFinancialBatch {
  private static final String CHARGE_AMOUNT = "CHARGE_AMOUNT";
  private static final String GENERALACCOUNT = "generalaccounttype";
  private static final String SUSPENSEGENERALACCOUNT = "generalsuspenseaccounttype";
  protected static final String ACCOUNT_ID = "ACCOUNT_ID";
  private static final String QUERY = "loanaccountquery";
  private Account genAccount;
  private Account genSuspenseAccount;
  {
    addRequiredProperty(QUERY, String.class, 
        "", 
        "Query that:  "
        + "<variablelist>"
        + "<varlistentry><term>" + ACCOUNT_ID + " (required)</term><listitem><para>.</para></listitem></varlistentry>"
        + "<varlistentry><term>" + CHARGE_AMOUNT + " (required)</term><listitem><para>.</para></listitem></varlistentry>"
        + "</variablelist>");
    addRequiredProperty(GENERALACCOUNT, String.class, "General account code for charge", "The charge's general account code.");
    addRequiredProperty(SUSPENSEGENERALACCOUNT, String.class, "General account code for charge", "The charge's general account code.");
    addStopOnError();
  }

  protected String getBatchDocumentation() {
    return "TODO.";
  }

  protected String getBatchSummary() {
    return "TODO";
  }

  protected TransferObject invoke() {
    boolean stopOnError = (Boolean) getPropertyValue(STOPONERROR);
    final IReadWriteDataProvider p = getReadWriteProvider();
    QuerySpec spec = new QuerySpec((String) getPropertyValue(QUERY), 
        new String[]{ACCOUNT_ID, CHARGE_AMOUNT}, 
        new Class[]{Long.class, BigDecimal.class});
    Map args = new HashMap();
    args.put(EFFECTIVE_DATE_PARAM_NAME, getEffectiveDate());
    try {
      Iterator i = p.query(IReadWriteDataProvider.STREAM_RESULTS, spec.getName(), args);
      while(i.hasNext()){
        final Map payment = spec.verifyResultInstance((Map) i.next());
        final Long accountId = (Long) payment.get(ACCOUNT_ID);
        final BigDecimal chargeAmount = (BigDecimal) payment.get(CHARGE_AMOUNT);
        final LoanAccount a = getLoanAccountService().getLoanAccount(accountId);
        p.execute(new IRunnableTransaction(){
          public void execute(ITransaction t) throws SQLException,
              AbortTransactionException {
            Date created = a.getLoanFinancialAccountsCreatedDate(p);
            // If the loan accounts were created on or before today 
            // we do not assess for charges
            if (created.after(getEffectiveDate()) 
                || created.equals(getEffectiveDate())){
              getLog().info("Charge for " + getIdentifier() + " will not be applied to " 
                  + a.getId() + " with accounts created on " + created + " on " 
                  + getEffectiveDate());
              return;
            }
            // This billing is based on due interest, not principal
            IScheduleManager psManager = a.getPostingScheduleManager(PostingType.IMMEDIATE_I, p, t);
            Date[] billingPeriod = psManager.getBillingPeriod(getEffectiveDate(), p, t);
            if (billingPeriod == null) {
              getLog().warn("No billing period for account " + a.getId() + " for " + getEffectiveDate() 
                  + " with financial accounts created on "
                  + created + " and can not be assessed");
              return;
            }
            Map arguments = new HashMap();
            arguments.put("ACCOUNT_ID", a.getId());
            arguments.put(EFFECTIVE_DATE_PARAM_NAME, getEffectiveDate());
            Date billStart = billingPeriod[0];
            Date billEnd = billingPeriod[1];
            arguments.put("START_DATE", billStart);
            arguments.put("END_DATE", billEnd);
            arguments.put("ACCOUNT_TYPE_ID", getGeneralAccount(p, t).getType().getId());
            Date lastTime = (Date) p.queryObject("getLastBillingPeriodChargeTransactionDate", arguments);
            if (lastTime != null && DateParser.betweenInclusive(lastTime, billStart, billEnd)){
              getLog().info("Charge for " + getIdentifier() + " already applied to " + a.getId() + " on " + lastTime);
              return;
            } else if (lastTime != null && lastTime.after(billEnd)) {
              throw new AbortTransactionException("Charge for " + getIdentifier() + " applied in future date of " + lastTime + " for " + a.getId() + " in spite of billing period ending " + billEnd);
            }
            Split gSplit = new Split(getGeneralChargeAccount(a, p, t), chargeAmount.multiply(BigDecimalUtil.NEG_ONE), null, null);
            Split aSplit = new Split(getLoanChargeAccount(a, p, t), chargeAmount, null, null);
            Transaction trans = new Transaction(gSplit.getCommodity(), new Split[]{gSplit, aSplit});
            Transaction posted = a.postTransaction(trans, getEffectiveDate(), p, t);
            arguments.put("TRANSACTION_ID", posted.getId());
            arguments.put("CHARGE_AMOUNT", chargeAmount);
            p.insert("insertBillingPeriodChargeTransactionHistory", arguments, t);
          }});
      }
    } catch (SQLException e) {
      getLog().error("Error querying transactions to process: " + e.getMessage(), e);
      if (stopOnError) {
        return new TransferObject(new Object[]{spec.getName(), e.getMessage()}, TransferObject.EXCEPTION, "ACCOUNT_QUERY_FAILED");
      }
    } catch (AbortTransactionException e) {
      getLog().error("Error in condition for transactions to process: " + e.getMessage(), e);
      if (stopOnError) {
        return new TransferObject(TransferObject.EXCEPTION, "LAST_CHARGE_OUT_OF_RANGE");
      }
    }
    return new TransferObject();
  }

  protected Account getGeneralChargeAccount(LoanAccount a,
      IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    if (a.isNonPerforming(getEffectiveDate(), p, t)){
      return getGeneralSuspenseAccount(p, t);
    } else {
      return getGeneralAccount(p, t);
    }
    
  }

  private Account getLoanChargeAccount(LoanAccount a, IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException {
    String code = IAccountTypes.CHARGE;
    if (a.isNonPerforming(getEffectiveDate(), p, t)){
      code = IAccountTypes.CIS;
    }
    AccountType type = getLoanAccountService().getAccountType(code, p, t);
    return a.getSetFinancialAccount(type, p, t);
  }

  private Account getGeneralAccount(IReadWriteDataProvider p,
      ITransaction t) throws AbortTransactionException, SQLException {
    if (genAccount == null) {
      String code = (String) getPropertyValue(GENERALACCOUNT);
      AccountType type = getLoanAccountService().getAccountType(code, p, t);
      genAccount = getLoanAccountService().getGeneralAccount(type, p, t);
    }
    return genAccount;
  }

  private Account getGeneralSuspenseAccount(IReadWriteDataProvider p,
      ITransaction t) throws AbortTransactionException, SQLException {
    if (genSuspenseAccount == null) {
      String code = (String) getPropertyValue(SUSPENSEGENERALACCOUNT);
      AccountType type = getLoanAccountService().getAccountType(code, p, t);
      genSuspenseAccount = getLoanAccountService().getGeneralAccount(type, p, t);
    }
    return genSuspenseAccount;
  }

}
