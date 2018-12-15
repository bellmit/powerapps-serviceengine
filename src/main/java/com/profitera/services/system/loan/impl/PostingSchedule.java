package com.profitera.services.system.loan.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.finance.PeriodicBalance;
import com.profitera.services.system.loan.IAccountTypes;
import com.profitera.util.DateParser;

public class PostingSchedule implements Comparable<PostingSchedule> {
  static final Date NO_END_DATE = DateParser.getStartOfDay(new Date(Long.MAX_VALUE));

  public enum PostingType {
    PRINCIPAL_INSTALLMENT, PRINCIPAL_LUMP_SUM, IMMEDIATE_I, DEFERRED_I, DEFERRED_T, PENALTY
  };
  
  private interface IQualifier {
    public boolean isQualified(ScheduleEntry e);
  }
  
  private static final IQualifier POSTING = new IQualifier(){
    public boolean isQualified(ScheduleEntry e) {
      return e.isPosting();
    }
  };

  public static class Rate {
    private final Date date;
    private final BigDecimal rate;

    Rate(Date d, BigDecimal r){
      this.date = d;
      this.rate = r;
    }
    public Date getDate(){
      return date;
    }
    public BigDecimal getRate() {
      return rate;
    }
  }
  private Long id;
  private final Date endDate;
  private final Date startDate;
  private final boolean isNoEndDate;
  private final List<ScheduleEntry> schedules = new ArrayList<ScheduleEntry>();
  public PostingSchedule(Long id, Date startDate, Date endDate) {
    this.id = id;
    if (startDate == null) {
      throw new IllegalArgumentException("Posting schedule must have a start date");
    }
    this.startDate = DateParser.getStartOfDay(startDate);
    isNoEndDate = endDate == null;
    if (isNoEndDate) {
      endDate = NO_END_DATE;
    }
    this.endDate = DateParser.getStartOfDay(endDate);
  }

  public void addSchedule(Date d, BigDecimal r, String billingCycle, String postingType) {
    addSchedule(new ScheduleEntry(d, r, billingCycle, postingType));
  }

  private void addSchedule(ScheduleEntry schedule) {
    schedules.add(schedule);
    Collections.sort(schedules);
  }
  private boolean hasScheduleEntryOnDate(Date date) {
    for (ScheduleEntry e : schedules) {
      if (e.getDate().equals(date)) {
        return true;
      }
    }
    return false;
  }

  
  public Date getPreviousDueDate(Date currentDate) {
    currentDate = DateParser.getStartOfDay(currentDate);
    checkDateRange(currentDate);
    Date next = getNextDueDate(currentDate);
    Date d = DateParser.getPreviousDay(currentDate);
    // I've checked the range so if d is before start date then
    // the currentDate must be equal to the startDate
    if (d.before(getStartDate()) && getScheduleEntries().size() > 0) {
      ScheduleEntry scheduleEntry = getScheduleEntries().get(0);
      boolean onStartDate = scheduleEntry.getDate().equals(getStartDate());
      boolean isPosting = POSTING.isQualified(scheduleEntry);
      boolean isDueOnDate = false;
      if (scheduleEntry.isBillingCycle() && !scheduleEntry.isTerminator()) {
        isDueOnDate = scheduleEntry.getNextCycleDueDate(getStartDate()).equals(getStartDate());
      } else {
        isDueOnDate = scheduleEntry.getDate().equals(getStartDate());
      }
      if (onStartDate && isPosting && isDueOnDate) {
        return scheduleEntry.getDate();
      }
    }
    // d can't be before the start date
    while (!d.before(getStartDate())) {
      Date prev = getNextDueDate(d);
      if (prev != null && next != null && !prev.equals(next)){
        return prev;
      } else if (next == null && prev != null) {
        return prev;
      }
      d = DateParser.getPreviousDay(d);
    }
    return null;
  }


  public Date getNextDueDate(Date currentDate) {
    ScheduleEntry s = getNextPostingSchedule(currentDate);
    if (s == null) return null;
    if (s.isBillingCycle() && !s.isTerminator()) {
      Date d = s.getNextCycleDueDate(currentDate);
      if (d.after(getEndDate())) {
        return null;
      } else {
        return d;
      }
    } else {
      return s.getDate();
    }
  }
  private ScheduleEntry getNextPostingSchedule(Date currentDate) {
    return getNextSchedule(currentDate, POSTING);
  }
  private ScheduleEntry getNextSchedule(Date currentDate, IQualifier q) {
    currentDate = DateParser.getStartOfDay(currentDate);
    checkDateRange(currentDate);
    ScheduleEntry applicableSingle = null;
    for (Iterator<ScheduleEntry> i = schedules.iterator(); i.hasNext();) {
      ScheduleEntry s = i.next();
      if (!q.isQualified(s)){
        continue;
      }
      // If is sorted by date, so the first single after is the one
      if (!currentDate.after(s.getDate()) && (!s.isBillingCycle() || s.isTerminator())){
        applicableSingle = s;
        break;
      }
    }
    ScheduleEntry applicableCycle = null;
    for (Iterator<ScheduleEntry> i = schedules.iterator(); i.hasNext();) {
      ScheduleEntry s = i.next();
      if (!s.isPosting()){
        continue;
      }
      if (s.isBillingCycle()){
        // If there is no cycle OR the cycle entry is a terminator
        if (s.isTerminator() && applicableCycle == null) {
          continue;
        } else if (applicableCycle == null){
          applicableCycle = s;
        } else {
          Date next = applicableCycle.getNextCycleDueDate(currentDate);
          // If you are a terminator & before the next due date
          // generated by the candidate schedule entry
          if (s.isTerminator() && s.getDate().before(next)){
            applicableCycle = null;
          } else if (s.isTerminator()) {
            break;
          } else {
            // if the next for the current candidate is before this
            // new candidate entry date then it is not under consideration
            if (next.before(s.getDate())) {
              continue;
            }
            if (s.getDate().after(applicableCycle.getDate())){
              applicableCycle = s;
            }
          }
        }
      }
    }
    if (applicableCycle == null) {
      return applicableSingle;
    }
    Date nextByCycle = applicableCycle.getNextCycleDueDate(currentDate);
    if (applicableSingle != null) {
      if (applicableSingle.getDate().before(nextByCycle)){
        return applicableSingle;
      }
    }
    return applicableCycle;
  }

  private void checkDateRange(Date currentDate) {
    if (currentDate.before(getStartDate())) {
      throw new IllegalArgumentException("Requested rate date for schedule " + id + " of " + currentDate + " is before start date " + getStartDate());
    }
    if (endDate != null && currentDate.after(endDate)) {
      throw new IllegalArgumentException("Requested rate date for schedule " + id + " of " + currentDate + " is after end date " + endDate);
    }
  }

  public Date getStartDate() {
    return startDate;
  }

  
  public Rate[] getRates() {
    List<Rate> l = new ArrayList<Rate>();
    for (ScheduleEntry s : schedules) {
      Rate r = new Rate(s.getDate(), s.getRate());
      // Once we hit a schedule entry after the 
      // end date we are done
      if (getEndDate().before(r.getDate())) {
        break;
      }
      l.add(r);
    }
    return l.toArray(new Rate[l.size()]);
  }

  public Date getEndDate() {
    return endDate;
  }
  
  private ScheduleEntry getDueDateEntry(Date dueDate) {
    if (schedules.size() == 0) {
      return null;
    }
    // This method works on a simple premise, today is
    // the "next" after yesterday, using yesterday
    // fails the date range check so we have a special case
    // for the start date itself
    Date checkDate = dueDate;
    if (dueDate.equals(getStartDate())){
      ScheduleEntry se = schedules.get(0);
      if (se.isPosting() && se.getDate().equals(dueDate)) {
        if (!se.isBillingCycle() || se.isTerminator()) {
          return se;
        } else {
          Date current = se.getNextCycleDueDate(dueDate);
          if (current.equals(dueDate)) {
            return se;
          }
        }
      }
    } else {
      checkDate = DateParser.getPreviousDay(dueDate);
    }
    ScheduleEntry se = getNextPostingSchedule(checkDate);
    if (se != null) {
      if (se.isBillingCycle() && !se.isTerminator()) {
        Date d = se.getNextCycleDueDate(dueDate);
        if (!d.equals(dueDate)) {
          se = null;
        }
      } else if (!se.getDate().equals(dueDate)){
        se = null;
      }
    }
    return se;
  }

  public BigDecimal getDueDateRate(Date dueDate) {
    dueDate = DateParser.getStartOfDay(dueDate);
    ScheduleEntry s = getDueDateEntry(dueDate);
    if (s == null) return null;
    return s.getRate();
  }

  public Long getId() {
    return id;
  }

  public boolean isAllPostingRates() {
    for (Iterator<ScheduleEntry> i = schedules.iterator(); i.hasNext();) {
      ScheduleEntry s = i.next();
      if (!s.isPosting()) {
        return false;
      }
    }
    return true;
  }

  public BigDecimal getMaximumRate() {
    Rate[] rates = getRates();
    BigDecimal max = null;
    for (int i = 0; i < rates.length; i++) {
      Rate r = rates[i];
      if (max == null) {
        max = r.getRate();
      } else if (r.getRate().compareTo(max) > 0) {
        max = r.getRate();
      }
    }
    return max;
  }

  public Date getFirstScheduleDate() {
    if (schedules.size() == 0){
      return null;
    } else {
      Date date = schedules.get(0).getDate();
      return date.after(getEndDate()) ? null : date;
    }
  }
  
  public Date getLastScheduleDate() {
    if (schedules.size() == 0){
      return null;
    } else {
      for (int i = 0; i < schedules.size(); i++) {
        Date date = schedules.get(schedules.size() - 1 - i).getDate();
        if (!date.after(getEndDate())) {
          return date;  
        }
      }
      return null;
    }
  }


  List<ScheduleEntry> getScheduleEntries() {
    List<ScheduleEntry> l = new ArrayList<ScheduleEntry>(schedules);
    return l;
  }
  public boolean equals(Object obj) {
    PostingSchedule o = (PostingSchedule) obj;
    if (o == this) return true;
    if (!o.getStartDate().equals(this.getStartDate())){
      return false;
    }
    if (!o.getEndDate().equals(this.getEndDate())){
      return false;
    }
    if (o.schedules.size() != this.schedules.size()) {
      return false;
    }
    for(int i = 0; i < this.schedules.size(); i++) {
      ScheduleEntry thisEntry = this.schedules.get(i);
      ScheduleEntry oEntry = o.schedules.get(i);
      if (!thisEntry.equals(oEntry)){
        return false;
      }
      
    }
    return true;
  }

  public static String getPostingTypeAccountCode(PostingType t){
    if (t.equals(PostingType.PRINCIPAL_INSTALLMENT)) {
      return IAccountTypes.PINST;
    }
    if (t.equals(PostingType.PRINCIPAL_LUMP_SUM)) {
      return IAccountTypes.PRINCIPAL;
    }
    if (t.equals(PostingType.IMMEDIATE_I)) {
      return IAccountTypes.INTEREST;
    }
    if (t.equals(PostingType.DEFERRED_I)) {
      return IAccountTypes.CUMULATIVE_INT;
    }
    if (t.equals(PostingType.DEFERRED_T)) {
      return "211";
    }
    if (t.equals(PostingType.PENALTY)) {
      return IAccountTypes.PENALTY;
    }
    throw new IllegalArgumentException("Invalid posting schedule specified: " + t);
  }

  public static PostingType getAccountCodePostingType(String code) {
      if (code.equals(IAccountTypes.PINST)) {
        return PostingType.PRINCIPAL_INSTALLMENT;
      }
      if (code.equals(IAccountTypes.PRINCIPAL)) {
        return PostingType.PRINCIPAL_LUMP_SUM;
      }
      if (code.equals(IAccountTypes.INTEREST)) {
        return PostingType.IMMEDIATE_I;
      }
      if (code.equals(IAccountTypes.CUMULATIVE_INT)) {
        return PostingType.DEFERRED_I;
      }
      if (code.equals("211")) {
        return PostingType.DEFERRED_T;
      }
      if (code.equals(IAccountTypes.PENALTY)) {
        return PostingType.PENALTY;
      }
      throw new IllegalArgumentException("Invalid posting schedule code specified: " + code);
    }

  public boolean isNoEndDate() {
    return isNoEndDate;
  }

  public boolean hasScheduleDateAfterEnd() {
    if (isNoEndDate()) {
      return false;
    } else if (schedules.size() > 0) {
      Date d = schedules.get(schedules.size() - 1).getDate();
      if (d.after(getEndDate())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int compareTo(PostingSchedule o) {
    if (o == null) {
      return 1;
    }
    return getStartDate().compareTo(o.getStartDate());
  }
  public static class VariableRate extends Rate implements Comparable<VariableRate> {
    public VariableRate(Date d, BigDecimal scheduled, BigDecimal external) {
      super(d, addRates(scheduled, external));
    }
    private static BigDecimal addRates(BigDecimal scheduled, BigDecimal external) {
      if (external == null && scheduled == null) {
        return BigDecimal.ZERO;
      } else if (external == null) {
        return scheduled;
      } else if (scheduled == null) {
        return external;
      } else {
        return external.add(scheduled);
      }
    }
    @Override
    public int compareTo(VariableRate o) {
      return this.getDate().compareTo(o.getDate());
    }
    
  }
  public static class ExternalRate extends Rate implements Comparable<ExternalRate> {
    public ExternalRate(Date d, BigDecimal external) {
      super(d, external);
    }
    public VariableRate merge(Rate mergeWith) {
      return new VariableRate(getDate(), getRate(), mergeWith.getRate());
    }
    @Override
    public int compareTo(ExternalRate o) {
      return this.getDate().compareTo(o.getDate());
    }
  }

  public PostingSchedule mergeExternalRates(List<ExternalRate> externalRates) {
    Collections.sort(externalRates);
    {
      Date firstExternalRateDate = externalRates.get(0).getDate();
      if (firstExternalRateDate.after(getStartDate())) {
        externalRates.add(0, new ExternalRate(getStartDate(), BigDecimal.ZERO));
      }
    }
    PeriodicBalance scheduledBalances = getPeriodicBalanceForRates(getRates());
    List<VariableRate> adjustedExternalRates = new ArrayList<>();
    for (ExternalRate externalRate : externalRates) {
      BigDecimal scheduleBalance;
      if (scheduledBalances.getOpeningDate().after(externalRate.getDate())) {
        scheduleBalance = BigDecimal.ZERO;
      } else {
        scheduleBalance = scheduledBalances.getBalance(externalRate.getDate());
      }
      adjustedExternalRates.add(new VariableRate(externalRate.getDate(), scheduleBalance, externalRate.getRate()));
    }
    PeriodicBalance externalOnlyBalances = getPeriodicBalanceForRates(externalRates);
    PostingSchedule revisedSchedule = new PostingSchedule(id, getStartDate(), getEndDate());
    for (Iterator<ScheduleEntry> i = schedules.iterator(); i.hasNext();) {
      ScheduleEntry s = i.next();
      revisedSchedule.addSchedule(s.withRevisedRate(s.getRate().add(externalOnlyBalances.getBalance(s.getDate()))));
    }
    for (VariableRate variableRate : adjustedExternalRates) {
      // External rates might progress past end date, ignore those rates
      if (revisedSchedule.getEndDate() != null && variableRate.getDate().after(revisedSchedule.getEndDate())) {
        continue;
      }
      // If an entry already exists then it was updated with the variable rate above.
      if (revisedSchedule.hasScheduleEntryOnDate(variableRate.getDate())) {
        continue;
      }
      // Create a non-posting, non-cycle entry with new rate
      revisedSchedule.addSchedule(variableRate.getDate(), variableRate.getRate(), null, "N");
    }
    return revisedSchedule;
  }

  public static PeriodicBalance getPeriodicBalanceForRates(Rate[] rates) {
    return getPeriodicBalanceForRates(Arrays.asList(rates));
  }
  public static PeriodicBalance getPeriodicBalanceForRates(List<? extends Rate> rates) {
    List<Map<String, Object>> maps = new ArrayList<>(rates.size());
    for (Iterator<? extends Rate> i = rates.iterator(); i.hasNext();) {
      Rate r = i.next();
      Map<String, Object> m = new HashMap<>();
      m.put("R", r.getRate());
      m.put("D", r.getDate());
      maps.add(m);
    }
    return new PeriodicBalance(maps, "D", "R");
  }
}


