package com.profitera.services.business.document.impl;

public class ProcessThrottle {
  public static final int MS_PER_MINUTE = 1000 *60;
  private int actionsPerMinute;
  private long start;
  private int counter;
  private final long intervalLength;

  public ProcessThrottle(int actionsPerInterval, long intervalLength) {
    this.actionsPerMinute = actionsPerInterval;
    this.intervalLength = intervalLength;
  }
  
  public void start() {
    this.start = System.currentTimeMillis();
    this.counter = 0;
  }
  
  public long processed() {
    long elapsed = System.currentTimeMillis() - start;
    if (elapsed > intervalLength) {
      start();
      elapsed = elapsed - intervalLength;
    }
    counter++;
    if (counter >= actionsPerMinute) {
      return intervalLength - elapsed;
    } else {
      return 10;
    }
    
    //double percentComplete = ((double)counter)/((double)actionsPerMinute);    
    //double percentElapsed = ((double)elapsed) / ((double)intervalLength);
    //if (percentElapsed < 0.01) {
    //  percentElapsed = 0.01;
    //}
  }
  

}
