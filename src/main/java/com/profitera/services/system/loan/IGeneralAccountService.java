package com.profitera.services.system.loan;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.AccountType;
import com.profitera.services.system.loan.impl.IAccountTypeProvider;

public interface IGeneralAccountService {
  public IAccountTypeProvider getAccountTypeProvider();
  public Account getGeneralAccount(AccountType t, IReadOnlyDataProvider p, ITransaction tr) throws AbortTransactionException;
  public Account getAccount(Long id, IReadOnlyDataProvider p,
      ITransaction t) throws AbortTransactionException;
}
