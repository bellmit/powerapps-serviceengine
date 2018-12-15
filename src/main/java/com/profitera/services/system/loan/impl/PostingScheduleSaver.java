package com.profitera.services.system.loan.impl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.loan.IAccountTypes;
import com.profitera.services.system.loan.impl.PostingSchedule.PostingType;
import com.profitera.util.DateParser;

public class PostingScheduleSaver {
  
  private static final String LEGACY_ACCOUNT_ID = "ACCOUNT_ID";
  private static Log LOG;

  public Long savePostingSchedule(Long loanId, String code, PostingSchedule s, IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException {
    //1. Verify the code as valid
    PostingType postingType = getAccountCodePostingType(code, loanId);
    // 2. Principal entries must all be posting
    // 3. Deferred transfer must all be posting
    if (postingType == PostingType.PRINCIPAL_INSTALLMENT 
        || postingType == PostingType.PRINCIPAL_LUMP_SUM
        || postingType == PostingType.DEFERRED_T){
      if (!s.isAllPostingRates()){
        throw new AbortTransactionException("Posting schedule for " + code + " must be made up of only posting entries");
      }
    }
    // 4. Deferred transfer must all be <= 1
    if (postingType == PostingType.DEFERRED_T) {
      BigDecimal r = s.getMaximumRate();
      if (r != null && r.compareTo(BigDecimal.ONE) > 0) {
        throw new AbortTransactionException("Posting schedule for " + code + " must not contain rates over 1, found " + r);
      }
    }
    // 5. None of the entries can be before the start date
    Date startDate = s.getStartDate();
    Date firstScheduleDate = s.getFirstScheduleDate();
    if (firstScheduleDate != null && firstScheduleDate.before(startDate)) {
      throw new AbortTransactionException("Posting schedule for " + code + " contains rate dated " + firstScheduleDate + ", which is before start date of " + startDate);
    }
    // 6. Check for two rate entries on the same day
    List<ScheduleEntry> scheduleEntries = s.getScheduleEntries();
    if (scheduleEntries.size() > 1) {
      Date last = scheduleEntries.get(0).getDate();
      for (int i = 1; i < scheduleEntries.size(); i++) {
        Date date = scheduleEntries.get(i).getDate();
        if (date.equals(last)) {
          throw new AbortTransactionException("Posting schedule for " + code + " contains two rate entries dated " + last + ", only one rate entry per day is permitted");
        } else {
          last = date;
        }
      }
    }
    // 7. Check for rates after end date
    if (s.hasScheduleDateAfterEnd()) {
      throw new AbortTransactionException("Posting schedule for " + code + " contains rate dated after end date of " + s.getEndDate());
    }
    // Disable any existing schedule that starts after the start date
    // of this new schedule, 
    {
      Map<String, Object> args = new HashMap<>();
      args.put(LEGACY_ACCOUNT_ID, loanId);
      args.put("LEDGER_ID", loanId);
      args.put("SCHEDULE_TYPE", code);
      args.put("START_DATE", s.getStartDate());
      int update = p.update("disableLoanPostingScheduleStartingAfter", args, t);
      if (update > 0) {
        getLog().info("Disabled " + update + " " + code + " schedules for loan " + loanId);
      }
    }
    // assign an end date of start - 1 to any schedule that overlaps
    {
      Map<String, Object> args = new HashMap<>();
      args.put(LEGACY_ACCOUNT_ID, loanId);
      args.put("LEDGER_ID", loanId);
      args.put("SCHEDULE_TYPE", code);
      args.put("START_DATE", s.getStartDate());
      Date newEndDate = DateParser.getPreviousDay(s.getStartDate());
      args.put("NEW_END_DATE", newEndDate);
      int update = p.update("updateLoanPostingScheduleEndDate", args, t);
      if (update > 0) {
        getLog().info("Adjusted end date for " + code + " to " + newEndDate + " schedule for loan " + loanId);
      }
    }
    Map<String, Object> schedule = new HashMap<>();
    schedule.put(LEGACY_ACCOUNT_ID, loanId);
    schedule.put("LEDGER_ID", loanId);
    schedule.put("SCHEDULE_TYPE", code);
    schedule.put("START_DATE", s.getStartDate());
    schedule.put("END_DATE", s.isNoEndDate() ? null : s.getEndDate());
    Long id = (Long) p.insert("insertLoanPostingSchedule", schedule, t);
    List<ScheduleEntry> schedules = s.getScheduleEntries();
    for (ScheduleEntry se : schedules) {
      Map<String, Object> entry = new HashMap<>();
      entry.put("POSTING_SCHEDULE_ID", id);
      entry.put("RATE_DATE", se.getDate());
      entry.put("RATE_AMOUNT", se.getRate());
      entry.put("BILLING_CYCLE", se.getBillingCycleCode());
      entry.put("POST", se.isPosting() ? "P" : "N");
      p.insert("insertLoanPostingScheduleRate", entry, t);
    }
    return id;
  }
  
  public static PostingType getAccountCodePostingType(String t, Long id){
    if (t.equals(IAccountTypes.PINST)) {
      return PostingType.PRINCIPAL_INSTALLMENT;
    }
    if (t.equals(IAccountTypes.PRINCIPAL)) {
      return PostingType.PRINCIPAL_LUMP_SUM;
    }
    if (t.equals(IAccountTypes.INTEREST)) {
      return PostingType.IMMEDIATE_I;
    }
    if (t.equals(IAccountTypes.CUMULATIVE_INT)) {
      return PostingType.DEFERRED_I;
    }
    if (t.equals("211")) {
      return PostingType.DEFERRED_T;
    }
    if (t.equals(IAccountTypes.PENALTY)) {
      return PostingType.PENALTY;
    }
    throw new IllegalArgumentException("Invalid posting schedule specified: " + t);
  }
  
  private Log getLog() {
    if (LOG == null) {
      LOG = LogFactory.getLog(getClass());
    }
    return LOG;
  }


}
