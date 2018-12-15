package com.profitera.services.system.loan;

import java.sql.SQLException;

import com.profitera.finance.InterestCalculator;
import com.profitera.services.system.loan.impl.IScheduleManager;


public class LoanInterest extends AbstractLoanInterest implements ILoanInterest {

  private final IScheduleManager m;

  public LoanInterest(IScheduleManager m){
    super(InterestCalculator.InterestType.ANNUAL);
    this.m = m;
  }

  protected IScheduleManager getPostingSchedule() throws SQLException {
    return m;
  }
  
}
