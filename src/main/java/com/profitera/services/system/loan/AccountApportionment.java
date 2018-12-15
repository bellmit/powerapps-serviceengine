package com.profitera.services.system.loan;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.finance.OutstandingCharges;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.Split;
import com.profitera.util.BigDecimalUtil;

public class AccountApportionment implements Comparable<AccountApportionment> {
  DateFormat format = new SimpleDateFormat("yyyyMMdd");
  private final Account loanAccount;
  public final OutstandingCharges charges;
  private Integer id;
  private final int level;
  private final int order;
  public int index = -1;
  public AccountApportionment(Account l, OutstandingCharges c, int level, int order){
    if (l == null) throw new IllegalArgumentException("Account is required for apportionment");
    if (c == null) throw new IllegalArgumentException("Charges for account is required for apportionment");
    loanAccount = l;
    charges = c;
    this.level = level;
    this.order = order;
    clearOldest();
  }
  public Account getAccount(){
    return loanAccount;
  }
  public void clearOldest() {
    index++;
    Date d = getOldestDate();
    id = d == null ? null : new Integer(format.format(d));
  }
  public BigDecimal getOldestOutstanding() throws AbortTransactionException, SQLException {
    if (index >= charges.getSplitCount()){
      return null;
    } else {
      return charges.getSplitOutstandingAmount(index);
    }
  }
  public Date getOldestDate() {
    if (index >= charges.getSplitCount()){
      return null;
    } else {
      return charges.getSplitDate(index);
    }
  }
  public Integer getId() {
    return id;
  }
  
  public Split apportionPaymentToAccount(BigDecimal paymentAmount) {
    BigDecimal appliedPaymentAmount = BigDecimalUtil.ZERO;
    BigDecimal unApportionedAmount = paymentAmount;
    // This is a massive, stupid hack - using the index in the AP
    // This function should be moved into the AP
    for (int i = index; i < charges.getSplitCount(); i++) {
      BigDecimal amt = charges.getSplitOutstandingAmount(i);
      if (amt.compareTo(unApportionedAmount) > 0){
        appliedPaymentAmount = appliedPaymentAmount.add(unApportionedAmount);
        unApportionedAmount = BigDecimalUtil.ZERO;
      } else {
        appliedPaymentAmount = appliedPaymentAmount.add(amt);
        unApportionedAmount = unApportionedAmount.subtract(amt);
      }
    }
    if (appliedPaymentAmount.compareTo(BigDecimalUtil.ZERO) > 0){
      return new Split(getAccount(), appliedPaymentAmount.multiply(BigDecimalUtil.NEG_ONE), null, null);
    } else {
      return null;
    }
  }
  
  public int compareTo(AccountApportionment toThis) {
    AccountApportionment compareMe = this;
    if (compareMe.level == toThis.level && compareMe.order == toThis.order) {
      throw new RuntimeException("Found two apportionment rules with same level and order");
    }
    boolean iAmNull = compareMe.getId() == null;
    boolean youAreNull = toThis.getId() == null;
    if (iAmNull && youAreNull){
      int result = compareByLevel(compareMe, toThis);
      if (result == 0) {
        return compareByOrder(compareMe, toThis);
      } else {
        return result;
      }
    }
    // I'm not null so I get priority
    if (!iAmNull && youAreNull){
      return -1;
    }
    // You are not null so you get priority
    if (iAmNull && !youAreNull){
      return 1;
    }
    // We are both not null the level is next
    int levelDiff = compareByLevel(compareMe, toThis);
    if (levelDiff != 0) {
      return levelDiff;
    }
    // Now IDs decide the priority, known to be same level
    int compare = compareMe.getId().compareTo(toThis.getId());
    // Same IDs, order is next
    if (compare == 0) {
      return compareByOrder(compareMe, toThis);
    } else {
      return compare;
    }
  }
  private int compareByOrder(AccountApportionment compareMe,
      AccountApportionment toThis) {
    return (compareMe.order < toThis.order ? -1 : (compareMe.order == toThis.order ? 0 : 1));
  }
  
  private int compareByLevel(AccountApportionment compareMe, AccountApportionment toThis) {
    return compareMe.level - toThis.level;
  }
  
  public String toString(){
    return "Apportion-" + id + "/"+ getOldestDate() + " " + level + "L" + order + "th " + loanAccount.getType() + " " + charges; 
  }
}