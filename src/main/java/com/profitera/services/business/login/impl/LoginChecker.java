package com.profitera.services.business.login.impl;

import java.util.Date;
import java.util.Map;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.admin.UserBusinessBean;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.util.DateParser;
import com.profitera.util.PassUtils;

public class LoginChecker {
  public static final String ACTIVE_STATUS = "ACTIVE_STATUS";
  public static final String LOGON_STATUS = "LOGON_STATUS";
  public static final String USER_EXP_DATE = "USER_EXP_DATE";
  public static final String PASSWD_EXP_DATE = "PASSWD_EXP_DATE";
  private final boolean isSingleLogin;
  private final int expiryNotice;

  public LoginChecker(boolean isSingleLoginOnly, int expiryNotice) {
    this.isSingleLogin = isSingleLoginOnly;
    this.expiryNotice = expiryNotice;
  }
  
  public void checkForLogin(final String userName, Map user) throws TransferObjectException {
    final String status = (String) user.get(ACTIVE_STATUS);
    if (status != null && status.equals("D"))  {
        throw new TransferObjectException("User has been deleted", new TransferObject(TransferObject.ERROR, PassUtils.USER_DELETED));
    }
    
    if (status == null || status.equals(UserBusinessBean.INACTIVE_STATUS.toString())) {
        throw new TransferObjectException("User is not active", new TransferObject(TransferObject.ERROR, PassUtils.NON_ACTIVE));
    }

    final String logonStatus = (String) user.get(LOGON_STATUS);
    if ( isSingleLogin && (logonStatus == null || logonStatus.equals(UserBusinessBean.LOGIN_ACTIVE.toString()))) {
        throw new TransferObjectException("Already Logged in", new TransferObject(TransferObject.ERROR, PassUtils.LOGIN));
    }

    Date userExpDate = (Date) user.get(USER_EXP_DATE);
    if (userExpDate != null){
      // Expiry is at MIDNIGHT on that day
      userExpDate = DateParser.getStartOfDay(userExpDate);
    }
    if (userExpDate != null && userExpDate.getTime() < System.currentTimeMillis()) {
      throw new TransferObjectException("User expired", new TransferObject(TransferObject.ERROR, PassUtils.USER_EXPIRED));
    }
  }

  public String getLoginMessage(Map user) {
    Date passwdExpDate = (Date) user.get(PASSWD_EXP_DATE);
    if (passwdExpDate != null){
      // Expiry is at MIDNIGHT on that day
      passwdExpDate = DateParser.getStartOfDay(passwdExpDate);
    }
    // Normalize today's date to midnight
    final Date now = DateParser.getStartOfDay(new Date());
    String message = null;
    if (passwdExpDate == null){
       //password never expires
    } else if (passwdExpDate != null && (passwdExpDate.before(now) || passwdExpDate.equals(now))) {
        message = PassUtils.CHANGE_PASSWORD;
    } else {
      // Difference less one day, if your expiration date is 2nd and today is first
      // you have zero days to expiration, not 1.
      int daysToExpire = DateParser.getDaysDifference(passwdExpDate, now) - 1;
      if (daysToExpire <= expiryNotice) { //check if the password is expiring in a few days
        message = PassUtils.PASSWORD_EXPIRY;
      }
    }
    return message;
  }
}
