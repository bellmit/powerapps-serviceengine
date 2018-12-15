package com.profitera.services.business.document.impl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
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
import com.profitera.deployment.rmi.ApplicationServerServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.appserver.impl.AppServerService;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.document.IDocumentService;
import com.profitera.util.DateParser;
import com.profitera.util.MapListUtil;

public class DocumentArchiveSettings {
  private static final String RATE = "RATE";
  private static final String END_HOUR = "END_HOUR";
  private static final String START_HOUR = "START_HOUR";
  private static final String DOCUMENT_TYPE_ID = "DOCUMENT_TYPE_ID";
  private static final String _ENABLED = "_ENABLED";
  private static final String DAY_OF_WEEK = "DAY_OF_WEEK";
  private final String[] PREFIXES = new String[]{
      "SUNDAY",
      "MONDAY",
    "TUESDAY",
    "WEDNESDAY",
    "THURSDAY",
    "FRIDAY",
    "SATURDAY"
  };
  private final String[] DAILY_FIELDS = new String[]{
    START_HOUR,
    END_HOUR,
    RATE
  };
  
  private static Log LOG;
  
  private Log getLog(){
    if (LOG == null) {
      LOG = LogFactory.getLog(this.getClass());
    }
    return LOG;
  }
  
  public TransferObject updateSettings(final long appServer, final Map data, final IReadWriteDataProvider p) {
    List docTypes = new ArrayList();
    Object docTypeList = data.get("DOCUMENT_TYPE_LIST");
    if (docTypeList == null || !(docTypeList instanceof List)){
      if (data.get(DOCUMENT_TYPE_ID) != null) {
        docTypes = MapListUtil.getSingleItemList(DOCUMENT_TYPE_ID, data.get(DOCUMENT_TYPE_ID));
      }
    } else {
      docTypes = (List) docTypeList;
    }
    final List daysOfWeek = new ArrayList();
    for (int i = 0; i < PREFIXES.length; i++) {
      Object isOn = data.get(PREFIXES[i] + _ENABLED);
      if (isOn == null || !((Boolean)isOn).booleanValue()) {
        continue;
      }
      Map row = new HashMap();
      row.put(DAY_OF_WEEK, i);
      for (int j = 0; j < DAILY_FIELDS.length; j++) {
        row.put(DAILY_FIELDS[j], data.get(PREFIXES[i] + "_" + DAILY_FIELDS[j]));
      }
      daysOfWeek.add(row);
    }
    final List documentTypes = docTypes;
    try {
      p.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          p.delete("deleteDocumentArchivingDocumentTypes", appServer, t);
          p.delete("deleteDocumentArchivingWindows", appServer, t);
          p.delete("deleteDocumentArchivingSettings", appServer, t);
          Boolean isMaster = (Boolean) data.get("IS_MASTER");
          if (isMaster != null && isMaster) {
            p.update("clearDocumentArchivingMaster", data, t);
          }
          data.put(AppServerService.APP_SERVER_ID, appServer);
          p.insert("insertDocumentArchivingSettings", data, t);
          for (Iterator i = documentTypes.iterator(); i.hasNext();) {
            Map dt = (Map) i.next();
            dt.put(AppServerService.APP_SERVER_ID, appServer);
            p.insert("insertDocumentArchivingDocumentType", dt, t);
          }
          for (Iterator i = daysOfWeek.iterator(); i.hasNext();) {
            Map dt = (Map) i.next();
            dt.put(AppServerService.APP_SERVER_ID, appServer);
            p.insert("insertDocumentArchivingWindow", dt, t);
          }
        }});
      return new TransferObject();
    } catch (AbortTransactionException e) {
      getLog().error("Error updating application server document archiving", e);
    } catch (SQLException e) {
      getLog().error("Error updating application server document archiving", e);
    }
    return new TransferObject(TransferObject.ERROR, "DATABASE_ERROR");
  }

  public TransferObject getSettings(final long appServer, final IReadWriteDataProvider p) {
    final Map result = new HashMap();
    try {
      p.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          {
            Map m = getArchivingSettings(appServer, p);
            if (m == null) {
              return;
            }
            result.putAll(m);
          }
          Iterator iter = getArchivingWindows(appServer, p);
          while(iter.hasNext()) {
            Map row = (Map) iter.next();
            Number day = (Number) row.get(DAY_OF_WEEK);
            String dayName = PREFIXES[day.intValue()];
            result.put(dayName + _ENABLED, true);
            for (int i = 0; i < DAILY_FIELDS.length; i++) {
              result.put(dayName + "_" + DAILY_FIELDS[i], row.get(DAILY_FIELDS[i]));
            }
          }
          iter = p.query(IReadOnlyDataProvider.LIST_RESULTS, "getDocumentArchivingDocumentTypes", appServer);
          while(iter.hasNext()) {
            Map row = (Map) iter.next();
            Object dt = row.get(DOCUMENT_TYPE_ID);
            result.put(DOCUMENT_TYPE_ID, dt);
          }
        }});
      return new TransferObject(result);
    } catch (AbortTransactionException e) {
      getLog().error("Error updating application server document archiving", e);
    } catch (SQLException e) {
      getLog().error("Error updating application server document archiving", e);
    }
    return new TransferObject(TransferObject.ERROR, "DATABASE_ERROR");    
  }

  public void runArchiver(ApplicationServerServiceIntf app, IDocumentService docService,
      IReadWriteDataProvider provider) {
    while (true) {
      TransferObject serverId = app.getCurrentServerId();
      if (!serverId.isFailed()) {
        Long appServerid = (Long) serverId.getBeanHolder();
        runArchivingForServer(appServerid.longValue(), docService, provider);
      }
      // Sleep for 15 minutes
      try {
        Thread.sleep(15 * 60 * 1000);
      } catch (InterruptedException e) {
        // Ignore
      }
    }
    
  }

  private void runArchivingForServer(long appServerid, IDocumentService docService, IReadWriteDataProvider provider) {
    try {
      ProcessThrottle t = null;
      while (true) {
        Date today = new Date();
        System.out.println("");
        ArchivingWindow w = getArchivingWindow(today, appServerid, provider);
        if (w == null || !w.isInWindow(new Date())) {
            return;
        }
        Map settings = getArchivingSettings(appServerid, provider);
        Boolean isMaster = (Boolean) (settings == null ? false : settings.get("IS_MASTER"));
        // for now I have nothing for slaves, so if you are not the master there is nothing to do
        if (!isMaster) {
          return;
        }
        if (t == null) {
          t = new ProcessThrottle(w.getRate(), ProcessThrottle.MS_PER_MINUTE);
          t.start();
        }
        //
        Long documentId = getOldestNonArchivedDocument(appServerid, provider);
        if (documentId == null) {
          return;
        }
        //
        docService.archiveDocument(documentId);
        //
        long waitTime = t.processed();
        Thread.sleep(waitTime);
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (AbortTransactionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private Long getOldestNonArchivedDocument(Long appServerid,
      IReadWriteDataProvider provider) throws SQLException {
    Map m = getArchivingSettings(appServerid, provider);
    if (m == null) {
      return null;
    }
    Number months = (Number) m.get("DOCUMENT_AGE");
    if (months == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.add(Calendar.MONTH, - months.intValue());
    m.put("APP_SERVER_ID", appServerid);
    m.put("DOCUMENT_AGE", DateParser.getStartOfDay(c.getTime()));
    Object o = provider.queryObject("getOldestNonArchivedDocument", m);
    return o == null ? null : new Long(((Number)o).longValue());
  }

  private ArchivingWindow getArchivingWindow(Date today, Long appServerid,
      IReadWriteDataProvider provider) throws SQLException {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(today);
    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
    Iterator iterator = getArchivingWindows(appServerid, provider);
    ArchivingWindow w = null;
    while(iterator.hasNext()) {
      Map row = (Map) iterator.next();
      Number n = (Number) row.get(DAY_OF_WEEK);
      if (n.intValue() == dayOfWeek) {
        Number startHour = (Number) row.get(START_HOUR);
        Number endHour = (Number) row.get(END_HOUR);
        Number r = (Number) row.get(RATE);
        w = new ArchivingWindow(startHour.intValue(), endHour.intValue(), r.intValue());
      }
    }
    return w;
  }

  private Iterator getArchivingWindows(final long appServer,
      final IReadWriteDataProvider p) throws SQLException {
    Iterator iter = p.query(IReadOnlyDataProvider.LIST_RESULTS, "getDocumentArchivingWindows", appServer);
    return iter;
  }

  private Map getArchivingSettings(final long appServer,
      final IReadWriteDataProvider p) throws SQLException {
    Map m = (Map)p.queryObject("getDocumentArchivingSettings", appServer);
    return m;
  }
}
