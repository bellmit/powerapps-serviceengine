package com.profitera.services.business.batch;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.IWorkList;
import com.profitera.descriptor.business.meta.IWorkListAssignment;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.worklistmanager.AllAccountWorklistGenerator;
import com.profitera.services.business.worklistmanager.IWorkListGenerator;
import com.profitera.services.business.worklistmanager.WorkListService;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;

public class AccountWorkListAssignmentBatch extends AbstractProviderBatch {
  private static final String GET_EXPIRED_ACCOUNT_WORK_LIST_ASSIGNMENT = "getExpiredTemporaryAccountWorkListAssignment";
  private static final String GET_NEW_ACCOUNT_WORK_LIST_ASSIGNMENT = "getNewTemporaryAccountWorkListAssignment";
  private static final String GET_ACCOUNT_PERMANENT_WORK_LIST_BY_TYPE = "getAccountPermanentWorkListAssignmentByType";
  private static final String COMMIT_SIZE = "commitsize";
  private static final String THREADS = "threads";
  private static final int DEFAULT_COMMIT = 10;
  private static final int DEFAULT_THREADS = 1;
  
  public AccountWorkListAssignmentBatch(){
    addProperty(COMMIT_SIZE, Integer.class, DEFAULT_COMMIT + "", "Commit size", "Number of accounts to process in a single database commit");
    addProperty(THREADS, Integer.class, DEFAULT_THREADS + "", "No. of threads", "Number of threads (concurrent processes) to use to process the batch");
  }

  private TransferObject processExpiredAccountWorkListAssignment(final Date effectiveDate){
    final int commitSize = getCommitSize();
    getLog().info("Batch Account Work List Assignment commit size: " + commitSize);
    int threads = getThreads();
    getLog().info("Batch Account Work List Assignment threads: " + threads);
    
    // process expired work list assignment
    final IWorkListGenerator generator = new AllAccountWorklistGenerator();
    // set multiple account worklist property
    boolean isMultipleAccountWorklist = ServiceEngine.getProp(WorkListService.MODULE_NAME + "." + WorkListService.MULTIPLE_ACCOUNT_WORKLIST, "F").toUpperCase().startsWith("T");
    generator.setMultipleAccountWorklist(isMultipleAccountWorklist);
    
    Iterator i  = null;
    try {
      i = getReadWriteProvider().query(IReadOnlyDataProvider.STREAM_RESULTS, GET_EXPIRED_ACCOUNT_WORK_LIST_ASSIGNMENT, effectiveDate);
    } catch (SQLException e) {
      getLog().fatal("Failed to execute query: " + GET_EXPIRED_ACCOUNT_WORK_LIST_ASSIGNMENT, e);
      return new TransferObject(TransferObject.ERROR, "QUERY_FAILED - " + GET_EXPIRED_ACCOUNT_WORK_LIST_ASSIGNMENT);
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
                Map m = (Map) iter.next();
                m.put("ASSIGNMENT_ID_TO_BE_DELETED", m.get(IWorkListAssignment.ID)); // to be deleted
                Map pwl = (Map) getReadWriteProvider().queryObject(GET_ACCOUNT_PERMANENT_WORK_LIST_BY_TYPE, m);
                // set work list id to permanent work list id
                m.put(IWorkList.WORK_LIST_ID, pwl.get(IWorkList.WORK_LIST_ID));
                m.put(IWorkListAssignment.ASSIGNMENT_TYPE, "IS_T2P");
                transactions.add(generator.getSetAccountWorkListTransaction(m, effectiveDate, getReadWriteProvider()));
              } catch (NoSuchElementException e){
                break;
              } catch (SQLException e){
                getLog().error("Failed to execute query: " + GET_ACCOUNT_PERMANENT_WORK_LIST_BY_TYPE, e);            	  
              }
            }
            try {
              getReadWriteProvider().execute(new RunnableTransactionSet((IRunnableTransaction[]) transactions.toArray(new IRunnableTransaction[0])));
            } catch (AbortTransactionException e) {
              getLog().error("Failed to update account work list assignment, transaction aborted", e);
            } catch (SQLException e) {
              getLog().error("Failed to update account work list assignment, transaction failed", e);
            }
          }
        }}, "Account Work List Assignment-" + j);
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

  private TransferObject processNewAccountWorkListAssignment(final Date effectiveDate){
    final int commitSize = getCommitSize();
    getLog().info("Batch Account Work List Assignment commit size: " + commitSize);
    int threads = getThreads();
    getLog().info("Batch Account Work List Assignment threads: " + threads);

    // process new work list assignment    
    final IWorkListGenerator generator = new AllAccountWorklistGenerator();
    // set multiple account worklist property
    boolean isMultipleAccountWorklist = ServiceEngine.getProp(WorkListService.MODULE_NAME + "." + WorkListService.MULTIPLE_ACCOUNT_WORKLIST, "F").toUpperCase().startsWith("T");
    generator.setMultipleAccountWorklist(isMultipleAccountWorklist);
    
    Iterator i  = null;
    try {
      i = getReadWriteProvider().query(IReadOnlyDataProvider.STREAM_RESULTS, GET_NEW_ACCOUNT_WORK_LIST_ASSIGNMENT, effectiveDate);
    } catch (SQLException e) {
      getLog().fatal("Failed to execute query: " + GET_NEW_ACCOUNT_WORK_LIST_ASSIGNMENT, e);
      return new TransferObject(TransferObject.ERROR, "QUERY_FAILED - " + GET_NEW_ACCOUNT_WORK_LIST_ASSIGNMENT);
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
                Map m = (Map) iter.next();
                m.put(IWorkListAssignment.ASSIGNMENT_TYPE, "IS_P2T");
                transactions.add(generator.getSetAccountWorkListTransaction(m, effectiveDate, getReadWriteProvider()));
              } catch (NoSuchElementException e){
                break;
              }
            }
            try {
              getReadWriteProvider().execute(new RunnableTransactionSet((IRunnableTransaction[]) transactions.toArray(new IRunnableTransaction[0])));
            } catch (AbortTransactionException e) {
              getLog().error("Failed to update account work list assignment, transaction aborted", e);
            } catch (SQLException e) {
              getLog().error("Failed to update account work list assignment, transaction failed", e);
            }
          }
        }}, "Account Work List Assignment-" + j);
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
  
  public TransferObject invoke() {
    TransferObject to = processExpiredAccountWorkListAssignment(getEffectiveDate());
    if (to.isFailed()){
      return to;
    }
    return processNewAccountWorkListAssignment(getEffectiveDate());
  }

	protected String getBatchDocumentation() {
		return "Batch program to assign work list to accounts";
	}

	protected String getBatchSummary() {
		return "This batch program goes through all the accounts and work lists in the system and assign them to each other based on the configuration in work list decision tree"; 
	}
}