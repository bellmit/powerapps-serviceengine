package com.profitera.services.system.loan.impl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.SystemService;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.AccountAction;
import com.profitera.services.system.financial.AccountType;
import com.profitera.services.system.financial.AccountTypeAction;
import com.profitera.services.system.loan.IGeneralAccountService;

public class GeneralAccountService extends SystemService implements IGeneralAccountService {
  private Map<Long, Account> generalAccounts = new HashMap<Long, Account>();
  protected final AccountAction accountFetcher = new AccountAction();
  AccountTypeAction accountTypeAction = new AccountTypeAction();
  private Map<String, AccountType> accountTypes = new HashMap<String, AccountType>();

  private final class AccountTypeProvider implements IAccountTypeProvider {
    public AccountType get(String code, IReadWriteDataProvider p,
        ITransaction t) throws SQLException {
      return getAccountType(code, p, t);
    }
  }
  
  private IAccountTypeProvider accountTypeProvider = new AccountTypeProvider();  
  
  public Account getGeneralAccount(AccountType t, final IReadOnlyDataProvider p, ITransaction trans) throws AbortTransactionException {
    if (t == null){
      throw new IllegalArgumentException("Account type not provided to general account request");
    }
    Account result = generalAccounts.get(t.getId());
    if (result == null){
      try {
        Long accountId;
        accountId = (Long) p.queryObject("getGeneralAccountId", t.getId());
        if (accountId == null){
          throw new AbortTransactionException("No identifier found for general account with type ID " + t.getId() + ", have the general accounts been intialized?");
        }
        Account generalAccount = getAccount(accountId, p, trans);
        generalAccounts.put(t.getId(), generalAccount);
        result = generalAccount;
      } catch (SQLException e) {
        throw new AbortTransactionException("Failed to query identifier for root general account", e);
      }
    }
    return result;
  }

  public Account getAccount(Long id, IReadOnlyDataProvider p, ITransaction t) throws AbortTransactionException{
    try {
      Account acc = accountFetcher.getAccount(id, p, t);
      if (acc == null){
        throw new AbortTransactionException("Failed to retrieve financial account : " + id);
      }
      return acc;
    } catch (SQLException e){
      throw new AbortTransactionException("Failed to retrieve financial account : " + id, e);
    } catch (AbortTransactionException e) {
      throw new AbortTransactionException("Failed to retrieve financial account : " + id, e);
    }
  }

  public IAccountTypeProvider getAccountTypeProvider() {
    return accountTypeProvider;
  }
  
  public AccountType getAccountType(String typeCode, IReadWriteDataProvider p,
      ITransaction t) throws SQLException {
    if (accountTypes.containsKey(typeCode)){
      return accountTypes.get(typeCode);
    } else {
      AccountType type = accountTypeAction.getAccountType(typeCode, p, t);
      accountTypes.put(typeCode, type);
      return type;  
    }
  }

}
