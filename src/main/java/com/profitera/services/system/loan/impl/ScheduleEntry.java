package com.profitera.services.system.loan.impl;

import java.math.BigDecimal;
import java.util.Date;

import com.profitera.util.BigDecimalUtil;

class ScheduleEntry implements Comparable<ScheduleEntry> {
  private final Date date;
  private final BigDecimal rate;
  private final boolean isPosting;
  private final String cycle;
  ScheduleEntry(Date d, BigDecimal r, String cycle, String postingType) {
    if (d == null) {
      throw new IllegalArgumentException("Schedule entry date is required");
    }
    if (r == null) {
      throw new IllegalArgumentException("Schedule entry rate is required");
    }
    if (postingType == null) {
      throw new IllegalArgumentException("Schedule posting type is required");
    } else if (!postingType.equals("P") &&  !postingType.equals("N")){
      throw new IllegalArgumentException("Schedule posting type must be 'N' or 'P'");
    }
    this.date = d;
    this.rate = r;
    this.isPosting = postingType.equals("P");
    this.cycle = cycle;
  }
  public int compareTo(ScheduleEntry o) {
    ScheduleEntry s = (ScheduleEntry) o;
    return date.compareTo(s.date);
  }
  public Date getDate() {
    return date;
  }
  public boolean isBillingCycle() {
    return cycle != null;
  }
  private int getMonthlyBillingCycle() {
    return Integer.parseInt(cycle);
  }
  public boolean isPosting() {
    return isPosting;
  }
  public Date getNextCycleDueDate(Date currentDate) {
    if (cycle.equals("0")){
      return null;
    }
    Date d = MonthlyBillingCycle.getNextDueDate(currentDate, getMonthlyBillingCycle());
    if (d.before(getDate())) {
      d = MonthlyBillingCycle.getNextDueDate(getDate(), getMonthlyBillingCycle());
    }
    return d;
  }
  public boolean isTerminator() {
    if (!isBillingCycle()) {
      throw new IllegalArgumentException("Attempted to request terminator status from non-cycle schedule");
    }
    return cycle.equals("0");
  }
  public BigDecimal getRate() {
    return rate;
  }
  public String getBillingCycleCode() {
    return cycle;
  }

  public boolean equals(Object obj) {
    ScheduleEntry o = (ScheduleEntry) obj;
    if (!this.date.equals(o.date)){
      return false;
    }
    if (!BigDecimalUtil.isEqual(this.rate, o.rate)){
      return false;
    }
    if (this.isPosting != o.isPosting){
      return false;
    }
    if (this.cycle != null && o.cycle == null) {
      return false;
    }
    if (this.cycle == null && o.cycle != null) {
      return false;
    }
    if (this.cycle != null && o.cycle != null && !this.cycle.equals(o.cycle)) {
      return false;
    }
    return true;
  }
  public ScheduleEntry withRevisedRate(BigDecimal newRate) {
    String postingType = "N";
    if (isPosting()) {
      postingType = "P";
    }
    return new ScheduleEntry(date, newRate, cycle, postingType);
  }
  
}