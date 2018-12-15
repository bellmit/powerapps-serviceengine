package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;

class DeleteTreatmentProcessActivity extends
    ConditionalTreatmentProcessActivity {

  protected void executeActivity(Long accountId, Map process, Map target,
      Date date, String user, ITransaction t, IReadWriteDataProvider p) throws SQLException, AbortTransactionException {
    p.delete(getStatement(), target, t);
  }

}
