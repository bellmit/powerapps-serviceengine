package com.profitera.services.system.financial;

public class Account {
  private final Commodity commodity;
  private final Long id;
  private final AccountType type;

  public Account(Long id, AccountType type, Commodity commodity){
    if (id == null) {
      throw new IllegalArgumentException("Account must have a identifier");
    }
    this.id = id;
    if (type == null) {
      throw new IllegalArgumentException("Account must have a type");
    }
    this.type = type;
    if (commodity == null) {
      throw new IllegalArgumentException("Account must have a denominating commodity");
    }
    this.commodity = commodity;
  }
  
  public Commodity getCommodity(){
    return commodity;
  }
  
  public Long getId(){
    return id;
  }

  public AccountType getType() {
    return type;
  }
  
  public boolean equals(Object o){
    return ((Account)o).getId().equals(id);
  }
  
  public int hashCode(){
    return getId().hashCode();
  }
  
  public String toString() {
    return "Account[" + getType() + " " + getId() + "]"; 
  }
  
  public static int indexOf(Account[] accounts, Account find) { 
    for (int i = 0; i < accounts.length; i++) { 
      if (accounts[i].equals(find)) {
        return i; 
      }
    } 
    return -1; 
  } 
}
