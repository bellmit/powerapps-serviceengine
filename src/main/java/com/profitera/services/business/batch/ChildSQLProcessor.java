package com.profitera.services.business.batch;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public class ChildSQLProcessor {
  private Log log;
  private String mChildQuery;
  private String name;
  private List mExecuteQuery;

  ChildSQLProcessor(String name, Log parentLog, String childQuery, List executeQuery){
    this.mExecuteQuery = executeQuery;
    log = parentLog;
    mChildQuery = childQuery;
    this.name = name;
  }
  private String getName(){
    return name;
  }
  private Log getLog(){
    return log;
  }
  
  void processMainQuery(List records, Map args, IReadWriteDataProvider readWriter) throws SQLException {

      List results = processMainRecords(records, args, readWriter);
      List transactions = getTransactions(results, readWriter);
      commitTransactions(transactions, readWriter);    
  }
  
  private List processMainRecords(List mainRecords, Map args, IReadWriteDataProvider readWriter) throws SQLException {
    final List finalResult = new ArrayList();
    for (int i = 0; i < mainRecords.size(); i++) {
      Map newArgs = new HashMap();
      Map mainRecord = (Map)mainRecords.get(i);
      newArgs.putAll(args);
      newArgs.putAll(mainRecord);
      if (mChildQuery != null) {
        Iterator cQueryResult = readWriter.query(IReadOnlyDataProvider.LIST_RESULTS, mChildQuery, newArgs);
        finalResult.addAll(mergeChildResultsWithParent(newArgs, cQueryResult));
      }
      else {
        finalResult.add(newArgs);
      }
    }
    return finalResult;
  }
  
  private List mergeChildResultsWithParent(Map mainRecord, Iterator cQueryResult) {
    List result = new ArrayList();
    while (cQueryResult.hasNext()) {
      Map tmpMain = new HashMap();
      tmpMain.putAll(mainRecord);
      Map childRecord = (Map)cQueryResult.next();
      tmpMain.putAll(childRecord);
      result.add(tmpMain);
    }
    return result;
  }
  
  private List getTransactions(List args, final IReadWriteDataProvider readWriter) {
    List transactions = new ArrayList();
    for (int i = 0; i < args.size(); i++) {
      final Map arg = (Map)args.get(i);
      IRunnableTransaction tran = new IRunnableTransaction() {
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          for (int j = 0; j < mExecuteQuery.size(); j++) {
            String qName = (String)mExecuteQuery.get(j);
            if (readWriter.isInsertStatement(qName)) {
              Object genKey = readWriter.insert(qName, arg, t);
              getLog().debug(getName() +  " Insert statement " + qName + " executed with arguments " + arg + ". New record key is " + genKey);
            } else if (readWriter.isUpdateStatement(qName)) {
              int updated = readWriter.update(qName, arg, t);
              getLog().debug(getName() +  " Update statement " + qName + " executed with arguments " + arg + ". Affected " + updated + " row(s)");
            } else if (readWriter.isDeleteStatement(qName)) {
              int updated = readWriter.delete(qName, arg, t);
              getLog().debug(getName() +  " Delete statement " + qName + " executed with arguments " + arg + ". Affected " + updated + " row(s)");
            }
          }
        }
      };
      transactions.add(tran);
    }
    return transactions;
  }

  private void commitTransactions(List transactions, IReadWriteDataProvider readWriter) throws SQLException {
    try {
      readWriter.execute(new RunnableTransactionSet((IRunnableTransaction[]) transactions.toArray(new IRunnableTransaction[0])));
    } catch (AbortTransactionException e) {
      // I just built these transactions, I know they do not abort.
    }
  }

}
