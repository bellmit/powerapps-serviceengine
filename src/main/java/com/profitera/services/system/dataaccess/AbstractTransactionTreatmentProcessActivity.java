package com.profitera.services.system.dataaccess;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.financial.Transaction;
import com.profitera.services.system.loan.ILoanAccountService;
import com.profitera.services.system.loan.LoanAccount;
import com.profitera.services.system.loan.impl.TransactionParser;
import com.profitera.services.system.lookup.LookupManager;

public abstract class AbstractTransactionTreatmentProcessActivity extends ConditionalTreatmentProcessActivity {
  private static final String ACTION_PROP = "action";
  private static final String TRANSACTIONDATE_PROP = "transactiondate";
  private static final String TRANSACTIONID_PROP = "transactionid";
  protected static final String ACCOUNTTYPE_PROP = "accounttype";
  protected static final String GENERAL_PROP = "generalaccounttype";
  protected static final String AMOUNT_PROP = "amount";
  private String amountKey;
  private String actionKey;
  private String generalAccountTypeKey;
  
  protected void executeActivity(Long accountId, Map process, Map target,
      Date date, String user, ITransaction t, IReadWriteDataProvider p)
      throws SQLException, AbortTransactionException {
    String transDateKey = getTransactionDateKey();
    Date transactionDate = TransactionParser.getTransactionDate(target, transDateKey, date);
    String actionField = getActionField();
    boolean isPosting = TransactionParser.isPosting(target, actionField);
    Transaction transaction = buildTransaction(accountId, target, transactionDate, date, p, t);
    LoanAccount loanAccount = getLoanAccountService().getLoanAccount(accountId);
    transaction = loanAccount.recordTransaction(transaction, date, transactionDate, isPosting, p, t);
    if (getTransactionField() != null){
      target.put(getTransactionField(), transaction.getId());
    }
  }



  protected String getTransactionDateKey() {
    return (String) getProperties().get(TRANSACTIONDATE_PROP);
  }

  

  protected abstract Transaction buildTransaction(Long accountId, Map target,
      Date transactionDate, Date today, IReadWriteDataProvider p, ITransaction t)
  throws AbortTransactionException, SQLException;
  
  protected BigDecimal getAmount(Map target) throws AbortTransactionException {
    return TransactionParser.getAmount(getAmountField(), target.get(getAmountField()));
  }

  protected String getActionField(){
    return actionKey;
  }
  private String getTransactionField() throws AbortTransactionException {
    String transIdKey = (String) getProperties().get(TRANSACTIONID_PROP);
    return transIdKey;
  }

  public void setProperties(Map conditions) {
    super.setProperties(conditions);
    amountKey = getRequiredProperty(AMOUNT_PROP);
    actionKey = getRequiredProperty(ACTION_PROP);
    generalAccountTypeKey = getRequiredProperty(GENERAL_PROP);
  }
  
  
  protected String getAmountField() {
    return amountKey;
  }

  
  protected String getGeneralAccountTypeField() {
    return generalAccountTypeKey;
  }

  protected ILoanAccountService getLoanAccountService(){
    final ILoanAccountService provider = (ILoanAccountService) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "LoanAccountService");
    return provider;
  }
}
