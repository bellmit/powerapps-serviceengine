package com.profitera.services.system.financial;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public class AccountTypeAction {  
  public AccountType getAccountType(String code, IReadWriteDataProvider p, ITransaction t) throws SQLException{
    if (p == null){
      throw new IllegalArgumentException("Provider is required");
    }
    if (code == null){
      throw new IllegalArgumentException("Account type code is required");
    }
    Long returnedId = (Long) p.queryObject("getFinancialAccountType", code);
    if (returnedId == null){
      Map m = new HashMap();
      m.put("CODE", code);
      returnedId = (Long) p.insert("insertFinancialAccountType", m, t);
    } 
    return new AccountType(returnedId);
  }
}
