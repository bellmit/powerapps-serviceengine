package com.profitera.rpm;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.RunnableTransactionFactory;
import com.profitera.services.system.dataaccess.TransactionConsumer;
import com.profitera.services.system.lookup.LookupManager;

public class WorstAccountCustomerProfiler implements ICustomerProfiler {
  private static final Log LOG = LogFactory.getLog(ICustomerProfiler.class);
  private static final String UPDATE_CUSTOMER_PROFILE_SEGMENT = "updateCustomerProfileSegment";
  private static final String GET_CUSTOMER_ACCOUNT_PROFILES = "getCustomerAccountWorstProfiles";
  private static final String END_CUSTOMER = "END_CUSTOMER";
  private static final String START_CUSTOMER = "START_CUSTOMER";

  public void buildCacheTables() {
    // Do nothing

  }

  public void dropCacheTables() {
    // Do nothing

  }

  public void profileCustomers(boolean useCache, String startingId, String endingId) {
    int commitSize = 100;
    // Cache argument is ignored since there is no caching for this implementation
    final IReadWriteDataProvider p = getReadWriteProvider();
    Map args = new HashMap();
    args.put(START_CUSTOMER, startingId);
    args.put(END_CUSTOMER, endingId);
    Iterator i;
    try {
      i = p.query(IReadOnlyDataProvider.STREAM_RESULTS, GET_CUSTOMER_ACCOUNT_PROFILES, args);
      RunnableTransactionFactory f = new RunnableTransactionFactory(i, commitSize){
        protected void process(Object element, ITransaction t) throws SQLException, AbortTransactionException {
          Map worstAccount = (Map) element;
          p.update(UPDATE_CUSTOMER_PROFILE_SEGMENT, worstAccount, t);
        }};
      TransactionConsumer c = new TransactionConsumer(p, f){
        protected void handleSQLException(SQLException e) {
          LOG.error(e.getMessage(), e);
        }
        protected void handleAbortException(AbortTransactionException e) {
          LOG.error(e.getMessage(), e);
        }};
      c.run();
    } catch (SQLException e) {
      throw new RuntimeException("Unable to execute " + GET_CUSTOMER_ACCOUNT_PROFILES, e);
    }
    

  }
  
  protected IReadWriteDataProvider getReadWriteProvider() {
    final IReadWriteDataProvider provider = (IReadWriteDataProvider) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "ReadWriteProvider");
    return provider;
  }


}
