package com.profitera.services.business.login.impl;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.descriptor.business.meta.IUser;
import com.profitera.ibatis.SQLMapFileRenderer;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public abstract class PersistentLoginSessionStore extends AbstractLoginSessionStore {
  private static final Class<?> H = HashMap.class;
  private static final String ROLE_ID = "ROLE_ID";
  private static final String V = SQLMapFileRenderer.V;
  private static final String SESSION_KEY = "SESSION_KEY";
  private static final String SESSION_DATA = "SESSION_DATA";
  private static final String LAST_REQUEST_TIME = "LAST_REQUEST_TIME";
  private static final String GET_USER = "getUser";
  private static final String DELETE = "deleteUserSession";
  private static final String INSERT = "insertUserSession";
  private static final String UPDATE = "updateUserSession";
  private static final String UPDATE_LAST_ACTIVE = "updateUserSessionLastActiveTime";

  private final IReadWriteDataProvider privateProvider;
  private MessageDigest digester;

  public PersistentLoginSessionStore(IReadWriteDataProvider privateProvider) {
    super();
    this.privateProvider = privateProvider;
    try {
      digester = MessageDigest.getInstance("SHA");
    } catch (NoSuchAlgorithmException e) {
      getLog().emit(LoginSessionLogClient.LOGIN_DIGESTER_FAILED, e, "SHA");
    }
  }
  
  private String digest(long session){
    if (digester == null){
      return String.valueOf(session);
    }
    byte[] bytes = null;
    try {
      bytes = String.valueOf(session).getBytes("UTF8");
    } catch (UnsupportedEncodingException e1) {} //NOPMD Impossible
    byte[] digest = digester.digest(bytes);
    byte[] encode = org.bouncycastle.util.encoders.Base64.encode(digest);
    return new String(encode);
  }

  public long createSession(String user, Object sessionAttachedData) {
    if (user == null){
      throw new IllegalArgumentException("No user for session");
    }
    long s = getRandom().nextLong();
    final Map<String, Object> args = new HashMap<String, Object>();
    args.put(IUser.USER_ID, user);
    args.put(SESSION_KEY, digest(s));
    args.put(SESSION_DATA, sessionAttachedData);
    try {
      privateProvider.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          privateProvider.delete(DELETE, args, t);
          privateProvider.delete(INSERT, args, t);
        }});
      return s;
    } catch (AbortTransactionException e) {
      getLog().emit(LoginSessionLogClient.STORE, e, user);
      throw new LoginDatabaseException(LoginSessionLogClient.STORE, e);
    } catch (SQLException e) {
      getLog().emit(LoginSessionLogClient.STORE, e, user);
      throw new LoginDatabaseException(LoginSessionLogClient.STORE, e);
    }
  }

  public String getUser(Long session) {
    return (String) get(session).get(IUser.USER_ID);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> get(long session) {
    try {
      return (Map<String, Object>) privateProvider.queryObject("getSession", digest(session));
    } catch (SQLException e) {
      getLog().emit(LoginSessionLogClient.RETRIEVE, e);
      throw new LoginDatabaseException(LoginSessionLogClient.RETRIEVE, e);
    }
  }
  
  public Long getSessionRole(long session) {
    return (Long) get(session).get(ROLE_ID);
  }

  public void setSessionRole(long session, long roleId) {
    String userId = getUser(session);
    final Map<String, Object> args = new HashMap<String, Object>();
    args.put(ROLE_ID, roleId);
    args.put(SESSION_KEY, digest(session));
    try {
      privateProvider.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          privateProvider.update(UPDATE, args, t);
        }});
    } catch (AbortTransactionException e) {
      getLog().emit(LoginSessionLogClient.STORE, e, userId);
      throw new LoginDatabaseException(LoginSessionLogClient.STORE, e);
    } catch (SQLException e) {
      getLog().emit(LoginSessionLogClient.STORE, e, userId);
      throw new LoginDatabaseException(LoginSessionLogClient.STORE, e);
    }
  }

  private void terminateSession(final String user, final long session) {
    final Map<String, String> args = new HashMap<String, String>();
    args.put(IUser.USER_ID, user);
    try {
      privateProvider.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          int count = privateProvider.delete(DELETE, args, t);
          if (count == 0){
            getLog().emit(LoginSessionLogClient.TERMINATE_NO_SESSION, user);
          }
        }});
    } catch (AbortTransactionException e) {
      getLog().emit(LoginSessionLogClient.TERMINATE, e, user);
      throw new LoginDatabaseException(LoginSessionLogClient.TERMINATE, e);
    } catch (SQLException e) {
      getLog().emit(LoginSessionLogClient.TERMINATE, e, user);
      throw new LoginDatabaseException(LoginSessionLogClient.TERMINATE, e);
    } finally {
      removeSession(session);
    }
  }

  public static String getSQL(SQLMapFileRenderer renderer) {
    StringBuffer b = new StringBuffer();
    addDelete(renderer, b);
    addInsert(renderer, b);
    addUpdate(renderer, b);
    addUpdateLastActive(renderer, b);
    addGet(renderer, b);
    return b.toString();
  }

  private static void addInsert(SQLMapFileRenderer renderer, StringBuffer b) {
    String ip = renderer.renderParameterMap(INSERT, Map.class, 
        new String[]{IUser.USER_ID, SESSION_KEY},
        new Class[]{String.class, String.class},
        new String[]{V, V});
    b.append(ip);
    String i = renderer.renderInsert(INSERT, INSERT, "insert into PTRUSER_SESSION (USER_ID, SESSION_KEY) values (?, ?)");
    b.append(i);
  }

  private static void addUpdate(SQLMapFileRenderer renderer, StringBuffer b) {
    String ip = renderer.renderParameterMap(UPDATE, Map.class, 
        new String[]{ROLE_ID, SESSION_KEY},
        new Class[]{Long.class, String.class},
        new String[]{"NUMERIC", V});
    b.append(ip);
    String i = renderer.renderInsert(UPDATE, UPDATE, "update PTRUSER_SESSION set ROLE_ID = ? where SESSION_KEY = ?");
    b.append(i);
  }
  private static void addUpdateLastActive(SQLMapFileRenderer renderer, StringBuffer b) {
    String ip = renderer.renderParameterMap(UPDATE_LAST_ACTIVE, Map.class, 
        new String[]{LAST_REQUEST_TIME, SESSION_KEY},
        new Class[]{Date.class, String.class},
        new String[]{"TIMESTAMP", V});
    b.append(ip);
    String i = renderer.renderInsert(UPDATE_LAST_ACTIVE, UPDATE_LAST_ACTIVE, "update PTRUSER_SESSION set LAST_REQUEST_TIME = ? where SESSION_KEY = ?");
    b.append(i);
  }

  private static void addDelete(SQLMapFileRenderer renderer, StringBuffer b) {
    String dp = renderer.renderParameterMap(DELETE, Map.class, 
        new String[]{IUser.USER_ID},
        new Class[]{String.class},
        new String[]{V});
    b.append(dp);
    String d = renderer.renderDelete(DELETE, DELETE, "delete from PTRUSER_SESSION where USER_ID = ?");
    b.append(d);
  }

  private static void addGet(SQLMapFileRenderer renderer, StringBuffer b) {
    String rm = renderer.renderResultMap(GET_USER, H, 
        new String[]{IUser.USER_ID, ROLE_ID, LAST_REQUEST_TIME}, 
        new Class[]{String.class, Long.class, Date.class});
    b.append(rm);
    String d = renderer.renderSelect("getSession", "getSession", String.class, 
        "select user_id, role_id, " + LAST_REQUEST_TIME
        + " from PTRUSER_SESSION where SESSION_KEY = #value#");
    b.append(d);
  }

  @Override
  public void terminateSession(long session) {
    terminateSession(getUser(session), session);
  }
  
  @Override
  protected void persistLastActiveTimes(Map<Long,Date> times) {
    for (Map.Entry<Long, Date> entry : times.entrySet()) {
      Long session = entry.getKey();
      String userId = getUser(session);
      final Map<String, Object> args = new HashMap<String, Object>();
      args.put("LAST_ACTIVE_TIME", entry.getValue());
      args.put(SESSION_KEY, digest(session));
      try {
        privateProvider.execute(new IRunnableTransaction(){
          public void execute(ITransaction t) throws SQLException,
              AbortTransactionException {
            privateProvider.update(UPDATE_LAST_ACTIVE, args, t);
          }});
      } catch (AbortTransactionException e) {
        getLog().emit(LoginSessionLogClient.STORE, e, userId);
        throw new LoginDatabaseException(LoginSessionLogClient.STORE, e);
      } catch (SQLException e) {
        getLog().emit(LoginSessionLogClient.STORE, e, userId);
        throw new LoginDatabaseException(LoginSessionLogClient.STORE, e);
      }
    }
  }
  protected void checkLastActiveTimesForExpirations(Map<Long,Date> times) {
    Long timeout = getCurrentSessionTimeout();
    if (timeout == null) {
      return;
    }
    long expirationTime = System.currentTimeMillis() - timeout;
    for (Map.Entry<Long, Date> entry : times.entrySet()) {
      Long session = entry.getKey();
      Map<String, Object> user = get(session);
      Date lastActive = (Date) user.get(LAST_REQUEST_TIME);
      if (lastActive.getTime() < expirationTime) {
        handleSessionTimeout(session, getUser(session), lastActive);
      }
    }
  }
  @Override
  public Object getSessionAttachedData(long session) {
    // Not supported.
    return null;
  }

}
