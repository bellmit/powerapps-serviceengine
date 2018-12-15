package com.profitera.services.business.schedule;

import java.util.Date;

import com.profitera.deployment.rmi.SchedulingServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.schedule.CronSchedule;
import com.profitera.services.business.BusinessService;
import com.profitera.services.business.schedule.impl.ScheduleManager;

public class ForwardingSchedulingService extends BusinessService implements SchedulingServiceIntf {
  private ScheduleManager manager = new ScheduleManager(false);
  public TransferObject scheduleEvent(String host, final String lookupName, final String id, final Date tiggerTime) {
    return manager.scheduleEvent(host, lookupName, id, tiggerTime);
  }

  public TransferObject scheduleEvent(String host, final String lookupName, final String id, final CronSchedule schedule) {
    return manager.scheduleEvent(host, lookupName, id, schedule);
  }

  public TransferObject removeEventSchedule(String host, final String lookupName, final String id) {
    return manager.removeEventSchedule(host, lookupName, id);
  }
}
