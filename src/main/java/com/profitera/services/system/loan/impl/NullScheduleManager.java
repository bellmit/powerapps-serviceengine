package com.profitera.services.system.loan.impl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.finance.PeriodicBalance;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public final class NullScheduleManager implements IScheduleManager {
  public Long addPostingSchedule(PostingSchedule s,
      IReadWriteDataProvider p, ITransaction t) throws SQLException,
      AbortTransactionException {
    return null;
  }

  public Date[] getBillingPeriod(Date currentDate,
      IReadWriteDataProvider p, ITransaction t) throws SQLException {
    return null;
  }

  public String getCode() {
    return null;
  }

  public Date getFirstScheduleStartDate(IReadWriteDataProvider p,
      ITransaction t) throws SQLException {
    return null;
  }

  public Date getNextDueDate(Date effective, IReadWriteDataProvider p,
      ITransaction t) throws SQLException {
    return null;
  }

  public BigDecimal getPostingDueRateOnDate(Date dueDate,
      IReadWriteDataProvider p, ITransaction t) throws SQLException {
    return null;
  }

  public PostingSchedule getPostingSchedule(Date coveringDate,
      IReadWriteDataProvider p, ITransaction t) throws SQLException {
    return null;
  }

  public Date getPreviousDueDate(Date effective, IReadWriteDataProvider p,
      ITransaction t) throws SQLException {
    return null;
  }

  public PeriodicBalance getRates(Date startDate, Date endDate,
      IReadWriteDataProvider p, ITransaction t) throws SQLException {
    return null;
  }
}