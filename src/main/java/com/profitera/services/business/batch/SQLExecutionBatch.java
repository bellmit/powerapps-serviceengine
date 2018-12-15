package com.profitera.services.business.batch;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.descriptor.business.TransferObject;

public class SQLExecutionBatch extends AbstractProviderBatch {
  
  public SQLExecutionBatch(){
    addRequiredProperty("statement", String.class, "Id of the sql", "ID of the statement provided as part of the server-side XML files currently being run in the application server");
  }

  public TransferObject invoke() {
    final Map args = new HashMap();
    args.put(EFFECTIVE_DATE_PARAM_NAME, getEffectiveDate());
    final String statement = (String)getPropertyValue("statement");
    getLog().info(getIdentifier() + ": " + args);
    try {
      getReadWriteProvider().execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          getReadWriteProvider().update(statement, args, t);
        }});
    } catch (AbortTransactionException e) {
      getLog().fatal("Transaction aborted", e);
      return new TransferObject(TransferObject.EXCEPTION, "TRANSACTION_ABORTED");
    } catch (SQLException e) {
      getLog().fatal("Transaction error", e);
      return new TransferObject(TransferObject.EXCEPTION, "TRANSACTION_FAILED");
    }
    return new TransferObject();
  }

	protected String getBatchDocumentation() {
		return "SQL execution batch for executing updates";
	}

	protected String getBatchSummary() {
		return "SQL execution batch which executes any given update statement";
	}
}