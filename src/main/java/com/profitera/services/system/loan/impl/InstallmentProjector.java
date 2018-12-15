package com.profitera.services.system.loan.impl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.ITransaction;
import com.profitera.finance.InterestCalculator;
import com.profitera.finance.PeriodicBalance;
import com.profitera.map.MapUtil;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Commodity;
import com.profitera.services.system.loan.LoanInterest;
import com.profitera.util.BigDecimalUtil;
import com.profitera.util.DateParser;
import com.profitera.util.MapListUtil;
import com.profitera.util.PrimitiveValue;

public class InstallmentProjector {
  private final IScheduleManager sched110;
  private final IScheduleManager sched100;
  private final Date startDate;
  private PeriodicBalance interestRates;
  private final Commodity commodity;
  private final IScheduleManager sched200;
  private final IScheduleManager sched210;
  private final IScheduleManager sched211;

  public InstallmentProjector(Commodity c, IScheduleManager s110, IScheduleManager s100, 
      IScheduleManager s200, Date startDate) {
    this(c, s110, s100, s200, new NullScheduleManager(), new NullScheduleManager(), startDate);
  }

  public InstallmentProjector(Commodity c, IScheduleManager s110, IScheduleManager s100, 
      IScheduleManager s200, IScheduleManager s210, IScheduleManager s211, Date startDate) {
    this.commodity = c;
    this.sched110 = s110;
    this.sched100 = s100;
    this.sched200 = s200;
    this.sched210 = s210;
    this.sched211 = s211;
    this.startDate = startDate;
  }
  
  private class PeriodicBalanceBuilder {
    private BigDecimal lastBalance;
    private List<Map<String, Object>> balances = new ArrayList<Map<String, Object>>();
    public PeriodicBalanceBuilder(Date start, BigDecimal openingBalance) {
      addBalance(start, openingBalance);
    }
    private void addBalance(Date start, BigDecimal balance) {
      lastBalance = balance;
      Map<String, Object> m = new HashMap<String, Object>();
      m.put("D", start);
      m.put("B", balance);
      balances.add(m);
    }
    public BigDecimal getLastBalance() {
      return lastBalance;
    }
    public PeriodicBalance getPeriodicBalance() {
      return new PeriodicBalance(balances, "D", "B");
    }
  }

  public List<ProjectedInstallment> getFutureInstallments(PeriodicBalance principal, List<ProjectedInstallment> partialInstallments, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    return getFutureInstallments(principal, partialInstallments, -1, p, t);
  }
  
  public List<ProjectedInstallment> getFutureInstallments(BigDecimal principalAmount, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    return getFutureInstallments(principalAmount, -1, p, t);
  }
  public List<ProjectedInstallment> getFutureInstallments(BigDecimal principalAmount, int paidInstallmentsToProject, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    Date lastDue = getStartPreviousDueDate(p, t);
    if (lastDue == null) {
      return  new ArrayList<ProjectedInstallment>();
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> singleEntry = MapUtil.build("A", principalAmount, "D", lastDue);
    List<Map<String,Object>> list = MapListUtil.getSingleItemList(singleEntry);
    return getFutureInstallments(new PeriodicBalance(list, "D", "A"), new ArrayList<ProjectedInstallment>(), paidInstallmentsToProject, p, t);
  }
  
  public List<ProjectedInstallment> getFutureInstallments(PeriodicBalance b, List<ProjectedInstallment> partialInstallments, int paidInstallmentsToProject, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    int zeroInstallmentsLeft = paidInstallmentsToProject;
    Date lastDue = getStartPreviousDueDate(p, t);
    if (lastDue == null) {
      return new ArrayList<ProjectedInstallment>();
    }
    Calendar c = Calendar.getInstance();
    c.setTime(lastDue);
    c.add(Calendar.YEAR, 100);
    Date endLimit = c.getTime();
    //
    BigDecimal balance210 = BigDecimal.ZERO;
    PeriodicBalanceBuilder balance = null;
    if (lastDue.before(b.getOpeningDate())) {
      // If the period balance passed in does not go back far enough assume that
      // the opening balance provided is in fact the balance at that time for
      // projection purposes.
      balance = new PeriodicBalanceBuilder(lastDue, b.getBalance(b.getOpeningDate()));
      List<Date> balanceDates = b.getBalanceDates();
      for (Iterator<Date> i = balanceDates.iterator(); i.hasNext();) {
         Date entry = i.next();
         balance.addBalance(entry, b.getBalance(entry));          
      }
    } else {
      balance = new PeriodicBalanceBuilder(b.getOpeningDate(), b.getBalance(b.getOpeningDate()));
      List<Date> balanceDates = b.getBalanceDates();
      for (Iterator<Date> i = balanceDates.iterator(); i.hasNext();) {
         Date entry = i.next();
         balance.addBalance(entry, b.getBalance(entry));          
      }
    }
    // Now we establish our starting principal balance, this has to be based on the
    // "current" principal amount that we will be drawing down, we can not use the 
    // lastDue date there because in real life there might have been adjustments
    // to the principal balance in the interim
    BigDecimal principalAmount = b.getLastBalance();
    List<ProjectedInstallment> l = new ArrayList<ProjectedInstallment>();
    Date nextDue110 = sched110.getNextDueDate(DateParser.getNextDay(lastDue), p, t);
    Date nextDue100 = sched100.getNextDueDate(DateParser.getNextDay(lastDue), p, t);
    Date nextDue200 = sched200.getNextDueDate(DateParser.getNextDay(lastDue), p, t);
    Date nextDue211 = sched211.getNextDueDate(DateParser.getNextDay(lastDue), p, t);
    Date nextDue210 = sched210.getNextDueDate(DateParser.getNextDay(lastDue), p, t);
    int nullInstallmentCount = 0;
    while (nullInstallmentCount < 6 && (principalAmount.compareTo(BigDecimal.ZERO) > 0 || zeroInstallmentsLeft >= 0) 
        && (nextDue100 != null || nextDue110 != null)) {// NB: Still more 200's is not a reason to continue because you will 
                                                        //never pay off and loan will continue indefinitely
      if (!(principalAmount.compareTo(BigDecimal.ZERO) > 0)) {
        zeroInstallmentsLeft--;
      }
      // First check the 210 due date so we can update that balance, we might need that amount
      while (isNextDate(nextDue210, nextDue100, nextDue110, nextDue200, nextDue211)){
        balance210 = getNew210Balance(nextDue210, balance210, balance, p, t);
        nextDue210 = sched210.getNextDueDate(DateParser.getNextDay(nextDue210), p, t);
      }
      
      ProjectedInstallment i = null;
      // If the 100 and 110 schedules have the same due date and thus both 'next' we need to take care of 
      // both of them.
      if (isNextDate(nextDue100, nextDue110, nextDue200, nextDue211) && isNextDate(nextDue110, nextDue100, nextDue200, nextDue211)) {
        BigDecimal prepaidForDate = getPaidPrincipalForDate(partialInstallments, nextDue110);
        ProjectedInstallment pi110 = get110Installment(lastDue, nextDue110, balance, zeroInstallmentsLeft >= 0, prepaidForDate, p, t);
        BigDecimal updatedP = balance.getLastBalance();
        if (pi110 != null) {
          if (prepaidForDate.compareTo(pi110.getPrincipal()) > 0) {
            prepaidForDate = prepaidForDate.subtract(pi110.getPrincipal());
          } else {
            updatedP = updatedP.subtract(pi110.getPrincipal()).add(prepaidForDate);
            prepaidForDate = BigDecimal.ZERO;
          }
        }
        ProjectedInstallment pi100 = get100Installment(lastDue, nextDue110, balance, updatedP, prepaidForDate, p, t);
        if (pi110 == null) {
          i = pi100;
        } else if (pi100 == null || pi100.isPaid()) {
          // If the 100 installment comes out as paid we can safely ignore it because
          // it is really zero
          i = pi110;
        } else {
          BigDecimal newTotalP = pi100.getPrincipal().add(pi110.getPrincipal());
          i = new ProjectedInstallment(pi100.getDate(), newTotalP, 
              pi110.getInterest(), // Just the 110 interest, don't add them or you would be double-charging interest
                                   // since both installment calcs include the interest amount
              balance.getLastBalance().subtract(newTotalP),
              // The period start date is a tough one but it makes the most sense to take the later
              // date. The truth is that some information will be lost with this installment
              // consolidation and this is it. If you had 1 lump sum in the whole schedule that aligned
              // with a 110 due date you would get null or an ancient date here and that would be confusing
              PrimitiveValue.dateValue(DateParser.getLaterDate(pi110.getPreviousPeriodEnd(), pi100.getPreviousPeriodEnd()), getStartPreviousDueDate(p, t)));
        }
        if (i != null) {
          nextDue200 = sched200.getNextDueDate(DateParser.getNextDay(i.getDate()), p, t);
          nextDue110 = sched110.getNextDueDate(DateParser.getNextDay(i.getDate()), p, t);
          nextDue100 = sched100.getNextDueDate(DateParser.getNextDay(i.getDate()), p, t);
        }
      } else if (isNextDate(nextDue110, nextDue100, nextDue200, nextDue211)) {
        i = get110Installment(lastDue, nextDue110, balance, zeroInstallmentsLeft >= 0, getPaidPrincipalForDate(partialInstallments, nextDue110), p, t);
        nextDue200 = sched200.getNextDueDate(DateParser.getNextDay(nextDue110), p, t);
        nextDue110 = sched110.getNextDueDate(DateParser.getNextDay(nextDue110), p, t);
      } else if (isNextDate(nextDue100, nextDue110, nextDue200, nextDue211)) {
        i = get100Installment(lastDue, nextDue100, balance, balance.getLastBalance(), getPaidPrincipalForDate(partialInstallments, nextDue100), p, t);
        nextDue200 = sched200.getNextDueDate(DateParser.getNextDay(nextDue100), p, t);
        nextDue100 = sched100.getNextDueDate(DateParser.getNextDay(nextDue100), p, t);
      } else if (isNextDate(nextDue200, nextDue110, nextDue100, nextDue211)) {
        i = get200Installment(nextDue200, balance, balance.getLastBalance(), p, t);
        nextDue200 = sched200.getNextDueDate(DateParser.getNextDay(nextDue200), p, t);
      } else if (isNextDate(nextDue211, nextDue110, nextDue100, nextDue200)) {
        i = get211Installment(nextDue211, balance210, balance, p, t);
        balance210 = balance210.subtract(i.getInterest());
        nextDue211 = sched211.getNextDueDate(DateParser.getNextDay(nextDue211), p, t);
      }
      
      if (i == null) {
        nullInstallmentCount++;
        continue;
      } else {
        nullInstallmentCount = 0;
        if (!i.isPaid()) {
          BigDecimal principalAdjustmentAmount = i.getPrincipal().abs();
          // If this installment in present in the partial installments
          // offered to the projector we need to reduce the adjustment
          // amount by the partial amount there
          for (Iterator<ProjectedInstallment> iter = partialInstallments.iterator(); iter.hasNext();) {
            ProjectedInstallment partial = (ProjectedInstallment) iter.next();
            if (partial.getDate().equals(i.getDate())) {
              principalAdjustmentAmount = principalAdjustmentAmount.subtract(partial.getPrincipal());
              // Now I have to rebuild the projected installment with a new balance that reflects the
              // partial payment I am merging with the installment itself.
              i = new ProjectedInstallment(i.getDate(), i.getPrincipal(), i.getInterest(), i.isPaid(), 
                  i.getPrincipalBalance().add(partial.getPrincipal()), 
                  i.getPreviousPeriodEnd());
            }
          }
          principalAmount = principalAmount.subtract(principalAdjustmentAmount);
        }
        balance.addBalance(i.getDate(), principalAmount);
        lastDue = i.getDate();
        l.add(i);
        if (i.getDate().after(endLimit)) {
          break;
        }
      }
    }
    return l;
  }

  private BigDecimal getPaidPrincipalForDate(List<ProjectedInstallment> partialInstallments, Date date) {
    BigDecimal amount = BigDecimal.ZERO;
    for (Iterator<ProjectedInstallment> i = partialInstallments.iterator(); i.hasNext();) {
      ProjectedInstallment installment = (ProjectedInstallment) i.next();
      if (installment.getDate().equals(date)) {
        amount = amount.add(installment.getPrincipal());
      }
    }
    return amount;
  }

  private Date getStartPreviousDueDate(IReadWriteDataProvider p, ITransaction t) throws SQLException {
    Date lastDue = sched110.getPreviousDueDate(getStartDate(), p, t);
    Date lastDue100 = sched100.getPreviousDueDate(getStartDate(), p, t);
    if (lastDue == null || (lastDue100 != null && lastDue.after(lastDue100))) {
      lastDue = lastDue100;
    }
    // Under consideration, but I don't think it should be required
    if (lastDue == null) {
      lastDue = DateParser.getPreviousDay(getStartDate());
    }
    return lastDue;
  }
  
  private BigDecimal getNew210Balance(Date nextDue210, BigDecimal balance210,
      PeriodicBalanceBuilder principal, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    Date last = sched210.getPreviousDueDate(nextDue210, p, t);
    if (last == null) {
      last = getStartDate();
    }
    PeriodicBalance rates = get210InterestRates(last, nextDue210, p, t);
    BigDecimal interestAmount = getInterestAmount(last, nextDue210, principal, rates, p, t);
    return balance210.add(commodity.scale(interestAmount));
  }

  private ProjectedInstallment get211Installment(Date nextDue211,
      BigDecimal balance210, PeriodicBalanceBuilder balance, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    BigDecimal rate = sched211.getPostingDueRateOnDate(nextDue211, p, t);
    BigDecimal amount = commodity.scale(balance210.multiply(rate));
    return new ProjectedInstallment(nextDue211, BigDecimal.ZERO, amount, balance.getLastBalance(), 
        PrimitiveValue.dateValue(sched211.getPreviousDueDate(nextDue211, p, t), getStartDate()));
  }

  private boolean isNextDate(Date candidate, Date... otherDates) {
    if (candidate == null) {
      return false;
    }
    for (int i = 0; i < otherDates.length; i++) {
      Date d = otherDates[i];
      if (d != null && d.before(candidate)) {
        return false;
      }
    }
    return true;
  }

  private ProjectedInstallment get100Installment(Date lastDue, Date nextDue100,
      PeriodicBalanceBuilder principal, BigDecimal currentPrincipal, BigDecimal alreadyPaidPrincipal, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    Date nextDue = nextDue100;
    Date previousDueDate = sched100.getPreviousDueDate(nextDue, p, t);
    BigDecimal installmentDueAmount = sched100.getPostingDueRateOnDate(nextDue, p, t);
    BigDecimal installmentInterestAmount = commodity.scale(getInterestAmount(lastDue, nextDue, principal, get200InterestRates(p, t), p, t));
    Date interestDue = sched200.getNextDueDate(nextDue, p, t);
    BigDecimal installmentAmount = commodity.scale(BigDecimalUtil.min(installmentDueAmount, currentPrincipal.add(alreadyPaidPrincipal)));
    BigDecimal installmentPrincipalAmount = installmentAmount;
    if (installmentDueAmount.compareTo(BigDecimal.ZERO) <= 0 || installmentPrincipalAmount.compareTo(BigDecimal.ZERO) < 0) {
      return null;
    }
    if (interestDue == null || !interestDue.equals(nextDue)) {
      installmentInterestAmount = BigDecimal.ZERO;
    }
    if (currentPrincipal.compareTo(BigDecimal.ZERO) > 0) {
      return new ProjectedInstallment(nextDue, installmentPrincipalAmount, installmentInterestAmount, principal.getLastBalance().subtract(installmentPrincipalAmount), PrimitiveValue.dateValue(previousDueDate, getStartPreviousDueDate(p, t)));
    } else {
      return new ProjectedInstallment(nextDue, installmentDueAmount, installmentInterestAmount, true, principal.getLastBalance().subtract(installmentDueAmount), PrimitiveValue.dateValue(previousDueDate, getStartPreviousDueDate(p, t)));
    }

  }

  private ProjectedInstallment get200Installment(Date nextDue200,
      PeriodicBalanceBuilder principal, BigDecimal currentPrincipal, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    Date nextDue = nextDue200;
    Date previousDueDate = sched200.getPreviousDueDate(nextDue, p, t);
    if (previousDueDate == null) {
      previousDueDate = DateParser.getPreviousDay(getStartDate());
    }
    BigDecimal installmentInterestAmount = commodity.scale(getInterestAmount(previousDueDate, nextDue, principal, get200InterestRates(p, t), p, t));
    BigDecimal installmentAmount = installmentInterestAmount;
    BigDecimal installmentPrincipalAmount = BigDecimal.ZERO;
    if (installmentAmount.compareTo(BigDecimal.ZERO) <= 0) {
      return null;
    }
    return new ProjectedInstallment(nextDue, installmentPrincipalAmount, installmentInterestAmount, principal.getLastBalance(), PrimitiveValue.dateValue(previousDueDate, DateParser.getPreviousDay(getStartDate())));
  }

  private ProjectedInstallment get110Installment(Date lastDue, Date nextDue110,
      PeriodicBalanceBuilder principal, boolean includePaid, BigDecimal alreadyPaidPrincipal,
      IReadWriteDataProvider p, ITransaction t)
      throws SQLException {
    Date nextDue = nextDue110;
    Date previousDueDate = sched110.getPreviousDueDate(nextDue, p, t);
    BigDecimal installmentDueAmount = sched110.getPostingDueRateOnDate(nextDue, p, t);
    if (principal.getLastBalance().compareTo(BigDecimal.ZERO) <= 0 && includePaid) {
      return new ProjectedInstallment(nextDue, installmentDueAmount, BigDecimal.ZERO, true, principal.getLastBalance(), PrimitiveValue.dateValue(previousDueDate, getStartPreviousDueDate(p, t)));
    }
    // It is not entirely clear which interest start date should be used, I'm going to go with the
    // last 200 posting date because otherwise you can end up with 110 installments greater than
    // the 110 rate assigned which seems wrong
    Date previous200DueDate = sched200.getPreviousDueDate(nextDue, p, t);
    if (previous200DueDate == null) {
      previous200DueDate = getStartDate();
    }
    BigDecimal installmentInterestAmount = commodity.scale(getInterestAmount(previous200DueDate, nextDue, principal, get200InterestRates(p, t), p, t));
    Date interestDue = sched200.getNextDueDate(nextDue, p, t);
    BigDecimal installmentAmount = commodity.scale(BigDecimalUtil.min(installmentDueAmount, principal.getLastBalance().add(alreadyPaidPrincipal).add(installmentInterestAmount)));
    BigDecimal installmentPrincipalAmount = installmentAmount.subtract(installmentInterestAmount);
    if (installmentPrincipalAmount.compareTo(BigDecimal.ZERO) < 0) {
      installmentPrincipalAmount = BigDecimal.ZERO;
      installmentAmount = installmentInterestAmount;
    }
    if (installmentAmount.compareTo(BigDecimal.ZERO) <= 0) {
      return null;
    }
    if (interestDue == null || !interestDue.equals(nextDue)) {
      installmentInterestAmount = BigDecimal.ZERO;
    }
    if (principal.getLastBalance().compareTo(BigDecimal.ZERO) > 0) {
      return new ProjectedInstallment(nextDue, installmentPrincipalAmount, installmentInterestAmount, principal.getLastBalance().subtract(installmentPrincipalAmount), PrimitiveValue.dateValue(previousDueDate, getStartPreviousDueDate(p, t)));
    } else {
      return new ProjectedInstallment(nextDue, installmentDueAmount, BigDecimal.ZERO, true, principal.getLastBalance().subtract(installmentDueAmount), PrimitiveValue.dateValue(previousDueDate, getStartPreviousDueDate(p, t)));
    }
  }

  private BigDecimal getInterestAmount(Date previousEnd, Date end,
      PeriodicBalanceBuilder principal, PeriodicBalance rate, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    if (rate == null) { // There is no interest rate at all, so that's zero
      return BigDecimal.ZERO;
    }
    PeriodicBalance balances = principal.getPeriodicBalance();
    InterestCalculator calc = new InterestCalculator("projection", InterestCalculator.InterestType.ANNUAL, balances, rate);
    BigDecimal interestForPeriod = calc.getInterestForPeriod(DateParser.getNextDay(previousEnd), end);
    return interestForPeriod;
  }
  
  private PeriodicBalance get200InterestRates(IReadWriteDataProvider p,
      ITransaction t) throws SQLException {
    if (interestRates == null) {
      LoanInterest loanInterest = new LoanInterest(sched200);
      Calendar c = Calendar.getInstance();
      c.setTime(getStartDate());
      c.add(Calendar.YEAR, 100);
      try {
        interestRates = loanInterest.getRates(getStartDate(), c.getTime(), p, t);
      } catch (IllegalArgumentException e) { 
        // In case the 200 schedule has an end date set then I will probably be able to get the last
        // due date with this for a valid range, if this doesn't work i don't know what will.
        interestRates = loanInterest.getRates(getStartDate(), sched200.getPreviousDueDate(c.getTime(), p, t), p, t);
      }
    }
    return interestRates;
  }
  
  private PeriodicBalance get210InterestRates(Date start, Date end, IReadWriteDataProvider p,
      ITransaction t) throws SQLException {
    LoanInterest loanInterest = new LoanInterest(sched210);
    return loanInterest.getRates(start, end, p, t);
  }

  private Date getStartDate() {
    return startDate;
  }
}
