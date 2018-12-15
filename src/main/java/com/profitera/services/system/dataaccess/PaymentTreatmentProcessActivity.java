package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.AccountType;
import com.profitera.services.system.financial.Transaction;
import com.profitera.services.system.loan.LoanAccount;
import com.profitera.services.system.loan.impl.TransactionParser;

public class PaymentTreatmentProcessActivity extends
    AbstractTransactionTreatmentProcessActivity {

  protected Transaction buildTransaction(Long accountId, Map target,
      Date transactionDate, Date today, IReadWriteDataProvider p, ITransaction t)
      throws AbortTransactionException, SQLException {
    LoanAccount loanAccount = getLoanAccountService().getLoanAccount(accountId);
    String typeCode = TransactionParser.getAccountCode("payment account", getGeneralAccountTypeField(), target);
    AccountType type = getLoanAccountService().getAccountType(typeCode , p, t);
    Account generalPaymentAccount = getLoanAccountService().getGeneralAccount(type, p, t);
    return loanAccount.apportionPayment(generalPaymentAccount, getAmount(target), 
        transactionDate, today, null, p, t);
  }
}
