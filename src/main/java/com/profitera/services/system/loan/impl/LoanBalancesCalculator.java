package com.profitera.services.system.loan.impl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.profitera.services.system.financial.Commodity;
import com.profitera.services.system.loan.impl.PostingSchedule.PostingType;

public class LoanBalancesCalculator {
  private Map<PostingType, PostingSchedule> schedules;
  private final Commodity commodity;

  public LoanBalancesCalculator(Commodity c, Map<PostingType, PostingSchedule> postingSchedules) {
    this.commodity = c;
    this.schedules = postingSchedules;
  }
  
  public List<ProjectedInstallment> getInstallments(Date startDate, BigDecimal principal) {
    InstallmentProjector p = new InstallmentProjector(commodity, 
        getManager(startDate, PostingType.PRINCIPAL_INSTALLMENT), 
        getManager(startDate, PostingType.PRINCIPAL_LUMP_SUM),
        getManager(startDate, PostingType.IMMEDIATE_I),
        getManager(startDate, PostingType.DEFERRED_I),
        getManager(startDate, PostingType.DEFERRED_T),startDate);
    try {
      return p.getFutureInstallments(principal, null, null);
    } catch (SQLException e) {
      // This should be impossible since nothing is going to access the database
      return null;
    }
  }

  private IScheduleManager getManager(Date startDate, PostingType t) {
    PostingSchedule s = schedules.get(t);
    if (s == null) {
      return new NullScheduleManager();
    }
    return new LocalPostingScheduleManager(t, s, startDate);
  }
}
