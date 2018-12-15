package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.financial.TransactionParserConfig;
import com.profitera.services.system.financial.Transaction;
import com.profitera.services.system.loan.ILoanAccountService;
import com.profitera.services.system.loan.LoanAccount;
import com.profitera.services.system.loan.impl.TransactionParser;

public class TransactionTreatmentProcessActivity extends AbstractTransactionTreatmentProcessActivity {
  private static final String SPLITLIST_PROP = "splitlist";
  private static final String REDIRECT_SUSPENSE_PROP = "redirectsuspense";
  private TransactionParserConfig parser;
  
  protected Transaction buildTransaction(Long accountId, Map target, Date transactionDate, Date today, IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    ILoanAccountService loanAccountService = getLoanAccountService();
    TransactionParser transactionParser = new TransactionParser(parser, loanAccountService.getAccountTypeProvider());
    LoanAccount loan = loanAccountService.getLoanAccount(accountId);
    return transactionParser.parseTransaction(loanAccountService, loan, target, today, p, t);
  }

  public void setProperties(Map conditions) {
    super.setProperties(conditions);
    String accountTypeKey = getRequiredProperty(ACCOUNTTYPE_PROP);
    String splitKey = getRequiredProperty(SPLITLIST_PROP);
    String redirectKey = getProperty(REDIRECT_SUSPENSE_PROP);
    String transDateKey = getTransactionDateKey();
    parser = new TransactionParserConfig(getActionField(), transDateKey, getGeneralAccountTypeField(), splitKey, accountTypeKey, getAmountField(), redirectKey);
  }
  
}
