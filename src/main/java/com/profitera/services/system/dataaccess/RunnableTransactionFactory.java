package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;


/**
 * @author jamison
 */
public abstract class RunnableTransactionFactory {
  private int recordsPerTrans;
  private final Iterator recordSource;
  public RunnableTransactionFactory(Iterator recordSource, int records) {
    super();
    this.recordSource = recordSource;
    recordsPerTrans = records;
  }
  
  public IRunnableTransaction getTransaction(){
    return getTransaction(recordSource);
  }
  
  protected IRunnableTransaction getTransaction(Iterator recordSource){
    final List trans = getTransactionElements(recordSource);
    if (trans.size() == 0)
      return null;
    return new IRunnableTransaction(){
      public void execute(ITransaction t) throws SQLException, AbortTransactionException {
        for (Iterator iter = trans.iterator(); iter.hasNext();) {
          Object element = (Object) iter.next();
          process(element, t);
        }
      }};
  }
  private synchronized List getTransactionElements(Iterator recordSource) {
    final List trans = new ArrayList(recordsPerTrans);
    for(int i=0; i < recordsPerTrans && recordSource.hasNext(); i++)
      trans.add(recordSource.next());
    return trans;
  }

  protected abstract void process(Object element, ITransaction t) throws SQLException, AbortTransactionException;
}
