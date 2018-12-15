package com.profitera.services.system.loan.impl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.NoSuchStatementException;
import com.profitera.finance.OutstandingCharges;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.AccountType;
import com.profitera.services.system.loan.AccountApportionment;
import com.profitera.services.system.loan.IAccountSet;
import com.profitera.services.system.loan.IAccountTypes;
import com.profitera.services.system.loan.LoanAccount;
import com.profitera.util.CollectionUtil;
import com.profitera.util.MapListUtil;


public class DefaultKnockoffScheduleManager implements IKnockoffScheduleManager {
  private static final String LEDGER_ID = "LEDGER_ID";
  public static final String ACCOUNT_TYPE_CODE = "ACCOUNT_TYPE_CODE";
  public static final String KNOCKOFF_MODE_ID = "KNOCKOFF_MODE_ID";
  public static final String LEVEL = "LEVEL";
  public static final String SORT_PRIORITY = "SORT_PRIORITY";
  public static final String KNOCKOFF_SCHEDULE_TYPE_ID = "KNOCKOFF_SCHEDULE_TYPE_ID";
  //
  public static final int INSTALLMENT_PLAN = 1;
  public static final int NO_INSTALLMENT_PLAN = 2;
  //
  private class Schedule {
    final AccountType type;
    final int level;
    private Schedule(AccountType type, int level) {
      this.type = type;
      this.level = level;
    }
  }
  private class ScheduleSet {
    private final List<Schedule> noInstallmentPlan;
    private final List<Schedule> withInstallmentPlan;
    private final List<Schedule> withInstallmentPlanNoPrincipal;
    public ScheduleSet(List<Schedule> noInst,
    List<Schedule> withInst,
    List<Schedule> withInstNoPrincipal){
      this.noInstallmentPlan = noInst;
      this.withInstallmentPlan = withInst;
      this.withInstallmentPlanNoPrincipal = withInstNoPrincipal;
    }
  }
  private final IAccountTypeProvider types;
  private Map<Long, ScheduleSet> cache = new HashMap<Long, ScheduleSet>();
  private final Date effectiveDate;
  public DefaultKnockoffScheduleManager(IAccountTypeProvider p, Date effectiveDate) {
    this.types = p;
    this.effectiveDate = effectiveDate;
  }
  
  public List<AccountApportionment> getAllAccountApportionments(IAccountSet loan, boolean allowPrincipal,
      boolean isPostResolution, IReadWriteDataProvider p, ITransaction t) 
      throws SQLException, AbortTransactionException {
    Long scheduleId = resolveAccountKnockoffScheduleId(loan, p, t);
    ScheduleSet scheduleSet = cache.get(scheduleId);
    if (scheduleSet == null) {
      if (scheduleId == null) {
        cache.put(scheduleId, new ScheduleSet(getNoInstallmentPlanDefault(loan, p, t), 
            getWithInstallmentPlanDefault(loan, true, p, t), getWithInstallmentPlanDefault(loan, false, p, t)));
      } else {
        cache.put(scheduleId, loadSchedule(scheduleId, p, t));
      }
      scheduleSet = cache.get(scheduleId);
    }
    if (isPostResolution){
      if (allowPrincipal) {
        return build(scheduleSet.withInstallmentPlan, loan, p, t);
      } else {
        return build(scheduleSet.withInstallmentPlanNoPrincipal, loan, p, t);
      }
    } else {
      return build(scheduleSet.noInstallmentPlan, loan, p, t);
    }
  }

  private List<AccountApportionment> build(List<Schedule> schedules, IAccountSet loan,
      IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    List<AccountApportionment> list = new ArrayList<AccountApportionment>();
    for (Schedule schedule : schedules) {
      list.add(buildAccountApportionment(loan, schedule.type, schedule.level, list.size(), p, t));
    }
    return list;
  }

  private ScheduleSet loadSchedule(Long scheduleId, IReadWriteDataProvider p, ITransaction t)  throws AbortTransactionException, SQLException {
    Iterator<Map<String, Object>> scheduleData = queryEntries(scheduleId, p);
    List<Map<String, Object>> noInst = new ArrayList<Map<String,Object>>();
    List<Map<String, Object>> withInst = new ArrayList<Map<String,Object>>();
    while (scheduleData.hasNext()) {
      Map<String, Object> row = scheduleData.next();
      if (isWithInstallment(row)) {
        withInst.add(row);
      } else {
        noInst.add(row);
      }
    }
    List<Schedule> withInstSchedule = marshallSchedule(withInst, p, t);
    List<Schedule> noInstSchedule = marshallSchedule(noInst, p, t);
    List<Schedule> withInstNoPrincipalSchedule = new ArrayList<Schedule>(withInstSchedule);
    for (Iterator<Schedule> i = withInstNoPrincipalSchedule.iterator(); i.hasNext();) {
      Schedule s = i.next();
      if (s.type.equals(types.get(IAccountTypes.PRINCIPAL, p, t))){
        i.remove();
      }
    }
    return new ScheduleSet(noInstSchedule, withInstSchedule, withInstNoPrincipalSchedule);
  }

  private List<Schedule> marshallSchedule(List<Map<String, Object>> withInst, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    withInst = MapListUtil.sortBy(new String[]{SORT_PRIORITY}, true, withInst);
    List<Schedule> list = new ArrayList<Schedule>();
    Iterator<Map<String, Object>> i = withInst.iterator();
    while(i.hasNext()) {
      Map<String, Object> row = i.next();
      int currentLevel = ((Number)row.get(LEVEL)).intValue();
      String code = getTypeCode(row);
      list.add(new Schedule(types.get(code, p, t), currentLevel));
    }
    return list;
  }

  private String getTypeCode(Map<String, Object> row) {
    return (String) row.get(ACCOUNT_TYPE_CODE);
  }

  private boolean isWithInstallment(Map<String, Object> row) {
    Number n = (Number) row.get(KNOCKOFF_SCHEDULE_TYPE_ID);
    if (n.intValue() == INSTALLMENT_PLAN){
      return true;
    }
    if (n.intValue() == NO_INSTALLMENT_PLAN){
      return false;
    }
    throw new IllegalArgumentException("Invalid schedule type returned for " + KNOCKOFF_SCHEDULE_TYPE_ID + " value " + n.intValue());
  }

  private List<Schedule> getWithInstallmentPlanDefault(IAccountSet loan, boolean allowPrincipal,
      IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    List<Schedule> app = new ArrayList<Schedule>();
    app.add(new Schedule(types.get(IAccountTypes.INTEREST, p, t), 1));
    app.add(new Schedule(types.get(IAccountTypes.IIS, p, t), 1));
    app.add(new Schedule(types.get(IAccountTypes.PINST, p, t), 1));
    //
    if (allowPrincipal) {
      app.add(new Schedule(types.get(IAccountTypes.PRINCIPAL, p, t), 5));
    }
    //
    app.add(new Schedule(types.get(IAccountTypes.CHARGE, p, t), 7)); 
    app.add(new Schedule(types.get(IAccountTypes.CIS, p, t), 7));
    app.add(new Schedule(types.get(IAccountTypes.PENALTY, p, t), 7));
    app.add(new Schedule(types.get(IAccountTypes.PIS, p, t), 7));
    
    return Collections.unmodifiableList(app);
  }

  private List<Schedule> getNoInstallmentPlanDefault(IAccountSet loan, IReadWriteDataProvider p,
      ITransaction t) throws AbortTransactionException, SQLException {
    List<Schedule> app = new ArrayList<Schedule>();
    app.add(new Schedule(types.get(IAccountTypes.CHARGE, p, t), 1));
    app.add(new Schedule(types.get(IAccountTypes.PENALTY, p, t), 1));
    app.add(new Schedule(types.get(IAccountTypes.INTEREST, p, t), 1));
    // now suspense
    app.add(new Schedule(types.get(IAccountTypes.CIS, p, t), 1));
    app.add(new Schedule(types.get(IAccountTypes.PIS, p, t), 1));
    app.add(new Schedule(types.get(IAccountTypes.IIS, p, t), 1));
    // P last
    app.add(new Schedule(types.get(IAccountTypes.PINST, p, t), 1));
    app.add(new Schedule(types.get(IAccountTypes.PRINCIPAL, p, t), 1));
    return Collections.unmodifiableList(app);
  }
  
  private AccountApportionment buildAccountApportionment(IAccountSet loan, AccountType accountType,
      int level, int order, final IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    Account account = loan.getSetFinancialAccount(accountType, p, t);
    AccountApportionment a = new AccountApportionment(account, 
        getOutstandingSplits(loan, account, p, t), level, order);
    return a;
  }

  private OutstandingCharges getOutstandingSplits(IAccountSet loan, Account a,
      IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException {
    BigDecimal d = loan.getAccountPostedBalance(a, p);
    if (d == null){
      throw new AbortTransactionException("No balance available for account: " + a.getId());
    }
    Map<String, Object> args = new HashMap<String, Object>();
    args.put(LoanAccount.LEGACY_ACCOUNT_ID, a.getId());
    args.put(LEDGER_ID, a.getId());
    Iterator<Map<String, Object>> i = queryCredits(args, p);
    return new OutstandingCharges(d, CollectionUtil.asList(i), "POSTING_DATE", "AMOUNT");
  }

  @SuppressWarnings("unchecked")
  private Iterator<Map<String, Object>> queryCredits(Map<String, Object> args, IReadWriteDataProvider p)
      throws SQLException {
    return p.query(IReadWriteDataProvider.LIST_RESULTS, "getFinancialAccountPostedSplitCreditAmounts", args);
  }

  @SuppressWarnings("unchecked")
  private Iterator<Map<String, Object>> queryEntries(Long scheduleId, IReadWriteDataProvider p) throws SQLException {
    return p.query(IReadWriteDataProvider.LIST_RESULTS, "getLoanKnockOffScheduleEntries", scheduleId);
  }
  
  private Long resolveAccountKnockoffScheduleId(IAccountSet loan, IReadWriteDataProvider p, ITransaction t)  throws AbortTransactionException, SQLException {
    Map<String, Object> args = new HashMap<String, Object>();
    args.put(LEDGER_ID, loan.getId());
    args.put(LoanAccount.LEGACY_ACCOUNT_ID, loan.getId());
    args.put("EFFECTIVE_DATE", effectiveDate);
    try {
      return (Long) p.queryObject("getLoanKnockOffScheduleId", args);
    } catch (NoSuchStatementException e) {
      return null;
    }
  }
}
