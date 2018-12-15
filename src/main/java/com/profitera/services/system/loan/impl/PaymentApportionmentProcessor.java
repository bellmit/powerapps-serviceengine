package com.profitera.services.system.loan.impl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.AccountType;
import com.profitera.services.system.financial.Split;
import com.profitera.services.system.financial.SplitReducer;
import com.profitera.services.system.financial.Transaction;
import com.profitera.services.system.financial.TransactionAction;
import com.profitera.services.system.loan.AccountApportionment;
import com.profitera.services.system.loan.IAccountSet;
import com.profitera.services.system.loan.IAccountTypes;
import com.profitera.services.system.loan.InterestAccrual;
import com.profitera.services.system.loan.SuspenseSplitRedirector;
import com.profitera.util.BigDecimalUtil;
import com.profitera.util.DateParser;

public class PaymentApportionmentProcessor {
  private static final BigDecimal ZERO = BigDecimal.ZERO;
  private final IAccountSet loan;
  private final ILoanInterestManager interest;
  private final ILoanPrincipalInstallmentManager principal;
  private final Account principalAccount;
  private final InterestAccrual immediateInterestAccrual;
  private final Account installmentPrincipalAccount;
  private final IAccountTypeProvider types;
  private final IScheduleManager postingManager110;
  private final IScheduleManager postingManager100;
  private final IScheduleManager postingManager200;
  private static Log LOG;
  private final IKnockoffScheduleManager k;
  private final boolean isNeverWithholdingPrincipal;
  public PaymentApportionmentProcessor(IAccountSet loan, ILoanPrincipalInstallmentManager p, 
      ILoanInterestManager interestManager, IScheduleManager pm110, IScheduleManager pm100,
      IScheduleManager pm200,
      Account principalAccount, InterestAccrual immediateInterestAccrual, Account installmentP,
      IKnockoffScheduleManager k, IAccountTypeProvider types, boolean isNeverWithholdingPrincipal) {
    this.loan = loan;
    this.principalAccount = principalAccount;
    this.immediateInterestAccrual = immediateInterestAccrual;
    this.installmentPrincipalAccount = installmentP;
    this.k = k;
    this.types = types;
    this.postingManager100 = pm100;
    this.postingManager110 = pm110;
    postingManager200 = pm200;
    interest = interestManager;
    principal = p;
    this.isNeverWithholdingPrincipal = isNeverWithholdingPrincipal;
  }
  
  private AccountType getAccountType(String c, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    return types.get(c, p, t);
  }
  
  private BigDecimal getAccountPostedBalance(Account a, IReadWriteDataProvider p) throws SQLException {
    return loan.getAccountPostedBalance(a, p);
  }
  
  /**
   * TODO: #1023 This method can post transactions in the background, but it
   * does not update aging on its own. It assumes that it will not affect 
   * aging if any linked transactions are posted in the process put I can't 
   * be sure that this is the case so it needs some testing
   */
  public Transaction apportionPayment(Account generalPaymentAccount, BigDecimal paymentAmount,
      Date paymentDate, Date effectiveDate, boolean usePaymentDateForRedirectionEntry, String[] interestBalanceAccountTypes, IReadWriteDataProvider p,
      ITransaction t) throws AbortTransactionException, SQLException {
      if (paymentAmount == null){
        paymentAmount = ZERO;
      }
      if (generalPaymentAccount == null && !BigDecimalUtil.isEqual(paymentAmount, ZERO)) {
        throw new IllegalArgumentException("Account not provided for payment but amount of " + paymentAmount + " was supplied");
      }
      boolean isOverpaymentOnly = generalPaymentAccount == null;
      Account overpaymentAccount = getOverpaymentAccount(p, t);
      // We subtract because in overpay a surplus is a negative balance
      BigDecimal currentOverpaymentBalance = getAccountPostedBalance(overpaymentAccount, p);
      BigDecimal totalAvailablePaymentAmount = paymentAmount.subtract(currentOverpaymentBalance);
      Account[] sourceAccounts = null;
      BigDecimal[] paymentAmounts = null;
      // we add both
      if (generalPaymentAccount != null && currentOverpaymentBalance.compareTo(ZERO) < 0){
        sourceAccounts = new Account[]{generalPaymentAccount, overpaymentAccount};
        paymentAmounts = new BigDecimal[]{paymentAmount, currentOverpaymentBalance.multiply(BigDecimalUtil.NEG_ONE)};
      // no overpayment
      } else if (generalPaymentAccount != null && currentOverpaymentBalance.compareTo(ZERO) >= 0){
        sourceAccounts = new Account[]{generalPaymentAccount};
        paymentAmounts = new BigDecimal[]{paymentAmount};
      } else {
        sourceAccounts = new Account[]{overpaymentAccount};
        paymentAmounts = new BigDecimal[]{currentOverpaymentBalance.multiply(BigDecimalUtil.NEG_ONE)};
      }
      boolean isPostResolution = principal.isPostResolution(effectiveDate, postingManager110, postingManager100, p, t);
      List<Split> transSplits = null;
      // This will preserve the old behaviour if the system is set to
      // not always knock off principal in configured order as was the
      // case until this flag was introduced.
      if (isPostResolution || !isNeverWithholdingPrincipal) {
        boolean is100NextDue = is100NextDue(effectiveDate, p, t);
        if (!is100NextDue) {
          transSplits = apportionFor110Schedule(generalPaymentAccount,
            paymentAmount, paymentDate, effectiveDate, p, t, isOverpaymentOnly,
            overpaymentAccount, totalAvailablePaymentAmount, sourceAccounts,
            paymentAmounts, isPostResolution, interestBalanceAccountTypes);
        } else {
          transSplits = apportionFor100Schedule(generalPaymentAccount,
              paymentAmount, paymentDate, effectiveDate, p, t, isOverpaymentOnly,
              overpaymentAccount, totalAvailablePaymentAmount, sourceAccounts,
              paymentAmounts, isPostResolution, interestBalanceAccountTypes);
        }
      } else {
        transSplits = apportionForPreresolution(generalPaymentAccount,
            paymentAmount, paymentDate, effectiveDate, p, t, isOverpaymentOnly,
            overpaymentAccount, totalAvailablePaymentAmount, sourceAccounts,
            paymentAmounts, interestBalanceAccountTypes);
      }
      Split[] allSplits = transSplits.toArray(new Split[0]);
      SplitReducer reducer = new SplitReducer();
      // reduce before redirect so the redirection transaction will
      // be reduced
      allSplits = reducer.reduceSplits(allSplits);
      SuspenseSplitRedirector redirector = new SuspenseSplitRedirector(loan, allSplits);
      Transaction trans = redirector.getRedirectionTransaction(p, t);
      if (trans != null){
        //TODO: This is where you would use the payment date to match up entry dates
        // for the payment and the suspense-ledger transfer transaction.
        if (usePaymentDateForRedirectionEntry) {
          trans = new TransactionAction(trans, paymentDate).enter(p, t);
        } else {
          trans = new TransactionAction(trans, effectiveDate).enter(p, t);
        }
      }
      allSplits = redirector.getRevisedSplits(p, t);
      // Reduce to merge redirected splits that now hit the same
      // account but did not before
      allSplits = reducer.reduceSplits(allSplits);
      Transaction[] tList = trans == null ? null : new Transaction[]{trans};
      return new Transaction(overpaymentAccount.getCommodity(), allSplits, tList);
    
  }
  private List<Split> apportionForPreresolution(Account generalPaymentAccount,
      BigDecimal paymentAmount, Date paymentDate, Date effectiveDate,
      IReadWriteDataProvider p, ITransaction t, boolean isOverpaymentOnly,
      Account overpaymentAccount, BigDecimal totalAvailablePaymentAmount,
      Account[] sourceAccounts, BigDecimal[] paymentAmounts, String[] interestBalanceAccountTypes) throws SQLException,
      AbortTransactionException {
    boolean isPostResolution = false;
    List<Split> transSplits = new ArrayList<Split>();
    // Apportion once without allowing principle knockoff, if we have excess
    // then we would put it in principal or overpayment
    BigDecimal overpaymentAmount = apportionPayment(sourceAccounts,
        paymentAmounts, effectiveDate, true, p, t, transSplits, isPostResolution);
    if (overpaymentAmount.compareTo(ZERO) > 0){
      transSplits.clear();
      Transaction postedInterestToDate = interest.postImmediateInterestAccrual(DateParser.getPreviousDay(effectiveDate), effectiveDate, interestBalanceAccountTypes, p, t);
      // This transaction will have 2 splits, the absolute value of either will be
      // the amount charged on interest
      BigDecimal interestAmount = postedInterestToDate.getSplits()[0].getAmount().abs();
      // Now I subtract this interest amount from the overpayment to
      // figure out how much is going to hit the principal
      BigDecimal availableAmount = overpaymentAmount.subtract(interestAmount);
      BigDecimal advanceInterestAmount = getAdvanceInterestAmount(availableAmount, overpaymentAccount, effectiveDate, postingManager110.getNextDueDate(effectiveDate, p, t), p, t);
      BigDecimal remainingAdvanceInterestAmount = advanceInterestAmount;
      // Now I need to make sure that the adv interest amount is either added to or
      // stays in the overpayment account:
      int indexOf = Account.indexOf(sourceAccounts, overpaymentAccount);
      if (indexOf != -1){
        BigDecimal overpayAmount = paymentAmounts[indexOf];
        if (overpayAmount.compareTo(remainingAdvanceInterestAmount) > 0){
          // Withhold the overpayment contribution to the payment in the amount of the advance
          paymentAmounts[indexOf] = overpayAmount.subtract(remainingAdvanceInterestAmount);
          remainingAdvanceInterestAmount = ZERO;
        } else { // wiped out the overpayment to be paid
          remainingAdvanceInterestAmount = remainingAdvanceInterestAmount.subtract(paymentAmounts[indexOf]); 
          paymentAmounts[indexOf] = ZERO;
        }
      }
      // If there is advance interest left to account for
      // I need to redirect it from the payment and I know I
      // am taking nothing from overpayment so it can be left out
      if (remainingAdvanceInterestAmount.compareTo(ZERO) > 0){
        Split pay = new Split(generalPaymentAccount, paymentAmount, null, null);
        transSplits.add(pay);
        Split overpay = new Split(overpaymentAccount, remainingAdvanceInterestAmount.multiply(BigDecimalUtil.NEG_ONE), null, null);
        transSplits.add(overpay);
        paymentAmount = paymentAmount.add(overpay.getAmount());
        apportionPaymentAgainstLoanAccounts(paymentAmount, effectiveDate, 
            true, p, t, transSplits, isPostResolution);
      } else {
        apportionPayment(sourceAccounts, paymentAmounts, effectiveDate, 
          true, p, t, transSplits, isPostResolution);
      }
    }
    return transSplits;
  }


  private List<Split> apportionFor110Schedule(Account generalPaymentAccount,
      BigDecimal paymentAmount, Date paymentDate, Date effectiveDate,
      IReadWriteDataProvider p, ITransaction t, boolean isOverpaymentOnly,
      Account overpaymentAccount, BigDecimal totalAvailablePaymentAmount,
      Account[] sourceAccounts, BigDecimal[] paymentAmounts,
      boolean isPostResolution, String[] interestBalanceAccountTypes) throws SQLException,
      AbortTransactionException {
    List<Split> transSplits = new ArrayList<Split>();
    // Apportion once without allowing principle knockoff, if we have excess
    // then we would put it in principal or overpayment
    BigDecimal overpaymentAmount = apportionPayment(sourceAccounts,
        paymentAmounts, effectiveDate, false, p, t, transSplits, isPostResolution);
    boolean isKnockOffPrincipal = false;
    if (overpaymentAmount.compareTo(ZERO) > 0 ){ 
    // If there is left over amounts after knocking off the due amounts then
    // we check to see if hitting the 100 acocunt is allowed
      isKnockOffPrincipal = canKnockOffPrincipalWithOverpayment(
          effectiveDate, isOverpaymentOnly,
          totalAvailablePaymentAmount, isPostResolution, p, t);
    }
    if (isKnockOffPrincipal){
      transSplits.clear();
      Transaction postedInterestToDate = interest.postImmediateInterestAccrual(DateParser.getPreviousDay(effectiveDate), effectiveDate, interestBalanceAccountTypes, p, t);
      // This transaction will have 2 splits, the absolute value of either will be
      // the amount charged on interest
      BigDecimal interestAmount = postedInterestToDate.getSplits()[0].getAmount().abs();
      // Now I subtract this interest amount from the overpayment to
      // figure out how much is going to hit the principal
      BigDecimal availableAmount = overpaymentAmount.subtract(interestAmount);
      BigDecimal advanceInterestAmount = getAdvanceInterestAmount(availableAmount, overpaymentAccount, effectiveDate, postingManager110.getNextDueDate(effectiveDate, p, t), p, t);
      BigDecimal remainingAdvanceInterestAmount = advanceInterestAmount;
      // Now I need to make sure that the adv interest amount is either added to or
      // stays in the overpayment account:
      int indexOf = Account.indexOf(sourceAccounts, overpaymentAccount);
      if (indexOf != -1){
        BigDecimal overpayAmount = paymentAmounts[indexOf];
        if (overpayAmount.compareTo(remainingAdvanceInterestAmount) > 0){
          // Withhold the overpayment contribution to the payment in the amount of the advance
          paymentAmounts[indexOf] = overpayAmount.subtract(remainingAdvanceInterestAmount);
          remainingAdvanceInterestAmount = ZERO;
        } else { // wiped out the overpayment to be paid
          remainingAdvanceInterestAmount = remainingAdvanceInterestAmount.subtract(paymentAmounts[indexOf]); 
          paymentAmounts[indexOf] = ZERO;
        }
      }
      // If there is advance interest left to account for
      // I need to redirect it from the payment and I know I
      // am taking nothing from overpayment so it can be left out
      if (remainingAdvanceInterestAmount.compareTo(ZERO) > 0){
        Split pay = new Split(generalPaymentAccount, paymentAmount, null, null);
        transSplits.add(pay);
        Split overpay = new Split(overpaymentAccount, remainingAdvanceInterestAmount.multiply(BigDecimalUtil.NEG_ONE), null, null);
        transSplits.add(overpay);
        paymentAmount = paymentAmount.add(overpay.getAmount());
        apportionPaymentAgainstLoanAccounts(paymentAmount, effectiveDate, 
            isKnockOffPrincipal, p, t, transSplits, isPostResolution);
      } else {
        apportionPayment(sourceAccounts, paymentAmounts, effectiveDate, 
          isKnockOffPrincipal, p, t, transSplits, isPostResolution);
      }
      // If any of the payment went against principal then we need to
      // make sure installment principal is cleared up first for the current
      // installment period
      int principalIndex = Transaction.getFirstAccountSplitIndex(principalAccount, transSplits);
      if (principalIndex != -1 && isPostResolution){
        Date nextDue110 = postingManager110.getNextDueDate(effectiveDate, p, t);
        if (nextDue110 == null) {
          // This means that there is no installments left on the 110 schedule so I 
          // need to skip this whole process
          Date prev = postingManager110.getPreviousDueDate(effectiveDate, p, t);
          getLog().info("Excess payment on " + loan.getId() + " will not be allocated to future installments, no installments after " + prev + " and payment date is " + paymentDate);
        } else { 
          allocateFuture110Amount(transSplits,
            advanceInterestAmount, paymentDate, effectiveDate, p, t);
        }
      }
    }
    return transSplits;
  }
  
  private List<Split> apportionFor100Schedule(Account generalPaymentAccount,
      BigDecimal paymentAmount, Date paymentDate, Date effectiveDate,
      IReadWriteDataProvider p, ITransaction t, boolean isOverpaymentOnly,
      Account overpaymentAccount, BigDecimal totalAvailablePaymentAmount,
      Account[] sourceAccounts, BigDecimal[] paymentAmounts,
      boolean isPostResolution, String[] interestBalanceAccountTypes) throws SQLException,
      AbortTransactionException {
    List<Split> transSplits = new ArrayList<Split>();
    BigDecimal overpaymentAmount = apportionPayment(sourceAccounts, paymentAmounts, effectiveDate, false, p, t, transSplits, isPostResolution);
    if (overpaymentAmount.compareTo(ZERO) > 0 ){
      transSplits.clear();
      Date yesterday = DateParser.getPreviousDay(effectiveDate);
      Date interestPostedUpToDate = interest.getLastImmediateInterestPostedDate(p, t);
      if (interestPostedUpToDate == null || yesterday.after(interestPostedUpToDate)) {
        interest.postImmediateInterestAccrual(yesterday, effectiveDate, interestBalanceAccountTypes, p, t);
        interestPostedUpToDate = yesterday;
      }
      // Re-apportion and throw away results because I just want a new overpayment amount
      BigDecimal availableAmount = apportionPayment(sourceAccounts, paymentAmounts, effectiveDate, false, p, t, new ArrayList<Split>(), isPostResolution);
      // Now we check to see if the 200 posting date is sooner than the next 100 date,
      // we need to hold the posting amount if we do then.
      Date next200Due = postingManager200.getNextDueDate(DateParser.getNextDay(interestPostedUpToDate), p, t);
      if (next200Due != null && next200Due.before(postingManager100.getNextDueDate(effectiveDate, p, t))) {
        BigDecimal reservedForOverpayment = getAdvanceInterestAmount(availableAmount, overpaymentAccount, DateParser.getNextDay(interestPostedUpToDate), next200Due, p, t);
        if (reservedForOverpayment.compareTo(ZERO) <= 0) {
          reservedForOverpayment = immediateInterestAccrual.getAccrualAmount(DateParser.getNextDay(interestPostedUpToDate), next200Due, p, t);
        }
        if (reservedForOverpayment.compareTo(availableAmount) > 0) {
          reservedForOverpayment = availableAmount;
          availableAmount = ZERO;
        } else {
          availableAmount = availableAmount.subtract(reservedForOverpayment);
        }
      }
      // This calculation includes the amount from above for a preceding
      // 200 due date if the principal is going to be reduced by the payment
      // so later on when the advance interest amount is held back in the 
      // case that the 100 account is being hit the appropriate amount
      // will be withheld to meet the preceding 200 due date and the
      // next one.
      BigDecimal advanceInterestAmount = getAdvanceInterestAmount(availableAmount, overpaymentAccount, DateParser.getNextDay(interestPostedUpToDate), postingManager100.getNextDueDate(effectiveDate, p, t), p, t);
      BigDecimal installmentRemaining = principal.getRemainingInstallmentPrincipalAmount(ZERO, effectiveDate, postingManager100, p, t);
      BigDecimal newOverpaymentAmount = null;
      if (installmentRemaining.compareTo(availableAmount) > 0) {
        BigDecimal withholdAmount = installmentRemaining.subtract(availableAmount);
        availableAmount = ZERO;
        principal.postAllRemainingPrincipalLumpSumPosting(withholdAmount, paymentDate, effectiveDate, p, t);
        // Making the 100 knockoff false is the change here
        newOverpaymentAmount = apportionPayment(sourceAccounts,
            paymentAmounts, effectiveDate, false, p, t, transSplits, isPostResolution);
      } else {
        availableAmount = availableAmount.subtract(installmentRemaining);
        principal.postAllRemainingPrincipalLumpSumPosting(ZERO, paymentDate, effectiveDate, p, t);
        newOverpaymentAmount = apportionPayment(sourceAccounts, paymentAmounts, effectiveDate, true, p, t, transSplits, isPostResolution);
        Split[] principalSplits = Transaction.getAllAccountSplits(transSplits.toArray(new Split[0]), principalAccount, installmentPrincipalAccount);
        if (principalSplits.length > 0) {
          BigDecimal totalAvailable = Transaction.total(principalSplits).multiply(BigDecimalUtil.NEG_ONE);
          advanceInterestAmount = getAdvanceInterestAmount(totalAvailable, overpaymentAccount, DateParser.getNextDay(interestPostedUpToDate), postingManager100.getNextDueDate(effectiveDate, p, t), p, t);
        }
      }
      int pIndex = Transaction.getFirstAccountSplitIndex(principalAccount, transSplits);
      if (pIndex != -1 && newOverpaymentAmount.compareTo(advanceInterestAmount) < 0) {
        // When we are short on overpayment we need to adjust down the principal hit
        // enough to hold back the advance interest amount
        Split s = (Split) transSplits.remove(pIndex);
        BigDecimal pAmount = s.getAmount().multiply(BigDecimalUtil.NEG_ONE);
        if(pAmount.compareTo(advanceInterestAmount.subtract(newOverpaymentAmount)) > 0) {
          BigDecimal revisedPAmount = pAmount.subtract(advanceInterestAmount.subtract(newOverpaymentAmount));
          transSplits.add(new Split(principalAccount, revisedPAmount.multiply(BigDecimalUtil.NEG_ONE), null, null));
          transSplits.add(new Split(overpaymentAccount, pAmount.subtract(revisedPAmount).multiply(BigDecimalUtil.NEG_ONE), null, null));
        } else {
          transSplits.add(new Split(overpaymentAccount, pAmount.multiply(BigDecimalUtil.NEG_ONE), null, null));
        }
      }
    }      
    return transSplits;
  }

  private boolean is100NextDue(Date paymentDate, IReadWriteDataProvider p,
      ITransaction t) throws SQLException {
    return isNextFirst(paymentDate, postingManager100, postingManager110, p, t);
  }
  
  private boolean isNextFirst(Date d, IScheduleManager me, IScheduleManager other, 
      IReadWriteDataProvider p, ITransaction t) throws SQLException {
    Date nextDue110 = me.getNextDueDate(d, p, t);
    Date nextDue100 = other.getNextDueDate(d, p, t);
    if (nextDue110 == null) {
      return false;
    } 
    return (nextDue100 == null || !nextDue110.after(nextDue100));
  }

  private void allocateFuture110Amount(List<Split> transSplits,
      BigDecimal advanceInterestAmount, Date paymentDate, Date effectiveDate,
      IReadWriteDataProvider p, ITransaction t) throws SQLException,
      AbortTransactionException {
    int principalIndex = Transaction.getFirstAccountSplitIndex(principalAccount, transSplits);
    Split pSplit = (Split) transSplits.get(principalIndex);
    BigDecimal principalTotal = pSplit.getAmount();
    BigDecimal installmentMax = principal.getRemainingInstallmentPrincipalAmount(advanceInterestAmount, paymentDate, postingManager110, p, t);
    BigDecimal installmentPAmount = null;
    BigDecimal normalPAmount = null;
    // If the principal that we are allocating covers all the installment P
    // then we can follow the normal course
    if (principalTotal.abs().compareTo(installmentMax) >= 0){
      installmentPAmount = installmentMax.multiply(BigDecimalUtil.NEG_ONE);
      normalPAmount = principalTotal.subtract(installmentPAmount);
    } else {
      installmentPAmount = principalTotal;
      normalPAmount = ZERO;
    }
    BigDecimal adjustedAdvanceForShortPrincipal = advanceInterestAmount.add(installmentMax.add(installmentPAmount));
    principal.postAllRemainingPrincipalInstallmentPosting(adjustedAdvanceForShortPrincipal, paymentDate, effectiveDate, p, t);
    Split pOsSplit = new Split(installmentPrincipalAccount, installmentPAmount, null, null);
    transSplits.set(principalIndex, pOsSplit);
    if (normalPAmount.compareTo(ZERO) != 0){
      Split principalSplit = new Split(principalAccount, normalPAmount, null, null);
      transSplits.add(principalSplit);
    }
  }

  private boolean canKnockOffPrincipalWithOverpayment(
      Date effectiveDate, boolean isOverpaymentOnly,
      BigDecimal totalAvailablePaymentAmount, boolean isPostResolution,
      IReadWriteDataProvider p, ITransaction t) throws SQLException {
    boolean isKnockOffPrincipal = false;
    if (isPostResolution){
      // If our money is coming only from overpayment we only pay installments
      if (isOverpaymentOnly){
        isKnockOffPrincipal = false;
      } else {
        //TODO This only covers the 110 schedule, we need to worry about the 
        // 100 schedule too, don't we??
        Date nextDue110 = postingManager110.getNextDueDate(effectiveDate, p, t);
        //Date nextDue100 = postingManager100.getNextDueDate(effectiveDate, p, t);
        boolean knockOffP100 = is110InstallmentSurplus(totalAvailablePaymentAmount, effectiveDate, nextDue110, postingManager110, p, t);
        isKnockOffPrincipal = knockOffP100;
      }
    } else {
      isKnockOffPrincipal = true;
    }
    return isKnockOffPrincipal;
  }

  private boolean is110InstallmentSurplus(BigDecimal totalAvailablePaymentAmount, Date effectiveDate, Date nextDue,
      IScheduleManager postingManager110, IReadWriteDataProvider p,
      ITransaction t) throws SQLException {
 // If there is no next due date then the next installment amount is zero
    BigDecimal installmentAmount = ZERO;
    if (nextDue != null) {
      installmentAmount = postingManager110.getPostingDueRateOnDate(nextDue, p, t);
    }
    if (totalAvailablePaymentAmount.compareTo(installmentAmount) > 0){
      return true;
    } else {
      // If that is not too much I need to check the payment amount for this month:
      BigDecimal paid = interest.getInterestAmountChargedForPeriod(effectiveDate, p, t);
      BigDecimal instPaid = principal.getInstallmentPrincipalAmountPaidForPeriod(nextDue, postingManager110, p, t);
      if (paid == null) {
        paid = ZERO;
      }
      paid = paid.add(instPaid == null ? ZERO : instPaid);
      if (paid.add(totalAvailablePaymentAmount).compareTo(installmentAmount) > 0){
        return true;
      }
    }
    return false;
  }

  private BigDecimal getAdvanceInterestAmount(BigDecimal availableAmount, Account overpaymentAccount,
      Date startDate, Date endDate, IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    InterestAccrual process = immediateInterestAccrual;
    BigDecimal a = process.getAdvanceInterestForPeriod(availableAmount, startDate, endDate, p, t);
    return overpaymentAccount.getCommodity().scale(a);
  }
  
  private BigDecimal apportionPayment(Account[] paymentSourceAccounts,
      BigDecimal[] paymentAmounts, Date effectiveDate,
      boolean isKnockOffPrincipleAllowed,
      IReadWriteDataProvider p, ITransaction t, List<Split> transSplits, boolean isPostResolution)
      throws SQLException, AbortTransactionException {
    BigDecimal paymentAmount = ZERO;
    {
      for (int i = 0; i < paymentSourceAccounts.length; i++) {
        Split pay = new Split(paymentSourceAccounts[i], paymentAmounts[i], null, null);
        transSplits.add(pay);
        paymentAmount = paymentAmount.add(paymentAmounts[i]);
      }
    }
    return apportionPaymentAgainstLoanAccounts(paymentAmount,
        effectiveDate, isKnockOffPrincipleAllowed, p, t, transSplits, isPostResolution);
  }
  private BigDecimal apportionPaymentAgainstLoanAccounts(
      BigDecimal paymentAmount, Date effectiveDate,
      boolean isKnockOffPrincipalAllowed, IReadWriteDataProvider p,
      ITransaction t, List<Split> transSplits, boolean isPostResolution) throws SQLException,
      AbortTransactionException {
    List<AccountApportionment> apportionments = k.getAllAccountApportionments(loan, isKnockOffPrincipalAllowed,
        isPostResolution, p, t);
    BigDecimal remainingPayment = paymentAmount;
    AccountApportionment oldestDue = getOldestDueApportionments(apportionments);
    while(oldestDue != null && remainingPayment.compareTo(ZERO) > 0){
      AccountApportionment ap = oldestDue;
      BigDecimal oldestAmount = ap.getOldestOutstanding();
      if (oldestAmount != null){
        BigDecimal paidAmount = oldestAmount;
        if (paidAmount.compareTo(remainingPayment) > 0){
          paidAmount = remainingPayment;
        }
        apportionPaymentAgainst(ap, paidAmount, transSplits, effectiveDate);
        remainingPayment = remainingPayment.subtract(paidAmount);
      }
      ap.clearOldest();
      oldestDue = getOldestDueApportionments(apportionments);
    }
    if (remainingPayment.compareTo(ZERO) > 0){
      Account overAccount = getOverpaymentAccount(p, t);
      Split over = new Split(overAccount, remainingPayment.multiply(BigDecimalUtil.NEG_ONE), null, null);
      transSplits.add(over);
    }
    // This is the amount that went into the overpayment account
    return remainingPayment;
  }


  
  private AccountApportionment getOldestDueApportionments(List<AccountApportionment> apportionmentStructs) {
    List<AccountApportionment> oldest = new ArrayList<AccountApportionment>(apportionmentStructs);
    Collections.sort(oldest);
    AccountApportionment o = (AccountApportionment)oldest.get(0);
    Integer minId = o.getId();
    return minId == null ? null : o;
  }


  private Account getLoanAccount(AccountType type,
      final IReadWriteDataProvider p, ITransaction t)
      throws AbortTransactionException, SQLException {
    return loan.getSetFinancialAccount(type, p, t);
  }
  
  private BigDecimal apportionPaymentAgainst(AccountApportionment ap, 
      BigDecimal remainingPayment, List<Split> transSplits, Date effectiveDate) {
    // Here I need to accrue interest before applying payment 
    // for special interest in suspense, this is not 100% clear
    // since the payments should be broken out by the month accrued
    if (!BigDecimalUtil.isEqual(remainingPayment, ZERO)){
      Split chargeSplit = ap.apportionPaymentToAccount(remainingPayment);
      if (chargeSplit != null){
        // We add here because the amount will be negative, payment against balance
        remainingPayment = remainingPayment.add(chargeSplit.getAmount());
        transSplits.add(chargeSplit);
      }
    }
    return remainingPayment;
  }
  
  private Account getOverpaymentAccount(IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    return getLoanAccount(getAccountType(IAccountTypes.OVERPAY, p, t), p, t);
  }
    
  private Log getLog() {
    if (LOG == null) {
      LOG = LogFactory.getLog(getClass());
    }
    return LOG;
  }

}
