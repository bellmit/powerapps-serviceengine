package com.profitera.services.business.archive.impl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.IArchive;
import com.profitera.iterator.DiskBufferedIterator;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.util.ArrayUtil;
import com.profitera.util.KeyedIterator;
import com.profitera.util.KeyedMapIterator;
import com.profitera.util.MapListUtil;

public class ArchiveProcess {
  static final String ARCHIVE_IN_PROGRESS = "ARCHIVE_IN_PROGRESS";
  private Long archiveProcessId;
  private static Log LOG;
  private long recordId = 0;
  private boolean isRunning = false;
  private Thread archivingThread;
  private final Archive archive;
  private final Long archivePackageId;

  public ArchiveProcess(Long archiveProcessId, Long archiveId, Archive archive) {
    this.archivePackageId = archiveId;
    this.archiveProcessId = archiveProcessId;
    this.archive = archive;
  }
  
  public TransferObject initializeArchiving(){
    final IReadWriteDataProvider p = archive.getProvider();
    try {
      p.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          Number count = (Number) p.queryObject(Archive.COUNT_IN_PROGRESS_ARCHIVE_PROCESSES);
          if (count != null && count.longValue() > 0) {
            throw new AbortTransactionException("One or more archiving processes is already in progress");
          }
          Map args = new HashMap();
          args.put("ID", archiveProcessId);
          args.put("ARCHIVE_ID", archivePackageId);
          args.put("NAME", archive.getName());
          args.put(IArchive.STATUS, IArchive.ARCHIVING_STATUS);
          args.put("CREATED_TIME", new Date());
          args.put("RECORD_ID", new Long(0));
          p.insert(Archive.INSERT_ARCHIVE_PROCESS, args, t);
        }});
      return new TransferObject();
    } catch (SQLException e){
      getLog().error("Failed to initalize archiving for " + archiveProcessId, e);
      return new TransferObject(TransferObject.EXCEPTION, "ARCHIVE_INIT_FAILED");
    } catch (AbortTransactionException e) {
      getLog().error("Failed to initalize archiving for " + archiveProcessId, e);
      return new TransferObject(TransferObject.ERROR, ARCHIVE_IN_PROGRESS);
    }
  }

  private Log getLog() {
    if (LOG == null) {
      LOG = LogFactory.getLog(this.getClass());
    }
    return LOG;
  }

  public synchronized TransferObject startArchiving(Object min, Object max, final int commitSize) {
    final IReadWriteDataProvider baseDataSource = archive.getProvider();
    final IReadWriteDataProvider sourceDataSource = archive.getSourceTablesetProvider();
    final IReadWriteDataProvider targetDataSource = archive.getArchiveTablesetProvider();
    final String[] deleteFromArchiveStatements = archive.getDeleteArchiveStatements();
    try {
      baseDataSource.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          Long recordId = (Long) baseDataSource.queryObject(Archive.GET_LAST_RECORD_ID_FOR_ARCHIVE, archiveProcessId);
          if (recordId.longValue() > 0) {
            // If this archiving process is in progress then we delete,
            // otherwise we fail
            String status = (String) baseDataSource.queryObject(Archive.GET_ARCHIVE_STATUS, archiveProcessId);
            if (!status.equals(IArchive.ARCHIVING_STATUS)){
              throw new AbortTransactionException("ARCHIVE_ALREADY_COMPLETE");
            }
            for(int i = 0; i < deleteFromArchiveStatements.length; i++) {
              String s = deleteFromArchiveStatements[deleteFromArchiveStatements.length - 1 - i];
              baseDataSource.delete(s, archiveProcessId, t);
            }
            baseDataSource.update(Archive.UPDATE_ARCHIVE_RECORD_ID_RESET, archiveProcessId, t);
          }
        }});
    } catch (AbortTransactionException e) {
      getLog().error("Error starting archive process", e);
      return new TransferObject(TransferObject.ERROR, e.getMessage());
    } catch (SQLException e) {
      getLog().error("Error starting archive process", e);
      return new TransferObject(TransferObject.EXCEPTION, "START_ARCHIVE_ERROR");
    }
    Iterator<Map> i = null;
    try {
      i = sourceDataSource.query(IReadWriteDataProvider.STREAM_RESULTS, Archive.SELECT_DATA_IN_RANGE, getMinMaxArgs(min, max));
      i = new DiskBufferedIterator<Map>(i);
    } catch (SQLException e){
      getLog().error("Error retrieving archive record keys", e);
      return new TransferObject(TransferObject.EXCEPTION, "START_ARCHIVE_ERROR");
    } catch (IOException e) {
      getLog().error("Error retrieving archive record keys", e);
      return new TransferObject(TransferObject.EXCEPTION, "START_ARCHIVE_ERROR");
    }
    if (i.hasNext()) {
      try {
        recordId = 0;
        isRunning = true;
        // Archive the first record synchronously, this will catch any
        // obvious error now, the process will then continue.
        archiveRecord(MapListUtil.getSingleItemList(i.next()), baseDataSource, sourceDataSource, targetDataSource);
      } catch (SQLException e){
        getLog().error("Error archiving initial record", e);
        return new TransferObject(TransferObject.EXCEPTION, "START_ARCHIVE_ERROR");
      } catch (AbortTransactionException e) {
        getLog().error("Error archiving initial record", e);
        return new TransferObject(TransferObject.ERROR, "START_ARCHIVE_ERROR");
      }
    }
    final Iterator<Map> recordKeys = i;
    archivingThread = new Thread(new Runnable(){
      public void run() {
        try {
          while (recordKeys.hasNext() && isRunning) {
            // this method resets the isRunning
            List<Map> toArchive = new ArrayList<Map>();
            while (recordKeys.hasNext() && isRunning && toArchive.size() < commitSize) {
              toArchive.add(recordKeys.next());
            }
            archiveRecord(toArchive, baseDataSource, sourceDataSource, targetDataSource);
          }
          baseDataSource.execute(new IRunnableTransaction(){
            public void execute(ITransaction t) throws SQLException,
                AbortTransactionException {
              baseDataSource.update("updateArchiveProcessComplete", archiveProcessId, t);
            }});
        } catch (SQLException e){
          getLog().error("Error archiving records, process halted", e);
        } catch (AbortTransactionException e) {
          getLog().error("Error archiving records, process halted", e);
        }
        isRunning = false;
      }});
    archivingThread.setName("Archiving-" + archive.getName());
    archivingThread.start();
    return new TransferObject(archiveProcessId);
  }
  
  public synchronized boolean isRunning(){
    return isRunning;
  }
  
  public void waitForCompletion() throws InterruptedException {
    if (isRunning) {
      archivingThread.join();
    }
  }
  
  private Map getMinMaxArgs(Object min, Object max) {
    Map args = new HashMap();
    args.put("MIN", min);
    args.put("MAX", max);
    return args;
  }


  private synchronized void archiveRecord(final List<Map> rootData, final IReadWriteDataProvider defaultSource, final IReadWriteDataProvider from, final IReadWriteDataProvider to) throws SQLException, AbortTransactionException {
    final String[] selectArchiveStatements = archive.getSelectArchiveStatements();
    final String[] insertDataArchiveStatements = archive.getInsertDataArchiveStatements();
    if (!isRunning) return;
    try {
      to.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
      for (int index = 0; index < rootData.size(); index++) {
        recordId++;
        final Map args = (Map) rootData.get(index);
        args.put("AR_ID", archiveProcessId);
        args.put("AR_REC_ID", new Long(recordId));
        to.insert(insertDataArchiveStatements[0], args, t);
        for (int i = 1; i < selectArchiveStatements.length; i++) {
        	Iterator<Map> queryKeys = to.query(IReadWriteDataProvider.LIST_RESULTS, Archive.SELECT_ARCHIVE_RECORD_KEY_T+i, args);
        	while(queryKeys.hasNext()){
        		Iterator<Map> query = from.query(IReadWriteDataProvider.LIST_RESULTS, selectArchiveStatements[i], queryKeys.next());
            while(query.hasNext()) {
              Map dataRow = query.next();
              dataRow.put("AR_ID", archiveProcessId);
              dataRow.put("AR_REC_ID", recordId);
              to.insert(insertDataArchiveStatements[i], dataRow, t);
            } 
        	}  
        }
        
      }
      final Map args = new HashMap();
      args.put("AR_ID", archiveProcessId);
      args.put("AR_REC_ID", new Long(recordId));
      args.put("RECORD_COUNT", new Long(rootData.size()));
      if (defaultSource != to) {
        defaultSource.execute(new IRunnableTransaction() {
          public void execute(ITransaction t) throws SQLException,
              AbortTransactionException {
            int count = defaultSource.update(Archive.INCREMENT_ARCHIVE, args, t);
            if (count != 1) {
              throw new AbortTransactionException("CONCURRENT_ARCHIVE");
            }
          }
        });
      } else {
        int count = defaultSource.update(Archive.INCREMENT_ARCHIVE, args, t);
        if (count != 1) {
          throw new AbortTransactionException("CONCURRENT_ARCHIVE");
        }
      }
      }});
    } catch (AbortTransactionException e) {
      isRunning = false;
      throw e;
    } catch (SQLException e) {
      isRunning = false;
      throw e;
    }
  }

  public synchronized TransferObject startDeletingSource(final int commitSize) {
    final IReadWriteDataProvider sourceProvider = archive.getSourceTablesetProvider();
    final Long[] recordCount = new Long[1];
    try {
      final IReadWriteDataProvider p = archive.getProvider();
      p.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          String status = (String) p.queryObject(Archive.GET_ARCHIVE_STATUS, archiveProcessId);
          if (!status.equals(IArchive.ARCHIVED_STATUS)){
            throw new AbortTransactionException("ARCHIVE_WRONG_STATUS");
          }
          recordCount[0] = (Long) p.queryObject(Archive.GET_LAST_RECORD_ID_FOR_ARCHIVE, archiveProcessId);
        }});
    } catch (AbortTransactionException e) {
      getLog().error("Error starting delete process", e);
      return new TransferObject(TransferObject.ERROR, e.getMessage());
    } catch (SQLException e) {
      getLog().error("Error starting delete process", e);
      return new TransferObject(TransferObject.EXCEPTION, "DELETE_SOURCE_ERROR");
    }
    //
    Iterator i = null;
    try {
      i = archive.getArchiveTablesetProvider().query(IReadWriteDataProvider.STREAM_RESULTS, Archive.GET_ALL_KEYS_FROM_ARCHIVE, archiveProcessId);
    } catch (SQLException e){
      getLog().error("Error retrieving archive record keys", e);
      return new TransferObject(TransferObject.EXCEPTION, "START_ARCHIVE_ERROR");
    }
    final KeyedMapIterator iter = new KeyedMapIterator(i, "AR_REC_ID");
    try {
      isRunning = true;
      // Archive the first record synchronously, this will catch any
      // obvious error now, the process will then continue.
      deleteRecord(iter, sourceProvider, 1);
    } catch (SQLException e){
      getLog().error("Error deleting initial record", e);
      return new TransferObject(TransferObject.EXCEPTION, "START_DELETE_ERROR");
    } catch (AbortTransactionException e) {
      getLog().error("Error deleting initial record", e);
      return new TransferObject(TransferObject.ERROR, "START_DELETE_ERROR");
    }
    archivingThread = new Thread(new Runnable(){
      public void run() {
        try {
          while (iter.hasNext() && isRunning) {
            // this method resets the isRunning on failure
            deleteRecord(iter, sourceProvider, commitSize);
          }
          final IReadWriteDataProvider p = archive.getProvider();
          p.execute(new IRunnableTransaction(){
            public void execute(ITransaction t) throws SQLException,
                AbortTransactionException {
              p.update("updateSourceDeleteProcessComplete", archiveProcessId, t);
            }});
        } catch (SQLException e){
          getLog().error("Error deleting records, process halted", e);
        } catch (AbortTransactionException e) {
          getLog().error("Error deleting records, process halted", e);
        }
        isRunning = false;
      }});
    archivingThread.setName("Archiving-" + archive.getName());
    archivingThread.start();
    return new TransferObject();
  }

  private synchronized void deleteRecord(final KeyedIterator iter, 
      final IReadWriteDataProvider p, final int commitSize) throws AbortTransactionException, SQLException {
    final String[] tables = archive.getTables();
    p.execute(new IRunnableTransaction(){
      public void execute(ITransaction t) throws SQLException,
          AbortTransactionException {
        for (int i = 0; i < commitSize && iter.hasNext(); i++) {
          deleteOneRecord(iter, p, tables, t);  
        }
      }});
  }

  public synchronized TransferObject migrateData(String from, String to, final int commitSize) {
    String[] sets = archive.getTablesSets();
    if (ArrayUtil.indexOf(from, sets) == -1) {
      return new TransferObject(new Object[]{from}, TransferObject.ERROR, "NO_SUCH_TABLE_SET");
    } else if (ArrayUtil.indexOf(to, sets) == -1) {
      return new TransferObject(new Object[]{to}, TransferObject.ERROR, "NO_SUCH_TABLE_SET");
    }
    String[] fromTables = archive.getTableSetTables(from);
    final String[] toTables = archive.getTableSetTables(to);
    final IReadWriteDataProvider fromProvider = archive.getTablesetProvider(from);
    final IReadWriteDataProvider toProvider = archive.getTablesetProvider(to);
    try {
      final Map args = new HashMap();
      args.put("TABLE_NAME", fromTables[0]);
      args.put("AR_ID", archiveProcessId);
      Long fromCount = (Long) fromProvider.queryObject(Archive.SELECT_ARCHIVE_RECORD_COUNT, args);
      if (fromCount.longValue() == 0) {
        return new TransferObject(new Object[]{from}, TransferObject.ERROR, "NO_ARCHIVE_RECORDS_PRESENT");
      }
      getLog().info("Archive " + archiveProcessId + " has " + fromCount + " records in " + from + " to be migrated to " + to);
      args.put("TABLE_NAME", toTables[0]);
      Long toCount = (Long) toProvider.queryObject("selectArchiveRecordCount", args);
      getLog().info("Archive " + archiveProcessId + " has " + toCount + " records in " + to + ", any existing records will be deleted before migration");
    } catch (SQLException e) {
      return new TransferObject(TransferObject.EXCEPTION, "ARCHIVE_RECORD_COUNT_FAILED");
    }
    TransferObject del = deleteArchiveRecords(toTables, toProvider);
    if (del.isFailed()){
      return del;
    }
    String[] selectStatements = archive.getSelectFromArchiveStatements();
    String[] insertStatements = archive.getInsertIntoArchiveStatements();
    for (int i = 0; i < fromTables.length; i++) {
      final Map args = new HashMap();
      final String toTable = toTables[i];
      final String fromTable = fromTables[i];
      final String dataInsert = insertStatements[i];
      args.put("AR_ID", archiveProcessId);
      args.put("TABLE_NAME", fromTable);
      args.put("FROM_TABLE", fromTable);
      args.put("TO_TABLE", toTable);
      try {
        final Iterator<Map> query = fromProvider.query(IReadWriteDataProvider.STREAM_RESULTS, selectStatements[i], args);
        while(query.hasNext()) {
          toProvider.execute(new IRunnableTransaction(){
            public void execute(ITransaction t) throws SQLException,
                AbortTransactionException {
              for (int i = 0; i < commitSize && query.hasNext(); i++) {
                final Map rowData = query.next();
                rowData.put("TO_TABLE", toTable);
                toProvider.insert(dataInsert, rowData, t);
              }
            }});
        }
        Long fromCount = (Long) fromProvider.queryObject(Archive.SELECT_ARCHIVE_RECORD_COUNT, args);
        args.put("TABLE_NAME", toTable);
        Long toCount = (Long) toProvider.queryObject(Archive.SELECT_ARCHIVE_RECORD_COUNT, args);
        if (fromCount.equals(toCount)){
          getLog().info("Copied " + fromCount + " from " + fromTable + " to " + toTable);
        } else {
          throw new AbortTransactionException();
        }
      } catch (AbortTransactionException e) {
        getLog().error("Insert of data into destination tables resulted in inconsistent record count", e);
        return new TransferObject(new String[]{fromTable, toTable}, TransferObject.ERROR, "INCONSISTENT_COPY_RECORD_COUNT");
      } catch (SQLException e) {
        getLog().error("Insert of data into destination tables failed", e);
        return new TransferObject(new String[]{fromTable, toTable}, TransferObject.EXCEPTION, "ERROR_COPYING_ARCHIVE_RECORDS");

      }
    }
    return new TransferObject();
  }

  private TransferObject deleteArchiveRecords(String[] tables, final IReadWriteDataProvider provider) {
    for (int i = 0; i < tables.length; i++) {
      String table = tables[tables.length - 1 - i];
      final Map deleteArgs = new HashMap();
      deleteArgs.put("AR_ID", archiveProcessId);
      deleteArgs.put("TABLE_NAME", table);
      try {
        provider.execute(new IRunnableTransaction(){
          public void execute(ITransaction t) throws SQLException,
              AbortTransactionException {
            provider.delete(Archive.DELETE_ARCHIVE_RECORDS, deleteArgs, t);
          }});
      } catch (AbortTransactionException e) {
        // This is unreachable
      } catch (SQLException e) {
        getLog().error("Failed to delete archive records", e);
        return new TransferObject(TransferObject.EXCEPTION, "ARCHIVE_DELETE_FAILED");
      }
    }
    return new TransferObject();
  }

  public TransferObject deleteData(String tableSet) {
    return deleteArchiveRecords(archive.getTableSetTables(tableSet), archive.getTablesetProvider(tableSet));
  }

  protected void deleteOneRecord(final KeyedIterator iter,
      final IReadWriteDataProvider p, final String[] tables, ITransaction t)
      throws SQLException {
    final List recordList = new ArrayList();
    Comparable c = iter.getNextKey();
    while(iter.hasNext() && c.compareTo(iter.getNextKey()) == 0){
      recordList.add(iter.next());
    }
    for (int i = 0; i < tables.length; i++) {
      int index = tables.length -1 - i;
      String table = tables[index];
      for (Iterator recs = recordList.iterator(); recs.hasNext();) {
        Map object = (Map) recs.next();
        if (archive.hasValidSourceDeleteArguments(object, table)){
          getLog().debug("Executing delete for source data keys: " + object);
          String d = archive.getDeleteSourceStatements()[index];
          p.delete(d, object, t);  
        } else {
          getLog().debug("Skipping execution for delete from " + table + " for invalid source data keys: " + object);
        }
      } 
    }
  }
}
