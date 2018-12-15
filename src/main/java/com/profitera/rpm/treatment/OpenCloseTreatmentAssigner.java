package com.profitera.rpm.treatment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.rpm.AgentFailureException;
import com.profitera.rpm.IncompleteAccountDataException;
import com.profitera.rpm.TreatmentAssigner;
import com.profitera.server.ServiceEngine;
import com.profitera.services.system.dataaccess.DefaultTreatmentProcessManager;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.TopLinkQuery;

public class OpenCloseTreatmentAssigner implements TreatmentAssigner {
  private static Log log = LogFactory.getLog(AbstractGraphTreatmentAssigner.class);
  private int commitSize = 100;
  private String queryStrategy = TopLinkQuery.STREAM;
  private DefaultTreatmentProcessManager manager;

  public OpenCloseTreatmentAssigner() {
    ServiceEngine.getConfig(true);
    commitSize = ServiceEngine.getIntProp("TREATMENT_COMMIT", commitSize);
    queryStrategy = ServiceEngine.getProp("TREATMENT_QUERY_STRATEGY", queryStrategy);
    log.info("Treatment Assignment commit size: " + commitSize);
    log.info("Treatment Assignment query strategy:  " + queryStrategy);
  }

  protected Log getLog(){
    return log;
  }
  private void assignTreatmentsWithoutDelqDetermination(String startId, String endId, final Date d) {
    // Mass-close open plans for non-delqs. so we won't rip through them for no reason here.
    closeCompletePlans(startId, endId, d);
    // Create plans for all the idiots who just went delinquent
    Map args = new HashMap();
    args.put("START_CUSTOMER", startId);
    args.put("END_CUSTOMER", endId);
    args.put("EFFECTIVE_DATE", d);
    Iterator accounts = null;
    try {
      accounts = getReadWriteProvider().query(IReadWriteDataProvider.STREAM_RESULTS, "getDelinquentAccountsWithoutPlansByCustomer", args);
    } catch(SQLException e){
      String msg = "Unable to query delinquent accounts for creating treatment plans for customers " + startId + " to " + endId;
      log.fatal(msg, e);
      throw new RuntimeException(msg);
    }
    try {
      IRunnableTransaction[] trans = new IRunnableTransaction[commitSize];
      int i = -1;
      while(accounts.hasNext()){
        Map account = (Map) accounts.next();
        final Long accountId = (Long) account.get("ACCOUNT_ID");
        i++;
        trans[i] = new IRunnableTransaction(){
          public void execute(ITransaction t) throws SQLException, AbortTransactionException {
            getManager().getTreatmentPlanForProcess(new HashMap(), accountId, d, null, null, null, t, getReadWriteProvider());
          }};
        if (i == trans.length - 1){
          getReadWriteProvider().execute(new RunnableTransactionSet(trans));
          Arrays.fill(trans, null);
          i = -1;
        }
      }
      getReadWriteProvider().execute(new RunnableTransactionSet(trans));
    } catch (AbortTransactionException e) {
      // TODO: Better info
      log.error("Failed to create treatment plans", e);
    } catch (SQLException e) {
      // TODO: Better info
      log.error("Failed to create treatment plans", e);
    }
  }

  public void assignTreatments(String startId, String endId, Date d, boolean useDelqDetermination, String rootID){
    if (useDelqDetermination)
      throw new RuntimeException("The use of Delq determiniation is currently not supported");
    else
      assignTreatmentsWithoutDelqDetermination(startId, endId, d);
  }

    private void closeCompletePlans(String startId, String endId, final Date evalDate){
      final IReadWriteDataProvider p = getReadWriteProvider();
      Map m = new HashMap();
      m.put("START_CUSTOMER", startId);
      m.put("END_CUSTOMER", endId);
      m.put("EFFECTIVE_DATE", evalDate);
      List trans = new ArrayList();
      try {
        int count = 0;
        Iterator i = p.query(IReadWriteDataProvider.STREAM_RESULTS, "getAccountTreatmentPlansForClosing", m);
        while(i.hasNext()){
          count++;
          Map plan = (Map) i.next();
          final Long accountId = (Long) plan.get("ACCOUNT_ID");
          final Long planId = (Long) plan.get("TREATMENT_PLAN_ID");
          IRunnableTransaction t = new IRunnableTransaction(){
            public void execute(ITransaction t) throws SQLException, AbortTransactionException {
              p.update("resetAccountTurnedNonDelinquent", accountId, t);
              Map args = new HashMap();
              args.put("TREATMENT_PLAN_ID", planId);
              args.put("TREATMENT_END_DATE", evalDate);
              p.update("closeAccountTreatmentPlan", args, t);
              p.update("updateAccountCustomerStage", accountId, t);
              p.update("cancelPendingAccountTreatmentPlanActions", args, t);
            }};
          trans.add(t);
          if(count % commitSize == 0){
            IRunnableTransaction[] ta = (IRunnableTransaction[]) trans.toArray(new IRunnableTransaction[0]);
            p.execute(new RunnableTransactionSet(ta));
            trans.clear();
          }
        }
        if (trans.size() > 0){
          IRunnableTransaction[] ta = (IRunnableTransaction[]) trans.toArray(new IRunnableTransaction[0]);
          p.execute(new RunnableTransactionSet(ta));
        }
      } catch (SQLException e) {
        log.fatal("Failed to close account treatment plans", e);
        throw new RuntimeException(e);
      } catch (AbortTransactionException e) {
        log.fatal("Failed to close account treatment plans", e);
        throw new RuntimeException(e);
      }     
    }
    
    protected IReadWriteDataProvider getReadWriteProvider() {
      final IReadWriteDataProvider provider = (IReadWriteDataProvider) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "ReadWriteProvider");
      return provider;
    }
    
    /**
   * This method is intended for single customer command line execution only
   */
  public void assignCustomerTreatments(String customerId, Date d, boolean useDelinquencyDetermination)
    throws AgentFailureException, IncompleteAccountDataException {
    assignTreatments(customerId, customerId, d, useDelinquencyDetermination, null);
    log.debug("Treatments assigned for customer: " + customerId);
  }
  
  protected DefaultTreatmentProcessManager getManager() {
    if (manager == null){
      manager = new DefaultTreatmentProcessManager();      
    }
    return manager;
  }

}
