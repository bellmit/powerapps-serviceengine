package com.profitera.services.business.statusevaluation.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.deployment.rmi.EvaluationTreeManagementServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.AsynchronousUpdateExecutor;

public class TreeDataEvaluator {
  private AsynchronousUpdateExecutor executor = new AsynchronousUpdateExecutor();
  private BlockingQueue transactionQueue = new LinkedBlockingQueue();
  private Log log;
  private Map evalCache = new HashMap();
  private Map evalCacheTimes = new HashMap();
  private final boolean isUpdatingDB;
  private final IReadWriteDataProvider provider;
  private final long treeCacheTime;
  public TreeDataEvaluator(long treeCacheTime, final int commitSize, final int concurrentCommits, final IReadWriteDataProvider p, Log log, boolean updateDB){
    this.treeCacheTime = treeCacheTime;
    this.provider = p;
    this.log = log;
    isUpdatingDB = updateDB;
    Thread t = new Thread(new Runnable(){
      public void run() {
        List toCommit = new ArrayList();
        while(true){
          Object one = null;
          try {
            one = transactionQueue.take();
          } catch (InterruptedException e1) {}
          transactionQueue.drainTo(toCommit, commitSize - 1);
          toCommit.add(0, one);
          final RunnableTransactionSet set = 
            new RunnableTransactionSet((IRunnableTransaction[]) 
                toCommit.toArray(new IRunnableTransaction[0]));
          
          executor.executeUpdates(new Runnable(){
            public void run() {
              try {
                p.execute(set);
              } catch (AbortTransactionException e) {
                getLog().error("Evaluation update failed for one or more records", e);
              } catch (SQLException e) {
                getLog().error("Evaluation update failed for one or more records", e);
              }
            }}, concurrentCommits);
          toCommit.clear();
        }
        
      }});
    t.setDaemon(true);
    t.start();
  }

  protected Log getLog() {
    return log;
  }
  
  private StatusEvaluator getTreeEvaluator(Long treeId) {
    synchronized (evalCache) {
      Long time = (Long) evalCacheTimes.get(treeId);
      if (time != null && time.longValue() < System.currentTimeMillis() + treeCacheTime){
        return (StatusEvaluator) evalCache.get(treeId);
      }
    }
    TransferObject tree = getService().getTree(treeId);
    if (tree.isFailed()){
      String message = "Failed to retrieve tree with ID " + treeId + ": " + tree.getMessage();
      getLog().error(message);
      throw new RuntimeException(message);
    }
    TransferObject treeType = getService().getTreeType(treeId);
    if (treeType.isFailed()){
      String message = "Failed to retrieve tree type for tree with ID " + treeId + ": " + treeType.getMessage();
      getLog().error(message);
      throw new RuntimeException(message);
    }
    Map typeInfo = (Map) treeType.getBeanHolder();
    String updateField = (String) typeInfo.get(EvaluationTreeManagementServiceIntf.UPDATE_FIELD_NAME);
    String updateDateField = (String) typeInfo.get(EvaluationTreeManagementServiceIntf.DATE_FIELD_NAME);
    String update = (String) typeInfo.get(EvaluationTreeManagementServiceIntf.UPDATE);
    String insert = (String) typeInfo.get(EvaluationTreeManagementServiceIntf.INSERT);
    String treeXML = (String) tree.getBeanHolder();
    
    StatusEvaluator eval;
    if (!isUpdatingDB){
      eval = new StatusEvaluator(treeXML, updateField, updateDateField);
    } else {
      eval = new StatusEvaluator(treeXML, updateField, updateDateField, insert, update);  
    }
    synchronized (evalCache) {
      evalCache.put(treeId, eval);
      evalCacheTimes.put(treeId, new Long(System.currentTimeMillis()));
    }
    return eval;
  }

  public void process(Iterator iterator, String treeIdField, List collector) {
    while (iterator.hasNext()) {
      Map data = (Map) iterator.next();
      Long treeId = getTreeId(data, treeIdField);
      if (treeId == null) {
        getLog().warn("No tree ID specified for result to be processed: " + data);
        continue;
      }
      StatusEvaluator eval = getTreeEvaluator(treeId);
      IRunnableTransaction trans = eval.evaluate(data, new Date(), null, provider);
      addToTransactionQueue(trans);
      if (collector != null) {
        Map d = new HashMap(data);
        collector.add(d);
      }
    }
  }
  private Long getTreeId(Map data, String f) {
    Object object = data.get(f);
    try {
      return (Long) object;
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Expected tree id value specified by " + f + " to be of type long, was " + object.getClass().getName());
    }
  }
  private EvaluationTreeManagementServiceIntf getService(){
    LookupManager lm = LookupManager.getInstance();
    Object s = lm.getLookupItem(LookupManager.BUSINESS, "EvaluationTreeManagementService");
    return (EvaluationTreeManagementServiceIntf) s;
  }

  private void addToTransactionQueue(IRunnableTransaction trans) {
    if (trans == null) return;
    putOnQueue(trans);
  }
  
  private void putOnQueue(Object o){
    try {
      transactionQueue.put(o);
    } catch (InterruptedException e) {
      // Should never happen, unlimited queue
    }
  }
  
  public void waitForEmptyQueue(){
    executor.waitForAllTransactions();
  }

}
