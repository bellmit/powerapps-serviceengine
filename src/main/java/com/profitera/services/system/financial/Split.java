package com.profitera.services.system.financial;

import java.math.BigDecimal;

public class Split {
  private static final BigDecimal ZERO = new BigDecimal(0);
  private final Account account;
  private final BigDecimal amount;
  private final BigDecimal exchangedAmount;
  private final Commodity commodity;
  private final Long id;

  public Split(Long id, Account account, BigDecimal amount, Commodity splitCommodity, BigDecimal exchangedAmount){
    this(id, false, account, amount, splitCommodity, exchangedAmount);
  }
  public Split(Account account, BigDecimal amount, Commodity splitCommodity, BigDecimal exchangedAmount){
    this(null, true, account, amount, splitCommodity, exchangedAmount);
  }
  private Split(Long id, boolean isNew, Account account, BigDecimal amount, Commodity splitCommodity, BigDecimal exchangedAmount){
    if (id == null && !isNew){
      throw new IllegalArgumentException("Existing splits must have an identifier");
    }
    this.id = id;
    if (account == null){
      throw new IllegalArgumentException("Split must have an adjusting account");
    }
    this.account = account;
    if (amount == null){
      throw new IllegalArgumentException("Split must have an adjusting amount");
    }
    this.amount = account.getCommodity().scale(amount);
    if (exchangedAmount == null && splitCommodity != null){
      throw new IllegalArgumentException("Split must have a defined exchanged amount when a conversion is specified");
    } else if (splitCommodity == null && exchangedAmount != null){
      throw new IllegalArgumentException("Split must have a defined conversion when a exchanged amount is specified");
    }
    if (exchangedAmount != null){
      this.exchangedAmount = splitCommodity.scale(exchangedAmount);
      this.commodity = splitCommodity;
    } else {
      this.exchangedAmount = this.amount;
      this.commodity = account.getCommodity();
    }
    if (getAmount().compareTo(ZERO) == 0 && getExchangedAmount().compareTo(ZERO) == 0){
      // This is OK a zero split is allowed as a 'MEMO'
    } else {
      int diff = this.amount.compareTo(ZERO) * this.exchangedAmount.compareTo(ZERO);
      if (diff <= 0){
        throw new IllegalArgumentException("Sign of amount and exchanged amount must not differ");
      }  
    }
  }
  
  public int hashCode(){
    return getId().hashCode();
  }
  
  public boolean equals(Object o){
    Account a = (Account) o;
    return a.getId().equals(getId());
  }

  public Long getId(){
    return id;
  }
  
  public Commodity getCommodity() {
    return commodity;
  }

  public Account getAccount() {
    return account;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public BigDecimal getExchangedAmount() {
    return exchangedAmount;
  }
  
  public String toString() {
    return "Split [" + getAmount() + " " + getAccount() + "]";
  }
}
