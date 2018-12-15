package com.profitera.services.system.loan;

import java.sql.SQLException;

import com.profitera.finance.InterestCalculator.InterestType;
import com.profitera.services.system.loan.impl.IScheduleManager;


public class PenaltyLoanInterest extends AbstractLoanInterest implements ILoanInterest {
  private final IScheduleManager m;
  public PenaltyLoanInterest(IScheduleManager m, InterestType interestType){
    super(interestType);
    this.m = m;
  }

  protected IScheduleManager getPostingSchedule() throws SQLException {
    return m;
  }
}
