package com.profitera.descriptor.rpm;

public class RuleStatusDescriptors  {
  public static final String NONE = " ";
  public static final String AUTHORED = "A";
  public static final String TESTED = "T";
  public static final String DEPLOYED = "D";
  public static final String AUTHORED_TESTED = "AT";
  public static final String TESTED_DEPLOYED = "TD";
  public static final String AUTHORED_DEPLOYED = "AD";
  public static final String AUTHORED_TESTED_DEPLOYED = "*";

  public static String getRuleStatus(boolean authored, boolean tested, boolean deployed){
    if (authored && !tested && !deployed)
      return AUTHORED;
    else if (!authored && tested && !deployed)
      return TESTED;
    else  if (!authored && !tested && deployed)
      return DEPLOYED; 
    else  if (authored && tested && !deployed)
      return AUTHORED_TESTED;
    else  if (!authored && tested && deployed)
      return TESTED_DEPLOYED;
    else  if (authored && !tested && deployed)
      return AUTHORED_DEPLOYED;
    else  if (authored && tested && deployed)
      return AUTHORED_TESTED_DEPLOYED;            
    else
      return null;
  }

  public static String getAuthoredStatus(String currentStatus){
    if (currentStatus.equals(AUTHORED))
      return NONE;
    else if (currentStatus.equals(TESTED))  
      return AUTHORED_TESTED;
    else if (currentStatus.equals(DEPLOYED))  
      return AUTHORED_DEPLOYED;
    else
      return null;
  }

  public static String getUnAuthoredStatus(String currentStatus){
    if (currentStatus.equals(AUTHORED_TESTED))
      return TESTED;
    else if (currentStatus.equals(AUTHORED_DEPLOYED))  
      return DEPLOYED;
    else if (currentStatus.equals(AUTHORED_TESTED_DEPLOYED))  
      return TESTED_DEPLOYED;
    else
      return NONE;
  }

  public static String getTestedStatus(String currentStatus){
    if (currentStatus.equals(AUTHORED))
      return AUTHORED_TESTED;
    else if (currentStatus.equals(NONE))
      return TESTED;  
    else if (currentStatus.equals(DEPLOYED))
      return TESTED_DEPLOYED;    
    else if (currentStatus.equals(AUTHORED_DEPLOYED))  
      return AUTHORED_TESTED_DEPLOYED;
    else
      return NONE;
  }

  public static String getDeployStatus(String currentStatus){
    if (currentStatus.equals(AUTHORED))
      return AUTHORED_DEPLOYED;
    else if (currentStatus.equals(TESTED))  
      return TESTED_DEPLOYED;
    else if (currentStatus.equals(AUTHORED_TESTED))     
      return AUTHORED_TESTED_DEPLOYED;
    else
      return NONE;
  }

  public static String getUnDeployStatus(String currentStatus){
    if (currentStatus.equals(AUTHORED_TESTED_DEPLOYED))
      return AUTHORED_TESTED;
    else if (currentStatus.equals(AUTHORED_DEPLOYED))  
      return AUTHORED;
    else if (currentStatus.equals(TESTED_DEPLOYED))  
      return TESTED;
    else
      return NONE;
  }

  public static boolean isDeployed(String ruleStatus){
  	if (ruleStatus == null) return false;
    if ( ruleStatus.equals(RuleStatusDescriptors.DEPLOYED) || 
         ruleStatus.equals(RuleStatusDescriptors.AUTHORED_DEPLOYED) ||
         ruleStatus.equals(RuleStatusDescriptors.TESTED_DEPLOYED) ||
         ruleStatus.equals(RuleStatusDescriptors.AUTHORED_TESTED_DEPLOYED))
      return true;
    else
      return false;
  }

  public static boolean isAuthored(String ruleStatus){
    if ( ruleStatus.equals(RuleStatusDescriptors.AUTHORED) || 
         ruleStatus.equals(RuleStatusDescriptors.AUTHORED_DEPLOYED) ||
         ruleStatus.equals(RuleStatusDescriptors.AUTHORED_TESTED) ||
         ruleStatus.equals(RuleStatusDescriptors.AUTHORED_TESTED_DEPLOYED))
      return true;
    else
      return false;
  }

  public static boolean isTested(String ruleStatus){
    if ( ruleStatus.equals(RuleStatusDescriptors.TESTED) || 
         ruleStatus.equals(RuleStatusDescriptors.TESTED_DEPLOYED) ||
         ruleStatus.equals(RuleStatusDescriptors.AUTHORED_TESTED) ||
         ruleStatus.equals(RuleStatusDescriptors.AUTHORED_TESTED_DEPLOYED))
      return true;
    else
      return false;
  }
}