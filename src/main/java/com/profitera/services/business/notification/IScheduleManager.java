package com.profitera.services.business.notification;

/**
 * @author jamison
 */
public interface IScheduleManager {

  public boolean isPermitted(String code, long elapsed);

}
