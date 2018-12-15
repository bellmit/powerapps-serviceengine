package com.profitera.services.business.batch.financial.impl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.finance.InterestCalculator;
import com.profitera.finance.PeriodicBalance;
import com.profitera.finance.InterestCalculator.InterestType;
import com.profitera.log.DefaultLogProvider;
import com.profitera.log.ILogProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.Transaction;
import com.profitera.services.system.loan.AccountHistory;
import com.profitera.services.system.loan.IAccountTypes;
import com.profitera.services.system.loan.ILoanInterest;
import com.profitera.services.system.loan.InterestAccrual;
import com.profitera.services.system.loan.LoanAccount;
import com.profitera.services.system.loan.impl.IAccountTypeProvider;
import com.profitera.services.system.loan.impl.IScheduleManager;
import com.profitera.services.system.loan.impl.PostingSchedule.PostingType;
import com.profitera.util.BigDecimalUtil;
import com.profitera.util.DateParser;
import com.profitera.util.Holder;

public class LoanPenaltyInterestProcessor {
  private final IAccountTypeProvider loanAccountService;
  private ILogProvider log;
  private final Account generalPenaltyAccount;
  private final Account generalPenaltyInSuspenseAccount;
  private String[] subjectTypes;
  private final String[] balanceTypes;
  private Integer graceDays = null;
  private static final String[] DEFAULT_SUBJECT = {
    IAccountTypes.PINST, IAccountTypes.INTEREST, IAccountTypes.PENALTY, 
    IAccountTypes.IIS, IAccountTypes.PIS
  };
  public LoanPenaltyInterestProcessor(String[] subjectTypes, String[] balanceTypes, IAccountTypeProvider loanAccountService, 
      Account generalPenaltyAccount, Account generalPenaltyInSuspenseAccount){
    this.subjectTypes = subjectTypes;
    if (this.subjectTypes == null) {
      this.subjectTypes = DEFAULT_SUBJECT;
    }
    this.balanceTypes = balanceTypes;
    this.loanAccountService = loanAccountService;
    this.generalPenaltyAccount = generalPenaltyAccount;
    this.generalPenaltyInSuspenseAccount = generalPenaltyInSuspenseAccount;
  }
  public LoanPenaltyInterestProcessor(IAccountTypeProvider loanAccountService, 
      Account generalPenaltyAccount, Account generalPenaltyInSuspenseAccount){
    this(DEFAULT_SUBJECT, null, loanAccountService, generalPenaltyAccount, generalPenaltyInSuspenseAccount);
  }

  private ILogProvider getLog(){
    if (log == null) {
      log = new DefaultLogProvider();
      log.register(new LedgerPenaltyLogClient());
    }
    return log;
  }

  private IAccountTypeProvider getTypeProvider() {
    return loanAccountService;
  }
  
  private Date getGraceAdjustment(final Date date) {
    Date graceDate = date;
    // The argument here is the first day of the period,
    // so that means that 1 day of grace is already the
    // date that is being passed in so we start at 1.
    //TODO: This creates a bug where there is always 1
    // day of grace no matter what
    for(int i = 1; i < getGraceDays(); i++){
      graceDate = DateParser.getNextDay(graceDate);
    }
    return graceDate;
  }

  private int getGraceDays() {
    if (graceDays == null) {
      return 5;
    }
    return graceDays;
  }
  public Transaction accruePenaltyInterest(final LoanAccount loan, InterestType interestType, final Date effectiveDate, final IReadWriteDataProvider p,
      ITransaction t) throws SQLException, AbortTransactionException {
    if (interestType == null) {
      interestType = InterestType.MONTHLY;
    }
    Holder<Transaction> transactionHolder = accruePenaltyInterest(loan, effectiveDate, interestType, p, t, Transaction.class);
    if (transactionHolder != null) {
      return transactionHolder.get();
    }
    return null;
  }
  public BigDecimal accruePenaltyInterestToDateWithoutPosting(LoanAccount loan, Date effectiveDate, InterestType interestType,
      IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    Holder<BigDecimal> amountHolder = accruePenaltyInterest(loan, effectiveDate, interestType, p, t, BigDecimal.class);
    if (amountHolder != null) {
      return amountHolder.get();
    }
    return null;
  }
  private <T> Holder<T> accruePenaltyInterest(final LoanAccount loan, final Date effectiveDate, InterestCalculator.InterestType interestType, final IReadWriteDataProvider p,
      ITransaction t, Class<T> returnType) throws SQLException, AbortTransactionException {
    boolean isAccrualAmountCheck = returnType.equals(BigDecimal.class);
    final IAccountTypeProvider types = getTypeProvider();
    IScheduleManager postingSchedule = loan.getPostingScheduleManager(PostingType.PENALTY, p, t);
    Date[] period = postingSchedule.getBillingPeriod(effectiveDate, p, t);
    if (period == null){
      getLog().emit(LedgerPenaltyLogClient.NO_PERIOD, effectiveDate, loan.getId());
      return null;
    }
    
    if (!DateParser.isEqualDate(period[1], effectiveDate) && !isAccrualAmountCheck){
      getLog().emit(LedgerPenaltyLogClient.NOT_DUE, loan.getId(), effectiveDate, period[1]);
      return null;
    } else {
      // Now we might be posting but we need to check the preconditions first
      if (period[0].equals(loan.getLoanFinancialAccountsCreatedDate(p))){
        getLog().emit(LedgerPenaltyLogClient.FORGIVE_INIT, effectiveDate, loan.getId());
        return null;
      }
      if (!period[0].after(loan.getLastPenaltyPostingDate(p, t))) {
        getLog().emit(LedgerPenaltyLogClient.ALREADY_POSTED, effectiveDate, loan.getId());
        return null;
      }
      Account loanPenaltyInSuspense = loan.getSetFinancialAccount(types.get(IAccountTypes.PIS, p, t), p, t);
      Account loanPenalty = loan.getSetFinancialAccount(types.get(IAccountTypes.PENALTY, p, t), p, t);
      Account[] subjectToPenaltyAccounts = getAccountsSubjectToPenalty(loan, p, t);
      Account postToAccount = loanPenalty;
      Account postFromTemp = generalPenaltyAccount;
      if (loan.isNonPerforming(effectiveDate, p, t)){
        postToAccount = loanPenaltyInSuspense;
        postFromTemp = generalPenaltyInSuspenseAccount;
      }
      final Account postTo = postToAccount;
      final Date start = period[0];
      // effectiveDate equals period[1] if we are posting and we don't want it to if we are not.
      final Date end = effectiveDate;
      final Account postFrom = postFromTemp;
      boolean isChargable = isPenaltyChargableForPeriod(period, getAccountsForPenaltyBalanceCheck(subjectToPenaltyAccounts, loan, p, t), loan, p);
      Transaction trans = null;
      ILoanInterest interest = loan.buildPenaltyLoanInterest(interestType, postingSchedule, p, t);
      final InterestAccrual accrual = new InterestAccrual(loan.getId(), interest, subjectToPenaltyAccounts);
      if (isChargable || isAccrualAmountCheck) {
        if (isAccrualAmountCheck) {
          BigDecimal amount = accrual.getAccrualAmount(start, end, p, t);
          return new Holder<T>((T) amount);
        } else {
          trans = accrual.getAccrualTransactionForPeriod(BigDecimal.ZERO, start, end, end, postTo, postFrom, p, t);
        }
      } else {
        trans = accrual.getZeroAccrualTransactionForPeriod(start, end, end, postTo, postFrom, p, t);
      }
      Transaction posted = loan.postTransaction(trans, effectiveDate, p, t);
      loan.recordPenaltyPostingHistory(postFrom, posted, start, end, end, end, p, t);
      return new Holder<T>((T) posted);
    }
  }
  private boolean isPenaltyChargableForPeriod(Date[] period, Account[] penaltyAccounts,
      final LoanAccount loan, final IReadWriteDataProvider p) throws SQLException, AbortTransactionException {
    if (getGraceDays() <= 0) {
      return true;
    }
    // If the penalty on the grace day has dropped to zero it means the customer has settled
    // up by that day, so I will discount them the full amount
    //TODO: incorporation grace period logic
    Date gracePeriod = getGraceAdjustment(period[0]);
    AccountHistory ah = new AccountHistory(penaltyAccounts);
    PeriodicBalance accountBalance = ah.getAccountBalance(period[0], gracePeriod, p);
    BigDecimal balanceAtStart = accountBalance.getBalance(period[0]);
    if (BigDecimalUtil.isEqual(balanceAtStart, BigDecimalUtil.ZERO)
        || BigDecimalUtil.isEqual(accountBalance.getBalance(gracePeriod), BigDecimalUtil.ZERO)){
      getLog().emit(LedgerPenaltyLogClient.PAID_WITHIN_GRACE, gracePeriod, period[1], loan.getId());
      return false;
    }
    return true;
  }
  private Account[] getAccountsForPenaltyBalanceCheck(Account[] subjectAccounts, final LoanAccount loan, final IReadWriteDataProvider p,
      ITransaction t) throws AbortTransactionException, SQLException {
    if (balanceTypes == null) {
      return subjectAccounts;
    }
    return getLoanAccountsByTypes(balanceTypes, loan, p, t);
  }
  private Account[] getAccountsSubjectToPenalty(final LoanAccount loan, final IReadWriteDataProvider p,
      ITransaction t) throws AbortTransactionException, SQLException {
    return getLoanAccountsByTypes(subjectTypes, loan, p, t);
  }

  private Account[] getLoanAccountsByTypes(String[] accountTypes, final LoanAccount loan,
      final IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    final IAccountTypeProvider types = getTypeProvider();
    Account[] subjectAccounts = new Account[accountTypes.length];
    for (int i = 0; i < subjectAccounts.length; i++) {
      subjectAccounts[i] = loan.getSetFinancialAccount(types.get(accountTypes[i], p, t), p, t);
    }
    return subjectAccounts;
  }
  public void setGraceDays(int days) {
    graceDays = days;
  }
}
