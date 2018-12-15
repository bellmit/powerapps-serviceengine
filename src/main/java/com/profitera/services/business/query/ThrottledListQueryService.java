package com.profitera.services.business.query;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import com.profitera.deployment.rmi.ListQueryServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.server.ServiceEngine;

public class ThrottledListQueryService extends ListQueryService implements ListQueryServiceIntf {
  private Object lock = new Object();
  private Map throttles = new HashMap(500);
  public TransferObject getQueryList(String name, Map arguments) {
    Semaphore s = null;
    synchronized (lock) {
      s = (Semaphore) throttles.get(name);
      if (s == null){
        s = new Semaphore(getPermitCount(name), true);
        throttles.put(name, s);
      }
    }
    try {
      try {
        s.acquire();
        log.debug("ListQueryService permits for " + name + ": " + s.availablePermits() + " available/" + s.getQueueLength() + " waiting");
      } catch (InterruptedException e) {
        // carry on!
      }
    return super.getQueryList(name, arguments);
    } finally {
      // I use the reference from before instead of retrieving from
      // the map so that if the semaphores are reloaded I don't release on
      // a different semaphore.
      s.release();
    } 
  }
  private int getPermitCount(String qName) {
    int count =  ServiceEngine.getIntProp("listqueryservice.concurrency", Short.MAX_VALUE);  
    count =  ServiceEngine.getIntProp("listqueryservice.concurrency." + qName, count);
    log.debug("ListQueryService permits for " + qName + ": " + count);
    return count;
  }
  public TransferObject reloadQueryProcessors() {
    throttles = new HashMap();
    return super.reloadQueryProcessors();
  }
  
  

}
