package com.profitera.services.business.batch;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.util.IRecordDispenser;
import com.profitera.util.IteratorRecordDispenser;

public class ParentChildSQLExecutionBatch extends AbstractProviderBatch {

  public static final String COMMITSIZE = "commitsize";
  public static final String THREADS = "threads";
  public static final String PARENT_QUERY = "parentquery";
  public static final String CHILD_QUERY = "childquery";
  public static final String EXECUTE_QUERY = "executequery";

  private int mCommitSize = 1;
  private int mNoOfThreads = 1;
  private String mParentQuery = null;
  private String mChildQuery = null;
  private List mExecuteQuery = null;
  
  public ParentChildSQLExecutionBatch(){
    addProperty(COMMITSIZE, Integer.class, "1", "Commit size", "Number of records to be included in a database commit");
    addProperty(THREADS, Integer.class, "1", "No. of threads", "Number of threads for concurrent processing");
    addRequiredProperty(PARENT_QUERY, String.class, "Parent query name", "The name of main query of which the results will be interated through");
    addProperty(CHILD_QUERY,String.class,  null, "Child query name", "The name of child query which will be called with every record from the main query as argument, optional");
    addRequiredListProperty(EXECUTE_QUERY, String.class, "Execution/transaction query name", "The name of insert/update queries, separated by semi colon ';'");
  }

  public TransferObject invoke() {
    mCommitSize = ((Integer)getPropertyValue(COMMITSIZE)).intValue();
    mNoOfThreads = ((Integer)getPropertyValue(THREADS)).intValue();
    mParentQuery = (String)getPropertyValue(PARENT_QUERY);
    mChildQuery = (String)getPropertyValue(CHILD_QUERY);
    mExecuteQuery = (List)getPropertyValue(EXECUTE_QUERY); 

    Date effectiveDate = getEffectiveDate();
    final IReadWriteDataProvider readWriter = getReadWriteProvider();
    Iterator mQueryResults = null;
    final Map arg = new HashMap();
    // this argument is just for legacy compatibility
    arg.put("effectivedate", effectiveDate);
    arg.put(EFFECTIVE_DATE_PARAM_NAME, effectiveDate);

    getLog().info(getIdentifier() + " Invoked with - commits=" + mCommitSize + ", threads=" + mNoOfThreads + ", parentquery=" + mParentQuery +  ", childquery=" + mChildQuery + ", executequery=" +  mExecuteQuery + ", effectiveDate=" + new SimpleDateFormat("yyyyMMdd").format(effectiveDate));

    try {
				mQueryResults = readWriter.query(IReadOnlyDataProvider.STREAM_RESULTS, mParentQuery, arg);
		} catch (SQLException e1) {
			getLog().error(getIdentifier() + " SQL Exception while executing main query " + mParentQuery + ".  Processing aborted.", e1);
			return new TransferObject(new Object[]{getIdentifier(), mParentQuery}, TransferObject.EXCEPTION, "EXCEPTION_RUNNING_PARENT_QUERY");
		}
		final IRecordDispenser dispenser = new IteratorRecordDispenser(mQueryResults, mCommitSize, getIdentifier());
  	Thread[] threads = new Thread[mNoOfThreads];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Thread(new Runnable() {
        public void run()  {
          while (true) {
            List records = new ArrayList();
            try {
              dispenser.dispenseRecords(records);
            } catch (Exception e) {
              getLog().error(getIdentifier() + " Thread " + Thread.currentThread().getName() + " encountered exception while dispensing records. Proceeding to the next set of records. Message: " + e.getMessage(), e);
            }

            if (records.size() == 0)
              break;

          	try {
              new ChildSQLProcessor(getIdentifier(), getLog(), mChildQuery, mExecuteQuery).
              	processMainQuery(records, arg, readWriter);
            } catch (SQLException e) {
              logException(e);
            }
          }
        }

        private void logException(SQLException e) {
          getLog().error(getIdentifier() +  " " 
              + Thread.currentThread().getName() 
              + " encountered a database error while trying to commit, " 
              + "proceeding to the next set of records", e);
        }
      });
      threads[i].setName(getIdentifier() +  "-" + (i+1));
      threads[i].start();
    }
    for (int i = 0; i < threads.length; i++) {
      try {
        threads[i].join();
      } catch (InterruptedException e) {
        // Should never happen
      }
    }
    
    return new TransferObject();
  }

	protected String getBatchDocumentation() {
		return "SQL execution batch for 'select' and transactions ('insert' or 'update')";
	}

	protected String getBatchSummary() {
		return "SQL execution batch which queries data from a parent sql and uses its records as a parameter into a list of child transaction sqls (insert/update)";
	}
}