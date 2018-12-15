package com.profitera.services.system.loan;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.finance.InterestCalculator;
import com.profitera.finance.PeriodicBalance;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.Split;
import com.profitera.services.system.financial.Transaction;
import com.profitera.util.BigDecimalUtil;

public class InterestAccrual {
  private final Account[] loanApplicableAccounts;
  private final Long loanAccountId;
  private static Log LOG;
  private final ILoanInterest loanInterest;
  
  public InterestAccrual(Long loanAccountId, ILoanInterest interestRate, Account[] loanApplicableAccounts){
    loanInterest = interestRate;
    if (loanApplicableAccounts == null){
      throw new IllegalArgumentException("Interest accrual requires a loan interest applicable accounts");
    }
    this.loanApplicableAccounts = loanApplicableAccounts;
    if (loanAccountId == null){
      throw new IllegalArgumentException("Interest accrual requires a loan account identifer");
    }
    this.loanAccountId = loanAccountId;
  }
  
  public BigDecimal getAccrualAmount(Date startDate, Date endDate, IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException{
    PeriodicBalance balances = null;
    PeriodicBalance interestRateHistory = null;
    try {
      balances = new AccountHistory(loanApplicableAccounts).getAccountBalance(startDate, endDate, p);
    } catch (IllegalArgumentException e){
      throw new IllegalArgumentException("Account balance retrieval query returned one or more illegal results");
    }
    try {
      interestRateHistory = loanInterest.getRates(startDate, endDate, p, t);
    } catch (IllegalArgumentException e){
      throw new IllegalArgumentException("Account interest rate retrieval query returned one or more illegal results");
    }
    getLog().debug("Calculating interest to accrue from " + startDate + " to " + endDate + " for " + loanAccountId);
    getLog().debug("Balances from " + startDate + " to " + endDate + " for " + loanAccountId + " " + balances);
    getLog().debug("Rates from " + startDate + " to " + endDate + " for " + loanAccountId + " " + interestRateHistory);    
    BigDecimal totalAccrued = new InterestCalculator(loanAccountId + "", loanInterest.getType(), balances, interestRateHistory).getInterestForPeriod(startDate, endDate);
    return totalAccrued;
  }
  
  protected Long getLoanAccountId() {
    return loanAccountId;
  }
  
  public Transaction getAccrualTransactionForPeriod(BigDecimal discountAmount, Date startDate, Date endDate,
      Date effectiveDate, Account loanAccrueInto, Account generalInterestAccount,
      IReadWriteDataProvider p, ITransaction t) 
  throws AbortTransactionException, SQLException{
    BigDecimal totalAccrued = getAccrualAmount(startDate, endDate, p, t);
    BigDecimal additionalAmount = totalAccrued.subtract(discountAmount);
    getLog().info("Discount for " + loanAccountId + ": " + discountAmount + ", new amount is " + additionalAmount);
    // Here we could decline to create the transaction if the amount is zero
    // but I am creating it for now since it acts as a marker that the process ran 
    // successfully for the account.
    Split to = new Split(loanAccrueInto, additionalAmount, null, null);
    Split from = new Split(generalInterestAccount, additionalAmount.multiply(BigDecimalUtil.NEG_ONE), null, null);
    Transaction trans = new Transaction(loanAccrueInto.getCommodity(), new Split[]{to, from});
    return trans;
  }
  public Transaction getZeroAccrualTransactionForPeriod(Date startDate, Date endDate,
      Date effectiveDate, Account loanAccrueInto, Account generalInterestAccount,
      IReadWriteDataProvider p, ITransaction t) 
  throws AbortTransactionException, SQLException{
    getLog().info("Zero accrual for " + loanAccountId);
    Split to = new Split(loanAccrueInto, BigDecimal.ZERO, null, null);
    Split from = new Split(generalInterestAccount, BigDecimal.ZERO, null, null);
    Transaction trans = new Transaction(loanAccrueInto.getCommodity(), new Split[]{to, from});
    return trans;
  }

  
  protected static Log getLog() {
    if (LOG == null) {
      LOG = LogFactory.getLog(InterestAccrual.class);
    }
    return LOG;
  }

  public BigDecimal getAdvanceInterestForPeriod(BigDecimal availableAmount, 
      Date startDate, Date endDate,
      IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    if(endDate==null){
    	return BigDecimal.ZERO;
    } // Bug 1068.
  	PeriodicBalance balances = new AccountHistory(loanApplicableAccounts).getAccountBalance(startDate, endDate, p);
    PeriodicBalance interestRateHistory = loanInterest.getRates(startDate, endDate, p, t);
    BigDecimal totalAccrued = new InterestCalculator(loanAccountId + "", loanInterest.getType(), balances, interestRateHistory).getAdvanceInterestForPeriod(availableAmount, startDate, endDate);
    return totalAccrued;
  }
}
