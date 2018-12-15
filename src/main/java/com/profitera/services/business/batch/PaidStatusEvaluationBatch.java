package com.profitera.services.business.batch;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.util.DateParser;

public class PaidStatusEvaluationBatch extends AbstractProviderBatch {
  private static final String GET_CHARGEOFF_DATE = "getChargeoffDate";
  private static final String GET_MONTH_START_OLDEST_OVERDUE_DATE = "getMonthStartOldestOverdueDate";
  private static final String GET_ACCOUNTS_FOR_PAID_STATUS_EVALUATION = "getAccountsForPaidStatusEvaluation";
  private static final String UPDATE_ACCOUNT_PAID_STATUS = "updateAccountPaidStatus";
  private static final String COMMIT_SIZE = "commitsize";
  private static final String THREADS = "threads";
  private static final int DEFAULT_COMMIT = 10;
  private static final int DEFAULT_THREADS = 1;
  Log log = LogFactory.getLog(getClass());
  
  public PaidStatusEvaluationBatch(){
    addProperty(COMMIT_SIZE, Integer.class, DEFAULT_COMMIT + "", "Commit size", "Number of records to process in a single database commit");
    addProperty(THREADS, Integer.class, DEFAULT_THREADS + "", "No. of threads", "Number of threads (concurrent processes) to use to process the batch");
  }

  private TransferObject evaluateAccounts(Date effectiveDate){
    final Date firstOfMonth = DateParser.getStartOfMonth(effectiveDate);
    final int commitSize = getCommitSize();
    log.info("Paid Status Evaluation commit size: " + commitSize);
    int threads = getThreads();
    log.info("Paid Status Evaluation threads: " + threads);
    Iterator i  = null;
    try {
      i = getReadWriteProvider().query(IReadOnlyDataProvider.STREAM_RESULTS, GET_ACCOUNTS_FOR_PAID_STATUS_EVALUATION, Collections.EMPTY_MAP);
    } catch (SQLException e) {
      log.fatal("Failed to execute main query: " + GET_ACCOUNTS_FOR_PAID_STATUS_EVALUATION, e);
      return new TransferObject(TransferObject.ERROR, "ACCOUNT_QUERY_FAILED");
    }
    final Iterator iter = i;
    Thread[] threadList = new Thread[threads];
    for (int j = 0; j < threadList.length; j++) {
      threadList[j] = new Thread(new Runnable(){
        public void run() {
          while (iter.hasNext()){
            List transactions = new ArrayList();
            for (int i = 0; i < commitSize && iter.hasNext(); i++) {
              try {
                final Map account = (Map) iter.next();
                final Long accountId = (Long) account.get("ACCOUNT_ID");
                final Date ood = (Date) account.get("OLDEST_OVERDUE_DATE");
                boolean isPaid = false;
                boolean isChargeOff = false;
                try {
                  isChargeOff = isChargeOff(account);
                } catch (SQLException e) {
                  log.error("Unable to assess account chargeoff status: " + account, e);
                  continue;
                }
                if(!isChargeOff){
                  try {
                    Date oodStartPosition = getMonthStartPosition(accountId, firstOfMonth);
                    log.debug("Account " + accountId + " OOD " + ood + " started at " + oodStartPosition);
                    if (oodStartPosition != null){
                      if (ood == null || ood.after(oodStartPosition)){
                        isPaid = true;
                      }
                    }
                  } catch (SQLException e) {
                    log.error("Unable to assess account start-of-month position: " + account, e);
                    continue;
                  }
                }
                log.debug("Account " + accountId + " paid status evaluated to " + isPaid);
                account.put("IS_PAID", new Boolean(isPaid));
                transactions.add(new IRunnableTransaction(){
                  public void execute(ITransaction t) throws SQLException, AbortTransactionException {
                    getReadWriteProvider().update(UPDATE_ACCOUNT_PAID_STATUS, account, t);
                  }
                });
              } catch (NoSuchElementException e){
                break;
              }
            }
            try {
              getReadWriteProvider().execute(new RunnableTransactionSet((IRunnableTransaction[]) transactions.toArray(new IRunnableTransaction[0])));
            } catch (AbortTransactionException e) {
              log.error("Failed to update account paid status, transaction aborted", e);
            } catch (SQLException e) {
              log.error("Failed to update account paid status, transaction failed", e);
            }
          }
        }}, "PaidStatusEvaluation-" + j);
      threadList[j].start();
    }
    for (int j = 0; j < threadList.length; j++) {
      try {
        threadList[j].join();
      } catch (InterruptedException e) {
        // Just try joining again!
        j--;
      }
    }
    return new TransferObject();
  }

  private int getCommitSize() {
  	return ((Integer)getPropertyValue(COMMIT_SIZE)).intValue();
  }
  
  private int getThreads() {
  	return ((Integer)getPropertyValue(THREADS)).intValue();
  }
  
  private boolean isChargeOff(Map account) throws SQLException {
    if (account.containsKey("IS_CHARGEOFF")){
      Object o = account.get("IS_CHARGEOFF");
      return o != null && ((Boolean)o).booleanValue();
    }
    Long accountId = (Long) account.get("ACCOUNT_ID");
    Date d = (Date) getReadOnlyProvider().queryObject(GET_CHARGEOFF_DATE, accountId);
    return d != null;
  }
  
  protected Date getMonthStartPosition(Long accountId, Date firstOfMonth) throws SQLException {
    Map args = new HashMap();
    args.put("ACCOUNT_ID", accountId);
    args.put("FIRST_OF_MONTH", firstOfMonth);
    Date d = (Date) getReadOnlyProvider().queryObject(GET_MONTH_START_OLDEST_OVERDUE_DATE, args);
    return d;
  }

  public TransferObject invoke() {
    return evaluateAccounts(getEffectiveDate());
  }

	protected String getBatchDocumentation() {
		return "Batch to evaluate paid status of an account";
	}

	protected String getBatchSummary() {
		return "This batch program evaluates accounts to see if they have received payment recently and flags the account to be 'PAID' which allows the front end to show the paid status on screen";
	}
}
