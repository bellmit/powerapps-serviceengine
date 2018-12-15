package com.profitera.services.system.loan.impl;

import java.util.Calendar;
import java.util.Date;

import com.profitera.util.DateParser;

public class MonthlyBillingCycle {
  
  public static Date getNextDueDate(Date currentDate, int day) {
    currentDate = DateParser.getStartOfDay(currentDate);
    Calendar c = Calendar.getInstance();
    c.setTime(currentDate);
    adjustToDueDate(day, c);
    if (c.getTimeInMillis() < currentDate.getTime()){
      c.add(Calendar.MONTH, 1);
      adjustToDueDate(day, c);
    }
    return c.getTime();
  }
  
  public static Date getPreviousDueDate(Date currentDate, int day, Date created) {
    currentDate = DateParser.getStartOfDay(currentDate);
    Calendar c = Calendar.getInstance();
    c.setTime(currentDate);
    adjustToDueDate(day, c);
    if (c.getTimeInMillis() >= currentDate.getTime()){
      c.add(Calendar.MONTH, -1);
      adjustToDueDate(day, c);
    }
    if (created == null || c.getTime().before(created)){
      return created;
    }
    return c.getTime();
  }
  
  private static void adjustToDueDate(int day, Calendar c) {
    if (c.getActualMaximum(Calendar.DAY_OF_MONTH) < day){
      c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
    } else {
      c.set(Calendar.DAY_OF_MONTH, day);
    }
  }

  public static Date[] getBillingPeriod(Date previousDueDate, Date nextDueDate) {
    Date start = DateParser.getNextDay(previousDueDate);    
    Date end = nextDueDate;
    if (start == null || end == null || start.after(end)){
      return null;
    }
    return new Date[]{start, end};
  }
}
