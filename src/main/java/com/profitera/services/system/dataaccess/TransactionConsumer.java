package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;



public abstract class TransactionConsumer implements Runnable {
  private final IReadWriteDataProvider provider;
  private final RunnableTransactionFactory f;

  public TransactionConsumer(IReadWriteDataProvider provider, RunnableTransactionFactory f) {
    this.provider = provider;
    this.f = f;
  }

  public void run() {
    for (IRunnableTransaction r = f.getTransaction(); r != null; r = f.getTransaction()){
      try {
        provider.execute(r);
      } catch (SQLException e1) {
        handleSQLException(e1);
      } catch (AbortTransactionException e1) {
        handleAbortException(e1);
      }
    }
  }

  protected abstract void handleSQLException(SQLException e);
  protected abstract void handleAbortException(AbortTransactionException e);
}