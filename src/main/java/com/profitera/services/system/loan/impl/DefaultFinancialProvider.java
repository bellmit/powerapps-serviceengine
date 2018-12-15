package com.profitera.services.system.loan.impl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ISqlMapProvider;
import com.profitera.dataaccess.ITransaction;
import com.profitera.finance.InterestCalculator.InterestType;
import com.profitera.financial.IFinancialProvider;
import com.profitera.financial.PostingScheduleParseConfig;
import com.profitera.financial.TransactionParserConfig;
import com.profitera.log.DefaultLogProvider;
import com.profitera.log.ILogProvider;
import com.profitera.services.business.batch.financial.impl.GeneralAccountInitializer;
import com.profitera.services.business.batch.financial.impl.LoanAccountIntializer;
import com.profitera.services.business.batch.financial.impl.LoanPenaltyInterestProcessor;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.AccountType;
import com.profitera.services.system.financial.Commodity;
import com.profitera.services.system.financial.CommodityAction;
import com.profitera.services.system.financial.Transaction;
import com.profitera.services.system.loan.IAccountSet;
import com.profitera.services.system.loan.IAccountTypes;
import com.profitera.services.system.loan.IGeneralAccountService;
import com.profitera.services.system.loan.ILoanAccountService;
import com.profitera.services.system.loan.LedgerAccountSet;
import com.profitera.services.system.loan.LoanAccount;
import com.profitera.services.system.loan.LoanAccountService;
import com.profitera.services.system.loan.impl.PostingSchedule.PostingType;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.services.system.lookup.ServiceLookup;
import com.profitera.util.DateParser;
import com.profitera.util.Holder;

public class DefaultFinancialProvider implements IFinancialProvider {
  private ILogProvider accountSetLogProvider;
  private IGeneralAccountService generalAccountService;
  private final ThreadLocal<Stack<IAccountSet>> accountSets = 
    new ThreadLocal <Stack<IAccountSet>> () {
        @Override protected Stack<IAccountSet> initialValue() {
            return new Stack<IAccountSet>();
    }
  };

  public List<Map<String, Object>> getProjectedInstallments(String commodityCode, BigDecimal principal, Date startDate,
      Map<String, Object> target, Map<String, PostingScheduleParseConfig> configs, int paidCount, ISqlMapProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    IReadWriteDataProvider wrp = (IReadWriteDataProvider) p;
    Commodity c  = new CommodityAction().getCommodity(commodityCode, wrp, t);
    IScheduleManager s110 = getSchedule(PostingType.PRINCIPAL_INSTALLMENT, configs, startDate, target);
    IScheduleManager s100 = getSchedule(PostingType.PRINCIPAL_LUMP_SUM, configs, startDate, target);
    IScheduleManager s200 = getSchedule(PostingType.IMMEDIATE_I, configs, startDate, target);
    IScheduleManager s210 = getSchedule(PostingType.DEFERRED_I, configs, startDate, target);
    IScheduleManager s211 = getSchedule(PostingType.DEFERRED_T, configs, startDate, target);
    InstallmentProjector installmentProjector = new InstallmentProjector(c, s110, s100, s200, s210, s211, startDate);
    List<ProjectedInstallment> futureInstallments = installmentProjector.getFutureInstallments(principal, paidCount, wrp, t);
    List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
    for (ProjectedInstallment row : futureInstallments) {
      Map<String, Object> m = new HashMap<String, Object>();
      m.put("DATE", row.getDate());
      m.put("PERIOD_START_DATE", row.getPeriodStartDate());
      m.put("PRINCIPAL_AMOUNT", row.getPrincipal());
      m.put("INTEREST_AMOUNT", row.getInterest());
      m.put("INSTALLMENT_AMOUNT", row.getInstallmentAmount());
      m.put("PRINCIPAL_REMAINING", row.getPrincipalBalance());
      if (row.isPaid()){
        m.put("PRINCIPAL_DUE_AMOUNT", BigDecimal.ZERO);
        m.put("INTEREST_DUE_AMOUNT", BigDecimal.ZERO);
        //
        m.put("PRINCIPAL_PAID_AMOUNT", row.getPrincipal());
        m.put("INTEREST_PAID_AMOUNT", row.getInterest());
      } else {
        m.put("PRINCIPAL_DUE_AMOUNT", row.getPrincipal());
        m.put("INTEREST_DUE_AMOUNT", row.getInterest());
        //
        m.put("PRINCIPAL_PAID_AMOUNT", BigDecimal.ZERO);
        m.put("INTEREST_PAID_AMOUNT", BigDecimal.ZERO);
      }
      list.add(m);
    }
    return list;
  }

  private IScheduleManager getSchedule(PostingType principalInstallment,
      Map<String, PostingScheduleParseConfig> configs, Date start, Map<String, Object> target) throws AbortTransactionException {
    String code = PostingSchedule.getPostingTypeAccountCode(principalInstallment);
    PostingScheduleParseConfig config = configs.get(code);
    if (config == null) {
      return new NullScheduleManager();
    }
    PostingScheduleParser p = new PostingScheduleParser(config);
    return new LocalPostingScheduleManager(principalInstallment, p.parse(target), start);
  }

  private Stack<IAccountSet> getAccounts() {
    return accountSets.get();
  }
  
  private ILoanAccountService getLoanAccountService(){
    LookupManager instance = LookupManager.getInstance();
    final ILoanAccountService provider = (ILoanAccountService) instance.getLookupItem(LookupManager.SYSTEM, "LoanAccountService");
    if (provider == null) {
      return createLoanAccountService(instance);
    }
    return provider;
  }

  private ILoanAccountService createLoanAccountService(LookupManager instance) {
    ServiceLookup lookup = instance.getLookup(LookupManager.SYSTEM);
    LoanAccountService service = new LoanAccountService();
    lookup.setService("LoanAccountService", service);
    return service;
  }

  public long recordTransaction(Map<String, Object> target, TransactionParserConfig conf, Date date, ISqlMapProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    IGeneralAccountService service = getGeneralService();
    TransactionParser transactionParser = new TransactionParser(conf, service.getAccountTypeProvider());
    IAccountSet loan = getCurrentAccountSet();
    Transaction transaction = transactionParser.parseTransaction(getGeneralService(), loan, target, date, (IReadWriteDataProvider) p, t);
    String transDateKey = conf.getTransactionDateKey();
    Date transactionDate = TransactionParser.getTransactionDate(target, transDateKey, date);
    String actionField = conf.getActionField();
    boolean isPosting = TransactionParser.isPosting(target, actionField);
    transaction = loan.recordTransaction(transaction, date, transactionDate, isPosting, (IReadWriteDataProvider)p, t);
    return transaction.getId();
  }

  public void initGeneralAccounts(String[] typeCodes, String commodity, Date effectiveDate,
      ISqlMapProvider sqlMapProvider, ITransaction transaction)
      throws SQLException, AbortTransactionException {
    GeneralAccountInitializer i = new GeneralAccountInitializer();
    i.create(typeCodes, commodity, effectiveDate, getGeneralService().getAccountTypeProvider(), (IReadWriteDataProvider) sqlMapProvider, transaction);
  }

  private IAccountSet getCurrentAccountSet() {
    try {
      return getAccounts().peek();
    } catch (EmptyStackException e) {
      throw new IllegalStateException("Attempted to execute financial action without first referencing a current account set");
    }
  }

  public long reverseTransaction(Long id, Date date,
      ISqlMapProvider sqlMapProvider, ITransaction transaction)
      throws AbortTransactionException, SQLException {
    IAccountSet account = getCurrentAccountSet();
    Transaction reversalTransaction = account.reverseTransaction(id, date, (IReadWriteDataProvider) sqlMapProvider, transaction);
    return reversalTransaction.getId();
  }
  
  private IGeneralAccountService getGeneralService() {
    if (generalAccountService == null) {
      generalAccountService = new GeneralAccountService();
    }
    return generalAccountService;
  }
  
  public void initAccountSet(String comm, String[] types, Date effectiveDate,
      Map<String, Object> data, ISqlMapProvider sqlMapProvider, ITransaction transaction)
      throws AbortTransactionException, SQLException {
    IReadWriteDataProvider p = (IReadWriteDataProvider) sqlMapProvider;
    IAccountSet account = getCurrentAccountSet();
    if (account instanceof LoanAccount) {
      ILoanAccountService s = getLoanAccountService();
      LoanAccountIntializer intializer = new LoanAccountIntializer(comm, s, p , transaction);
      intializer.initializeLoan((LoanAccount) account, data, effectiveDate, s, p, transaction);
    } else {
      LedgerAccountSet a = (LedgerAccountSet) account;
      a.initLedgerAccountSet(comm, types, effectiveDate, p, transaction);
    }
  }
  
  public void initAccountSet(long id, AccountSetType type, ITransaction transaction) {
    if (isBadType(type)) {
      throw new IllegalArgumentException("Unknown account set type requested: " + type);
    }
    Stack<IAccountSet> accounts = getAccounts();
    if (type.equals(AccountSetType.LOAN)) {
      accounts.push(getLoanAccountService().getLoanAccount(id));
    } else {
      accounts.push(new LedgerAccountSet(id, getGeneralService()));
    }
  }
  
  public void releaseAccountSet(long id, AccountSetType type, ITransaction transaction) {
    if (isBadType(type)) {
      throw new IllegalArgumentException("Unknown account set type requested: " + type);
    }
    Stack<IAccountSet> accounts = getAccounts();
    // If we hit the error below the stack might be empty
    if (!accounts.isEmpty()) {
      IAccountSet pop = accounts.pop();
      if(!pop.getId().equals(id)) {
        // This means we have gotten ourselves into a real mess,
        // I have no idea how to recover from this properly but we will release
        // all the loans we have and throw an exception, the call stack should clear
        // the locks properly
        accounts.clear();
        throw new IllegalArgumentException("Release of " + type + " with id " + id + " failed, found " + pop.getId() + " instead");
      }
    } 
  }

  private boolean isBadType(AccountSetType type) {
    return !type.equals(AccountSetType.LOAN) && !type.equals(AccountSetType.LEDGER);
  }
  
  public List<Map<String, Object>> getLoanFormattedLedger(String eventName, String modeArg, 
      String statusArg, String firstTransactionArg, Map<String, Object> arguments, List<Map<String, Object>> splits) {
    String mode = (String) arguments.get(modeArg);
    if (mode == null) {
      throw new IllegalArgumentException("Loan ledger formatting mode " + modeArg + " not specified");
    }
    String status = (String) arguments.get(statusArg);
    LedgerFormatter ledgerFormatter = new LedgerFormatter(eventName, mode, status, firstTransactionArg, getAccountSetLogProvider());
    return ledgerFormatter.format(arguments, splits);
  }

  private ILogProvider getAccountSetLogProvider() {
    if (accountSetLogProvider == null) {
      accountSetLogProvider = new DefaultLogProvider();
      accountSetLogProvider.register(new LoanAccountSetLog());
    }
    return accountSetLogProvider;
  }
  @Override
  public BigDecimal accruePenaltyInterestToDate(Date effectiveDate, InterestType interestType, String[] subjectTypes, String[] balanceTypes,
      Integer graceDays, ISqlMapProvider sqlMapProvider, ITransaction transaction) throws AbortTransactionException, SQLException {
    IReadWriteDataProvider p = (IReadWriteDataProvider) sqlMapProvider;
    IAccountSet loan = getCurrentAccountSet();
    LoanPenaltyInterestProcessor pip = buildPenaltyInterestProcessor(subjectTypes, balanceTypes, graceDays, transaction, p);
    return pip.accruePenaltyInterestToDateWithoutPosting((LoanAccount) loan, effectiveDate, interestType, p, transaction);
  }

  @Override
  public Long accruePenaltyInterest(Date date, InterestType interestType, String[] subjectTypes, String[] balanceTypes, Integer graceDays, ISqlMapProvider sqlMapProvider, ITransaction transaction)
      throws AbortTransactionException, SQLException {
    IReadWriteDataProvider p = (IReadWriteDataProvider) sqlMapProvider;
    IAccountSet loan = getCurrentAccountSet();
    LoanPenaltyInterestProcessor pip = buildPenaltyInterestProcessor(subjectTypes, balanceTypes, graceDays, transaction, p);
    Transaction postingTransaction = pip.accruePenaltyInterest((LoanAccount) loan, interestType, date, p, transaction);
    if (postingTransaction == null) {
      return null;
    } else {
      return postingTransaction.getId();
    }
  }

  public LoanPenaltyInterestProcessor buildPenaltyInterestProcessor(String[] subjectTypes, String[] balanceTypes,
      Integer graceDays, ITransaction transaction, IReadWriteDataProvider p) throws SQLException,
      AbortTransactionException {
    IAccountTypeProvider typeProvider = getGeneralService().getAccountTypeProvider();
    AccountType p400 = typeProvider.get(IAccountTypes.PENALTY, p, transaction);
    AccountType p740 = typeProvider.get(IAccountTypes.PIS, p, transaction);
    Account g400 = getGeneralService().getGeneralAccount(p400, p, transaction);
    Account g740 = getGeneralService().getGeneralAccount(p740, p, transaction);
    LoanPenaltyInterestProcessor pip = new LoanPenaltyInterestProcessor(subjectTypes, balanceTypes, typeProvider, g400, g740);
    if (graceDays != null) {
      pip.setGraceDays(graceDays);
    }
    return pip;
  }
  @Override
  public Long accrueInterest(Date date, String[] subjectTypes, ISqlMapProvider sqlMapProvider, ITransaction transaction)
      throws AbortTransactionException, SQLException {
    IReadWriteDataProvider p = (IReadWriteDataProvider) sqlMapProvider;
    IAccountSet loan = getCurrentAccountSet();
    /*IAccountTypeProvider typeProvider = getGeneralService().getAccountTypeProvider();
    AccountType p200 = typeProvider.get(IAccountTypes.INTEREST, p, transaction);
    AccountType p720 = typeProvider.get(IAccountTypes.IIS, p, transaction);
    Account g200 = getGeneralService().getGeneralAccount(p200, p, transaction);
    Account g720 = getGeneralService().getGeneralAccount(p720, p, transaction);*/
    LoanAccount l = (LoanAccount) loan;
    final Holder<Transaction> h = new Holder<Transaction>();
    l.getAccrualTransaction(date, subjectTypes, h, p).execute(transaction);
    Transaction postingTransaction = h.get();
    if (postingTransaction == null) {
      return null;
    } else {
      return postingTransaction.getId();
    }
  }

  @Override
  public Long createPayment(Date effectiveDate, BigDecimal paymentAmount, Date paymentDate, String paymentCode, boolean post,
      String[] interestBalanceAccountTypes, ISqlMapProvider sqlMapProvider, ITransaction transaction) throws AbortTransactionException, SQLException {
    IReadWriteDataProvider p = (IReadWriteDataProvider) sqlMapProvider;
    LoanAccount loan = (LoanAccount) getCurrentAccountSet();
    AccountType code = getLoanAccountService().getAccountType(paymentCode, p, transaction);
    Account generalPaymentAccount = getGeneralService().getGeneralAccount(code, p, transaction);
    Transaction payment = loan.apportionPayment(generalPaymentAccount, paymentAmount, paymentDate, effectiveDate, interestBalanceAccountTypes, p, transaction);
    payment = loan.recordTransaction(payment, effectiveDate, paymentDate, post, p, transaction);
    return payment.getId();
  }

  @Override
  public Long postPrincipal(Date effectiveDate, String[] interestBalanceAccountTypes, ISqlMapProvider sqlMapProvider, ITransaction transaction)
      throws AbortTransactionException, SQLException {
    IReadWriteDataProvider p = (IReadWriteDataProvider) sqlMapProvider;
    LoanAccount loan = (LoanAccount) getCurrentAccountSet();
    Transaction t = loan.applyPrincipalInstallmentSchedules(effectiveDate, interestBalanceAccountTypes, p, transaction);
    if (t == null) {
      return null;
    }
    return t.getId();
  }

  @Override
  public void postTransaction(long id, Date date, boolean isPostingInterest, String[] interestBalanceAccountTypes, ISqlMapProvider sqlMapProvider, ITransaction transaction)
      throws AbortTransactionException, SQLException {
    IReadWriteDataProvider p = (IReadWriteDataProvider) sqlMapProvider;
    LoanAccount loan = (LoanAccount) getCurrentAccountSet();
    if (isPostingInterest) {
      loan.postImmediateInterestAccrual(DateParser.getPreviousDay(date), date, interestBalanceAccountTypes, p, transaction);
    }
    loan.postTransaction(id, date, p, transaction);
  }

  @Override
  public long addPostingSchedule(Map<String, Object> data, PostingScheduleParseConfig conf,
      ISqlMapProvider sqlMapProvider, ITransaction transaction) throws AbortTransactionException, SQLException {
    LoanAccount loan = (LoanAccount) getCurrentAccountSet();
    PostingScheduleParser parser = new PostingScheduleParser(conf);
    PostingType pType = parser.getScheduleType(data);
    PostingSchedule s = parser.parse(data);
    IReadWriteDataProvider p = (IReadWriteDataProvider) sqlMapProvider;
    IScheduleManager m = loan.getPostingScheduleManager(pType, p, transaction);
    return m.addPostingSchedule(s, p, transaction);
  }
}
