package com.profitera.services.system.loan.impl;

import java.sql.SQLException;
import java.util.Date;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.loan.impl.PostingSchedule.PostingType;

public class PostingScheduleManager extends AbstractPostingScheduleManager {
  private IPostingScheduleCache cache;

  public PostingScheduleManager(Long loanAccountId, Date loanStartDate, PostingType pType, ITransaction trans) {
    super(loanAccountId, loanStartDate, pType, trans);
  }
  
  public void setCache(IPostingScheduleCache c) {
    this.cache = c;
  }
  
  private IPostingScheduleCache getCache() {
    if (cache == null) {
      cache = new PostingScheduleCache();
    }
    return cache;
  }
  
  @Override
  protected PostingSchedule getFollowingPostingSchedule(Date coveringDate, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    PostingScheduleFetcher fetcher = new PostingScheduleFetcher(getLoanAccountId());
    Date d = fetcher.fetchFollowingScheduleStartDate(coveringDate, getCode(), p, t);
    if (d == null) {
      return null;
    } else {
      return fetchPostingSchedule(d, p, t);
    }
  }
  
  @Override
  protected PostingSchedule fetchPostingSchedule(Date coveringDate, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    PostingSchedule ps = getCache().get(coveringDate);
    if (ps == null) {
      PostingScheduleFetcher fetcher = new PostingScheduleFetcher(getLoanAccountId());
      ps = fetcher.fetchSchedule(coveringDate, getCode(), p, t);
      getCache().put(ps);
    }
    return ps;
  }
  
  @Override
  protected PostingSchedule fetchDefaultInterestPostingSchedule(
      PostingScheduleFetcher fetcher, Date coveringDate,
      IReadWriteDataProvider p, ITransaction t) throws SQLException {
    PostingSchedule ps = fetcher.fetchDefaultInterestPostingSchedule(getLoanFinancialAccountsCreatedDate(), coveringDate, p, t);
    if (ps != null) {
      getCache().put(ps);
    }
    return ps;
  }

  @Override
  protected PostingSchedule fetchDefaultPenaltyPostingSchedule(
      PostingScheduleFetcher fetcher, Date coveringDate,
      IReadWriteDataProvider p, ITransaction t) throws SQLException {
    PostingSchedule ps = fetcher.fetchDefaultPenaltyPostingSchedule(getLoanFinancialAccountsCreatedDate(), coveringDate, p, t);
    if (ps != null) {
      getCache().put(ps);
    }
    return ps;
  }

  
  @Override
  public Long addPostingSchedule(PostingSchedule s,
      IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException {
    getCache().clear();
    return new PostingScheduleSaver().savePostingSchedule(getLoanAccountId(), getCode(), s, p, t);
  }
}
