package com.profitera.services.system.financial;

import java.math.BigDecimal;

public class Commodity {
  private final Long id;
  Commodity(Long id){
    if (id == null){
      throw new IllegalArgumentException("Commodity must have an identifier");
    }
    this.id = id;
  }
  public Long getId() {
    return id;
  }
  public boolean equals(Object obj) {
    return getId().equals(((Commodity)obj).getId());
  }
  
  public int hashCode() {
    return getId().hashCode();
  }
  public BigDecimal scale(BigDecimal amount) {
    if (amount == null) throw new IllegalArgumentException("Commodity can not scale null amount");
    return amount.setScale(2, BigDecimal.ROUND_HALF_EVEN);
  }

}
