package com.profitera.services.system.financial;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.profitera.util.Strings;

public class Transaction {
  static final int UNDEFINED = 0;
  static final int POSTED = 2;
  static final int ENTERED = 1;
  private static final BigDecimal NEGATIVE_ONE = new BigDecimal(-1);
  private final Split[] splits;
  private final Commodity commodity;
  private final Long id;
  private final int status;
  private final Date transactionDate;
  private Transaction[] relatedTransactions;
  
  public Transaction(final Commodity commodity, final Split[] entries){
    this(commodity, entries, null);
  }
  
  public Transaction(final Commodity commodity, final Split[] entries, Transaction[] related){
    this(UNDEFINED, null, null, null, commodity, entries, related);
  }
  
  public Transaction(Long id, int status, Date transactionDate, Date postingDate, final Commodity commodity, final Split[] entries){
    this(status, id, transactionDate, postingDate, commodity, entries, null);
  }
  
  private Transaction(int status, Long id, Date transactionDate, Date postingDate, 
      final Commodity commodity, final Split[] entries, Transaction[] related){
    if (status != UNDEFINED && id == null){
      throw new IllegalArgumentException("A transaction ID is required for existing transactions");
    }
    this.id = id;
    if (id != null && status != POSTED && status != ENTERED){
      throw new IllegalArgumentException("Invalid transaction status: " + status);
    }
    if (id != null && transactionDate == null){
      throw new IllegalArgumentException("Existing transactions must have a transaction date");
    }
    this.transactionDate = transactionDate;
    if (status == POSTED && postingDate == null){
      throw new IllegalArgumentException("Posted transactions must have a posting date");
    } else if (status != POSTED && postingDate != null){
      throw new IllegalArgumentException("Non-posted transactions must not have a posting date");
    }
    this.status = status;
    if (commodity ==null){
      throw new IllegalArgumentException("A transaction requires a base for conversion");
    }
    this.commodity = commodity;
    if (entries == null || entries.length == 0){
      throw new IllegalArgumentException("A transaction requires splits to update");
    }
    splits = new Split[entries.length];
    System.arraycopy(entries, 0, splits, 0, entries.length);
    BigDecimal total = new BigDecimal(0); 
    for (int i = 0; i < splits.length; i++) {
      Split s = splits[i];
      if (s == null){
        throw new IllegalArgumentException("One or more splits in transaction is null");
      }
      if (!s.getCommodity().equals(commodity)){
        throw new IllegalArgumentException("One or more splits in transaction is of the wrong commodity");
      }
      BigDecimal exchange = s.getExchangedAmount();
      total = total.add(exchange);
    }
    
    if (0 != total.compareTo(new BigDecimal(0))){
      List temp = new ArrayList();
      for (int i = 0; i < splits.length; i++) {
        Split s = splits[i];
        temp.add(s.getAmount());
      }
      throw new IllegalArgumentException("Transaction splits are imbalanced, total is " + total + " on " + Strings.getListString(temp, ", "));
    }
    if (related != null){
      for (int i = 0; i < related.length; i++) {
        if (related[i] == null){
          throw new IllegalArgumentException("Missing transaction at index " + i + " for related transactions");
        } else if (related[i].getId() == null){
          throw new IllegalArgumentException("Transaction at index " + i + " for related transactions is not in the database");
        } else if (related[i].getStatus() == Transaction.POSTED){
          throw new IllegalArgumentException("Transaction at index " + i + " for related transactions is already POSTED");
        }
      }
    }
    this.relatedTransactions = related;
  }
  
  public Split[] getSplits(){
    Split[] copy = new Split[splits.length];
    System.arraycopy(splits, 0, copy, 0, splits.length);
    return copy;
  }
  
  public Long getId(){
    return id;
  }

  public int getStatus() {
    return status;
  }

  public Commodity getCommodity() {
    return commodity;
  }

  public Date getTransactionDate() {
    return transactionDate;
  }

  public Transaction getReversal(Transaction[] attached) {
    Split[] rev = new Split[splits.length];
    for (int i = 0; i < rev.length; i++) {
      Split o = splits[i];
      rev[i] = new Split(o.getAccount(), o.getAmount().multiply(NEGATIVE_ONE), o.getCommodity(), o.getExchangedAmount().multiply(NEGATIVE_ONE));
    }
    return new Transaction(getCommodity(), rev, attached);
  }

  public Split[] getAccountSplits(Account a) {
    return getAllAccountSplits(getSplits(), a);
  }
  public static Split[] getAllAccountSplits(Split[] transSplits, Account... accounts) {
    List<Split> splits = new ArrayList<Split>();
    for (int k = 0; k < accounts.length; k++) {
      for (int i = 0; i < transSplits.length; i++) {
        Split s = transSplits[i];
        if (s.getAccount().equals(accounts[k])){
          splits.add(s);
        }
      }
    }
    // Majority of results will be 1 in length
    return (Split[]) splits.toArray(new Split[0]);
  }

  public static int getFirstAccountSplitIndex(Account a, List splits){
    for (int i = 0; i < splits.size(); i++) {
      Split s = (Split) splits.get(i);
      if (s.getAccount().equals(a)){
        return i;
      }
    }
    return -1;
  }
  
  protected Transaction[] getRelatedTransactions(){
    if (getId() != null){
      throw new IllegalStateException("Related transactions should not be requested for persisted transactions");
    }
    return relatedTransactions;
  }

  public static BigDecimal total(Split[] splits) {
    BigDecimal d = BigDecimal.ZERO;
    for (int i = 0; i < splits.length; i++) {
      d = d.add(splits[i].getExchangedAmount());
    }
    return d;
  }
}
