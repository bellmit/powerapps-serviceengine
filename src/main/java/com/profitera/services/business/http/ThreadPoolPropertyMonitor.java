package com.profitera.services.business.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.thread.QueuedThreadPool;

class ThreadPoolPropertyMonitor {
  
  private static final int DEFAULT_MAX = 100;
  private static final int DEFAULT_MIN = 5;

  public interface IPropertyProvider {
    public void refresh();
    public int getIntProperty(String name, int defaultValue);
  }

  private final QueuedThreadPool pool;
  private IPropertyProvider pp;
  private Log log;
  private boolean doStop = false;

  ThreadPoolPropertyMonitor(IPropertyProvider p, QueuedThreadPool pool, final long period) {
    this.pp = p;
    this.pool = pool;
    pool.setMaxIdleTimeMs(5000);
    pool.setMinThreads(DEFAULT_MIN);
    pool.setMaxThreads(DEFAULT_MAX);
    setPoolThresholds(false);
    Thread monitorThread = new Thread(new Runnable(){
      public void run() {
        try {
          Thread.sleep(period);
        } catch (InterruptedException e) { }
        if (doStop) return;
        setPoolThresholds(true);
      }});
    monitorThread.setName("WebServerThreadThresholdMonitor");
    monitorThread.start();
  }

  private void setPoolThresholds(boolean doRefresh) {
    if (doRefresh) pp.refresh();
    int newMin = pp.getIntProperty("minthreads", DEFAULT_MIN);
    int newMax = pp.getIntProperty("maxthreads", DEFAULT_MAX);
    int curMin = pool.getMinThreads();
    int curMax = pool.getMaxThreads();
    if (newMin == curMin && newMax == curMax){
      return;
    }
    if (newMin > newMax){
      getLog().warn("Web server new minimum and maximum thread count settings invalid, " 
          + newMin + " > " + newMax + ", thresholds remain at " + curMin + "/" + curMax);
      return;
    }
    // If the new minimum will exceed the current 
    // max we need to bump up the max first so we
    // don't try to set the min > max
    if (newMin > curMax){
      pool.setMaxThreads(newMax);  
    }
    // If the new max is less than the current min
    // we have no problem, we set the new min first
    pool.setMinThreads(newMin);
    pool.setMaxThreads(newMax);
    getLog().info("Web server new minimum and maximum thread counts set to "+ newMin + "/" + newMax + " (changed from " + curMin + "/" + curMax + ")");
  }
  
  private Log getLog(){
    if (log == null) {
      log = LogFactory.getLog(this.getClass());
    }
    return log;
  }

  public void stop() {
    doStop = true;
  }

}
