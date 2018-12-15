package com.profitera.services.business.archive.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.w3c.dom.Document;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.datasource.IDataSourceConfigurationSet;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.IArchive;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.services.business.archive.DataArchivingService;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.util.ExtractMapValueMapCar;
import com.profitera.util.MapCar;
import com.profitera.util.MapListUtil;
import com.profitera.util.NoopMapCar;
import com.profitera.util.xml.DocumentLoader;

public class ArchivingService {
  private abstract class ArchiveRunner {
    private final IReadWriteDataProvider p;
    private final Long id;

    public ArchiveRunner(Long id, IReadWriteDataProvider p) {
      this.id = id;
      this.p = p;
    }
    
    public TransferObject run(Long archiveProcessId, final ArchiveTracker tracker, boolean waitForCompletion) {
      TransferObject to = checkNoIncompleteArchiveProcess(id, archiveProcessId, tracker, p);
      if (to.isFailed()) {
        getLog().error("There is an existing archiving process in progress");
        return to;
      }
      String name = null;
      boolean ok = false;
      ArchiveProcess archiveProcess = null;
      try {
        Archive a = getArchiveById(id, p);
        name = a.getName();
        tracker.setRunning(a);
        if (archiveProcessId == null) {
          archiveProcessId = allocateProcessId(id, a);
        }
        archiveProcess = new ArchiveProcess(archiveProcessId, id, a);
        TransferObject o = run(a, archiveProcess);
        ok = !o.isFailed();
        if (ok && waitForCompletion) {
          try {
            archiveProcess.waitForCompletion();
            tracker.setRunning(null);
          } catch (InterruptedException e) {
            // Ignore
          }
        } 
        return o;
      } catch (SQLException e){
        return getSQLError(e);
      } catch (AbortTransactionException e) {
        return getInvalidArchiveFormat(name, e);
      } catch (FileNotFoundException e) {
        return getPathError(e);
      } finally {
        if (!ok) {
          tracker.setRunning(null);
        } else if (!waitForCompletion){
          final ArchiveProcess proc = archiveProcess;
          Thread t = new Thread(new Runnable(){
            public void run() {
              try {
                proc.waitForCompletion();
              } catch (InterruptedException e) {
                // Nothing to do here, assume the process finished
              }
              tracker.setRunning(null);
            }});
          t.setName("Archive tracker");
          t.start();
        }
      }
    }
    
    protected abstract TransferObject run(Archive a, ArchiveProcess archiveProcess)
    throws SQLException, AbortTransactionException;
  }
  
  private Log logger;
  private final IDataSourceConfigurationSet dataSources;

  public ArchivingService(Log log, IDataSourceConfigurationSet dataSources) {
    this.logger = log;
    this.dataSources = dataSources;
  }
  
  private Log getLog(){
    return logger;
  }
  
  public TransferObject getArchiveDefinitions(IReadWriteDataProvider p) {
    File dir;
    try {
      dir = getArchivePath();
    } catch (FileNotFoundException e) {
      return getPathError(e);
    }
    getLog().info("Retrieving archive definitions file from " + dir.getAbsolutePath());
    File[] files = dir.listFiles(new FileFilter(){
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(".archive");
      }});
    List names = new ArrayList();
    for (int i = 0; i < files.length; i++) {
      names.add(files[i].getName());
    }
    try {
      List db = getArchivesInDatabase(p);
      List dbNames = MapCar.map(new ExtractMapValueMapCar("FILE_NAME"), db);
      List notInDb = new ArrayList();
      for (Iterator i = names.iterator(); i.hasNext();) {
        String name = (String) i.next();
        if (!dbNames.contains(name)){
          notInDb.add(name);
        }
      }
      db = addMissingArchivesToDatabase(dir, notInDb, db, p);
      List noSuchFile = new ArrayList();
      for (Iterator i = db.iterator(); i.hasNext();) {
        Map m = (Map) i.next();
        String name = (String) m.get("FILE_NAME");
        if (!names.contains(name)){
          noSuchFile.add(name);
          getLog().warn("Archive path " + dir.getAbsolutePath() + " missing expected archive file found in the database: " + name);
        } else {
          try {
            Archive a = getArchive(dir, name);
            m.put(IArchive.RANGE_NAME, a.getRangeName());
            m.put(IArchive.RANGE_TYPE, a.getRangeType());
            String[] tablesSets = a.getTablesSets();
            m.put(IArchive.TABLE_SET_LIST, new ArrayList(Arrays.asList(tablesSets)));
          } catch (AbortTransactionException e) {
            getLog().warn("Archive path " + dir.getAbsolutePath() + " file " + name + " is invalid");
            noSuchFile.add(name);
          }
        }
      }
      for (Iterator i = noSuchFile.iterator(); i.hasNext();) {
        String name = (String) i.next();
        MapListUtil.filterInPlace("FILE_NAME", name, db, false);
      }
      return new TransferObject(db);
    } catch (SQLException e) {
      return getSQLError(e);
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
  }

  private TransferObject getPathError(FileNotFoundException e) {
    getLog().error(e.getMessage(), e);
    return new TransferObject(new Object[]{DataArchivingService.ARCHIVE_PATH_PROP}, TransferObject.EXCEPTION, "ARCHIVE_PATH_ERROR");
  }
  
  private Archive getArchive(File dir, String name) throws AbortTransactionException {
    File path = new File(dir, name);
    Document d = DocumentLoader.loadDocument(path);
    if (d == null) {
      throw new AbortTransactionException("XML format not valid for archive file " + path.getAbsolutePath());
    }
    return new Archive(name, d, dataSources);
    
  }

  private List addMissingArchivesToDatabase(final File archivePath, final List notInDb, List db,
      final IReadWriteDataProvider readWriteProvider) throws SQLException, TransferObjectException {
    if (notInDb.size() ==0) {
      return db;
    }
    final String[] evaluating = new String[1];
    try {
      readWriteProvider.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          for (Iterator i = notInDb.iterator(); i.hasNext();) {
            String name = (String) i.next();
            evaluating[0] = name;
            try {
              Archive a = getArchive(archivePath, name);
              String rootTable = a.getTables()[0];
              Map args = new HashMap();
              args.put("FILE_NAME", name);
              args.put("ROOT_TABLE", rootTable);
              readWriteProvider.insert("insertDataArchivePackage", args, t);
            } catch (IllegalArgumentException e) {
              throw new AbortTransactionException(e.getMessage(), e);
            }
          }
          
        }});
    } catch (AbortTransactionException e) {
      TransferObject t = getInvalidArchiveFormat(evaluating[0], e);
      throw new TransferObjectException(t);
    }
    return getArchivesInDatabase(readWriteProvider);
  }

  private TransferObject getInvalidArchiveFormat(String name, AbortTransactionException e) {
    getLog().error("Archive format invalid: " + name, e);
    return new TransferObject(new Object[]{name}, TransferObject.EXCEPTION, "ARCHIVE_FORMAT_INVALID");
  }

  private List getArchivesInDatabase(IReadWriteDataProvider provider) throws SQLException {
    Iterator query = provider.query(IReadWriteDataProvider.LIST_RESULTS, "getDataArchivePackages", null);
    ArrayList l = new ArrayList();
    MapCar.map(new NoopMapCar(), query, l);
    return l;
  }

  private File getArchivePath() throws FileNotFoundException {
    String value = ServiceEngine.getProp(DataArchivingService.ARCHIVE_PATH_PROP, "../config");
    File f = new File(value);
    if (!f.exists()) {
      throw new FileNotFoundException(DataArchivingService.ARCHIVE_PATH_PROP + " location for archive files does not exist: " + value);
    }
    if (!f.isDirectory()) {
      throw new FileNotFoundException(DataArchivingService.ARCHIVE_PATH_PROP + " location for archive files is not a directory: " + value);
    }
    return f;
  }

  public TransferObject getArchiveProcesses(Long archiveId,
      IReadWriteDataProvider provider) {
    try {
      Iterator query = provider.query(IReadWriteDataProvider.LIST_RESULTS, "getDataArchivePackageProcesses", archiveId);
      ArrayList l = new ArrayList();
      MapCar.map(new NoopMapCar(), query, l);
      return new TransferObject(l);
    } catch (SQLException e) {
      return getSQLError(e);
    }
  }

  public TransferObject checkNoIncompleteArchiveProcess(Long requestingArchiveId, Long requestingProcessId, ArchiveTracker tracker,
      IReadWriteDataProvider readWriteProvider) {
    String fileName = null;
    Archive running = tracker.getRunning();
    if (running != null) {
      return getInProgress(tracker.getRunning().getName());
    } else {
      try {
        List l = getArchivesInDatabase(readWriteProvider);
        Map rec01 = (Map) l.get(0);
        fileName = (String) rec01.get("FILE_NAME");
        Archive a = getArchive(getArchivePath(), fileName);
        Long[] archiveIds = a.getInProgressArchiveProcesses();
        for (int i = 0; i < archiveIds.length; i++) {
          Long id = archiveIds[i];
          if (requestingArchiveId != null && id.equals(requestingArchiveId)) {
            // Here I'm not sure what conditions are OK and what are not.
          } else {
            int indexOf = MapListUtil.firstIndexOf("ID", id, l);
            Map archive = (Map) l.get(indexOf);
            String name = (String) archive.get("FILE_NAME");
            return getInProgress(name);
          }
        }
      } catch (SQLException e) {
        return getSQLError(e);
      } catch (AbortTransactionException e) {
        return getInvalidArchiveFormat(fileName, e);
      } catch (FileNotFoundException e) {
        return getPathError(e);
      }
    }
    return new TransferObject();
  }

  private TransferObject getInProgress(String name) {
    return new TransferObject(new Object[]{name}, TransferObject.ERROR, ArchiveProcess.ARCHIVE_IN_PROGRESS);
  }
  
  private String getArchiveName(Long id, IReadWriteDataProvider readWriteProvider) throws SQLException {
    List l = getArchivesInDatabase(readWriteProvider);
    int indexOf = MapListUtil.firstIndexOf("ID", id, l);
    Map archive = (Map) l.get(indexOf);
    String name = (String) archive.get("FILE_NAME");
    return name;
  }

  public TransferObject getSelectedArchiveRecordCountInRange(Long archiveId,
			Object minValue, Object maxValue, IReadWriteDataProvider p){
		String name = null;
		try{
			name = getArchiveName(archiveId, p);
			Archive a = getArchiveById(archiveId, p);
			return new TransferObject(a.getRecordCountInRange(maxValue));
		} catch (SQLException e) {
			return getSQLError(e);
		} catch (AbortTransactionException e) {
			return getInvalidArchiveFormat(name, e);
		} catch (FileNotFoundException e) {
			return getPathError(e);
		}
	}
  
  public TransferObject getArchiveMinimumRange(Long archiveId,
      IReadWriteDataProvider readWriteProvider) {
    String n = null;
    try {
      n = getArchiveName(archiveId, readWriteProvider);
      Archive a = getArchive(getArchivePath(), n);
      return new TransferObject(a.getMinimumRangeValue());
    } catch (SQLException e) {
      return getSQLError(e);
    } catch (AbortTransactionException e) {
      return getInvalidArchiveFormat(n, e);
    } catch (FileNotFoundException e) {
      return getPathError(e);
    }
    
  }

  public TransferObject startArchiving(Long archiveId, Long archiveProcessId,
      final Comparable min, final Comparable max, ArchiveTracker tracker, 
      boolean wait, final int commitSize, IReadWriteDataProvider provider) {
    final boolean isNew = archiveProcessId == null;
    ArchiveRunner a = new ArchiveRunner(archiveId, provider){
      protected TransferObject run(Archive a, ArchiveProcess archiveProcess)
          throws SQLException, AbortTransactionException {
        if (isNew) {
          TransferObject t = archiveProcess.initializeArchiving();
          if (t.isFailed()) return t;
        }
        return archiveProcess.startArchiving(min, max, commitSize);
      }};
    return a.run(archiveProcessId, tracker, wait);
  }

  private TransferObject getSQLError(SQLException e) {
    getLog().error("Database error in archive processing", e);
    return new TransferObject(TransferObject.EXCEPTION, "DATABASE_ERROR");
  }

  private Archive getArchiveById(Long archiveId, IReadWriteDataProvider provider)
      throws SQLException, AbortTransactionException, FileNotFoundException {
    String n = getArchiveName(archiveId, provider);
    Archive a = getArchive(getArchivePath(), n);
    return a;
  }

  private Long allocateProcessId(Long archiveId, Archive a) throws SQLException {
    Long id = a.getLastArchiveProcessId();
    return new Long(id.longValue() + 1);
  }

  public TransferObject startDeletingSource(Long archiveId,
      Long archiveProcessId, ArchiveTracker tracker, 
      boolean wait, int commitSize, IReadWriteDataProvider provider) {
    ArchiveRunner a = new ArchiveRunner(archiveId, provider){
      protected TransferObject run(Archive a, ArchiveProcess archiveProcess)
          throws SQLException, AbortTransactionException {
        return archiveProcess.startDeletingSource(1);
      }};
    return a.run(archiveProcessId, tracker, wait);
  }

  public TransferObject startCopying(Long archiveId, Long archiveProcessId,
      final String fromArchive, final String toArchive, ArchiveTracker tracker,
      boolean wait, final int commitSize, IReadWriteDataProvider readWriteProvider) {
    ArchiveRunner a = new ArchiveRunner(archiveId, readWriteProvider){
      protected TransferObject run(Archive a, ArchiveProcess archiveProcess)
          throws SQLException, AbortTransactionException {
        return archiveProcess.migrateData(fromArchive, toArchive, commitSize);
      }};
    return a.run(archiveProcessId, tracker, wait);
  }

  public TransferObject startDeleting(Long archiveId, Long archiveProcessId,
      final String fromArchive, ArchiveTracker tracker,
      boolean wait, IReadWriteDataProvider readWriteProvider) {
    ArchiveRunner a = new ArchiveRunner(archiveId, readWriteProvider){
      protected TransferObject run(Archive a, ArchiveProcess archiveProcess)
          throws SQLException, AbortTransactionException {
        return archiveProcess.deleteData(fromArchive);
      }};
    return a.run(archiveProcessId, tracker, wait);
  }

}
