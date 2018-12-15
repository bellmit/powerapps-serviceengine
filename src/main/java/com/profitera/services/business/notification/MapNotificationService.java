package com.profitera.services.business.notification;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.NoSuchStatementException;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.deployment.rmi.NotificationServiceIntf;
import com.profitera.deployment.rmi.ScheduleListenerIntf;
import com.profitera.deployment.rmi.SchedulingServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.ITreatmentProcess;
import com.profitera.descriptor.business.schedule.CronSchedule;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.ITreatmentProcessService;
import com.profitera.services.system.dataaccess.TreatmentProcessUpdateException;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.ExtractMapValueMapCar;
import com.profitera.util.LimitingIterator;
import com.profitera.util.MapCar;
import com.profitera.util.Strings;

/**
 * @author jamison
 */
public class MapNotificationService extends ProviderDrivenService implements
    NotificationServiceIntf {
  public static Log log = LogFactory.getLog(MapNotificationService.class);

  /**
   * @see com.profitera.deployment.rmi.NotificationServiceIntf#notify(java.lang.String)
   */
  public TransferObject notify(String notifierCode) {
    IReadWriteDataProvider p = getReadWriteProvider();
    INotificationProcessor notifierInstance;
    try {
      notifierInstance = NotifierFactory
          .getNotificationProcessorInstance(notifierCode);
    } catch (NotificationFailure e2) {
      log.error(notifierCode + ": Unable to load notifier implementation", e2);
      return new TransferObject(TransferObject.ERROR, e2.getMessage());
    }
    // Here query treat proc template to get the treatment type that
    // is associated with the sending code, use that to determine the
    Long[] subTypes = null;
    try {
      subTypes = getSubTypesForCodeRef(notifierCode, p);
    } catch (Exception e) {
      log.fatal(
          "Unable to retrieve treatment process subtypes assocaited with notifier code "
              + notifierCode + ", no processing can be done", e);
      return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
    }
    if (subTypes == null || subTypes.length == 0) {
      MapNotificationService.log
          .warn(notifierCode
              + " has no treatment subtypes assigned, notification agent will not send anything.");
      return new TransferObject(new Long(0));
    } else {
      log.debug("Notifying the following subtype IDs: "
          + Strings.getListString(subTypes, ", "));
    }

    Iterator i;
    try {
      // This used to stream but the limiter later causes the cursor to 
      // stay open, we can list here because we are only returning the
      // ID, a Long (wrapped in a HashMap) so memory should not be
      // as issue up to the hundreds of thousands or more
      i = p.query(IReadWriteDataProvider.LIST_RESULTS,
          "getTreatmentProcessIds", Arrays.asList(subTypes));
    } catch (SQLException e2) {
      log
          .fatal(
              "Unable to retrieve treatment process ids for notification, no processing can be done",
              e2);
      return new TransferObject(TransferObject.EXCEPTION, e2.getMessage());
    }
    int batchSize = notifierInstance.getBatchSize();
    if (batchSize > 0) {
      i = new LimitingIterator(i, batchSize);
      MapNotificationService.log.info(notifierCode + ": Batch size is "
          + batchSize);
    } else {
      MapNotificationService.log.info(notifierCode
          + ": Has no batch size assigned, batch size is unlimited.");
    }
    if (!i.hasNext())
      return new TransferObject(new Long(0));
    List l = new ArrayList(batchSize > 0 ? batchSize : 1000);
    MapCar.map(new ExtractMapValueMapCar("TREATMENT_PROCESS_ID"), i, l);
    try {
      Map[] sendThese = retrieveForProcessing((Long[]) l.toArray(new Long[l
          .size()]), notifierInstance, p);
      if (sendThese.length == 0){
        log.info("Notification for " + notifierCode + " completed, "
            + 0 + " successful of " + 0);
        return new TransferObject(new Long(0));
      }
      notify(notifierInstance, sendThese);
      Map[] unsuccessful = notifierInstance.getUnsuccessfulTreatmentProcesses();
      Map[] successful = notifierInstance.getSuccessfulTreatmentProcesses();
      Long unsuccessfulTypeStatusId = notifierInstance
          .getUnsuccessfulTypeStatusId();
      Long successfulTypeStatusId = notifierInstance
          .getSuccessfulTypeStatusId();
      ITreatmentProcessService tps = getTreatmentProcessService();
      Date date = new Date();
      String user = "Admin";
      IRunnableTransaction t0 = updateTreatmentProcessStatuses(successful,
          successfulTypeStatusId, date, ITreatmentProcess.SUCCESSFUL_STATUS,
          tps, user);
      IRunnableTransaction t1 = updateTreatmentProcessStatuses(unsuccessful,
          unsuccessfulTypeStatusId, date,
          ITreatmentProcess.UNSUCCESSFUL_STATUS, tps, user);
      p.execute(new RunnableTransactionSet(
          new IRunnableTransaction[] { t0, t1 }));
      log.info("Notification for " + notifierCode + " completed, "
          + successful.length + " successful of " + sendThese.length);
      return new TransferObject(new Long(successful.length));
    } catch (NotificationFailure e) {
      MapNotificationService.log.error(
          "Notification failed: " + e.getMessage(), e);
      notifierInstance.notificationFailed(e);
      return new TransferObject(TransferObject.ERROR, e.getMessage());
    } catch (TreatmentProcessUpdateException e) {
      MapNotificationService.log.error(
          "Notification failed: " + e.getMessage(), e);
      notifierInstance.notificationFailed(e);
      return new TransferObject(TransferObject.ERROR, e.getMessage());
    } catch (AbortTransactionException e) {
      MapNotificationService.log.error(
          "Notification failed: " + e.getMessage(), e);
      notifierInstance.notificationFailed(e);
      return new TransferObject(TransferObject.ERROR, e.getMessage());
    } catch (SQLException e) {
      MapNotificationService.log.error(
          "Notification failed: " + e.getMessage(), e);
      notifierInstance.notificationFailed(e);
      return new TransferObject(TransferObject.ERROR, e.getMessage());
    }

  }

  private IRunnableTransaction updateTreatmentProcessStatuses(Map[] successful,
      Long successfulTypeStatusId, Date date, Long status,
      ITreatmentProcessService tps, String userId)
      throws TreatmentProcessUpdateException {
    IRunnableTransaction[] trans = new IRunnableTransaction[successful.length];
    for (int j = 0; j < successful.length; j++) {
      Map proc = successful[j];
      proc.put(ITreatmentProcess.PROCESS_STATUS_ID, status);
      proc
          .put(ITreatmentProcess.PROCESS_TYPE_STATUS_ID, successfulTypeStatusId);
      Date actualEnd = (Date) proc.get(ITreatmentProcess.ACTUAL_END_DATE);
      if (actualEnd == null) {
        proc.put(ITreatmentProcess.ACTUAL_END_DATE, date);
      }
      Date actualStart = (Date) proc.get(ITreatmentProcess.ACTUAL_START_DATE);
      if (actualStart == null) {
        proc.put(ITreatmentProcess.ACTUAL_START_DATE, date);
      }
      trans[j] = tps.updateTreatmentProcess(proc, date, userId);
    }
    return new RunnableTransactionSet(trans);
  }

  private Long[] getSubTypesForCodeRef(String code, IReadWriteDataProvider p)
      throws SQLException {
    Iterator i = p.query(IReadOnlyDataProvider.LIST_RESULTS,
        "getSubtypesByNotifierCode", code);
    List l = new ArrayList();
    while (i.hasNext())
      l.add(((Map) i.next()).get("ID"));
    return (Long[]) l.toArray(new Long[0]);
  }

  /**
   * @param l
   * @param s
   * @return
   * @throws NotificationFailure
   */
  private Map[] retrieveForProcessing(Long[] treatmentProcessIds,
      INotificationProcessor notifierInstance, IReadWriteDataProvider p)
      throws NotificationFailure {
    String queryName = notifierInstance.getTreatmentProcessQueryName();
    try {
      Iterator i = p.query(IReadOnlyDataProvider.STREAM_RESULTS, queryName,
          Arrays.asList(treatmentProcessIds));
      List l = new ArrayList();
      while (i.hasNext())
        l.add(i.next());
      return (Map[]) l.toArray(new Map[l.size()]);
    } catch (SQLException e) {
      throw new NotificationFailure(
          "Unable to execute query for treatment processes: " + queryName, e);
    } catch (NoSuchStatementException e) {
      throw new NotificationFailure(
          "Query for treatment processes does not exist: " + queryName, e);
    }
  }

  private void notify(INotificationProcessor notifierInstance, Map[] sendNow)
      throws NotificationFailure {
    notifierInstance.notify(sendNow);
  }

  private ITreatmentProcessService getTreatmentProcessService() {
    ITreatmentProcessService processService = (ITreatmentProcessService) lookup
        .getLookupItem(LookupManager.SYSTEM, "TreatmentProcessService");
    return processService;
  }
}
