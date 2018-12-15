package com.profitera.services.business.document.impl;

import java.util.Calendar;
import java.util.Date;

public class ArchivingWindow {
  private int startHour;
  private int endHour;
  private int rate;
  
  public ArchivingWindow(int start, int end, int rate) {
    this.startHour = start;
    this.endHour = end == 0 ? 24 : end;
    this.rate = rate;
  }
  
  public boolean isInWindow(Date d) {
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
    return hourOfDay >= startHour && hourOfDay < endHour;
  }
  
  public int getRate() {
    return rate;
  }
}
