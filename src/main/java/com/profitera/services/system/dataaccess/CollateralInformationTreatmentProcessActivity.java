package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.DynamicTreatmentProcessManager.IActivity;
import com.profitera.services.system.info.CollateralInfoService;
import com.profitera.services.system.lookup.LookupManager;

public class CollateralInformationTreatmentProcessActivity extends
    ConditionalTreatmentProcessActivity implements IActivity {

  protected void executeActivity(Long accountId, Map process, Map target,
      Date date, String user, ITransaction t, IReadWriteDataProvider p)
      throws SQLException, AbortTransactionException {
    Long id = (Long) target.get(getKey());
    Map copy = new HashMap(target);
    CollateralInfoService c = getInfoService();
    IRunnableTransaction r = c.updateCollateralInformation(copy, id, user, date, p);
    r.execute(t);
  }
  
  private CollateralInfoService getInfoService() {
    final Object s = LookupManager
        .getInstance().getLookupItem(LookupManager.SYSTEM,
            "CollateralInfoService");
    return (CollateralInfoService) s;
  }

}
