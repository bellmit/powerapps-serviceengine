package com.profitera.services.system.financial;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public class CreateAccountAction {

  private static final String INSERT_FINANCIAL_ACCOUNT = "insertFinancialAccount";
  private final Commodity commodity;
  private final AccountType type;
  public CreateAccountAction(AccountType type, Commodity commodity){
    if (type == null){
      throw new IllegalArgumentException("New account type can not be null");
    }
    this.type = type;
    if (commodity == null){
      throw new IllegalArgumentException("New account commodity can not be null");
    }
    this.commodity = commodity;
  }
  
  public Account create(IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException {
    if (p == null){
      throw new IllegalArgumentException("Transaction provider can not be null");
    }
    Map args = new HashMap();
    args.put("COMMODITY_ID", commodity.getId());
    args.put("ACCOUNT_TYPE_ID", type.getId());
    Object temp = null;
    try {
      temp = p.insert(INSERT_FINANCIAL_ACCOUNT, args, t);
      Long newId = (Long) temp;
      return new Account(newId, type, commodity);
    } catch (ClassCastException e){
      throw ExceptionUtil.getClassCastAbort(INSERT_FINANCIAL_ACCOUNT, Long.class, temp, e);
    }
  }
}
