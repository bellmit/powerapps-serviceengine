package com.profitera.services.business.batch.promiseevaluation.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;

public class PromiseLevelGraceCalculator implements IPromiseGraceCalculator {
  
  private final Date currentDate;
  private final int days;
  private final Log log;
  private final String graceField;
  public PromiseLevelGraceCalculator(Date currentDate, int days, String graceField, Log log) {
    this.currentDate = currentDate;
    this.days = days;
    this.graceField = graceField;
    this.log = log;
  }
  
  public int getDays() {
    return days;
  }

  public Date getGraceDate(Map promiseData) {
    Object o = promiseData.get(graceField);
    if (o != null) {
      if (o instanceof Number) {
        int days = ((Number)o).intValue();
        return getGraceDate(currentDate, days);
      } else {
        getLog().warn("Grace period field of '" + graceField + "' is not a number (" + o.getClass().getName() + ") falling back to default value " + getDays());
      }
    }
    return getGraceDate(currentDate, getDays());
  }
  
  private Date getGraceDate(Date evalDate, int gracePeriod) {
    String msg = "Promise Evaluation date will be adjusted from " + evalDate;
    Calendar c = Calendar.getInstance();
    c.setTime(evalDate);
    // Add -1*grace period to push date BACK
    c.add(Calendar.DATE, -gracePeriod);
    final Date graceDate = c.getTime();
    getLog().info(msg + " to " + graceDate);
    return graceDate;
  }

  private Log getLog() {
    return log;
  }


}
