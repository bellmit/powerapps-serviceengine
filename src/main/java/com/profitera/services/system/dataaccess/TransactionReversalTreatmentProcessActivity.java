package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.financial.Transaction;
import com.profitera.services.system.loan.ILoanAccountService;
import com.profitera.services.system.loan.LoanAccount;
import com.profitera.services.system.lookup.LookupManager;

public class TransactionReversalTreatmentProcessActivity extends ConditionalTreatmentProcessActivity {
  private static final String TRANSACTIONID_PROP = "transactionid";
  private static final String REVERSAL_TRANSACTIONID_PROP = "reversaltransactionid";
  private static final String TRANSACTIONDATE_PROP = "transactiondate";
  
  private String transactionIdKey;
  private String reversalTransactionIdKey;
  
  protected void executeActivity(Long accountId, Map process, Map target,
      Date date, String user, ITransaction t, IReadWriteDataProvider p)
      throws SQLException, AbortTransactionException {
    Date transactionDate = getTransactionDate(target, date);
    Long id = getTransactionIdValue(transactionIdKey, target.get(transactionIdKey));
    LoanAccount loanAccount = getLoanAccountService().getLoanAccount(accountId);
    Transaction reversal = loanAccount.reverseTransaction(id, transactionDate, p, t);
    if (reversalTransactionIdKey != null){
      target.put(reversalTransactionIdKey, reversal.getId());
    }
  }
  
  protected ILoanAccountService getLoanAccountService(){
    final ILoanAccountService provider = (ILoanAccountService) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "LoanAccountService");
    return provider;
  }
  
  private Long getTransactionIdValue(String key, Object idValue)
      throws AbortTransactionException {
    if (idValue == null) {
      throw new AbortTransactionException("Transaction ID value is required to reverse a transaction");
    }
    try {
      return (Long) idValue;
    } catch (ClassCastException e){
      throw getWrongTypeAbort("transaction id", key, idValue, Long.class);
    }
  }

  private Date getTransactionDate(Map target, Date date) throws AbortTransactionException {
    String transDateKey = (String) getProperties().get(TRANSACTIONDATE_PROP);
    Object tDate = target.get(transDateKey);
    if (tDate == null) return date;
    try {
      Date d = (Date) tDate;
      return d;
    } catch (ClassCastException e){
      throw getWrongTypeAbort("transaction date", transDateKey, tDate, Date.class);
    }
  }

  public void setProperties(Map conditions) {
    super.setProperties(conditions);
    transactionIdKey = getRequiredProperty(TRANSACTIONID_PROP);
    reversalTransactionIdKey = getProperty(REVERSAL_TRANSACTIONID_PROP);
  }
  
}
