package com.profitera.services.system.loan;

import java.sql.SQLException;
import java.util.Date;

import com.profitera.dataaccess.ITransaction;
import com.profitera.finance.InterestCalculator;
import com.profitera.finance.PeriodicBalance;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public interface ILoanInterest {

  public abstract PeriodicBalance getRates(Date startDate, Date endDate,
      IReadWriteDataProvider p, ITransaction t) throws SQLException;

  public abstract InterestCalculator.InterestType getType();

}