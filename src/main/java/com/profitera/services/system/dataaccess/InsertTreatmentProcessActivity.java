package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;

class InsertTreatmentProcessActivity extends
    ConditionalTreatmentProcessActivity {

  protected void executeActivity(Long accountId, Map process, Map target,
      Date date, String user, ITransaction t, IReadWriteDataProvider p) throws SQLException, AbortTransactionException {
    Object keyValue = p.insert(getStatement(), target, t);
    if (getKey() != null) {
      target.put(getKey(), keyValue);
    }
  }

}
