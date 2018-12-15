package com.profitera.services.system.loan.impl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.loan.impl.PostingSchedule.PostingType;

public interface ILoanPrincipalInstallmentManager {

  BigDecimal getRemainingInstallmentPrincipalAmount(BigDecimal advanceInterestAmount,
      Date paymentDate, IScheduleManager postingManager110,
      IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException;

  BigDecimal postAllRemainingPrincipalInstallmentPosting(
      BigDecimal adjustedAdvanceForShortPrincipal, Date paymentDate,
      Date effectiveDate, IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException;

  BigDecimal getInstallmentPrincipalAmountPaidForPeriod(Date nextDue,
      IScheduleManager postingManager110, IReadWriteDataProvider p,
      ITransaction t) throws SQLException;

  BigDecimal postAllRemainingPrincipalLumpSumPosting(BigDecimal withholdAmount, Date date,
      Date effectiveDate, IReadWriteDataProvider p, ITransaction t)
      throws AbortTransactionException, SQLException;

  public BigDecimal postPrincipalInstallmentPosting(Account sourceAccount,
      BigDecimal principalAmount, Date date, Date startOfPeriod,
      Date endOfPeriod, Date billingDate, Date effectiveDate, PostingType pt, IReadWriteDataProvider p,
      ITransaction t) throws AbortTransactionException, SQLException;
  public boolean isPostResolution(Date effective, IScheduleManager m110, IScheduleManager m100, 
      IReadWriteDataProvider p, ITransaction t) throws SQLException;
}
