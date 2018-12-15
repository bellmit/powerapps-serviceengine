package com.profitera.services.system.loan.impl;

import java.sql.SQLException;
import java.util.Date;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.loan.impl.PostingSchedule.PostingType;
import com.profitera.util.DateParser;

/**
 * A manager that supports only a single posting schedule and never
 * goes to the database for anything.
 */
public class LocalPostingScheduleManager extends AbstractPostingScheduleManager {
  private final PostingSchedule schedule;

  public LocalPostingScheduleManager(PostingType pType, PostingSchedule s, Date start) {
    super(null, start, pType, null);
    this.schedule = s;
  }
  @Override
  public Long addPostingSchedule(PostingSchedule s, IReadWriteDataProvider p,
      ITransaction t) throws SQLException, AbortTransactionException {
    // Ignore
    return null;
  }
  
  @Override
  public PostingSchedule getPostingSchedule(Date coveringDate, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    return fetchPostingSchedule(coveringDate, p, t);
  }

  @Override
  protected PostingSchedule fetchPostingSchedule(Date coveringDate,
      IReadWriteDataProvider p, ITransaction t) throws SQLException {
    if (coveringDate.before(schedule.getStartDate())) {
      return null;
    }
    if (coveringDate.after(schedule.getEndDate())) {
      return null;
    }
    return schedule;
  }

  @Override
  protected PostingSchedule getFollowingPostingSchedule(Date coveringDate,
      IReadWriteDataProvider p, ITransaction t) throws SQLException {
    return fetchPostingSchedule(DateParser.getNextDay(coveringDate), p, t);
  }
  @Override
  protected PostingSchedule fetchDefaultInterestPostingSchedule(
      PostingScheduleFetcher fetcher, Date coveringDate,
      IReadWriteDataProvider p, ITransaction t) throws SQLException {
    return null;
  }
  @Override
  protected PostingSchedule fetchDefaultPenaltyPostingSchedule(
      PostingScheduleFetcher fetcher, Date coveringDate,
      IReadWriteDataProvider p, ITransaction t) throws SQLException {
    return null;
  }

  
}