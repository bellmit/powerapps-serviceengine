package com.profitera.services.business.login.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class LoginSessionStore extends AbstractLoginSessionStore {
  private class Tuple {
    private final String user;
    private final Object data;
    Tuple(String s, Object o) {
      this.user = s;
      this.data = o;
    }
  }
  private final Map<Long, Tuple> userSessions = new HashMap<Long, Tuple>();
  private final Map<Long, Long> roles = new HashMap<Long, Long>();

  /* (non-Javadoc)
   * @see com.profitera.services.business.login.impl.ILoginSessionStore#createSession(java.lang.String)
   */
  public long createSession(String user, Object sessionAttachedData) {
    if (user == null){
      throw new IllegalArgumentException("No user for session");
    }
    long key = getRandom().nextLong();
    userSessions.put(key, new Tuple(user, sessionAttachedData));
    return key;
  }
  
  public void terminateSession(long session) {
    userSessions.remove(session);
    roles.remove(session);
    removeSession(session);
  }

  
  /* (non-Javadoc)
   * @see com.profitera.services.business.login.impl.ILoginSessionStore#getUser(java.lang.Long)
   */
  public String getUser(Long session) {
    Tuple tuple = userSessions.get(session);
    if (tuple != null) {
      return tuple.user;
    }
    return null;
  }
  @Override
  public Object getSessionAttachedData(long session) {
    Tuple tuple = userSessions.get(session);
    if (tuple != null) {
      return tuple.data;
    }
    return null;
  }


  public Long getSessionRole(long session) {
    return roles.get(session);
  }

  public void setSessionRole(long session, long roleId) {
    roles.put(session, roleId);
  }

  @Override
  protected void persistLastActiveTimes(Map<Long,Date> times) { //NOPMD
    // Nothing to do here, always persisted in local map
  }
  protected void checkLastActiveTimesForExpirations(Map<Long,Date> times) {
    Long timeout = getCurrentSessionTimeout();
    if (timeout == null) {
      return;
    }
    long expirationTime = System.currentTimeMillis() - timeout;
    for (Map.Entry<Long, Date> entry : times.entrySet()) {
      Long session = entry.getKey();
      Date lastActive = entry.getValue();
      if (lastActive.getTime() < expirationTime) {
        try {
          handleSessionTimeout(session, getUser(session), lastActive);
        } finally {
          terminateSession(session);
        }
      }
    }
  }
}
