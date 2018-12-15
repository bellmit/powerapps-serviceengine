package com.profitera.services.business.notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.deployment.rmi.NotificationServiceIntf;
import com.profitera.descriptor.business.TransferObject;


public class NotificationScheduler implements Runnable {
  Log log = LogFactory.getLog(NotificationScheduler.class);
  private int tickSize;
  private Timer tickTimer;
  private List notifiers = Collections.synchronizedList(new ArrayList()); 
  private final NotificationServiceIntf notificationService;
  
  public NotificationScheduler(NotificationServiceIntf n, int tickSize){
    notificationService = n;
    this.tickSize = tickSize;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run() {
    tickTimer = new Timer();
    tickTimer.scheduleAtFixedRate(new TimerTask(){
      public void run() {
        try{
          doTick();
        } catch (Exception e){
          log.fatal("Notification scheduling failed.", e);
        }
      }
      }, tickSize, tickSize);
    
  }
  
  private void doTick() {
     for(int i = 0; i < notifiers.size(); i++){
       List l = (List) notifiers.get(i);
       try {
	       String code = (String) l.get(0);
	       long elapsed = ((Long) l.get(1)).longValue() + 1; // +1 is the tick!
	       l.set(1, new Long(elapsed));
	       IScheduleManager manager = (IScheduleManager) l.get(2);
	       doTick(code, elapsed, manager);
       } catch(Throwable t){
         log.error("Notification failed for notifier(" + i + "): " + l, t);
       }
     }
  }

  private void doTick(String code, long elapsed, IScheduleManager manager) {
    // No manager means do not run the notifier, had to be set internally 
    // bc null managers not permitted by addNotifierCode() code.
    if (manager == null) return;
    if (manager.isPermitted(code, elapsed)){
      log.info("Scheduler requesting notification for " + code);
      TransferObject to = notificationService.notify(code);
      if (to.isFailed()){
        log.error("Scheduler requested notification for " + code + " failed: " + to.getMessage());
      } else {
        log.info("Scheduler requested notification for " + code + " successful: " + to.getBeanHolder());
      }
    }
  }
  
  public void addNotifierCode(String code, IScheduleManager manager){
    if (code == null)
      throw new IllegalArgumentException("Notifier code can not be null");
    if (manager == null)
      throw new IllegalArgumentException("Notifier schedule manager can not be null");
    for(int i = 0; i < notifiers.size(); i++){
      List n = (List)notifiers.get(0);
      if (n.get(2) != null && code.equals(n.get(0))){
        log.info("Notification for " + code + " already present, no action taken");
        return;
      }
    }
    List l = new ArrayList();
    l.add(0, code);
    l.add(1, new Long(0));
    l.add(2, manager);
    notifiers.add(l);
    log.info("Added notification for " + code);
  }
  
  public void disableNotifierCode(String disableCode){
    if (disableCode == null)
      throw new IllegalArgumentException("Notifier code can not be null");
    for(int i = 0; i < notifiers.size(); i++){
      List l = (List) notifiers.get(i);
      String code = (String) l.get(0);
      if (disableCode.equals(code)){
        l.set(1, new Long(0)); // reset elapsed for (possible) restart
        l.set(2, null); // null manager turns notifier off
      }
    }
    log.info("Disabled notification for " + disableCode);
  }
  
}