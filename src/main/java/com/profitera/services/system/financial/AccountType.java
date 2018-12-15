package com.profitera.services.system.financial;

public class AccountType {
  private final Long id;

  AccountType(Long id){
    if (id == null) {
      throw new IllegalArgumentException("Account type requires an Identifier");
    }
    this.id = id;
  }
  
  public Long getId(){
    return id;
  }
  
  public boolean equals(Object o){
    return getId().equals(((AccountType)o).getId());
  }

  public int hashCode() {
    return getId().hashCode();
  }
  
  public String toString() {
    return "AccountType-" + getId();
  }
  
  

}
