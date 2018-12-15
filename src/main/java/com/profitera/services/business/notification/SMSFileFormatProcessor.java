package com.profitera.services.business.notification;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.util.Strings;

public class SMSFileFormatProcessor {
  private static final int CUSTOMER_ID_LEN = 20;
  Log log = LogFactory.getLog(SMSFileFormatProcessor.class);
  private static final String MESSAGE_LINE_DATE_FORMAT = "dd/MM/yyyyhh:mm:ss";
  private static final String DEFAULT_DEPT = "CCP";
  private static final String DEFAULT_BUSINESS = "04";
  public static final int DEFAULT_NUMBER_LEN = 11;
  private int numberLength = DEFAULT_NUMBER_LEN;
  private static final String DEFAULT_CUSTOMER_ID = Strings.pad("", CUSTOMER_ID_LEN, ' ');
  private static final int MESSAGE_LEN = 160;
  private static final int MESSAGE_ID_LEN = 15;
  private static final int BATCH_TYPE_LEN = 2;
  private String numberPrefix = "6";
  private String department = DEFAULT_DEPT;
  private String business = DEFAULT_BUSINESS;
  private SimpleDateFormat format = new SimpleDateFormat(MESSAGE_LINE_DATE_FORMAT);
  
  public void appendSMSInformation(StringBuffer contentBuffer, String content, String number, String batchType, long id) {
    //This MIGHT replace all the newlines, I'm not sure. It should work on windows or Unix
    content = content.replaceAll("\\n"," ").replaceAll("\\r", " ").replaceAll("\\f"," ");
    contentBuffer.append(format.format(new Date()));
    contentBuffer.append(getDepartment());
    contentBuffer.append(DEFAULT_BUSINESS);
    contentBuffer.append(Strings.leftPad(number == null ? "" : number, numberLength));
    contentBuffer.append(DEFAULT_CUSTOMER_ID);
    contentBuffer.append(Strings.pad(content, MESSAGE_LEN));
    contentBuffer.append(Strings.leftPad(id + "", MESSAGE_ID_LEN, '0'));
    contentBuffer.append(Strings.pad(batchType, BATCH_TYPE_LEN));
    contentBuffer.append(System.getProperty("line.separator"));
  }

  public String getMobileNumber(String rawMobileNumber) {
    if (rawMobileNumber == null) {
      return null;
    }
    String[] segments = rawMobileNumber.replaceAll("\\W","").split("\\D+");
    if (segments.length == 0){
      log.warn("Unable to find mobile number in stored value of " + rawMobileNumber);
      return null;
    }
    int maxLen = 0;
    int biggest = 0;
    for (int i = 1; i < segments.length; i++) {
        if (segments[i].length() > maxLen)
          biggest = i;
      }
    String val = segments[biggest];
    if (maxLen < rawMobileNumber.length()){
      log.debug("Stored mobile number '" + rawMobileNumber + "' will be interpreted as number '" + val + "'");
    }
    if (!val.startsWith(getNumberPrefix()))
      val = getNumberPrefix() + val;
    return Strings.pad(val, 20);
  }

  public String getNumberPrefix() {
    return numberPrefix == null ? "" : numberPrefix;
  }

  public void setNumberPrefix(String numberPrefix) {
    if (numberPrefix == null){
      this.numberPrefix = "";
    } else {
      this.numberPrefix = numberPrefix;
    }
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    if (department == null){
      department = "";
    }
    this.department = Strings.pad(department, DEFAULT_DEPT.length(), ' ');
  }

  public String getBusiness() {
    return business;
  }

  public void setBusiness(String business) {
    if (business == null){
      business = "";
    }
    this.business = Strings.pad(business, DEFAULT_BUSINESS.length(), ' ');
  }

  public int getMessageLineLength() {
    return MESSAGE_LINE_DATE_FORMAT.length() 
    + DEFAULT_DEPT.length() + DEFAULT_BUSINESS.length() + numberLength 
    + CUSTOMER_ID_LEN + MESSAGE_LEN + MESSAGE_ID_LEN + BATCH_TYPE_LEN;
  }

  public void setNumberLength(int numberLength) {
    this.numberLength = numberLength;
  }

}
