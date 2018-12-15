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
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.Transaction;
import com.profitera.services.system.loan.IAccountTypes;
import com.profitera.services.system.loan.LoanAccount;
import com.profitera.util.BigDecimalUtil;
import com.profitera.util.DateParser;

public class InstallmentPrincipalPostingBatch extends AbstractFinancialBatch {

  
  public InstallmentPrincipalPostingBatch() {
    addRequiredProperty(ACCOUNTQUERY, String.class, 
        ACCOUNTQUERY_SHORT_DOC, ACCOUNTQUERY_LONG_DOC);
    addStopOnError();
  }
  
  protected String getBatchDocumentation() {
    return "" +
    "Accounts to be evaluated will only have transactions applied if their due date corresponds to the batch processing date.";
  }

  protected String getBatchSummary() {
    return "Posts transaction to move principal amount to the principal outstanding account based on "
    + "the loan installment schedule";
  }

  protected TransferObject invoke() {
    try {
      intializeAccountTypes(new String[]{
          IAccountTypes.PRINCIPAL, IAccountTypes.PINST, IAccountTypes.OVERPAY
          });
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
    boolean stopOnError = (Boolean)getPropertyValue(STOPONERROR);
    final IReadWriteDataProvider p = getReadWriteProvider();
    QuerySpec spec = new QuerySpec((String) getPropertyValue(ACCOUNTQUERY), new String[]{ACCOUNT_ID}, new Class[]{Long.class});
    Map args = new HashMap();
    args.put(EFFECTIVE_DATE_PARAM_NAME, getEffectiveDate());
    try {
      Iterator i = getReadWriteProvider().query(IReadWriteDataProvider.STREAM_RESULTS, spec.getName(), args);
      while(i.hasNext()){
        Map result = spec.verifyResultInstance((Map) i.next());
        Long loanAccountId = (Long) result.get(spec.getFields()[0]);
        final LoanAccount account = getLoanAccountService().getLoanAccount(loanAccountId);
        try {
          p.execute(new IRunnableTransaction(){
            public void execute(ITransaction t) throws SQLException,
                AbortTransactionException {
              Date evalDate = DateParser.getPreviousDay(getEffectiveDate());
              account.applyPrincipalInstallmentSchedules(evalDate, null, p, t);
            }});
        } catch (SQLException e) {
          getLog().error("Error processing loan account for principal installments: " + e.getMessage(), e);
          if (stopOnError) {
            return new TransferObject(new Object[]{e.getMessage()}, TransferObject.EXCEPTION, "LOAN_INST_PRINCIPAL_ERROR");
          }
        } catch (AbortTransactionException e) {
          getLog().error("Error processing loan account for principal installments: " + e.getMessage(), e);
          if (stopOnError) {
            return new TransferObject(new Object[]{e.getMessage()}, TransferObject.EXCEPTION, "LOAN_INST_PRINCIPAL_ERROR");
          }
        }
      }
    } catch (SQLException e) {
      getLog().error("Error querying loan accounts to process: " + e.getMessage(), e);
      return new TransferObject(new Object[]{spec.getName(), e.getMessage()}, TransferObject.EXCEPTION, "LOAN_QUERY_FAILED");
    } catch (AbortTransactionException e) {
      getLog().error("Error in query for loan accounts to process: " + e.getMessage(), e);
      return new TransferObject(new Object[]{spec.getName(), e.getMessage()}, TransferObject.EXCEPTION, "LOAN_QUERY_FAILED");
    }
    return new TransferObject();
  }
}
