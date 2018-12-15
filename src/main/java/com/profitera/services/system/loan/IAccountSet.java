package com.profitera.services.system.loan;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.AccountType;
import com.profitera.services.system.financial.Split;
import com.profitera.services.system.financial.Transaction;

public interface IAccountSet {

  Long getId();

  Transaction postTransaction(Transaction trans,
      Date effectiveDate, IReadWriteDataProvider p, ITransaction t)
      throws AbortTransactionException, SQLException;

  Transaction reverseTransaction(Long id, Date date,
      IReadWriteDataProvider p, ITransaction t)
      throws AbortTransactionException, SQLException;
  
  Transaction recordTransaction(Transaction transaction, Date date, 
      Date transactionDate, boolean isPosting, IReadWriteDataProvider p, 
      ITransaction t) throws AbortTransactionException, SQLException;

  boolean isSuspense(Split split, IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException;
  Account getLedgerAccount(Account suspAccount, IReadWriteDataProvider p,
      ITransaction t) throws SQLException, AbortTransactionException;
  BigDecimal getAccountPostedBalance(Account a, IReadWriteDataProvider p) throws SQLException;
  Account getSetFinancialAccount(AccountType type, IReadWriteDataProvider p,
      ITransaction t) throws SQLException, AbortTransactionException;

}