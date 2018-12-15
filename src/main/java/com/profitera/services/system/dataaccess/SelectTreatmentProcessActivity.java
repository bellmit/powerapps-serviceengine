package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.deployment.rmi.ListQueryServiceIntf;
import com.profitera.services.system.lookup.LookupManager;

class SelectTreatmentProcessActivity extends
    ConditionalTreatmentProcessActivity {

  protected void executeActivity(Long accountId, Map process, Map target,
      Date date, String user, ITransaction t, IReadWriteDataProvider p) throws SQLException, AbortTransactionException {
    List l = (List) getListQueryService().getQueryList(getStatement(),target).getBeanHolder();
    if (getKey() != null&&l!=null) {
      target.put(getKey(), l);
    }
  }
  
  private ListQueryServiceIntf getListQueryService() {
    return (ListQueryServiceIntf) LookupManager.getInstance().getLookup(LookupManager.BUSINESS).getService("ListQueryService");
  }

}
