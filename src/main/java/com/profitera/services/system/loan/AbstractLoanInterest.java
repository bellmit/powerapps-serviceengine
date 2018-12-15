package com.profitera.services.system.loan;

import java.sql.SQLException;
import java.util.Date;

import com.profitera.dataaccess.ITransaction;
import com.profitera.finance.InterestCalculator.InterestType;
import com.profitera.finance.PeriodicBalance;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.loan.impl.IScheduleManager;

public abstract class AbstractLoanInterest implements ILoanInterest {
  private final InterestType interestType;

  public AbstractLoanInterest(InterestType intType){
    interestType = intType;
  }
  
  public PeriodicBalance getRates(Date startDate, Date endDate, IReadWriteDataProvider p, ITransaction t) throws SQLException{
    return getPostingSchedule().getRates(startDate, endDate, p, t);
  }

  protected abstract IScheduleManager getPostingSchedule() throws SQLException;

  public InterestType getType() {
    return interestType;
  }
}
