package com.profitera.services.system.loan.impl;

import java.math.BigDecimal;
import java.util.Date;

import com.profitera.util.DateParser;

public class ProjectedInstallment {
  private final BigDecimal p;
  private final BigDecimal i;
  private final Date d;
  private final boolean isPaid;
  private final BigDecimal remainingP;
  private final Date lastPeriodEnd;

  public ProjectedInstallment(Date d, BigDecimal p, BigDecimal i, BigDecimal remainingP, Date lastPeriodEnd) {
    this(d, p, i, false, remainingP, lastPeriodEnd);
  }
  
  public ProjectedInstallment(Date d, BigDecimal p, BigDecimal i, boolean isPaid, BigDecimal remainingP, Date lastPeriodEnd) {
    this.p = p;
    this.i = i;
    this.d = d;
    this.isPaid = isPaid;
    this.remainingP = remainingP;
    if (lastPeriodEnd == null) {
      throw new NullPointerException("Previous period end date is required for projected installment");
    }
    this.lastPeriodEnd = lastPeriodEnd;
  }
  
  public Date getDate() {
    return d;
  }
  
  public BigDecimal getPrincipal(){
    return p;
  }
  public BigDecimal getInterest(){
    return i;
  }
  public BigDecimal getInstallmentAmount(){
    return p.add(i);
  }
  
  public boolean isPaid() {
    return isPaid;
  }
  
  public String toString() {
    String paidSuffix = isPaid() ? "(paid)" : ""; 
    return getDate() + " " + getPrincipal() + "/" + getInterest() + "|" + getInstallmentAmount() + paidSuffix + ", " + getPrincipalBalance();
  }
  
  public BigDecimal getPrincipalBalance() {
    return remainingP;
  }
  
  public Date getPreviousPeriodEnd() {
    return lastPeriodEnd;
  }

  public Date getPeriodStartDate() {
    return DateParser.getNextDay(getPreviousPeriodEnd());
  }
}
