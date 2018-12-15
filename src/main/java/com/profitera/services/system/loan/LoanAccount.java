package com.profitera.services.system.loan;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.NoSuchStatementException;
import com.profitera.finance.PeriodicBalance;
import com.profitera.finance.InterestCalculator.InterestType;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.AccountAction;
import com.profitera.services.system.financial.AccountType;
import com.profitera.services.system.financial.Split;
import com.profitera.services.system.financial.Transaction;
import com.profitera.services.system.financial.TransactionQuery;
import com.profitera.services.system.loan.impl.AgeCalculator;
import com.profitera.services.system.loan.impl.DefaultKnockoffScheduleManager;
import com.profitera.services.system.loan.impl.IAccountTypeProvider;
import com.profitera.services.system.loan.impl.IKnockoffScheduleManager;
import com.profitera.services.system.loan.impl.ILoanInterestManager;
import com.profitera.services.system.loan.impl.ILoanPrincipalInstallmentManager;
import com.profitera.services.system.loan.impl.IScheduleManager;
import com.profitera.services.system.loan.impl.InstallmentProjector;
import com.profitera.services.system.loan.impl.PaymentApportionmentProcessor;
import com.profitera.services.system.loan.impl.PostingSchedule;
import com.profitera.services.system.loan.impl.PostingScheduleManager;
import com.profitera.services.system.loan.impl.ProjectedInstallment;
import com.profitera.services.system.loan.impl.PostingSchedule.PostingType;
import com.profitera.util.BigDecimalUtil;
import com.profitera.util.CollectionUtil;
import com.profitera.util.DateParser;
import com.profitera.util.Holder;

public class LoanAccount extends AbstractAccountSet implements ILoanInterestManager, IAccountSet {
  public static final String LEGACY_ACCOUNT_ID = "ACCOUNT_ID";
  private final class LoanPrincipalInstallmentManager implements
      ILoanPrincipalInstallmentManager {
    private static final String INSERT_INSTALLMENT_PRINCIPLE_POSTING_RECORD = "insertInstallmentPrinciplePostingRecord";
    //
    public BigDecimal getRemainingInstallmentPrincipalAmount(
        BigDecimal withholdAmount, Date date, IScheduleManager pInstSchedule, IReadWriteDataProvider p,
        ITransaction t) throws SQLException, AbortTransactionException {
      Date nextDue = pInstSchedule.getNextDueDate(date, p, t);
      //TODO: if we have no next due what is the right thing to do?
      // this result will end up returning a negative value, but
      // this might never get called in that case anyway.
      if (nextDue == null) {
        return BigDecimal.ZERO;
      }
      boolean isLumpSum = pInstSchedule.getCode().equals(PostingSchedule.getPostingTypeAccountCode(PostingType.PRINCIPAL_LUMP_SUM));
      BigDecimal installmentAmount = pInstSchedule.getPostingDueRateOnDate(nextDue, p, t);
      BigDecimal principalAmount = installmentAmount;
      if (!isLumpSum && 
          !BigDecimalUtil.isEqual(installmentAmount, BigDecimalUtil.ZERO)){
        // Here how do I know that the interest is already posted?
        BigDecimal interestAmount = getInterestAmountChargedForPeriod(date, p, t);
        if (interestAmount != null){
          principalAmount = principalAmount.subtract(interestAmount);
        }
      }
      BigDecimal alreadyAllocated = getInstallmentPrincipalAmountPaidForPeriod(date, pInstSchedule, p, t);
      if (alreadyAllocated != null){
        principalAmount = principalAmount.subtract(alreadyAllocated);
      }
      principalAmount = principalAmount.subtract(withholdAmount);
      final Account principal1 = getSetFinancialAccount(getAccountType(IAccountTypes.PRINCIPAL, p, t), p, t);
      Account principal = principal1;
      BigDecimal principalBalance = getAccountPostedBalance(principal, p);
      if (principalBalance.compareTo(principalAmount) < 0){
        principalAmount = principalBalance.compareTo(BigDecimalUtil.ZERO) > 0 ? principalBalance : BigDecimalUtil.ZERO;
      }
      return principalAmount;
    }

    public BigDecimal getInstallmentPrincipalAmountPaidForPeriod(Date dueDate, IScheduleManager schedule, IReadWriteDataProvider p, ITransaction t) throws SQLException {
      Date[] period = schedule.getBillingPeriod(dueDate, p, t);
      if (period == null) {
        throw getBadScheduleRequestException(dueDate, schedule);
      }
      Map<String, Object> args = new HashMap<String, Object>();
      args.put("LEDGER_ID", getId());
      args.put(LEGACY_ACCOUNT_ID, getId());
      args.put("DUE_DATE", dueDate);
      args.put("START_DATE", period[0]);
      args.put("END_DATE", period[1]);
      args.put("SOURCE", schedule.getCode());
      return (BigDecimal) p.queryObject("getLoanInstallmentPrincipalAmount", args);
    }

    public BigDecimal postAllRemainingPrincipalInstallmentPosting(BigDecimal advanceInterestAmount, Date date, Date effectiveDate, IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException{
      return post(advanceInterestAmount, PostingType.PRINCIPAL_INSTALLMENT, date, effectiveDate, p, t);
    }

    public BigDecimal postAllRemainingPrincipalLumpSumPosting(BigDecimal withholdAmount, Date date, Date effectiveDate, IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException{
      return post(withholdAmount, PostingType.PRINCIPAL_LUMP_SUM, date, effectiveDate, p, t);
    }

    private BigDecimal post(BigDecimal advanceInterestAmount,
        PostingType postingType, Date date, Date effectiveDate,
        IReadWriteDataProvider p, ITransaction t) throws SQLException,
        AbortTransactionException {
      IScheduleManager schedule = getPostingScheduleManager(postingType, p, t);
      BigDecimal principalAmount = getRemainingInstallmentPrincipalAmount(
          advanceInterestAmount, date, schedule, p, t);
      Date endOfPeriod = date;
      Date[] period = schedule.getBillingPeriod(date, p, t);
      if (period == null) {
        throw getBadScheduleRequestException(date, schedule);
      }
      Date startOfPeriod = period[0];
      final Account principal = getSetFinancialAccount(getAccountType(IAccountTypes.PRINCIPAL, p, t), p, t);
      Account sourceAccount = principal;
      Date dueDate = period[1];
      BigDecimal val = postPrincipalInstallmentPosting(sourceAccount, principalAmount,
          //The "billing date" is usually going to be equal to the endOfPeriod
          date, startOfPeriod, endOfPeriod, dueDate, effectiveDate, postingType, p, t);
      updateAccountAging(effectiveDate, p, t);
      return val;
    }
    
    public BigDecimal postPrincipalInstallmentPosting(Account sourceAccount,
        BigDecimal principalAmount, Date date, Date startOfPeriod,
        Date endOfPeriod, Date billingDate, Date effectiveDate, PostingType pt, IReadWriteDataProvider p,
        ITransaction t) throws AbortTransactionException, SQLException {
      final Account installmentPrincipal = getSetFinancialAccount(getAccountType(IAccountTypes.PINST, p, t), p, t);
      Account princOS = installmentPrincipal;
      Split s1 = new Split(sourceAccount, principalAmount.multiply(BigDecimalUtil.NEG_ONE), null, null);
      Split s2 = new Split(princOS, principalAmount, null, null);
      Transaction tran = new Transaction(princOS.getCommodity(), new Split[]{s1, s2});
      Transaction posted = postTransactionInternal(tran, date, p, t);
      String code = PostingSchedule.getPostingTypeAccountCode(pt);
      insertPostingHistory(INSERT_INSTALLMENT_PRINCIPLE_POSTING_RECORD, 
          startOfPeriod, endOfPeriod, billingDate, code, posted.getId(), principalAmount, effectiveDate, p, t);
      updateAccountAging(effectiveDate, p, t);
      return principalAmount;
    }

    public boolean isPostResolution(Date effective, IScheduleManager m110, IScheduleManager m100,
        IReadWriteDataProvider p, ITransaction t) throws SQLException {
      return LoanAccount.this.isPostResolution(effective, m110, m100, p, t);
    }
  }

  private static final String INSERT_INTEREST_POSTING_RECORD = "insertInterestPostingRecord";
  private static final String INSERT_DEFERRED_INTEREST_POSTING_RECORD = "insertDeferredInterestPostingRecord";
  private static final String INSERT_PENALTY_POSTING_RECORD = "insertPenaltyPostingRecord";
  
  private Date accountsCreatedDate = null;
  private Boolean isNPA = null;
  private final boolean isNewApportionmentMode;
  private static Log LOG;
  
  public LoanAccount(Long id, IGeneralAccountService g, boolean isNeverWitholdPrincipalAccounts) {
    super(id, g);
    isNewApportionmentMode = isNeverWitholdPrincipalAccounts;
    
  }
    
  public Date getLoanFinancialAccountsCreatedDate(final IReadOnlyDataProvider p) throws SQLException {
    if (accountsCreatedDate == null) {
      Date createdDate = (Date) p.queryObject("getLoanFinancialAccountsLinkCreatedDate", getId());
      accountsCreatedDate = createdDate == null ? null : DateParser.getStartOfDay(createdDate);
    }
    return accountsCreatedDate;
  }
  
  
  protected Long getAccountSetAccountId(AccountType t, IReadOnlyDataProvider p, ITransaction tr)
      throws SQLException {
    Map<String, Object> args = new HashMap<String, Object>();
    args.put("LEDGER_ID", getId());
    args.put(LEGACY_ACCOUNT_ID, getId());
    args.put("ACCOUNT_TYPE_ID", t.getId());
    return (Long) p.queryObject("getLoanFinancialAccountId", args);
  }
  
  public Transaction getSimpleTransaction(Account equityAccount,
      AccountType accountAccountType, BigDecimal amt, IReadWriteDataProvider p,
      ITransaction t) throws AbortTransactionException, SQLException {
    if (equityAccount == null) throw new IllegalArgumentException("A source account is required for a simple transaction");
    if (accountAccountType == null) throw new IllegalArgumentException("No account type provided for simple transaction");
    if (amt == null) throw new IllegalArgumentException("An amount is required to create this transaction");
    Account credit = getSetFinancialAccount(accountAccountType, p, t);
    Split eqDebit = new Split(equityAccount, amt.multiply(BigDecimalUtil.NEG_ONE), null, null);
    Split loanCredit = new Split(credit, amt, null, null);
    return new Transaction(credit.getCommodity(), new Split[]{eqDebit, loanCredit});
  }
  
  public boolean isNonPerforming(Date effectiveDate, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    if (isNPA == null) {
      Map<String, Object> args = new HashMap<String, Object>();
      args.put("LEDGER_ID", getId());
      args.put(LEGACY_ACCOUNT_ID, getId());
      args.put("EFFECTIVE_DATE", effectiveDate);
      Boolean answer = null;
      IScheduleManager m110 = getPostingScheduleManager(PostingType.PRINCIPAL_INSTALLMENT, p, t);
      IScheduleManager m100 = getPostingScheduleManager(PostingType.PRINCIPAL_LUMP_SUM, p, t);
      if (isPostResolution(effectiveDate, m110, m100, p, t)){
        answer = (Boolean) p.queryObject("getLoanInstallmentPlanNonPerformingStatus", args);
      } else {
        answer = (Boolean) p.queryObject("getLoanNoInstallmentPlanNonPerformingStatus", args);
      }
      isNPA = answer == null ? Boolean.FALSE : answer; 
    }
    return isNPA.booleanValue();
  }

  public boolean isPostResolution(Date effective, IScheduleManager m110, IScheduleManager m100, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    try {
      Map<String, Object> args = new HashMap<String, Object>();
      args.put("LEDGER_ID", getId());
      args.put(LEGACY_ACCOUNT_ID, getId());
      args.put("EFFECTIVE_DATE", effective);
      Boolean answer = (Boolean) p.queryObject("getLoanPostResolutionStatus", args);      
      if (answer != null) {
        return answer; 
      }
    } catch (NoSuchStatementException e) {
      // Do nothing, fall back to default behaviour
    }
    Date d = m110.getFirstScheduleStartDate(p, t);
    if (d != null && !d.after(effective)) {
      return true;
    } else {
      d = m100.getFirstScheduleStartDate(p, t);
      return d != null && !d.after(effective);
    }
  }
  
  public Transaction apportionPayment(Account generalPaymentAccount, BigDecimal paymentAmount,
      Date paymentDate, Date effectiveDate, String[] interestBalanceAccountTypes, IReadWriteDataProvider p,
      ITransaction t) throws AbortTransactionException, SQLException {
    IKnockoffScheduleManager k;
    k = new DefaultKnockoffScheduleManager(getAccountTypeProvider(), effectiveDate);
    return apportionPayment(generalPaymentAccount, paymentAmount, paymentDate, effectiveDate, k, interestBalanceAccountTypes, p, t);
  }
  public Transaction apportionPayment(Account generalPaymentAccount, BigDecimal paymentAmount,
      Date paymentDate, Date effectiveDate, IKnockoffScheduleManager k, String[] interestBalanceAccountTypes, IReadWriteDataProvider p,
      ITransaction t) throws AbortTransactionException, SQLException {
    Account pAccount = getSetFinancialAccount(getAccountType(IAccountTypes.PRINCIPAL, p, t), p, t);
    Account iPAccount = getSetFinancialAccount(getAccountType(IAccountTypes.PINST, p, t), p, t);
    IAccountTypeProvider atp = getAccountTypeProvider();
    IScheduleManager postingManager110 = getPostingScheduleManager(PostingType.PRINCIPAL_INSTALLMENT, p, t);
    IScheduleManager postingManager100 = getPostingScheduleManager(PostingType.PRINCIPAL_LUMP_SUM, p, t);
    PaymentApportionmentProcessor processor = 
    new PaymentApportionmentProcessor(this, getLoanPrincipalInstallmentManager(), this, 
        postingManager110, postingManager100, getPostingScheduleManager(PostingType.IMMEDIATE_I, p, t), 
        pAccount, getImmediateInterestAccrualInstance(interestBalanceAccountTypes, p, t), iPAccount, k, atp, isNewApportionmentMode());
    return processor.apportionPayment(generalPaymentAccount, paymentAmount, paymentDate, effectiveDate, true, interestBalanceAccountTypes, p, t);
  }

  private AccountType getAccountType(String code,
      IReadWriteDataProvider p, ITransaction t) throws SQLException {
    return getAccountTypeProvider().get(code, p, t);
  }

  public ILoanPrincipalInstallmentManager getLoanPrincipalInstallmentManager() {
    return new LoanPrincipalInstallmentManager();
  }
    
  public Account getLedgerAccount(Account suspenseAccount,
      IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException {
    AccountType[] suspenseTypes = {
        getAccountType(IAccountTypes.IIS, p, t), getAccountType(IAccountTypes.CIS, p, t), getAccountType(IAccountTypes.PIS, p, t), getAccountType(IAccountTypes.CUMULATIVE_IIS, p, t)};
    AccountType[] ledgerTypes = {
        getAccountType(IAccountTypes.INTEREST, p, t), getAccountType(IAccountTypes.CHARGE, p, t), getAccountType(IAccountTypes.PENALTY, p, t), getAccountType(IAccountTypes.CUMULATIVE_INT, p, t)};
    AccountType type = suspenseAccount.getType();
    for (int i = 0; i < suspenseTypes.length; i++) {
      if (suspenseTypes[i].equals(type)) {
        return getSetFinancialAccount(ledgerTypes[i], p, t);
      }
    }
    return null;
  }
  public boolean isSuspense(Split split, IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException {
    return isSuspense(split.getAccount(), p, t);
  }
  private boolean isSuspense(Account account, IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException {
    return getLedgerAccount(account, p, t) != null;
  }
  
  private InterestAccrual getImmediateInterestAccrualInstance(String[] balanceAccounts,
      IReadWriteDataProvider p, ITransaction t)
      throws AbortTransactionException, SQLException {
    if (balanceAccounts == null) {
      balanceAccounts = new String[]{IAccountTypes.PRINCIPAL, IAccountTypes.PINST};
    }
    List<Account> accounts = new ArrayList<Account>();
    for (int i = 0; i < balanceAccounts.length; i++) {
      accounts.add(getSetFinancialAccount(getAccountType(balanceAccounts[i], p, t), p, t));
    }
    InterestAccrual process = new InterestAccrual(getId(), buildLoanInterest(PostingType.IMMEDIATE_I, p, t), accounts.toArray(new Account[0]));
    return process;
  }
  
  private InterestAccrual getDeferredInterestAccrualInstance(
      IReadWriteDataProvider p, ITransaction t)
      throws AbortTransactionException, SQLException {
    final Account principal = getSetFinancialAccount(getAccountType(IAccountTypes.PRINCIPAL, p, t), p, t);
    final Account installmentPrincipal = getSetFinancialAccount(getAccountType(IAccountTypes.PINST, p, t), p, t);
    InterestAccrual process = new InterestAccrual(getId(), buildLoanInterest(PostingType.DEFERRED_I, p, t), new Account[]{principal, installmentPrincipal});
    return process;
  }
  
  private Transaction postDeferredInterestAccrual(Date endOfPeriod,
      Date effectiveDate, IScheduleManager m, IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    Account genInterest = getGeneralAccount(getAccountType(isNonPerforming(effectiveDate, p, t) ? IAccountTypes.IIS : IAccountTypes.INTEREST, p, t), p, t);
    // For deferred interest the last date is always the start of the billing period
    final Date startOfPeriod = m.getBillingPeriod(endOfPeriod, p, t)[0];
    InterestAccrual process = getDeferredInterestAccrualInstance(p, t); 
    Transaction transaction = getDeferredAccrualTransactionForPeriod(startOfPeriod, endOfPeriod, effectiveDate, genInterest, p, t, process);
    Transaction posted = postTransactionInternal(transaction, effectiveDate, p, t);
    recordInterestPostingHistory(false, genInterest, posted, startOfPeriod,
        endOfPeriod, endOfPeriod, effectiveDate, p, t);
    updateAccountAging(effectiveDate, p, t);
    return posted;

  }

  
  public Transaction postImmediateInterestAccrual(Date endOfPeriod,
      Date effectiveDate, String[] balanceAccounts, IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    Account genInterest = getGeneralAccount(getAccountType(isNonPerforming(effectiveDate, p, t) ? IAccountTypes.IIS : IAccountTypes.INTEREST, p, t), p, t);
    final Date startOfPeriod = DateParser.getNextDay(getLastImmediateInterestPostedDate(p, t));
    InterestAccrual process = getImmediateInterestAccrualInstance(balanceAccounts, p, t); 
    Transaction transaction = getImmediateAccrualTransactionForPeriod(startOfPeriod, endOfPeriod, effectiveDate, genInterest, p, t, process);
    Transaction posted = postTransactionInternal(transaction, effectiveDate, p, t);
    IScheduleManager immediatePosting = getPostingScheduleManager(PostingType.IMMEDIATE_I, p, t);
    recordInterestPostingHistory(true, genInterest, posted, startOfPeriod,
        endOfPeriod, immediatePosting.getNextDueDate(endOfPeriod, p, t), effectiveDate, p, t);
    updateAccountAging(effectiveDate, p, t);
    return posted;
  }
  
  private void updateAccountAging(final Date effectiveDate, final IReadWriteDataProvider p, ITransaction t) throws SQLException {
    @SuppressWarnings("unchecked")
    Iterator<Object> balances = p.query(IReadOnlyDataProvider.LIST_RESULTS, "getFinancialAccountPostedBalancesForLoan", getId());
    List<Object> data = CollectionUtil.asList(balances);
    
    AgeCalculator calc = new AgeCalculator(getId(), data, new LoanInstallmentProvider(getId(), effectiveDate, p));
    IScheduleManager pInstPostingManager = getPostingScheduleManager(PostingType.PRINCIPAL_INSTALLMENT, p, t);
    IScheduleManager immediateInterestPostingManager = getPostingScheduleManager(PostingType.IMMEDIATE_I, p, t);
    IScheduleManager deferredTransferInterestPostingManager = getPostingScheduleManager(PostingType.DEFERRED_T, p, t);
    Date regAge = calc.getOldestOverdueDate();
    Date suspenseAge = calc.getSuspenseOldestOverdueDate();
    Date nextPrincipalDue = pInstPostingManager.getNextDueDate(effectiveDate, p, t);
    Date nextInterestDue = immediateInterestPostingManager.getNextDueDate(effectiveDate, p, t);
    Date nextDeferredPosting = deferredTransferInterestPostingManager.getNextDueDate(effectiveDate, p, t);
    if (nextDeferredPosting != null && (nextInterestDue == null || nextInterestDue.after(nextDeferredPosting))){
      nextInterestDue = nextDeferredPosting;
    }
    Map<String, Object> args = new HashMap<String, Object>();
    args.put("LEDGER_ID", getId());
    args.put(LEGACY_ACCOUNT_ID, getId());
    args.put("OLDEST_OVERDUE_DATE", regAge);
    args.put("SUSPENSE_OLDEST_OVERDUE_DATE", suspenseAge);
    args.put("NEXT_DUE_DATE", nextPrincipalDue);
    int count = p.update("updateLoanAgeInformation", args, t);
    if (count == 0) {
      p.insert("insertLoanAgeInformation", args, t);
    }
  }
  
  public void recordInterestPostingHistory(boolean isImmediate, Account genInterest,
      Transaction posted, final Date startOfPeriod, Date endOfPeriod, Date billingDate,
      Date effectiveDate, IReadWriteDataProvider p, ITransaction t)
      throws SQLException {
    BigDecimal amount = posted.getAccountSplits(genInterest)[0].getAmount();
    BigDecimal interestAmount = amount.multiply(BigDecimalUtil.NEG_ONE);
    String statement = isImmediate ? INSERT_INTEREST_POSTING_RECORD : INSERT_DEFERRED_INTEREST_POSTING_RECORD;
    String code = isImmediate ? PostingSchedule.getPostingTypeAccountCode(PostingType.IMMEDIATE_I) 
        : PostingSchedule.getPostingTypeAccountCode(PostingType.DEFERRED_I);
    insertPostingHistory(statement, startOfPeriod, endOfPeriod, billingDate, code,
        posted.getId(), interestAmount, effectiveDate, p, t);
  }
  
  public void recordPenaltyPostingHistory(Account source,
      Transaction posted, final Date startOfPeriod, Date endOfPeriod, Date billingDate,
      Date effectiveDate, IReadWriteDataProvider p, ITransaction t)
      throws SQLException {
    BigDecimal amount = posted.getAccountSplits(source)[0].getAmount();
    BigDecimal interestAmount = amount.multiply(BigDecimalUtil.NEG_ONE);
    insertPostingHistory(INSERT_PENALTY_POSTING_RECORD, startOfPeriod, endOfPeriod, billingDate, 
        PostingSchedule.getPostingTypeAccountCode(PostingType.PENALTY), posted.getId(), 
        interestAmount, effectiveDate, p, t);
  }
  
  private void insertPostingHistory(String statement, Date start, Date end,
      Date billingEndDate, String sourceCode, Long transactionId, BigDecimal amount,
      Date effectiveDate, IReadWriteDataProvider p, ITransaction t)
      throws SQLException {
    Map<String, Object> args = new HashMap<String, Object>();
    args.put("AMOUNT", amount);
    args.put("EFFECTIVE_DATE", effectiveDate);
    args.put("START_DATE", start);
    args.put("END_DATE", end);
    args.put("TRANSACTION_ID", transactionId);
    args.put("LEDGER_ID", getId());
    args.put(LEGACY_ACCOUNT_ID, getId());
    args.put("BILLING_DATE", billingEndDate);
    args.put("SOURCE", sourceCode);
    p.insert(statement, args, t);
  }

  public String getInterestAccrualTargetAccount(Date effectiveDate, Date endOfPeriod, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    if (isNonPerforming(effectiveDate, p, t)){
      return IAccountTypes.IIS;
    } else {
      return IAccountTypes.INTEREST;
    }
  }
  
  public String getDeferredInterestAccrualTargetAccount(Date effectiveDate, Date endOfPeriod, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    if (isNonPerforming(effectiveDate, p, t)){
      return IAccountTypes.CUMULATIVE_IIS;
    } else {
      return IAccountTypes.CUMULATIVE_INT;
    }
  }
  public Date getLastImmediateInterestPostedDate(IReadOnlyDataProvider p, ITransaction t) throws SQLException{
    Date lastPosted = (Date) p.queryObject("getLastImmediateInterestPostedDate", getId());
    if (lastPosted == null) { 
      return getLoanFinancialAccountsCreatedDate(p);
    } else {
      return lastPosted;
    }
  }

  public Date getLastPenaltyPostingDate(IReadWriteDataProvider p, ITransaction t) throws SQLException {
    Date lastPosted = (Date) p.queryObject("getLastPenaltyPostedDate", getId());
    if (lastPosted == null) { 
      return getLoanFinancialAccountsCreatedDate(p);
    } else {
      return lastPosted;
    }
  }
  
  private Date getLastDeferredInterestPostedDate(IReadOnlyDataProvider p) throws SQLException{
    Date lastPosted = (Date) p.queryObject("getLastDeferredInterestPostedDate", getId());
    if (lastPosted == null) { 
      return null;
    } else {
      return lastPosted;
    }
  }

  public BigDecimal getInterestAmountChargedForPeriod(Date dueDate, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    //TODO: Consider caller-supplied schedule manager
    IScheduleManager manager = getPostingScheduleManager(PostingType.IMMEDIATE_I, p, t);
    Date[] period = manager.getBillingPeriod(dueDate, p, t);
    if (period == null) {
      return null;
    }
    Map<String, Object> args = new HashMap<String, Object>();
    args.put(LEGACY_ACCOUNT_ID, getId());
    args.put("LEDGER_ID", getId());
    args.put("DUE_DATE", dueDate);
    args.put("START_DATE", period[0]);
    args.put("END_DATE", period[1]);
    return (BigDecimal) p.queryObject("getLoanInterestAmount", args);
  }
  
  private IllegalArgumentException getBadScheduleRequestException(Date dueDate,
      IScheduleManager schedule) {
    return new IllegalArgumentException("Attempted to retrieve billing period for account " + getId() + " schedule " + schedule.getCode() + " on date " + dueDate + " which is out of range");
  }

  
  public void postPrincipalInstallmentPosting(Date date, Date effectiveDate, String[] interestBalanceAccountTypes, IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException{
    IScheduleManager immediateInterest = getPostingScheduleManager(PostingType.IMMEDIATE_I, p, t);
    //Here we get the interest amount that is not yet posted
    // against immediate interest accounts because the due
    // date has not yet arrived
    BigDecimal advance = BigDecimal.ZERO;
    {
      Date[] iDue = immediateInterest.getBillingPeriod(date, p, t);
      if (iDue != null && !iDue[1].equals(date)){
        // get immediate interest for period up to date
        InterestAccrual accrual = getImmediateInterestAccrualInstance(interestBalanceAccountTypes, p, t);
        BigDecimal interestAmount = accrual.getAccrualAmount(iDue[0], date, p, t);
        advance = advance.add(interestAmount);
      }
    }
    // Here we do not check the deferred interest to-date because
    // it will not be due, even if it is to become due soon we
    // still should not consider it until that time
    getLoanPrincipalInstallmentManager().postAllRemainingPrincipalInstallmentPosting(advance, date, effectiveDate, p, t);
    updateAccountAging(effectiveDate, p, t);
  }
  
  
  public Transaction postTransaction(Transaction trans, Date effectiveDate, 
      IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    Transaction posted = postTransactionInternal(trans, effectiveDate, p, t);
    updateAccountAging(effectiveDate, p, t);
    return posted;
  }
  public Transaction postTransaction(long id, Date effectiveDate, 
      IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    TransactionQuery tq = new TransactionQuery();
    Transaction trans = tq.getTransaction(id, new AccountAction(), p, t);
    return postTransaction(trans, effectiveDate, p, t);
  }
  
    
  public List<ProjectedInstallment> getProjectedInstallments(List<ProjectedInstallment> recentInstallments, IReadWriteDataProvider p, final ITransaction t) throws AbortTransactionException, SQLException {
    final IScheduleManager pInstManager = getPostingScheduleManager(PostingType.PRINCIPAL_INSTALLMENT, p, t);
    final IScheduleManager pLumpSumManager = getPostingScheduleManager(PostingType.PRINCIPAL_LUMP_SUM, p, t);
    final IScheduleManager immediateIntManager = getPostingScheduleManager(PostingType.IMMEDIATE_I, p, t);
    final Account principal = getSetFinancialAccount(getAccountType(IAccountTypes.PRINCIPAL, p, t), p, t);
    Account principalAccount = principal;
    final Account installmentPrincipal = getSetFinancialAccount(getAccountType(IAccountTypes.PINST, p, t), p, t);
    Account ipAccount = installmentPrincipal;
    // interest is posted up to the date we will now use as the start date
    // The 110 schedule just posts on a particular date so it SHOULD not
    // be an issue unless the 110 or 100 schedules have an unposted installment
    // on the date that the 200 schedule is posted up to.
    Date startDate = getLastImmediateInterestPostedDate(p, t);
    Date nextDay = startDate; //DateParser.getNextDay(startDate);
    InstallmentProjector proj = new InstallmentProjector(principalAccount.getCommodity(), pInstManager, pLumpSumManager, immediateIntManager, nextDay);
    PeriodicBalance principalBalance = new AccountHistory(new Account[]{principalAccount, ipAccount}).getAccountBalance(getLoanFinancialAccountsCreatedDate(p), DateParser.getNextDay(startDate), p);
    if (principalBalance.getLastBalance().compareTo(BigDecimal.ZERO) > 0) {
      return proj.getFutureInstallments(principalBalance, recentInstallments, p, t);
    } else {
      return proj.getFutureInstallments(principalBalance, recentInstallments, 10, p, t);
    }
  }
  
  private LoanInterest buildLoanInterest(PostingType pType, IReadWriteDataProvider p,
      final ITransaction t) throws SQLException {
    IScheduleManager mgr = getPostingScheduleManager(pType, p, t);
    LoanInterest loanInterest = new LoanInterest(mgr);
    return loanInterest;
  }
  
  public IScheduleManager getPostingScheduleManager(PostingType pType,
      IReadWriteDataProvider p, final ITransaction t) throws SQLException {
    IScheduleManager mgr = new PostingScheduleManager(getId(), getLoanFinancialAccountsCreatedDate(p), pType, t);
    return mgr;
  }
  
  private Log getLog() {
    if (LOG == null) {
      LOG = LogFactory.getLog(getClass());
    }
    return LOG;
  }
  
  public IRunnableTransaction getAccrualTransaction(final Date date, final String[] balanceTypes, final Holder<Transaction> h, final IReadWriteDataProvider p) {
    final Date effective = DateParser.getStartOfDay(date);
    return new IRunnableTransaction(){
      public void execute(ITransaction t) throws SQLException,
          AbortTransactionException {
        IScheduleManager immediatePosting = getPostingScheduleManager(PostingType.IMMEDIATE_I, p, t);
        IScheduleManager deferredPosting = getPostingScheduleManager(PostingType.DEFERRED_I, p, t);
        IScheduleManager deferredTransfer = getPostingScheduleManager(PostingType.DEFERRED_T, p, t);
        boolean isNew = false;
        BigDecimal postingToImmediate = null;
        // We may be at the start of the new cycle, so we use the previous day
        Date[] currentPeriod = immediatePosting.getBillingPeriod(effective, p, t);
        Date[] deferredCurrentPeriod = deferredPosting.getBillingPeriod(effective, p, t);
        if (currentPeriod == null && deferredCurrentPeriod == null){
          getLog().info("Account interest accrual is not required: " + getId() + " (" + effective + ")");
          return;
        }
        Date lastAccrualDate = (Date) p.queryObject("getInterestLastAccruedDate", getId());
        if (lastAccrualDate == null){
          isNew = true;
          if (currentPeriod != null) {
            lastAccrualDate = currentPeriod[0];  
          }
          if (deferredCurrentPeriod != null && (lastAccrualDate == null || lastAccrualDate.after(deferredCurrentPeriod[0]))){
            lastAccrualDate = deferredCurrentPeriod[0];
          }
        }
        if (lastAccrualDate.compareTo(effective) > 0){
          getLog().info("Account interest accrual is indicated as already current for " + getId() + " as of " + lastAccrualDate + "(" + effective + ")");
        }
        BigDecimal immediateAccrualAmount = BigDecimal.ZERO;
        if (currentPeriod != null) {
          Date startDate = DateParser.getNextDay(getLastImmediateInterestPostedDate(p, t));
          startDate = DateParser.getStartOfDay(startDate);
          getLog().debug("Immediate interest already accrued up to (excluding) " + startDate + " for " + getId());
          // if the interest for the period has already been posted to the effective
          // date then the remaining to accrue is 0
          if (startDate.compareTo(effective) <= 0){
            Date nextFromStart = immediatePosting.getNextDueDate(startDate, p, t);
            Date nextFromEnd = immediatePosting.getNextDueDate(effective, p, t);
            if (!nextFromStart.equals(nextFromEnd)){
              getLog().error(startDate + " -> " + nextFromStart + " / " + effective + " -> " + nextFromEnd + " for account " + getId());
              throw new AbortTransactionException("Can not accrue interest across due date for period from " + startDate + " to " + effective + " (" + nextFromStart + ")");
            }
            if (nextFromEnd.equals(effective)){
              Transaction ii = postImmediateInterestAccrual(effective, effective, balanceTypes, p, t);
              h.set(ii);
              postingToImmediate = ii.getSplits()[0].getExchangedAmount().abs();
            } else {
              InterestAccrual ia = getImmediateInterestAccrualInstance(balanceTypes, p, t);
              BigDecimal amount = ia.getAccrualAmount(startDate, effective, p, t);
              immediateAccrualAmount = amount;
            }
          }
        }
        BigDecimal deferredAccrualAmount = BigDecimal.ZERO;
        if (deferredCurrentPeriod != null) {
          Date startDate = getLastDeferredInterestPostedDate(p);
          if (startDate == null) {
            startDate = deferredCurrentPeriod[0];
          } else {
            startDate = DateParser.getNextDay(startDate);
          }
          startDate = DateParser.getStartOfDay(startDate);
          getLog().debug("Deferred interest already accrued up to (excluding) " + startDate + " for " + getId());
          if (startDate.compareTo(effective) <= 0){
            Date nextFromStart = deferredPosting.getNextDueDate(startDate, p, t);
            Date nextFromEnd = deferredPosting.getNextDueDate(effective, p, t);
            if (!nextFromStart.equals(nextFromEnd)){
              getLog().error(startDate + " -> " + nextFromStart + " / " + effective + " -> " + nextFromEnd + " for account " + getId());
              throw new AbortTransactionException("Can not accrue interest across due date for period from " + startDate + " to " + effective + " (" + nextFromStart + ")");
            }
            if (nextFromEnd.equals(effective)){
              Transaction ii = postDeferredInterestAccrual(effective, effective, deferredPosting, p, t);
              h.set(ii);
            } else {
              InterestAccrual ia = getDeferredInterestAccrualInstance(p, t);
              BigDecimal amount = ia.getAccrualAmount(startDate, effective, p, t);
              deferredAccrualAmount = amount;
            }
          }
        }
        {
          //get next transfer date for deferred and see if we have
          // matched it, the rate is the percentage to post across from
          // the 210 account to the 200 and 720 to 721
          Date nextTransferDate = deferredTransfer.getNextDueDate(effective, p, t);
          if (nextTransferDate != null && nextTransferDate.equals(effective)) {
            IScheduleManager postingSchedule = getPostingScheduleManager(PostingType.DEFERRED_T, p, t);
            BigDecimal rate = postingSchedule.getPostingDueRateOnDate(effective, p, t);
            Account account210 = getSetFinancialAccount(getAccountType(IAccountTypes.CUMULATIVE_INT, p, t), p, t);
            BigDecimal balance210 = getAccountPostedBalance(account210, p);
            Account account721 = getSetFinancialAccount(getAccountType(IAccountTypes.CUMULATIVE_IIS, p, t), p, t);
            BigDecimal balance721 = getAccountPostedBalance(account721, p);
            BigDecimal transfer210 = account210.getCommodity().scale(balance210.multiply(rate));
            BigDecimal transfer721 = account721.getCommodity().scale(balance721.multiply(rate));
            // If there is only 1 penny being left over by the transfer we will asssume it
            // is a rounding error and clear the designated account out.
            if (balance210.subtract(transfer210).compareTo(new BigDecimal("0.01")) <= 0){
              transfer210 = balance210;
            }
            if (balance721.subtract(transfer721).compareTo(new BigDecimal("0.01")) <= 0){
              transfer721 = balance721;
            }
            String targetAccountType = getInterestAccrualTargetAccount(effective, effective, p, t);
            Account targetAccount = getSetFinancialAccount(getAccountType(targetAccountType, p, t), p, t);
            List<Split> splits = new ArrayList<Split>();
            if (transfer210.compareTo(BigDecimal.ZERO) > 0) {
              splits.add(new Split(account210, BigDecimalUtil.NEG_ONE.multiply(transfer210), null, null));
            }
            if (transfer721.compareTo(BigDecimal.ZERO) > 0) {
              splits.add(new Split(account721, BigDecimalUtil.NEG_ONE.multiply(transfer721), null, null));
            }
            BigDecimal total = transfer721.add(transfer210);
            splits.add(new Split(targetAccount, total, null, null));
            Transaction trans = new Transaction(targetAccount.getCommodity(), splits.toArray(new Split[0]));
            trans = postTransactionInternal(trans, effective, p, t);
            h.set(trans);
            // Use the deferred transfer code
            String scheduleCode = PostingSchedule.getPostingTypeAccountCode(PostingType.DEFERRED_T);
            insertPostingHistory(INSERT_INTEREST_POSTING_RECORD, effective, effective, effective, scheduleCode,
                trans.getId(), total, effective, p, t);
            insertPostingHistory(INSERT_DEFERRED_INTEREST_POSTING_RECORD, effective, effective, effective, scheduleCode,
                trans.getId(), total.multiply(BigDecimalUtil.NEG_ONE), effective, p, t);
            postingToImmediate = total;
            updateAccountAging(effective, p, t);
          }
        }
        updateAccrualToDate(immediateAccrualAmount.add(deferredAccrualAmount), effective, isNew, p, t);
        if (postingToImmediate != null && postingToImmediate.compareTo(BigDecimal.ZERO) != 0) {
          // After all this is done we need to consider if apportioning payment is appropriate
          final Account overpayment = getSetFinancialAccount(getAccountType(IAccountTypes.OVERPAY, p, t), p, t);
          BigDecimal overpaymentBalance = getAccountPostedBalance(overpayment, p);
          if (overpaymentBalance.multiply(BigDecimalUtil.NEG_ONE).compareTo(BigDecimalUtil.ZERO) > 0){
            IScheduleManager s110 = getPostingScheduleManager(PostingType.PRINCIPAL_INSTALLMENT, p, t);
            Date next110 = s110.getNextDueDate(effective, p, t);
            if (next110 == null || !next110.equals(effective)) {
              IScheduleManager s100 = getPostingScheduleManager(PostingType.PRINCIPAL_LUMP_SUM, p, t);
              Date next100 = s100.getNextDueDate(effective, p, t);
              if (next100 == null || !next100.equals(effective)) {
                Transaction apportionPayment = apportionPayment(null, null, effective, effective, balanceTypes, p, t);
                postTransaction(apportionPayment, effective, p, t);
              }  
            }
          }
        }
      }};
  }
  
  private void updateAccrualToDate(BigDecimal totalAccrued, Date endDate, boolean isInsert,
      IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException {
    Map<String, Object> arguments = new HashMap<String, Object>();
    arguments.put("LEDGER_ID", getId());
    arguments.put(LEGACY_ACCOUNT_ID, getId());
    arguments.put("ACCRUED_AMOUNT", totalAccrued);
    arguments.put("EFFECTIVE_DATE", endDate);
    getLog().info("Total accrued to " + endDate + " for " + getId() + ": " + totalAccrued);
    if (isInsert){
      p.insert("insertInterestAccruedAmount", arguments, t);
    } else {
      p.update("updateInterestAccruedAmount", arguments, t);
    }
  }
  
  public Transaction getImmediateAccrualTransactionForPeriod(Date startDate, Date endDate,
      Date effectiveDate, Account generalInterestAccount,
      IReadWriteDataProvider p, ITransaction t, InterestAccrual ia) 
  throws AbortTransactionException, SQLException{
    Map<String, Object> arguments = new HashMap<String, Object>();
    arguments.put("LEDGER_ID", getId());
    arguments.put(LEGACY_ACCOUNT_ID, getId());
    arguments.put("EFFECTIVE_DATE", effectiveDate);
    p.update("clearInterestAccruedAmount", arguments, t);
    final Account interest = getSetFinancialAccount(getAccountType(getInterestAccrualTargetAccount(effectiveDate, endDate, p, t), p, t), p, t);
    return ia.getAccrualTransactionForPeriod(BigDecimal.ZERO, startDate, endDate, effectiveDate, interest, generalInterestAccount, p, t);
  }

  public Transaction getDeferredAccrualTransactionForPeriod(Date startDate, Date endDate,
      Date effectiveDate, Account generalInterestAccount,
      IReadWriteDataProvider p, ITransaction t, InterestAccrual ia) 
  throws AbortTransactionException, SQLException{
    Map<String, Object> arguments = new HashMap<String, Object>();
    arguments.put("LEDGER_ID", getId());
    arguments.put(LEGACY_ACCOUNT_ID, getId());
    arguments.put("EFFECTIVE_DATE", effectiveDate);
    p.update("clearInterestAccruedAmount", arguments, t);
    final Account loanSpecialInterest = getSetFinancialAccount(getAccountType(getDeferredInterestAccrualTargetAccount(effectiveDate, endDate, p, t), p, t), p, t);
    return ia.getAccrualTransactionForPeriod(BigDecimal.ZERO, startDate, endDate, effectiveDate, loanSpecialInterest, generalInterestAccount, p, t);
  }
  
  public ILoanInterest buildPenaltyLoanInterest(InterestType interestType, IScheduleManager manager, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    return new PenaltyLoanInterest(manager, interestType);
  }
  
  public Transaction reverseTransaction(Long id, Date date,
      IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    Transaction reverseTransactionInternal = reverseTransactionInternal(id, date, p, t);
    // TODO: For a payment this will not back out interest due posting 
    // so technically I imagine that is a bug.
    // It DOES reverse the payment and any redirection though
    //
    // We should update aging here, but right now we don't, afraid to add it
    //updateAccountAging(date, p, t);
    //
    return reverseTransactionInternal;
  }
  private boolean applyInstallmentPrincipalSchedule(Date evalDate, String[] interestBalanceAccountTypes,
      final IReadWriteDataProvider p, ITransaction t) throws SQLException,
      AbortTransactionException {
    IScheduleManager m = getPostingScheduleManager(PostingType.PRINCIPAL_INSTALLMENT, p, t);
    if (isDue(evalDate, m, p, t)){
      postPrincipalInstallmentPosting(evalDate, evalDate, interestBalanceAccountTypes, p, t);
      return true;
    } else {
      return false;
    }
  }
  private boolean applyLumpSumPrincipalSchedule(Date evalDate,
      final IReadWriteDataProvider p, ITransaction t) throws SQLException,
      AbortTransactionException {
    IScheduleManager m = getPostingScheduleManager(PostingType.PRINCIPAL_LUMP_SUM, p, t);
    if (isDue(evalDate, m, p, t)){
      getLoanPrincipalInstallmentManager().postAllRemainingPrincipalLumpSumPosting(BigDecimal.ZERO, evalDate, evalDate, p, t);
      return true;
    } else {
      return false;
    }
  }
  private boolean isDue(Date evalDate, IScheduleManager manager, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    final Date nextDue = manager.getNextDueDate(evalDate, p, t);
    if (nextDue == null || !nextDue.equals(evalDate)){
      String code = manager.getCode();
      getLog().info("Due date for loan account " + getId() + " is on " + nextDue + " for " + code + ", processing " + evalDate);
      return false;
    }
    return true;
  }

  public Transaction applyPrincipalInstallmentSchedules(Date evalDate, String[] interestBalanceAccountTypes, IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException {
    boolean postedInst = applyInstallmentPrincipalSchedule(evalDate, interestBalanceAccountTypes, p, t);
    boolean postedP = applyLumpSumPrincipalSchedule(evalDate, p, t);
    if (!postedInst && !postedP){
      return null;
    }
    // Now we apply the overpayment if available
    AccountType over = getAccountType(IAccountTypes.OVERPAY, p, t);
    Account overpay = getSetFinancialAccount(over, p, t);
    BigDecimal overpayAmount = getAccountPostedBalance(overpay, p);
    // Overpay balance is negative, means there is money to spend
    overpayAmount = overpayAmount.multiply(BigDecimalUtil.NEG_ONE);
    if (overpayAmount.compareTo(BigDecimalUtil.ZERO) > 0){
      Transaction apportionPayment = apportionPayment(null, null, evalDate, evalDate, interestBalanceAccountTypes, p, t);
      return postTransaction(apportionPayment, evalDate, p, t);
    }
    return null;
  }
  
  private boolean isNewApportionmentMode() {
    return isNewApportionmentMode;
  }


}
