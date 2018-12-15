package com.profitera.services.business.schedule.impl;

import java.util.Date;

import com.profitera.descriptor.business.schedule.CronSchedule;

public class Event {
  public Long primaryKey;
  public String lookupName;
  public String id;
  public CronSchedule schedule;
  public Date intialSchedulingDate;

  public Event(String lookupName, String id, CronSchedule schedule, Date initialSchedulingDate) {
    if (schedule == null){
      throw new RuntimeException("Valid schedule for event not supplied " + id + " " + lookupName);
    }
    internalConst(lookupName, id, schedule, initialSchedulingDate);
  }

  private Event(String service, String id2) {
    internalConst(service, id2, null, null);
  }

  private void internalConst(String lookupName, String id, CronSchedule schedule,
      Date initialSchedulingDate) {
    this.lookupName = lookupName;
    this.id = id;
    this.schedule = schedule;
    this.intialSchedulingDate = initialSchedulingDate;
  }

  /**
   * We can use @ here because if it is part of the lookup name it would be
   * escaped anyway: http://www.faqs.org/rfcs/rfc2396.html
   */
  public String getScheduledEventId() {
    return lookupName + "@" + id;
  }

  public static Event getDummyEvent(String host, String service, String id) {
    Event e = new Event(service, id);
    return e;
  }
}