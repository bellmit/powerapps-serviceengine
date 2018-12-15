package com.profitera.services.system.loan.impl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.QuerySpec;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.loan.impl.PostingSchedule.PostingType;
import com.profitera.services.system.loan.impl.PostingSchedule.ExternalRate;
import com.profitera.util.DateParser;
import com.profitera.util.MapListUtil;

public class PostingScheduleFetcher {

  private static final String LEGACY_ACCOUNT_ID_PARAMETER = "ACCOUNT_ID";
  private static final String DATE = "DATE";
  private static final String RATE = "RATE";
  private static final String POST = "POST";
  private static final String BILLING_CYCLE = "BILLING_CYCLE";
  private static final String EXTERNAL_RATE = "EXTERNAL_RATE";
  private static final String END_DATE = "END_DATE";
  private static final String START_DATE = "START_DATE";
  private static final String ID = "ID";
  
  private final Long loanAccountId;
  
  public PostingScheduleFetcher(Long loanId) {
    this.loanAccountId = loanId;
  }
  
  public PostingSchedule fetchFollowingSchedule(Date coveringDate,
      String postingTypeAccountCode, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    Date d = fetchFollowingScheduleStartDate(coveringDate,
        postingTypeAccountCode, p, t);
    if (d == null) return null;
    return fetchSchedule(d, postingTypeAccountCode, p, t);
  }

  public Date fetchFollowingScheduleStartDate(Date coveringDate,
      String postingTypeAccountCode, IReadWriteDataProvider p, ITransaction t)
      throws SQLException {
    Map<String, Object> args = new HashMap<>();
    args.put(LEGACY_ACCOUNT_ID_PARAMETER, loanAccountId);
    args.put("ID", loanAccountId);
    args.put("EFFECTIVE_DATE", coveringDate);
    args.put("SCHEDULE_TYPE", postingTypeAccountCode);
    Date d = (Date) p.queryObject("getLoanPostingScheduleNextStartDate", args);
    return d;
  }


  public PostingSchedule fetchSchedule(Date coveringDate,
      String postingTypeAccountCode, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    coveringDate = DateParser.getStartOfDay(coveringDate);
    Map<String, Object> args = new HashMap<>();
    args.put("ID", loanAccountId);
    args.put(LEGACY_ACCOUNT_ID_PARAMETER, loanAccountId);
    args.put("EFFECTIVE_DATE", coveringDate);
    args.put("SCHEDULE_TYPE", postingTypeAccountCode);
    QuerySpec qs = new QuerySpec("getLoanPostingScheduleForDate", 
        new String[]{ID, START_DATE, END_DATE, DATE, RATE, POST, BILLING_CYCLE, EXTERNAL_RATE}, 
        new Class[]{Long.class, Date.class, Date.class, Date.class, BigDecimal.class, String.class, String.class, String.class});
    qs.allowNull(END_DATE);
    qs.allowNull(POST);
    qs.allowNull(BILLING_CYCLE);
    qs.allowNull(EXTERNAL_RATE);
    Iterator<Map<String, Object>> schedules = queryScheduleData(p, args, qs);
    if (!schedules.hasNext()) {
      return null;
    }
    Map<String, Object> firstRow = schedules.next();
    Long id = (Long) firstRow.get(ID);
    Date startDate = (Date) firstRow.get(START_DATE);
    Date endDate = (Date) firstRow.get(END_DATE);
    String externalRate = (String) firstRow.get(EXTERNAL_RATE);
    PostingSchedule postingSchedule = new PostingSchedule(id, startDate, endDate);
    // If there is only one row & that row does not have a date
    // we decide that we only got a header back
    if (!schedules.hasNext() && firstRow.get(DATE) == null) {
      return postingSchedule;
    }
    // Now we can verify because the first row was not simply a "header"
    try {
      addSchedule(postingSchedule, qs.verifyResultInstance(firstRow));
      while (schedules.hasNext()) {
        addSchedule(postingSchedule, qs.verifyResultInstance(schedules.next()));
      }
    } catch (AbortTransactionException e) {
      //TODO: Not the best way to handle this, but an OK option for now
      throw new IllegalArgumentException(e);
    }
    if (externalRate != null) {
      return mergeExternalRateForPeriod(externalRate, postingSchedule, p, t);
    }
    return postingSchedule;
  }

  @SuppressWarnings("unchecked")
  public Iterator<Map<String, Object>> queryScheduleData(IReadWriteDataProvider p, Map<String, Object> args,
      QuerySpec qs) throws SQLException {
    return p.query(IReadOnlyDataProvider.LIST_RESULTS, qs.getName(), args);
  }
  
  private PostingSchedule mergeExternalRateForPeriod(String externalRate, PostingSchedule postingSchedule, IReadWriteDataProvider p,
      ITransaction t) throws SQLException {
    Map<String, Object> args = new HashMap<>();
    args.put("POSTING_SCHEDULE_ID", postingSchedule.getId());
    args.put(EXTERNAL_RATE, externalRate);
    args.put(START_DATE, postingSchedule.getStartDate());
    args.put(END_DATE, postingSchedule.getEndDate());
    QuerySpec qs = new QuerySpec("getPostingScheduleExternalRatesForPeriod", 
        new String[]{DATE, RATE},
        new Class[]{Date.class, BigDecimal.class});
    Iterator<Map<String, Object>> entries = queryScheduleData(p, args, qs);
    List<Map<String, Object>> entryList = MapListUtil.sort(new String[]{DATE}, true, entries);
    if (entryList.isEmpty()) {
      return postingSchedule;
    }
    List<ExternalRate> externalRates = new ArrayList<>();
    for (Map<String, Object> map : entryList) {
      externalRates.add(new ExternalRate((Date)map.get(DATE), (BigDecimal)map.get("RATE")));
    }
    return postingSchedule.mergeExternalRates(externalRates);
  }

  private void addSchedule(PostingSchedule postingSchedule, Map<String, Object> s) {
    Long id = (Long) s.get(ID);
    if (!id.equals(postingSchedule.getId())) {
      throw new IllegalArgumentException("Posting schedule query for " + postingSchedule.getId() + " returned record from posting schedule " + id);
    }
    Date d = (Date) s.get(DATE);
    BigDecimal r = (BigDecimal) s.get(RATE);
    String post = (String) s.get(POST);
    String billingCycle = (String) s.get(BILLING_CYCLE);
    postingSchedule.addSchedule(d, r, billingCycle, post);
  }

  private PostingSchedule fetchDefaultPostingSchedule(Date startDate, 
      Date coveringDate, BigDecimal rate, String postingType, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    coveringDate = DateParser.getStartOfDay(coveringDate);
    if (startDate.after(coveringDate)){
      return null;
    }
    // If there is no schedule after the default lasts forever.
    Date endDate = null;
    // But if we find a following valid schedule then we can proceed to set the end date 
    // as the day before that schedule's start date
    PostingSchedule fetchFollowingSchedule = fetchFollowingSchedule(coveringDate, postingType, p, t);
    if (fetchFollowingSchedule != null) {
      endDate = DateParser.getPreviousDay(fetchFollowingSchedule.getStartDate());
    }
    PostingSchedule postingSchedule = new PostingSchedule(null, startDate, endDate);
    postingSchedule.addSchedule(startDate, rate, getDefaultBillingCycle(loanAccountId, p, t) + "", "P");
    return postingSchedule;
  }
  
  private int getDefaultBillingCycle(Long id, IReadOnlyDataProvider p, ITransaction t) throws SQLException {
    String code = (String) p.queryObject("getLoanDefaultBillingCycle", id);
    int day = Integer.parseInt(code);
    return day;
  }

  public PostingSchedule fetchDefaultPenaltyPostingSchedule(
      Date startDate, Date coveringDate, IReadWriteDataProvider p,
      ITransaction t) throws SQLException {
    BigDecimal rate = getDefaultPenaltyRate(p, t);
    return fetchDefaultPostingSchedule(startDate, coveringDate, rate, PostingSchedule.getPostingTypeAccountCode(PostingType.PENALTY), p, t);
  }

  private BigDecimal getDefaultPenaltyRate(IReadOnlyDataProvider p, ITransaction t) throws SQLException {
    return (BigDecimal) p.queryObject("getLoanDefaultPenaltyRate", loanAccountId);
  }

  public PostingSchedule fetchDefaultInterestPostingSchedule(Date startDate,
      Date coveringDate, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    BigDecimal rate = getDefaultInterestRate(p, t);
    return fetchDefaultPostingSchedule(startDate, coveringDate, rate, PostingSchedule.getPostingTypeAccountCode(PostingType.IMMEDIATE_I), p, t);
  }
  
  private BigDecimal getDefaultInterestRate(IReadOnlyDataProvider p, ITransaction t) throws SQLException {
    return (BigDecimal) p.queryObject("getLoanDefaultInterestRate", loanAccountId);
  }
}
