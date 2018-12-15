package com.profitera.services.business.schedule.impl;

import com.profitera.log.ILogClient;
import com.profitera.log.ILogProvider;
import com.profitera.log.ILogProvider.Level;

public class SchedulingServiceLogClient implements ILogClient {

  public static final String FAIL = "SCHEDULE_FAIL";
  public static final String SENT = "SCHEDULE_SENT";
  public static final String REPLACE = "SCHEDULE_REPLACE";
  public static final String REMOVE = "SCHEDULE_REMOVE";
  public static final String UNEXP = "SCHEDULE_UNEXPECTED";
  public static final String FIRE = "SCHEDULE_FIRING";
  public static final String SYNC_FAIL = "SCHEDULE_SYNC_FAIL";
  public static final String NO_FORWARDING = "SCHEDULE_NO_FORWARDING_SERVER";
  public static final String NO_FORWARDED = "SCHEDULE_NO_FORWARDED_SET";
  public static final String BAD_FORWARD = "SCHEDULE_FORWARD_FAIL";

  public String getName() {
    return "Scheduling Service";
  }

  public void registerMessages(ILogProvider provider) {
    provider.registerMessage(this, FAIL, Level.E, "Scheduled event {0} notification failed for {1}", 
        "The scheduled event failed due to the exception details provided and was not executed");
    provider.registerMessage(this, SENT, Level.I, "Sent scheduled event {0} for {1}", 
    "The scheduled event was executed.");
    provider.registerMessage(this, REPLACE, Level.I, "Replacing scheduled event {0} with new schedule {1}", 
    "The scheduled event was rescheduled based on the cron string provided.");
    provider.registerMessage(this, REMOVE, Level.I, "Removing scheduled event {0}", 
    "The scheduled event was removed and the event will not be triggered based on the schedule that was associated with it.");
    //Event schedule firing failed
    provider.registerMessage(this, UNEXP, Level.E, "An unexpected error occurred while monitoring or adjusting schedule information", 
    "The scheduling server encountered an unexpected error while waiting for the next event or updating event information.");
    provider.registerMessage(this, FIRE, Level.I, "Firing event {0} for {1} at {2} on service {3}", 
        "Firing the event based on the current schedule.");
    provider.registerMessage(this, SYNC_FAIL, Level.E, "Scheduling can not be synchronized with database, no events will fire", 
        "Scheduling information can not be refeshed from the database and as a result is deemed unreliable, no scheduled " +
        "events will execute until the refresh is successful");
    provider.registerMessage(this, NO_FORWARDING, Level.E, "Forwarding host not configured for scheduling service in server properties ({0})", 
        "The required property is not set in the server properties to specify the forwarding server to which scheduled events should be " +
        "sent for actual scheduling and execution from this server.");
    provider.registerMessage(this, NO_FORWARDED, Level.W, "Forwarded host not configured for scheduling service in server properties ({0}), attempting to discover host name", 
        "The optional property to specify a host name for the server that is forwarding events to ensure the events are fired on the originating server is not provided.");
    provider.registerMessage(this, BAD_FORWARD, Level.E, "Failed to lookup destination scheduler", 
        "The forwarding host could not be found or reached or an unexpected response was returned.");
  }
}
