package com.profitera.services.business.schedule.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.deployment.rmi.ScheduleListenerIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.schedule.CronSchedule;
import com.profitera.log.DefaultLogProvider;
import com.profitera.log.ILogProvider;
import com.profitera.services.business.http.IMessageHandler;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.InvalidQueryResultException;
import com.profitera.services.system.dataaccess.MapVerifyingMapCar;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.services.system.lookup.ServiceLookup;
import com.profitera.util.AsynchronousUpdateExecutor;
import com.profitera.util.MapCar;
import com.profitera.util.Strings;
import com.profitera.util.exception.InvalidArgumentException;

/**
 * TODO: Maintain event firing history in DB
 * 
 * @author jamison
 */
public class ScheduleManager {
  private static final String REPORT_MANAGEMENT_SERVICE = "ReportManagementService";
  private static final String GET_LAST_ACTIVE_SCHEDULE_ID = "getLastActiveEventScheduleId";
  private static final String GET_SCHEDULE_STATUS = "getEventScheduleStatus";
  private static final String GET_ACTIVE_EVENT_SCEDULES = "getActiveEventSchedules";
  private static final String INSERT_EVENT_SCHEDULE = "insertEventSchedule";
  private static final String UPDATE_EVENT_SCHEDULE_STATUS = "updateEventScheduleStatus";
  private static final String SCHEDULE_CRON = "SCHEDULE_CRON";
  private static final String REQUEST_DATE_TIME = "REQUEST_DATE_TIME";
  private static final String REQUESTING_SERVICE = "REQUESTING_SERVICE";
  private static final String REQUEST_ID = "REQUEST_ID";
  private static final String SCHEDULE_STATUS_ID = "SCHEDULE_STATUS_ID";
  private static final String[] EVENT_FIELDS = new String[] {SCHEDULE_CRON, REQUEST_DATE_TIME, REQUESTING_SERVICE,
      REQUEST_ID, SCHEDULE_STATUS_ID};

  private final static int MAX_ID_LENGTH = 200;
  private final static int TICK_SIZE = 60 * 1000;
  protected static final long ACTIVE = 1;
  protected static final long REPLACED = 2;
  protected static final long REMOVED = 3;
  private DefaultLogProvider logProvider;
  // This is final so you can't accidentally replace it with an unsynchronized
  // one.
  final private Map<String, Event> crons = Collections.synchronizedMap(new HashMap<String, Event>());
  private Timer tickTimer;

  public ScheduleManager(boolean isFiring) {
    if (isFiring) {
      tickTimer = new Timer();
      tickTimer.scheduleAtFixedRate(new TimerTask() {
        public void run() {
          try {
            tickFired();
          } catch (Exception e) {
            getLog().emit(SchedulingServiceLogClient.UNEXP, e);
          }
        }
      }, TICK_SIZE, TICK_SIZE);
    }
  }

  public TransferObject scheduleEvent(String host, String lookupName, String id, Date tiggerTime) {
    CronSchedule cs = new CronSchedule();
    cs.setCronOnceDate(tiggerTime);
    return scheduleEvent(host, lookupName, id, cs);
  }

  public TransferObject scheduleEvent(String host, String lookupName, String id, CronSchedule schedule) {
    if (id.length() > MAX_ID_LENGTH) {
      return new TransferObject(TransferObject.ERROR, "ID_TOO_LONG");
    }
    boolean isAlreadyPresent = false;
    Event e = new Event(lookupName, id, schedule, new Date());
    // Synch here so that if a DB refresh happens on other thread we don't have
    // an issue.
    synchronized (crons) {
      try {
        synchronizeCronsWithDatabase();
      } catch (InvalidQueryResultException e1) {
        throw new IllegalArgumentException(e1);
      } catch (SQLException e1) {
        throw new IllegalArgumentException(e1);
      }
      Long oldKey = null;
      if (crons.containsKey(e.getScheduledEventId())) {
        oldKey = ((Event) crons.get(e.getScheduledEventId())).primaryKey;
        getLog().emit(SchedulingServiceLogClient.REPLACE, e.getScheduledEventId(), schedule.getCronString());
      }
      try {
        addEvent(e, oldKey);
      } catch (SQLException ex) {
        throw new IllegalArgumentException(ex);
      } catch (AbortTransactionException ex) {
        throw new IllegalArgumentException(ex);
      }
      scheduleEvent(e);
    }
    return new TransferObject(isAlreadyPresent ? "SCHEDULE_REPLACED" : "NEW_SCHEDULE");
  }

  public TransferObject getEventSchedule(String host, String lookupName, String id) {
    if (Strings.nullifyIfBlank(host) == null) {
      return new TransferObject(new InvalidArgumentException("Host name is null or empty."));
    }
    if (Strings.nullifyIfBlank(lookupName) == null) {
      return new TransferObject(new InvalidArgumentException("Lookup name is null or empty."));
    }
    if (Strings.nullifyIfBlank(id) == null) {
      return new TransferObject(new InvalidArgumentException("Schedule event id is null or empty."));
    }
    if (id.length() > MAX_ID_LENGTH) {
      return new TransferObject(TransferObject.ERROR, "ID_TOO_LONG");
    }
    String eventId = Event.getDummyEvent(host, lookupName, id).getScheduledEventId();
    try {
      synchronizeCronsWithDatabase();
    } catch (InvalidQueryResultException ex) {
      throw new IllegalArgumentException(ex);
    } catch (SQLException ex) {
      throw new IllegalArgumentException(ex);
    }
    synchronized (crons) {
      if (crons.containsKey(eventId)) {
        Event e = (Event) crons.get(eventId);
        return new TransferObject(e.schedule.getCronString());
      }
      return new TransferObject(TransferObject.ERROR, "SCHEDULE_NOT_FOUND");
    }
  }

  public TransferObject removeEventSchedule(String host, String lookupName, String id) {
    String eventId = Event.getDummyEvent(host, lookupName, id).getScheduledEventId();
    synchronized (crons) {
      if (crons.containsKey(eventId)) {
        getLog().emit(SchedulingServiceLogClient.REMOVE, eventId);
        try {
          Event e = (Event) crons.get(eventId);
          removeEvent(e);
          // Rather than removing the event I will trigger a full sync
          synchronizeCronsWithDatabase();
        } catch (SQLException ex) {
          throw new IllegalArgumentException(ex);
        } catch (AbortTransactionException ex) {
          throw new IllegalArgumentException(ex);
        }
        return new TransferObject("SCHEDULE_REMOVED");
      }
      return new TransferObject("SCHEDULE_NOT_FOUND");
    }
  }

  private void removeEvent(final Event e) throws SQLException, AbortTransactionException {
    final Long id = e.primaryKey;
    final IRunnableTransaction t1 = getChangeScheduleStatusTransaction(id, REMOVED);
    // TODO
    Runnable object = new Runnable() {
      public void run() {
        try {
          getReadWriteProvider().execute(t1);
        } catch (SQLException sqle) {
          getLog().emit(SchedulingServiceLogClient.UNEXP, sqle);
          throw new IllegalArgumentException(sqle);
        } catch (AbortTransactionException ate) {
          getLog().emit(SchedulingServiceLogClient.UNEXP, ate);
          throw new IllegalArgumentException(ate);
        }
      }
    };

    AsynchronousUpdateExecutor exe = new AsynchronousUpdateExecutor();
    exe.executeUpdates(object, 1);
    exe.waitForAllTransactions();
  }

  private void addEvent(final Event e, final Long oldKey) throws SQLException, AbortTransactionException {
    IRunnableTransaction t1 = null;
    if (oldKey != null) {
      t1 = getChangeScheduleStatusTransaction(oldKey, REPLACED);
    }
    final IRunnableTransaction t2 = new IRunnableTransaction() {
      public void execute(ITransaction t) throws SQLException, AbortTransactionException {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put(SCHEDULE_STATUS_ID, ACTIVE);
        m.put(REQUEST_ID, e.id);
        m.put(REQUESTING_SERVICE, e.lookupName);
        m.put(REQUEST_DATE_TIME, e.intialSchedulingDate);
        m.put(SCHEDULE_CRON, e.schedule.getCronString());
        e.primaryKey = (Long) getReadWriteProvider().insert(INSERT_EVENT_SCHEDULE, m, t);
      }
    };
    final IRunnableTransaction t1clone = t1;
    // TODO
    AsynchronousUpdateExecutor exe = new AsynchronousUpdateExecutor();
    Runnable object = new Runnable() {
      public void run() {
        try {
          getReadWriteProvider().execute(new RunnableTransactionSet(new IRunnableTransaction[] {t1clone, t2}));
        } catch (AbortTransactionException e) {
          getLog().emit(SchedulingServiceLogClient.UNEXP, e);
          throw new IllegalArgumentException(e);
        } catch (SQLException se) {
          getLog().emit(SchedulingServiceLogClient.UNEXP, e);
          throw new IllegalArgumentException(se);
        }
      }
    };
    exe.executeUpdates(object, 1);
    exe.waitForAllTransactions();
  }

  private IRunnableTransaction getChangeScheduleStatusTransaction(final Long scheduleId, final long newStatus) {
    IRunnableTransaction t1;
    t1 = new IRunnableTransaction() {
      public void execute(ITransaction t) throws SQLException, AbortTransactionException {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("ID", scheduleId);
        m.put(SCHEDULE_STATUS_ID, newStatus);
        getReadWriteProvider().update(UPDATE_EVENT_SCHEDULE_STATUS, m, t);
      }
    };
    return t1;
  }

  private IReadWriteDataProvider getReadWriteProvider() {
    final IReadWriteDataProvider provider = (IReadWriteDataProvider) LookupManager.getInstance().getLookupItem(
        LookupManager.SYSTEM, "ReadWriteProvider");
    return provider;
  }

  private void scheduleEvent(Event e) {
    crons.put(e.getScheduledEventId(), e);
  }

  /**
   * TODO: This implementation is fine for just a few events, but MAY NOT SCALE.
   */
  private void tickFired() {
    final Date date = new Date();
    try {
      // if ticks elapsed == 0 then this will refresh, meaning that
      // at startup this will refresh.
      Long lastId = (Long) getReadWriteProvider().queryObject(GET_LAST_ACTIVE_SCHEDULE_ID);
      long maxIdInMemory = Long.MIN_VALUE;
      synchronized (crons) {
        for (Iterator<Map.Entry<String, Event>> i = crons.entrySet().iterator(); i.hasNext();) {
          Event event = i.next().getValue();
          if (event.primaryKey != null && maxIdInMemory < event.primaryKey) {
            maxIdInMemory = event.primaryKey;
          }
        }
      }
      if (lastId != null && lastId > maxIdInMemory) {
        synchronizeCronsWithDatabase();
      }
    } catch (InvalidQueryResultException e) {
      getLog().emit(SchedulingServiceLogClient.SYNC_FAIL, e);
      return;
    } catch (SQLException e) {
      getLog().emit(SchedulingServiceLogClient.SYNC_FAIL, e);
      return;
    }
    synchronized (crons) {
      for (Iterator<Map.Entry<String, Event>> i = crons.entrySet().iterator(); i.hasNext();) {
        Event event = i.next().getValue();
        tickFired(event, date);
      }
    }
  }

  private void synchronizeCronsWithDatabase() throws InvalidQueryResultException, SQLException {
    synchronized (crons) {
      // Query cron info from DB and refresh the data in the service.
      crons.clear();
      Event[] events = getDatabaseScheduledEvents();
      for (int i = 0; i < events.length; i++) {
        scheduleEvent(events[i]);
      }
    }
  }

  private Event[] getDatabaseScheduledEvents() throws SQLException, InvalidQueryResultException {
    Iterator<Map<String, Object>> i = queryActiveSchedules();
    List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
    MapCar.map(new MapVerifyingMapCar(EVENT_FIELDS), i, l);
    Event[] events = new Event[l.size()];
    for (int j = 0; j < events.length; j++) {
      events[j] = asEvent(l.get(j));
    }
    return events;
  }

  @SuppressWarnings("unchecked")
  private Iterator<Map<String, Object>> queryActiveSchedules() throws SQLException {
    return getReadWriteProvider()
        .query(IReadOnlyDataProvider.LIST_RESULTS, GET_ACTIVE_EVENT_SCEDULES, ACTIVE);
  }

  private Event asEvent(Map<String, Object> m) {
    Long primaryKey = (Long) m.get("ID");
    String lookupName = (String) m.get(REQUESTING_SERVICE);
    String id = (String) m.get(REQUEST_ID);
    CronSchedule schedule = new CronSchedule();
    schedule.setCronString((String) m.get(SCHEDULE_CRON));
    Date intialSchedulingDate = (Date) m.get(REQUEST_DATE_TIME);
    Event e = new Event(lookupName, id, schedule, intialSchedulingDate);
    e.primaryKey = primaryKey;
    return e;
  }

  private void tickFired(Event event, Date date) {
    if (event.schedule.isTriggerTime(date)) {
      // Here we need to make sure that the event of interest is still active
      // since it could have been killed by another server or something.
      try {
        Long status = (Long) getReadWriteProvider().queryObject(GET_SCHEDULE_STATUS, event.primaryKey);
        if (status == null || status.longValue() != ACTIVE) {
          return;
        }
      } catch (SQLException e) {
        getLog().emit(SchedulingServiceLogClient.SYNC_FAIL, e);
        return;
      }
      fireEvent(event, date);
    }
  }

  private void fireEvent(final Event event, final Date date) {
    getLog().emit(SchedulingServiceLogClient.FIRE, event.getScheduledEventId(), event.schedule.getCronString(), date,
        event.lookupName);
    // Events are fired on another thread to minimise the probability of missed
    // event cycles
    // caused by many events firing at once or an event listener that will
    // operate
    // in a synchronous manner.
    Runnable r = new Runnable() {
      public void run() {
        try {
          String name = event.lookupName;
          boolean isEventIntercepted = fireViaEvent(event);
          if (!isEventIntercepted) {
            ScheduleListenerIntf s = (ScheduleListenerIntf) LookupManager.getInstance().getLookupItem(
                LookupManager.BUSINESS, name);
            s.invokeScheduledEvent(event.id);
            getLog().emit(SchedulingServiceLogClient.SENT, event.getScheduledEventId(), event.schedule.getCronString());
          }
        } catch (Throwable t) {
          getLog().emit(SchedulingServiceLogClient.FAIL, t, event.getScheduledEventId(), event.schedule.getCronString());
        }
        // TODO: Insert record based on success/failure.
      }
    };
    new Thread(r).start();
  }

  private ILogProvider getLog() {
    if (logProvider == null) {
      logProvider = new DefaultLogProvider();
      logProvider.register(new SchedulingServiceLogClient());
    }
    return logProvider;
  }

  private boolean fireViaEvent(final Event event) {
    if (REPORT_MANAGEMENT_SERVICE.equals(event.lookupName)) {
      ServiceLookup lookup = LookupManager.getInstance().getLookup(LookupManager.SYSTEM);
      IMessageHandler m = (IMessageHandler) lookup.getService("MessageHandler");
      Object result = m.handleMessage(this, REPORT_MANAGEMENT_SERVICE, "invokeScheduledEvent", new Class[]{String.class},
          new Object[]{event.id}, new HashMap<String, Object>());
      return result != null;
    }
    return false;
  }
}
