package com.profitera.services.system.loan;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.AccountAction;
import com.profitera.services.system.financial.AccountType;
import com.profitera.services.system.financial.Split;
import com.profitera.services.system.financial.Transaction;
import com.profitera.services.system.financial.TransactionAction;
import com.profitera.services.system.financial.TransactionQuery;
import com.profitera.services.system.loan.impl.IAccountTypeProvider;

public abstract class AbstractAccountSet implements IAccountSet {
  private final Long loanAccountId;
  private final IGeneralAccountService general;
  private Map<Long, Account> finAccounts = new HashMap<Long, Account>();

  public AbstractAccountSet(Long id, IGeneralAccountService g){
    if (id == null){
      throw new IllegalArgumentException("Loan account ID is required");
    }
    this.loanAccountId = id;
    this.general = g;
  }
  
  protected abstract Long getAccountSetAccountId(AccountType t, IReadOnlyDataProvider p, 
      ITransaction tr) throws SQLException;

  
  public Long getId(){
    return loanAccountId;
  }
  
  protected IGeneralAccountService getGeneral() {
    return general;
  }
  
  public BigDecimal getAccountPostedBalance(Account a, IReadWriteDataProvider p) throws SQLException{
    BigDecimal d = (BigDecimal) p.queryObject("getFinancialAccountPostedBalance", a.getId());
    return d;
  }
  
  protected Account getAccount(Long id, IReadOnlyDataProvider p, ITransaction t) throws AbortTransactionException {
    return this.getGeneral().getAccount(id, p, t);
  }

  protected Account getGeneralAccount(AccountType type,
      IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException {
    return getGeneral().getGeneralAccount(type, p, t);
  }
  
  protected IAccountTypeProvider getAccountTypeProvider() {
    return getGeneral().getAccountTypeProvider();
  }
  
  public Account getSetFinancialAccount(AccountType type, IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    if (type== null){
      throw new IllegalArgumentException("Financial account request must have a type specified");
    }
    Account a = finAccounts.get(type.getId());
    if (a == null){
      a = getSetFinancialAccountByType(type, p, t);
      finAccounts.put(type.getId(), a);
    }
    return a;
  }
  
  private Account getSetFinancialAccountByType(AccountType t, IReadOnlyDataProvider p, ITransaction trans) throws AbortTransactionException {
    Long accountId = null;
    try {
      accountId = getAccountSetAccountId(t, p, trans);
    } catch (SQLException e) {
      throw new AbortTransactionException("Unable to fetch account set financial accounts for " + getId(), e);
    }
    if (accountId == null){
      throw new AbortTransactionException("Account set financial account not found for " + getId() + " with type id " + t.getId());
    }
    return getAccount(accountId, p, trans);
  }

  
  public Transaction recordTransaction(Transaction transaction, Date postingDate, Date transactionDate, boolean isPosting, IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    if (isPosting && !transactionDate.equals(postingDate)){
      transaction = new TransactionAction(transaction, transactionDate).enter(p, t);
    }
    if (isPosting){
      // Here we use the current date because we are posting
      transaction = this.postTransaction(transaction, postingDate, p, t);
    } else {
      transaction = new TransactionAction(transaction, transactionDate).enter(p, t);
    }
    return transaction;
  }

  protected Transaction postTransactionInternal(Transaction trans, Date effectiveDate, 
      IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    TransactionAction action = new TransactionAction(trans, effectiveDate);
    return action.post(p, t);
  }
  
  protected Transaction reverseTransactionInternal(Long id, Date date,
      IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    TransactionQuery q = new TransactionQuery();
    Transaction toReverse = q.getTransaction(id, new AccountAction(), p, t);
    // Now I need to relate this loan to at least one of the accounts
    // in the transaction being reversed.
    Split[] splits = toReverse.getSplits();
    boolean found = false;
    for (int i = 0; i < splits.length; i++) {
      Account spltAccount = splits[i].getAccount();
      AccountType accountType = spltAccount.getType();
      Long loanFinancialAccount = getAccountSetAccountId(accountType, p, t);
      if (loanFinancialAccount != null && loanFinancialAccount.longValue() == spltAccount.getId()) {
        found = true;
        break;
      }
    }
    if (found == false) {
      throw new AbortTransactionException("Transaction " + id + " to be reversed is not linked to loan account with id " + getId());
    }
    TransactionAction ta = new TransactionAction(toReverse, date);
    return ta.reverse(p, t);
  }


}
