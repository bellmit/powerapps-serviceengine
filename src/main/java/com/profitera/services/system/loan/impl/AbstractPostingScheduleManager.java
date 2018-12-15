package com.profitera.services.system.loan.impl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.finance.PeriodicBalance;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.loan.impl.PostingSchedule.PostingType;
import com.profitera.services.system.loan.impl.PostingSchedule.Rate;
import com.profitera.util.BigDecimalUtil;
import com.profitera.util.DateParser;

public abstract class AbstractPostingScheduleManager implements IScheduleManager {
  private final ITransaction transaction;
  private final PostingType type;
  private final Long loanAccountId;
  private final Date loanStartDate;

  public AbstractPostingScheduleManager(Long loanAccountId, Date loanStartDate, PostingType pType, ITransaction trans) {
    this.loanAccountId = loanAccountId;
    this.loanStartDate = loanStartDate;
    type = pType;
    this.transaction = trans;
  }
  
  protected abstract PostingSchedule getFollowingPostingSchedule(Date coveringDate, IReadWriteDataProvider p, ITransaction t) throws SQLException;
  protected abstract PostingSchedule fetchPostingSchedule(Date coveringDate, IReadWriteDataProvider p, ITransaction t) throws SQLException;
  public abstract Long addPostingSchedule(PostingSchedule s, IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException;

  private void check(ITransaction t) {
    if (t != this.transaction) {
      throw new IllegalArgumentException("A posting schedule manager can not be used across separate transactions");
    }
  }
  
  /* (non-Javadoc)
   * @see com.profitera.services.system.loan.impl.IScheduleManager#getPreviousDueDate(java.util.Date, com.profitera.services.system.dataaccess.IReadWriteDataProvider, com.profitera.dataaccess.ITransaction)
   */
  public Date getPreviousDueDate(Date effective, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    Date d = null;
    PostingSchedule postingSchedule = getPostingSchedule(effective, p, t);
    if (postingSchedule == null) {
      postingSchedule = getLastScheduleTerminatingBefore(effective, p, t);
      if (postingSchedule != null) {
        d = postingSchedule.getPreviousDueDate(postingSchedule.getEndDate());
      }
    } else {
      d = postingSchedule.getPreviousDueDate(effective); 
    }
    if (postingSchedule == null) return null;
    if (d == null) {
      Date scheduleStart = postingSchedule.getStartDate();
      Date beforeStart = DateParser.getPreviousDay(scheduleStart);
      if (beforeStart.before(getLoanFinancialAccountsCreatedDate())) {
        return null;
      }
      postingSchedule = getPostingSchedule(beforeStart, p, t);
      if (postingSchedule == null) return null;
      // There is an edge case to handle here, if the end date of
      // the previous schedule 
      d = postingSchedule.getNextDueDate(beforeStart);
      if (d == null) {
        d = postingSchedule.getPreviousDueDate(beforeStart);
      }
    }
    return d;
  }
  
  private PostingSchedule getLastScheduleTerminatingBefore(Date effective,
      IReadWriteDataProvider p, ITransaction t) throws SQLException {
    PostingSchedule last = null;
    // We do the day before because in theory there could be a schedule starting on the created date
    PostingSchedule current = getFollowingPostingSchedule(DateParser.getPreviousDay(getLoanFinancialAccountsCreatedDate()), p, t);
    while (true) {
      if (current == null) {
        break;
      }
      if (current.getEndDate().after(effective)) {
        break;
      }
      last = current;
      current = getFollowingPostingSchedule(current.getEndDate(), p, t);
    }
    return last;
  }

  /* (non-Javadoc)
   * @see com.profitera.services.system.loan.impl.IScheduleManager#getPostingSchedule(java.util.Date, com.profitera.services.system.dataaccess.IReadWriteDataProvider, com.profitera.dataaccess.ITransaction)
   */
  public PostingSchedule getPostingSchedule(Date coveringDate, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    check(t);
    PostingSchedule ps = fetchPostingSchedule(coveringDate, p, t);
    if (ps != null) {
      return ps;
    }
    if (type != PostingType.IMMEDIATE_I && type != PostingType.PENALTY) {
      return null;
    } else {
      PostingScheduleFetcher fetcher = new PostingScheduleFetcher(getLoanAccountId());
      if (type == PostingType.IMMEDIATE_I) {
        return fetchDefaultInterestPostingSchedule(fetcher, coveringDate, p, t);
      } else {
        return fetchDefaultPenaltyPostingSchedule(fetcher, coveringDate, p, t);
      }
    }
  }
  
  protected abstract PostingSchedule fetchDefaultInterestPostingSchedule(
      PostingScheduleFetcher fetcher, Date coveringDate,
      IReadWriteDataProvider p, ITransaction t) throws SQLException;

  /* (non-Javadoc)
   * @see com.profitera.services.system.loan.impl.IScheduleManager#getBillingPeriod(java.util.Date, com.profitera.services.system.dataaccess.IReadWriteDataProvider, com.profitera.dataaccess.ITransaction)
   */
  public Date[] getBillingPeriod(Date currentDate, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    Date next = getNextDueDate(currentDate, p, t);
    Date prev = getPreviousDueDate(currentDate, p, t);
    // If next is null then there really is no schedule, return null
    if (next == null) return null;
    // If next is not null but previous is then we are at the first due date
    // for this loan on this schedule type
    if (prev == null) {
      Date createdDate = getLoanFinancialAccountsCreatedDate();
      PostingSchedule postingSchedule = getPostingSchedule(createdDate, p, t);
      // It has an initial schedule, use that start as the period start
      if (postingSchedule != null) {
        prev = postingSchedule.getStartDate();
      } else {
        // I know this will exist, because we have a valid next due date
        PostingSchedule followingSchedule = new PostingScheduleFetcher(getLoanAccountId()).fetchFollowingSchedule(createdDate, PostingSchedule.getPostingTypeAccountCode(type), p, t);
        // This looks right, but the actual period will start the day after this...
        // and that seems wrong
        prev = DateParser.getPreviousDay(followingSchedule.getStartDate());
      }
    }
    // I need to do this so we can have 1-day periods where the first day of a schedule is
    // a due date and there is no preceding schedule
    if (prev.equals(next)) {
      prev = DateParser.getPreviousDay(prev);
    }
    return MonthlyBillingCycle.getBillingPeriod(prev, next);
  }
  
  /* (non-Javadoc)
   * @see com.profitera.services.system.loan.impl.IScheduleManager#getPostingDueRateOnDate(java.util.Date, com.profitera.services.system.dataaccess.IReadWriteDataProvider, com.profitera.dataaccess.ITransaction)
   */
  public BigDecimal getPostingDueRateOnDate(Date dueDate, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    PostingSchedule postingSchedule = getPostingSchedule(dueDate, p, t);
    if (postingSchedule == null) return BigDecimal.ZERO;
    BigDecimal amount = postingSchedule.getDueDateRate(dueDate);
    return amount != null ? amount : BigDecimalUtil.ZERO;
  }

  
  protected Date getLoanFinancialAccountsCreatedDate() {
    return loanStartDate;
  }

  protected Long getLoanAccountId() {
    return loanAccountId;
  }

  /* (non-Javadoc)
   * @see com.profitera.services.system.loan.impl.IScheduleManager#getNextDueDate(java.util.Date, com.profitera.services.system.dataaccess.IReadWriteDataProvider, com.profitera.dataaccess.ITransaction)
   */
  public Date getNextDueDate(Date effective, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    PostingSchedule postingSchedule = getPostingSchedule(effective, p, t);
    if (postingSchedule == null) {
      postingSchedule = getFollowingPostingSchedule(effective, p, t);
      if (postingSchedule != null) {
        effective = postingSchedule.getStartDate();
        Date prev = postingSchedule.getPreviousDueDate(effective);
        if (prev != null) {
          return prev;
        }
      }
    }
    if (postingSchedule == null) return null;
    Date next = postingSchedule.getNextDueDate(effective);
    if (next == null && !postingSchedule.isNoEndDate()) {
      Date dayAfterEnd = DateParser.getNextDay(postingSchedule.getEndDate());
      return getNextDueDate(dayAfterEnd, p, t);
    } else {
      return next;
    }
  }
  
  /* (non-Javadoc)
   * @see com.profitera.services.system.loan.impl.IScheduleManager#getFirstScheduleStartDate(com.profitera.services.system.dataaccess.IReadWriteDataProvider, com.profitera.dataaccess.ITransaction)
   */
  public Date getFirstScheduleStartDate(IReadWriteDataProvider p, ITransaction t)  throws SQLException {
    PostingSchedule postingSchedule = getPostingSchedule(getLoanFinancialAccountsCreatedDate(), p, t);
    if (postingSchedule == null) {
      postingSchedule = getFollowingPostingSchedule(getLoanFinancialAccountsCreatedDate(), p, t);
    }
    if (postingSchedule == null) {
      return null;
    } else {
      return postingSchedule.getStartDate();
    }
  }

  /* (non-Javadoc)
   * @see com.profitera.services.system.loan.impl.IScheduleManager#getCode()
   */
  public String getCode() {
    return PostingSchedule.getPostingTypeAccountCode(type);
  }
  
  public PeriodicBalance getRates(Date startDate, Date endDate, IReadWriteDataProvider p, ITransaction t) throws SQLException{
    List<PostingSchedule> schedules = new ArrayList<>();
    // As an optimization we first ask for the last one and if its
    // start date is on or before our start date then that is the only
    // schedule we need, so we got it done in one query
    // The optimal behaviour might be a binary search after this "last test".
    PostingSchedule last = getPostingSchedule(DateParser.getNextDay(endDate), p, t);
    Date firstActualRate = null;
    if (last != null && !last.getStartDate().after(startDate)) {
      schedules.add(last);
      firstActualRate = last.getFirstScheduleDate();
    } else {
      PostingSchedule s = getPostingSchedule(startDate, p, t);
      if (s == null) {
        throw new IllegalArgumentException("Start date for rate period of " + startDate + " is not valid for schedule " + getCode() + " for " + getLoanAccountId());
      }
      schedules.add(s);
      firstActualRate = s.getFirstScheduleDate();
      while (endDate.after(s.getEndDate())){
        s = getPostingSchedule(DateParser.getNextDay(s.getEndDate()), p, t);
        if (s == null) {
          throw new IllegalArgumentException("End date for rate period of " + endDate + " is not valid for schedule " + getCode() + " for " + getLoanAccountId());
        }
        schedules.add(s);
      }
    }
    // If the first actual rate date is after the start of the rate period we need to ask for
    // the rate before that and dump those until we get there or run out of schedules and give up
    for (Date counterDate = DateParser.getPreviousDay(startDate);
      firstActualRate == null || firstActualRate.after(startDate);
      counterDate = DateParser.getPreviousDay(counterDate)) {
      PostingSchedule s = getPostingSchedule(counterDate, p, t);
      if (s == null) {
        break;
      } else {
        schedules.add(s);
        firstActualRate = s.getFirstScheduleDate();
        counterDate = s.getStartDate();
      }
    }
    // Chronological ordering gives us proper schedule progression 
    Collections.sort(schedules);
    Date firstRateDate = null;
    List<Rate> rates = new ArrayList<>();
    for (PostingSchedule postingSchedule : schedules) {
      if (firstRateDate == null && postingSchedule.getFirstScheduleDate() != null) {
        firstRateDate = postingSchedule.getFirstScheduleDate();
      }
      rates = dumpRates(postingSchedule, rates);
    }
    // No rate at all for start date, default to zero.
    if (firstRateDate == null || firstRateDate.after(startDate)) {
      rates.add(new Rate(startDate, BigDecimal.ZERO));
    }
    return PostingSchedule.getPeriodicBalanceForRates(rates);
  }

  private List<Rate> dumpRates(PostingSchedule s, List<Rate> l) {
    List<Rate> result = new ArrayList<>();
    result.addAll(l);
    result.addAll(Arrays.asList(s.getRates()));
    return result;
  }

  protected abstract PostingSchedule fetchDefaultPenaltyPostingSchedule(
      PostingScheduleFetcher fetcher, Date coveringDate,
      IReadWriteDataProvider p, ITransaction t) throws SQLException;
}
