package com.profitera.services.system.loan.impl;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.util.DateParser;

public class NonMonthlyBillingCycle {

  public static Date getNextDueDate(Long accountId, Date currentDate, IReadOnlyDataProvider p) throws SQLException {
    if (accountId == null) {
      throw new IllegalArgumentException("No account ID supplied for next due date query");
    }
    if (currentDate == null) {
      throw new IllegalArgumentException("No current date supplied for next due date query for account " + accountId);
    }
    Map args = new HashMap();
    args.put("ACCOUNT_ID", accountId);
    currentDate = DateParser.getStartOfDay(currentDate);
    args.put("CURRENT_DATE", currentDate);
    Date d = (Date) p.queryObject("getLoanNextNonMonthlyDueDate", args);
    if (d != null) {
      d = DateParser.getStartOfDay(d);
      if (d.before(currentDate)){
        throw new IllegalArgumentException("Next due date for " + accountId + " from date " + currentDate + " invalid: " + d);
      }
    }
    return d;
  }
  
  public static Date getPreviousDueDate(Long accountId, Date currentDate, IReadOnlyDataProvider p) throws SQLException {
    if (accountId == null) {
      throw new IllegalArgumentException("No account ID supplied for previous due date query");
    }
    if (currentDate == null) {
      throw new IllegalArgumentException("No current date supplied for previous due date query for account " + accountId);
    }
    Map args = new HashMap();
    args.put("ACCOUNT_ID", accountId);
    currentDate = DateParser.getStartOfDay(currentDate);
    args.put("CURRENT_DATE", currentDate);
    Date d = (Date) p.queryObject("getLoanPreviousNonMonthlyDueDate", args);
    if (d != null) {
      d = DateParser.getStartOfDay(d);
      if (currentDate.before(d) || currentDate.equals(d)){
        throw new IllegalArgumentException("Previous due date for " + accountId + " from date " + currentDate + " invalid: " + d);
      }
    }
    return d;
  }
}
