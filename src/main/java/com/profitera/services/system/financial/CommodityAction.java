package com.profitera.services.system.financial;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public class CommodityAction {
  
  public Commodity getCommodity(String code, IReadWriteDataProvider p, ITransaction t) throws SQLException{
    if (code == null){
      throw new IllegalArgumentException("Commodity code is required");
    }
    if (p == null){
      throw new IllegalArgumentException("Provider is required");
    }
    Long returnedId = (Long) p.queryObject("getFinancialCommodity", code);
    if (returnedId == null){
      Map m = new HashMap();
      m.put("CODE", code);
      returnedId = (Long) p.insert("insertFinancialCommodity", m, t);
    } 
    return new Commodity(returnedId);
  }

}
