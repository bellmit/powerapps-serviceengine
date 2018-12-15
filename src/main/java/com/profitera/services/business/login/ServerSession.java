/*
 * Created by IntelliJ IDEA.
 * User: wso
 * Date: Mar 17, 2004
 * Time: 7:37:02 PM
 */
package com.profitera.services.business.login;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.interceptor.Interceptor;

public class ServerSession implements Interceptor {
  public static final ThreadLocal<Long> THREAD_SESSION = new ThreadLocal<Long>();
  private static final Map<String, Boolean> PERMITTED_METHODS;
  static {
    Map<String, Boolean> methods = new HashMap<String, Boolean>();
    methods.put("invokeScheduledEvent", Boolean.TRUE);
    methods.put("scheduleEvent", Boolean.TRUE);
    methods.put("removeEventSchedule", Boolean.TRUE);
    methods.put("getEventSchedule", Boolean.TRUE);
    //
    methods.put("login", Boolean.TRUE);
    methods.put("loginViaSingleSignOnTicket", Boolean.TRUE);
    methods.put("resetPassword", Boolean.TRUE);
    methods.put("getPasswordConstraints", Boolean.TRUE);
    PERMITTED_METHODS = Collections.unmodifiableMap(methods);
  }
  private MapLoginService login; // must be lazily instantiated or else we'll
                                  // hit a cyclic dependency
  public void beforeInvoke(Object service, Method m, Object[] args, Map<String, Object> context) {
    THREAD_SESSION.remove();
    if (PERMITTED_METHODS.get(m.getName()) != null) {
      return;
    }
    Long session = (Long) context.get("session");
    if (session == null) {
      session = getTransientSession(context);
      context.put("session", session);
    }
    if (session == null) {
      throw getNoSession();
    }
    String sessionUser = getLogin().getSessionUser(session);
    if (sessionUser == null) {
      throw getNoSession();
    } else {
      THREAD_SESSION.set(session);
    }
  }

  private Long getTransientSession(Map<String, Object> context) {
    String userId = (String) context.get("authuser");
    if (userId == null) {
      return null;
    }
    String password = (String) context.get("authpassword");
    TransferObject to = getLogin().loginTransient(userId, password);
    // We don't bother checking the return object, the bean holder will
    // be null if there is an error anyway.
    return (Long) to.getBeanHolder();
  }

  private void endTransientSession(Map<String, Object> context) {
    String userId = (String) context.get("authuser");
    // String password = (String) context.get("authpassword");
    Long session = (Long) context.get("session");
    // If does not matter if it fails, it means nothing to me
    getLogin().logoffTransient(userId, session);
  }

  private MapLoginService getLogin() {
    if (login == null) {
      MapLoginService s = (MapLoginService) LookupManager.getInstance().getLookupItem(LookupManager.BUSINESS, "LoginService");
      if (s == null) {
        throw new IllegalStateException("Request made to server before startup sequence is complete, LoginService not available");
      }
      login = s;
      
    }
    return login;
  }

  private RuntimeException getNoSession() {
    return new SessionRequiredException("Security violation: You do not have a session", null);
  }

  public void afterInvoke(Object service, Method m, Object[] args, Map<String, Object> context, Object result) {
    Object auth = context.get("authuser");
    if (auth != null) {
      endTransientSession(context);
    }
    THREAD_SESSION.remove();
  }
}
