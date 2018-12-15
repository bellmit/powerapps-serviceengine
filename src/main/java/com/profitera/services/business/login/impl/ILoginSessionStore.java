package com.profitera.services.business.login.impl;

import java.util.Date;

public interface ILoginSessionStore {
  long createSession(String user, Object sessionAttachedData);
  Object getSessionAttachedData(long session);
  void terminateSession(long session);
  String getUser(Long session);
  void setSessionRole(long session, long roleId);
  Long getSessionRole(long session);
  void updateSessionLastActive(long session, Date requestTime);
  void handleSessionTimeout(long session, String userId, Date lastRequestTime);
  void setSessionTimeout(long milliseconds);
}