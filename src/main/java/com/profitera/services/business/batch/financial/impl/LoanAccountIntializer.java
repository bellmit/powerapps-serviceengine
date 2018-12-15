package com.profitera.services.business.batch.financial.impl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.financial.IFinancialProvider;
import com.profitera.services.business.batch.AbstractBatchProcess;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.Commodity;
import com.profitera.services.system.financial.CommodityAction;
import com.profitera.services.system.financial.CreateAccountAction;
import com.profitera.services.system.financial.Split;
import com.profitera.services.system.financial.Transaction;
import com.profitera.services.system.financial.TransactionAction;
import com.profitera.services.system.loan.IAccountTypes;
import com.profitera.services.system.loan.ILoanAccountService;
import com.profitera.services.system.loan.LoanAccount;
import com.profitera.services.system.loan.impl.ILoanPrincipalInstallmentManager;
import com.profitera.services.system.loan.impl.PostingSchedule.PostingType;
import com.profitera.util.BigDecimalUtil;

public class LoanAccountIntializer {
  private Commodity c;
  private Account sourcePrincipalAccount;
  private Account sourceInterestInSuspenseAccount;
  private Account sourceChargeInSuspenseAccount;
  private Account sourcePenaltyInSuspenseAccount;
  private Account sourceInterestAccount;
  private Account sourceChargeAccount;
  private Account sourcePenaltyAccount;
  private Account sourcePrincipalOverdueAccount;

  public LoanAccountIntializer(String commodityCode, final ILoanAccountService s, IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    c = new CommodityAction().getCommodity(commodityCode, p, t);
    sourcePrincipalAccount = s.getGeneralAccount(s.getAccountType(IAccountTypes.SRC_P, p, t), p, t);
    sourcePrincipalOverdueAccount = s.getGeneralAccount(s.getAccountType(IAccountTypes.SRC_PINST, p, t), p, t);
    sourceInterestAccount = s.getGeneralAccount(s.getAccountType(IAccountTypes.SRC_I, p, t), p, t);
    sourceChargeAccount = s.getGeneralAccount(s.getAccountType(IAccountTypes.SRC_C, p, t), p, t);
    sourcePenaltyAccount = s.getGeneralAccount(s.getAccountType(IAccountTypes.SRC_PN, p, t), p, t);
    sourceInterestInSuspenseAccount = s.getGeneralAccount(s.getAccountType(IAccountTypes.SRC_IIS, p, t), p, t);
    sourceChargeInSuspenseAccount = s.getGeneralAccount(s.getAccountType(IAccountTypes.SRC_CIS, p, t), p, t);
    sourcePenaltyInSuspenseAccount = s.getGeneralAccount(s.getAccountType(IAccountTypes.SRC_PIS, p, t), p, t);
  }
  
  public void initializeLoan(LoanAccount loanAccount, Map<String, Object> data,
      Date date, ILoanAccountService iLoanAccountService, IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    checkRequired(loanAccount.getId(), data, new String[]{IFinancialProvider.PRINCIPAL_AMOUNT, IFinancialProvider.PRINCIPAL_OVERDUE_AMOUNT, IFinancialProvider.INTEREST_AMOUNT, IFinancialProvider.CHARGE_AMOUNT, IFinancialProvider.PENALTY_AMOUNT,
        IFinancialProvider.INTEREST_IN_SUSPENSE_AMOUNT, IFinancialProvider.CHARGE_IN_SUSPENSE_AMOUNT, IFinancialProvider.PENALTY_IN_SUSPENSE_AMOUNT});
    BigDecimal principalAmount = (BigDecimal) data.get(IFinancialProvider.PRINCIPAL_AMOUNT);
    BigDecimal interestAmount = (BigDecimal) data.get(IFinancialProvider.INTEREST_AMOUNT);
    BigDecimal chargeAmount = (BigDecimal) data.get(IFinancialProvider.CHARGE_AMOUNT);
    BigDecimal penaltyAmount = (BigDecimal) data.get(IFinancialProvider.PENALTY_AMOUNT);
    createLink(principalAmount,
        (BigDecimal) data.get(IFinancialProvider.PRINCIPAL_OVERDUE_AMOUNT),
        interestAmount, chargeAmount, penaltyAmount,
        (BigDecimal) data.get(IFinancialProvider.INTEREST_IN_SUSPENSE_AMOUNT),
        (BigDecimal) data.get(IFinancialProvider.CHARGE_IN_SUSPENSE_AMOUNT),
        (BigDecimal) data.get(IFinancialProvider.PENALTY_IN_SUSPENSE_AMOUNT),
        p, loanAccount, iLoanAccountService, date, t);
  }

  private void checkRequired(long id, Map<String, Object> data, String[] strings) throws AbortTransactionException {
    for (int i = 0; i < strings.length; i++) {
      if (data.get(strings[i]) == null) {
        throw new AbortTransactionException("No value found for required '" + strings[i] + "' in loan account initialization for id " + id);
      }
    }
  }

  private void createLink(final BigDecimal principalAmount, 
      final BigDecimal principalOverdueAmount,
      final BigDecimal interestAmount, 
      final BigDecimal chargeAmount, 
      final BigDecimal penaltyAmount,
      final BigDecimal interestInSusAmount, 
      final BigDecimal chargeInSusAmount, 
      final BigDecimal penaltyInSusAmount,
      final IReadWriteDataProvider p, final LoanAccount loanAccount, 
      final ILoanAccountService s, final Date effectiveDate, ITransaction t)
      throws SQLException, AbortTransactionException {
    Account principal = new CreateAccountAction(s.getAccountType(IAccountTypes.PRINCIPAL, p, t), c).create(p, t);
    Account iis = new CreateAccountAction(s.getAccountType(IAccountTypes.IIS, p, t), c).create(p, t);
    Account cis = new CreateAccountAction(s.getAccountType(IAccountTypes.CIS, p, t), c).create(p, t);
    Account pis = new CreateAccountAction(s.getAccountType(IAccountTypes.PIS, p, t), c).create(p, t);
    Account i = new CreateAccountAction(s.getAccountType(IAccountTypes.INTEREST, p, t), c).create(p, t);
    Account chg = new CreateAccountAction(s.getAccountType(IAccountTypes.CHARGE, p, t), c).create(p, t);
    Account pn = new CreateAccountAction(s.getAccountType(IAccountTypes.PENALTY, p, t), c).create(p, t);
    Account ovr = new CreateAccountAction(s.getAccountType(IAccountTypes.OVERPAY, p, t), c).create(p, t);
    Account cum = new CreateAccountAction(s.getAccountType(IAccountTypes.CUMULATIVE_INT, p, t), c).create(p, t);
    Account cumis = new CreateAccountAction(s.getAccountType(IAccountTypes.CUMULATIVE_IIS, p, t), c).create(p, t);
    Account pinst = new CreateAccountAction(s.getAccountType(IAccountTypes.PINST, p, t), c).create(p, t);
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("ID", loanAccount.getId());
    arguments.put("ACCOUNT_ID", loanAccount.getId());
    arguments.put(AbstractBatchProcess.EFFECTIVE_DATE_PARAM_NAME, effectiveDate);
    Account[] accounts = {principal, iis, cis, pis, i, chg, pn, ovr, cum, cumis, pinst};
    for (int j = 0; j < accounts.length; j++) {
      arguments.put("FINANCIAL_ACCOUNT_ID", accounts[j].getId());
      p.insert("insertLoanFinancialAccountsLink", arguments, t);
    }
    Date d = effectiveDate;
    if (principalOverdueAmount.compareTo(BigDecimalUtil.ZERO) != 0){
      ILoanPrincipalInstallmentManager mgr = loanAccount.getLoanPrincipalInstallmentManager();
      mgr.postPrincipalInstallmentPosting(sourcePrincipalOverdueAccount, principalOverdueAmount, d, d, d, d, d, PostingType.PRINCIPAL_INSTALLMENT, p, t);
    }
    if (interestAmount.compareTo(BigDecimalUtil.ZERO) != 0){
      Transaction interestTransaction = postTransaction(i, sourceInterestAccount, interestAmount, c, d, p, t);
      loanAccount.recordInterestPostingHistory(true, sourceInterestAccount, interestTransaction, d, d, d, d, p, t);
    }
    if (chargeAmount.compareTo(BigDecimalUtil.ZERO) != 0){
      postTransaction(chg, sourceChargeAccount, chargeAmount, c, d, p, t);
    }
    if (penaltyAmount.compareTo(BigDecimalUtil.ZERO) != 0){
      Transaction posted = postTransaction(pn, sourcePenaltyAccount, penaltyAmount, c, d, p, t);
      loanAccount.recordPenaltyPostingHistory(sourcePenaltyAccount, posted, d, d, d, d, p, t);
    }
    if (interestInSusAmount.compareTo(BigDecimalUtil.ZERO) != 0){
      Transaction posted = postTransaction(iis, sourceInterestInSuspenseAccount, interestInSusAmount, c, d, p, t);
      loanAccount.recordInterestPostingHistory(true, sourceInterestInSuspenseAccount, posted, d, d, d, d, p, t);
    }
    if (chargeInSusAmount.compareTo(BigDecimalUtil.ZERO) != 0){
      postTransaction(cis, sourceChargeInSuspenseAccount, chargeInSusAmount, c, d, p, t);
    }
    if (penaltyInSusAmount.compareTo(BigDecimalUtil.ZERO) != 0){
      Transaction posted = postTransaction(pis, sourcePenaltyInSuspenseAccount, penaltyInSusAmount, c, d, p, t);
      loanAccount.recordPenaltyPostingHistory(sourcePenaltyInSuspenseAccount, posted, d, d, d, d, p, t);
    }
    // Always create the principal transactions just to be explicit,
    // even if there is no known principal amount.
    Transaction trans = getTransaction(principal, sourcePrincipalAccount, principalAmount, c);
    // Use LoanAccount to post this to get aging calculated
    loanAccount.postTransaction(trans, d, p, t);
  }

  protected Transaction postTransaction(Account to,
      Account from, BigDecimal amount, Commodity c, Date d,
      IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    Transaction trans = getTransaction(to, from, amount, c);
    return new TransactionAction(trans, d).post(p, t);
  }

  private Transaction getTransaction(Account to, Account from,
      BigDecimal amount, Commodity c) {
    Split s1 = new Split(from, amount.multiply(BigDecimalUtil.NEG_ONE), null, null);
    Split s2 = new Split(to, amount, null, null);
    Transaction trans = new Transaction(c, new Split[]{s1, s2});
    return trans;
  }
}