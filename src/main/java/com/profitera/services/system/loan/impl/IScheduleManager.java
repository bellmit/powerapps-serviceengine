package com.profitera.services.system.loan.impl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.finance.PeriodicBalance;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public interface IScheduleManager {

  public abstract Date getPreviousDueDate(Date effective,
      IReadWriteDataProvider p, ITransaction t) throws SQLException;

  public abstract PostingSchedule getPostingSchedule(Date coveringDate,
      IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException;

  public abstract Date[] getBillingPeriod(Date currentDate,
      IReadWriteDataProvider p, ITransaction t) throws SQLException;

  public abstract BigDecimal getPostingDueRateOnDate(Date dueDate,
      IReadWriteDataProvider p, ITransaction t) throws SQLException;

  public abstract Date getNextDueDate(Date effective, IReadWriteDataProvider p,
      ITransaction t) throws SQLException;

  public abstract Date getFirstScheduleStartDate(IReadWriteDataProvider p,
      ITransaction t) throws SQLException;

  public abstract Long addPostingSchedule(PostingSchedule s,
      IReadWriteDataProvider p, ITransaction t) throws SQLException,
      AbortTransactionException;

  public abstract String getCode();
  
  public PeriodicBalance getRates(Date startDate, Date endDate, IReadWriteDataProvider p, 
      ITransaction t) throws SQLException;

}