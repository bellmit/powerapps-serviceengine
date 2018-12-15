package com.profitera.services.business.schedule;

import java.util.Date;

import com.profitera.deployment.rmi.SchedulingServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.schedule.CronSchedule;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.business.schedule.impl.ScheduleManager;

public class SchedulingService extends ProviderDrivenService implements SchedulingServiceIntf {
  private ScheduleManager manager = new ScheduleManager(true);
  @Override
  public TransferObject scheduleEvent(String host, String lookupName, String id, Date tiggerTime) {
    return manager.scheduleEvent(host, lookupName, id, tiggerTime);
  }

  @Override
  public TransferObject scheduleEvent(String host, String lookupName, String id, CronSchedule schedule) {
    return manager.scheduleEvent(host, lookupName, id, schedule);
  }

  @Override
  public TransferObject removeEventSchedule(String host, String lookupName, String id) {
    return manager.removeEventSchedule(host, lookupName, id);
  }
}
