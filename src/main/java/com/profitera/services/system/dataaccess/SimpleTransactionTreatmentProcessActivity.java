package com.profitera.services.system.dataaccess;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.AccountType;
import com.profitera.services.system.financial.Transaction;
import com.profitera.services.system.loan.ILoanAccountService;
import com.profitera.services.system.loan.LoanAccount;
import com.profitera.services.system.loan.impl.TransactionParser;

public class SimpleTransactionTreatmentProcessActivity extends AbstractTransactionTreatmentProcessActivity {
  private String accountTypeKey;
  protected String getAccountTypeField() {
    return accountTypeKey;
  }
  
  public void setProperties(Map conditions) {
    super.setProperties(conditions);
    accountTypeKey = getRequiredProperty(ACCOUNTTYPE_PROP);
  }

  protected Transaction buildTransaction(Long accountId, Map target, Date transactionDate, Date today, IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    return buildTwoLeggedTransaction(accountId, target, p, t);
  }

  private String getSimpleCreditField() {
    return getAccountTypeField();
  }

  private String getSimpleDebitField() {
    return getGeneralAccountTypeField();
  }

  private Transaction buildTwoLeggedTransaction(Long accountId, 
      Map target, IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    String debit = TransactionParser.getAccountCode("debit account code", getSimpleDebitField(), target);
    String credit = TransactionParser.getAccountCode("credit account code", getSimpleCreditField(), target);
    ILoanAccountService service = getLoanAccountService();
    AccountType equityAccountType = service.getAccountType(debit, p, t);
    AccountType accountAccountType = service.getAccountType(credit, p, t);
    Account equityAccount = service.getGeneralAccount(equityAccountType, p, t);
    BigDecimal amt = getAmount(target);
    LoanAccount loan = service.getLoanAccount(accountId);
    return loan.getSimpleTransaction(equityAccount, accountAccountType, amt, p, t);
  }
}
