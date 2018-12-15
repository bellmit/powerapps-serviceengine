package com.profitera.services.system.financial;

import java.sql.SQLException;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.QuerySpec;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;

public class AccountAction {
  
  private static final String COMMODITY_ID = "COMMODITY_ID";
  private static final String ACCOUNT_TYPE_ID = "ACCOUNT_TYPE_ID";
  private static final String GETTER_QUERY = "getFinancialAccount";
  private static QuerySpec GETTER_SPEC;
  
  private QuerySpec getGetterSpec(){
    if (GETTER_SPEC == null){
      GETTER_SPEC = new QuerySpec(GETTER_QUERY, new String[]{ACCOUNT_TYPE_ID, COMMODITY_ID}, new Class[]{Long.class, Long.class});
    }
    return GETTER_SPEC;
  }
  
  public Account getAccount(Long id, IReadOnlyDataProvider p, ITransaction t) throws SQLException, AbortTransactionException{
    if (id == null){
      throw new IllegalArgumentException("Identifier is required");
    }
    requireProvider(p);
    Map returned = (Map) p.queryObject(getGetterSpec().getName(), id);
    if (returned == null) {
      throw new AbortTransactionException("Account identified by " + id + " does not exist");
    }
    getGetterSpec().verifyResultInstance(returned);
    return buildAccount(id, returned);
  }

  private Account buildAccount(Long id, Map returned) {
    Long typeId = (Long) returned.get(ACCOUNT_TYPE_ID);
    AccountType at = new AccountType(typeId);
    Long cId = (Long) returned.get(COMMODITY_ID);
    Commodity c = new Commodity(cId);
    return new Account(id, at, c);
  }

  private void requireProvider(IReadOnlyDataProvider p) {
    if (p == null){
      throw new IllegalArgumentException("Provider is required");
    }
  }
}
