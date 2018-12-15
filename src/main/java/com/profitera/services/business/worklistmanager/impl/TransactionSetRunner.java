package com.profitera.services.business.worklistmanager.impl;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.util.AsynchronousUpdateExecutor;

public class TransactionSetRunner {
  private static Log LOG;
  private final AsynchronousUpdateExecutor exe;
  private final int maxConcurrency;

  public TransactionSetRunner(AsynchronousUpdateExecutor exe, int maxConcurrency) {
    this.exe = exe;
    this.maxConcurrency = maxConcurrency;
  }
  
  private Log getLog() {
    if (LOG == null) {
      LOG = LogFactory.getLog(getClass());
    }
    return LOG;
  }
  
  public void executeUpdates(final IReadWriteDataProvider provider, final List transactions) {
    if (transactions.size() == 0) {
      return;
    }
    IRunnableTransaction[] trans = (IRunnableTransaction[]) transactions.toArray(new IRunnableTransaction[0]);
    transactions.clear();
    final RunnableTransactionSet set = new RunnableTransactionSet(trans);
    Runnable object = new Runnable(){
        public void run() {
          try {
            provider.execute(set);
          } catch (SQLException e) {
            getLog().error(e.getMessage(), e);
          } catch (AbortTransactionException e) {
            getLog().error(e.getMessage(), e);
          } catch (RuntimeException e) {
            getLog().error(e.getMessage(), e);
          }
        }};
    exe.executeUpdates(object, maxConcurrency);
  }
  
  public void waitForUpdates(){
    exe.waitForAllTransactions();
  }
}
