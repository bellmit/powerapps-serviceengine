package com.profitera.services.business.login.impl;

import java.util.HashMap;
import java.util.Map;

public class LoginAttemptTracker {
  private Map<Object, Integer> users = new HashMap<Object, Integer>();
  private final int maxAllowedAttempts;
  
  public LoginAttemptTracker(int maxAttempts) {
    maxAllowedAttempts = maxAttempts;
  }
  public void successfulLogin(Object userId) {
    users.remove(userId);
  }
  
  public boolean unsuccessfulLogin(Object userId) {
    if (getMaxAttemptCount() == 0) {
      return false;
    }
    Integer i = users.get(userId);
    if (i == null) {
      i = new Integer(1);
    } else {
      i = new Integer(i.intValue() + 1);
    }
    users.put(userId, i);
    if (i.intValue() >= getMaxAttemptCount()) {
      return true;
    }
    return false;
  }
  
  public int getMaxAttemptCount() {
    return maxAllowedAttempts;
  }

}
