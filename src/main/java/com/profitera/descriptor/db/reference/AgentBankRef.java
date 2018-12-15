package com.profitera.descriptor.db.reference;

public class AgentBankRef implements java.io.Serializable{

  public static final String ID = "id";
  public static final String CODE = "code";
  public static final String DESCRIPTION = "description";
  public static final String DISABLE = "disable";
  public static final String SORT_PRIORITY = "sortPriority";  
  
  private java.lang.Double id;  
  private java.lang.String code;
  private java.lang.String description;
  private java.lang.Double disable;
  private java.lang.Double sortPriority;

  public AgentBankRef(){

  }

  public java.lang.Double getRelTypeID(){
    return id;
  }

  public java.lang.String getRelTypeCode(){
    return code;
  }

  public java.lang.String getDescription(){
    return description;
  }

  public java.lang.Double getDisable(){
    return disable;
  }

  public java.lang.Double getSortPriority(){
    return sortPriority;
  }

  public void setRelTypeID(java.lang.Double id){
    this.id = id;	
  } 
  
  public void setRelTypeCode(java.lang.String code){
    this.code = code;
  }

  public void setDescription(java.lang.String description){
    this.description = description;
  }

  public void setDisable(java.lang.Double disable){
    this.disable = disable;
  }

  public void setSort(java.lang.Double sortPriority){
    this.sortPriority = sortPriority;
  }
}