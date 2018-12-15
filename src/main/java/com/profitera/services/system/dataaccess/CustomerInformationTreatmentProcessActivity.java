package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.DynamicTreatmentProcessManager.IActivity;
import com.profitera.services.system.info.CustomerInfoService;
import com.profitera.services.system.lookup.LookupManager;

public class CustomerInformationTreatmentProcessActivity extends
    ConditionalTreatmentProcessActivity implements IActivity {

  protected void executeActivity(Long accountId, Map process, Map target,
      Date date, String user, ITransaction t, IReadWriteDataProvider p)
      throws SQLException, AbortTransactionException {
    String customerId = null;
    try{
    	customerId= target.get(getKey()).toString();
    }catch(ClassCastException e){
    	throw new AbortTransactionException(getKey(), e);
    }
    Map copy = new HashMap(target);
    CustomerInfoService c = getCustomerInfoService();
    IRunnableTransaction r = c.updateCustomerInformation(copy, customerId, user, date, p);
    r.execute(t);
  }
  
  private CustomerInfoService getCustomerInfoService() {
    final CustomerInfoService c = (CustomerInfoService) LookupManager
        .getInstance().getLookupItem(LookupManager.SYSTEM,
            "CustomerInfoService");
    return c;
  }

}
