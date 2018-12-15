/*
 * Created on Jan 4, 2005
 */
package com.profitera.services.business.notification;

import java.util.Map;

public interface INotificationProcessor {
  public static final String PATH = "_PATH";
  public static final String CONTENT_DATE_FORMAT = "_CONTENT_DATE_FORMAT";
  public static final String BATCH_SIZE = "_BATCH_SIZE";
  public static final String PROCESSOR_CLASS = "_PROCESSOR_CLASS";

  /**
   * @param treatment
   *          process to be notified about
   * @return treatment processes that failed to be notified about
   * @throws NotificationFailure
   *           if unexpected problem occured causing all process to fail
   */
  public void notify(Map[] processes) throws NotificationFailure;

  public String getNotifierCode();

  public void setNotifierCode(String code);

  public void setPropertyProvider(INotifierPropertyProvider pp);

  public int getBatchSize();

  public String getTreatmentProcessQueryName();

  public Map[] getUnsuccessfulTreatmentProcesses();

  public Map[] getSuccessfulTreatmentProcesses();

  public Long getUnsuccessfulTypeStatusId();

  public Long getSuccessfulTypeStatusId();

  public void notificationFailed(Throwable t);
}
