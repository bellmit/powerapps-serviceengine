package com.profitera.services.system.loan.impl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Transaction;

public interface ILoanInterestManager {

  Transaction postImmediateInterestAccrual(Date endOfPeriod,
      Date effectiveDate, String[] balanceAccountTypes, IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException;

  BigDecimal getInterestAmountChargedForPeriod(Date upToDate,
      IReadWriteDataProvider p, ITransaction t) throws SQLException;
  
  public Date getLastImmediateInterestPostedDate(IReadOnlyDataProvider p, ITransaction t) throws SQLException;

}
