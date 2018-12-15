package com.profitera.services.business.login;

import java.io.File;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import com.profitera.auth.PasswordHasher;
import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.SqlMapProvider;
import com.profitera.datasource.IDataSourceConfigurationSet;
import com.profitera.deployment.rmi.LoginServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.admin.AccessRightsBean;
import com.profitera.descriptor.business.admin.PasswordManagerBean;
import com.profitera.descriptor.business.admin.UserBusinessBean;
import com.profitera.descriptor.business.login.UserBean;
import com.profitera.descriptor.business.login.UserRoleBean;
import com.profitera.descriptor.business.meta.IUser;
import com.profitera.ibatis.SQLMapFileRenderer;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.http.IMessageHandler;
import com.profitera.services.business.login.ISingleSignOnImplementation.ISingleSignOnSession;
import com.profitera.services.business.login.impl.ILoginSessionStore;
import com.profitera.services.business.login.impl.LicenseWarningTracker;
import com.profitera.services.business.login.impl.LoginAttemptTracker;
import com.profitera.services.business.login.impl.LoginChecker;
import com.profitera.services.business.login.impl.LoginServiceLogClient;
import com.profitera.services.business.login.impl.LoginSessionStore;
import com.profitera.services.business.login.impl.PowerLoginModule;
import com.profitera.services.business.login.impl.UserPassHandler;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.ProtocolLoadedSqlMapProvider;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.MapCar;
import com.profitera.util.MapListUtil;
import com.profitera.util.PassUtils;
import com.profitera.util.Strings;
import com.profitera.util.io.FileUtil;
import com.profitera.util.reflect.Reflect;
import com.profitera.util.reflect.ReflectionException;

public class MapLoginService extends LoginService implements LoginServiceIntf {
  private static final String LOGIN = "Login";
  private static final String NAME = "NAME";
  private static final String DESCRIPTION = "DESCRIPTION";
  private static final String GET_USER_STATEMENT = "getUser";
  private static final String PASSWORD_HASH = "loginservice.passwordhash";
  private static final String DETAIL_ERROR_CODE = "loginservice.detailedloginerrorcode";
  protected static final String MAX_IDLE_CONN = "loginservice.maxidleconnections";
  protected static final String MAX_ACTIVE_CONN = "loginservice.maxactiveconnections";
  private static final String SSO_IMPLEMENTATION = "loginservice.sso";
  private static final String SEC_SETTINGS_TABLE = "PTRSECURITY_SETTINGS";
  private static final long DEFAULT_VERIFY_PERIOD = 1000 * 60 * 30;
  private static final long BAD_CHANGE_WAIT = 1000 * 30;
  public enum HashMode {Strict, Transitional, None};
  //
    private static final String GET_USER_PASSWORD_HISTORY = "getUserPasswordHistory";
    private static final String UPDATE_PASSWORD = "updatePassword";
    private static final String PASSWORD_DATE = "PASSWORD_DATE";
    private static final String INSERT_PASSWORD_HISTORY = "insertPasswordHistory";
    private static final String DELETE_PASSWORD_HISTORY = "deletePasswordHistory";
    private static final String UPDATE_LOGIN_STATUS = "updateLoginStatus";
    private static final String GET_USER_ROLES = "getUserRoles";
    private static final String USER_EMAIL_ADDRESS = "USER_EMAIL_ADDRESS";
    private static final String PASSWORD = "PASSWORD";
    private static final String USER_ID = "USER_ID";
    private static final String[] PASSWORD_HISTORY_FIELDS = new String[]{USER_ID, PASSWORD_DATE, PASSWORD};
    private static final String PASSWD_EXP_DATE = LoginChecker.PASSWD_EXP_DATE;
    private static final String USER_EXP_DATE = LoginChecker.USER_EXP_DATE;
    private static final String LOGON_STATUS = LoginChecker.LOGON_STATUS;
    private static final String ACTIVE_STATUS = LoginChecker.ACTIVE_STATUS;
    private static final Long SUCCESS = 80801L;
    private static final Long FAILURE = 80802L;
    private static final Long EXCEPTION = 80803L;
    private static final int PASSWORD_COLUMN_WIDTH = 32;
    private int expiryNotice;
    private int passwordAge;
    private int minLength;
    private int maxLength;
    private int historySize;
    private int idlePeriod;
    private int maxAttempt;
    IReadWriteDataProvider privateProvider = null;
    private final LicenseWarningTracker warning = new LicenseWarningTracker(DEFAULT_VERIFY_PERIOD);
    private ILoginSessionStore transientSessions;
    private LoginAttemptTracker attemptTracker;

    public IReadWriteDataProvider getPrivateProvider(){
      if (privateProvider == null) {
        SQLMapFileRenderer renderer = new SQLMapFileRenderer();
        String securitySettings = getSecuritySettingsSelect(renderer);
        String rName = "login";
        String content = renderer.renderHeader(rName)
            + securitySettings  
            + getGetUserSelect(renderer)
            + getGetUserRolesSelect(renderer)
            + getUpdateLogonStatus(renderer)
            + getInsertPasswordHistory(renderer)
            + getUpdatePassword(renderer)
            + getUserPasswordHistory(renderer)
            + getDeletePasswordHistory(renderer)
            + getUpdateSecurtySetting(renderer)
            + getGetRoleAccessRights(renderer)
            + getAdditionalSQL(renderer)
            + getDeactivateUser(renderer)
            + renderer.renderFooter();
        IDataSourceConfigurationSet confSet = getDataSourceConfigurations();
        privateProvider = new ProtocolLoadedSqlMapProvider(rName, content, confSet.getDefaultDataSource(), LOGIN){
          @Override
          protected int getMaxIdleConnections() {
            final String count = ServiceEngine.getConfig(false).getProperty(MAX_IDLE_CONN, "3");
            return Integer.parseInt(count);
          }
          @Override
          protected int getMaxActiveConnections() {
            final String count = ServiceEngine.getConfig(false).getProperty(MAX_ACTIVE_CONN, "10");
            return Integer.parseInt(count);
          }
        };
      }
      return privateProvider; 
    }

    protected String getAdditionalSQL(SQLMapFileRenderer r) {
      return r.renderSelect("getAllAccessRights", "getRoleAccessRights-map", Map.class, "select r.access_rights_id, r.access_rights_desc, r.parent_access_rights_id, r.disable, r.sort_priority from ptraccess_rights_ref r");

    }

    private String getDeletePasswordHistory(SQLMapFileRenderer renderer) {
      return renderer.renderParameterMap(DELETE_PASSWORD_HISTORY + "-pmap", Map.class, new String[]{USER_ID, PASSWORD_DATE}) + 
      renderer.renderDelete(DELETE_PASSWORD_HISTORY, DELETE_PASSWORD_HISTORY + "-pmap", "delete from PTRPASSWORD_HISTORY where USER_ID = ? and PASSWORD_DATE = ?");
    }


    private String getUserPasswordHistory(SQLMapFileRenderer r) {
      return r.renderResultMap(GET_USER_PASSWORD_HISTORY + "-map", HashMap.class, new String[]{PASSWORD, PASSWORD_DATE, USER_ID}, new Class[]{String.class, Timestamp.class, String.class}) + 
      r.renderSelect(GET_USER_PASSWORD_HISTORY, GET_USER_PASSWORD_HISTORY + "-map", String.class, "SELECT PASSWORD, PASSWORD_DATE, USER_ID FROM PTRPASSWORD_HISTORY WHERE USER_ID = #value#");
    }

    private String getUpdatePassword(SQLMapFileRenderer r) {
      String p = UPDATE_PASSWORD + "-pmap";
      String[] fields = new String[]{PASSWD_EXP_DATE, PASSWORD, USER_ID};
      return r.renderParameterMap(p, Map.class, fields, new String[]{"TIMESTAMP", "VARCHAR", "VARCHAR"})
      + r.renderUpdate(UPDATE_PASSWORD, p, "UPDATE PTRUSER set " + PASSWD_EXP_DATE + " = ?, " + PASSWORD + " = ? where USER_ID = ?");
    }


    private String getInsertPasswordHistory(SQLMapFileRenderer r) {
      String pmap = INSERT_PASSWORD_HISTORY + "-pmap";
      return r.renderParameterMap(pmap, Map.class, PASSWORD_HISTORY_FIELDS, new String[]{"VARCHAR", "TIMESTAMP", "VARCHAR"})
      + r.renderInsert(INSERT_PASSWORD_HISTORY, pmap, "insert into ptrpassword_history (" + Strings.getListString(PASSWORD_HISTORY_FIELDS, ", ") + ") values ( ?, ?, ?)");
    }


    private String getUpdateLogonStatus(SQLMapFileRenderer r) {
      return r.renderParameterMap(UPDATE_LOGIN_STATUS + "-pmap", Map.class, new String[]{LOGON_STATUS, USER_ID}) +  
      r.renderUpdate(UPDATE_LOGIN_STATUS, UPDATE_LOGIN_STATUS + "-pmap", "Update ptruser set " + LOGON_STATUS + " = ? where " + USER_ID + " = ?");
    }


    protected String getGetUserRolesSelect(SQLMapFileRenderer r) {
      String[] p = {"ID", NAME, DESCRIPTION};
      Class<?>[] t = {Double.class, String.class, String.class};
      return r.renderResultMap(GET_USER_ROLES+"-map", HashMap.class, p, t) 
      + r.renderSelect(GET_USER_ROLES, GET_USER_ROLES + "-map", String.class, "select r.ROLE_ID as ID, r.ROLE_NAME as NAME, r.ROLE_DESC as DESCRIPTION from PTRUSER_ROLE_REF r inner join PTRUSER_ROLE u on r.ROLE_ID = u.ROLE_ID" 
          + " where (r.DISABLE = 0 or r.DISABLE is NULL) and u.USER_ID = #VALUE#");
      
    }


    private String getGetUserSelect(SQLMapFileRenderer r) {
      String[] p = new String[]{USER_EXP_DATE, USER_ID, PASSWORD, USER_EMAIL_ADDRESS, PASSWD_EXP_DATE, ACTIVE_STATUS, LOGON_STATUS, "USER_TYPE_ID"};
      Class<?>[] c = new Class[]{Timestamp.class, String.class, String.class, String.class, Timestamp.class, String.class, String.class, Long.class};
      return r.renderResultMap("getUser-map", HashMap.class, p, c) +
      r.renderSelect(GET_USER_STATEMENT, "getUser-map", String.class, "SELECT USER_EXP_DATE, USER_ID, PASSWORD, USER_EMAIL_ADDRESS, PASSWD_EXP_DATE, ACTIVE_STATUS, LOGON_STATUS, USER_TYPE_ID FROM PTRUSER WHERE (USER_ID = #value#)");
    }


    private String getSecuritySettingsSelect(SQLMapFileRenderer renderer) {
      String securitySettings = renderer.renderResultMap("getSecuritySettings-map", HashMap.class, new String[]{NAME, "VALUE"}, new Class[]{String.class, String.class})
                      + renderer.renderSelect("getSecuritySettings", "getSecuritySettings-map", "select NAME, VALUE from " + SEC_SETTINGS_TABLE);
      return securitySettings;
    }
 


    protected String getGetRoleAccessRights(SQLMapFileRenderer r) {
      return r.renderResultMap("getRoleAccessRights-map", HashMap.class, new String[]{"ID", DESCRIPTION, "PARENT_ID", "DISABLE", "SORT_PRIORITY"}, new Class[]{Long.class, String.class, Long.class, Boolean.class, Long.class}) +
      r.renderSelect("getRoleAccessRights", "getRoleAccessRights-map", Long.class, "select r.access_rights_id, r.access_rights_desc, r.parent_access_rights_id, r.disable, r.sort_priority from ptraccess_rights_ref r inner join ptrrole_access_rel a on (r.access_rights_id = a.access_rights_id)      where a.role_id = #value#");
    }

    protected String getUpdateSecurtySetting(SQLMapFileRenderer r) {
      return r.renderParameterMap("updateSecuritySetting-pmap", Map.class, new String[]{"VALUE", NAME})
      + r.renderUpdate("updateSecuritySetting", "updateSecuritySetting-pmap", "update PTRSECURITY_SETTINGS set VALUE = ? where NAME = ?");
    }
    
    protected String getDeactivateUser(SQLMapFileRenderer r){
    	return r.renderParameterMap("deactivateUser" + "-pmap", String.class, new String[]{USER_ID}) +  
        r.renderUpdate("deactivateUser", "deactivateUser" + "-pmap", "Update ptruser set " + ACTIVE_STATUS + " = 'N' where " + USER_ID + " = ?");
    }
    
    public void audit(String userId, String module, String action, String hostname, final String auditRemarks, int status, Timestamp startTime) {
      final Map<String, Object> m = new HashMap<String, Object>();
      /*
       *  CREATE TABLE "PTRAUDIT_LOG"  (
       *    "ID" BIGINT NOT NULL ,
       *    "REQUEST_TIME" DATETIME NOT NULL ,
       *    "REQUEST_DURATION" BIGINT NOT NULL,
       *    "USER_ID" NVARCHAR(10) NOT NULL ,
       *    "MODULE" NVARCHAR(50) NOT NULL ,
       *    "PC_NAME" NVARCHAR(30) NOT NULL ,
       *    "AUDIT_STATUS_ID" BIGINT NOT NULL ,
       *    "REMARKS" NVARCHAR(500) )
       *    ; 
       */
      m.put("REQUEST_TIME", startTime);
      m.put("REQUEST_DURATION", System.currentTimeMillis() - startTime.getTime());
      m.put("MODULE", module);
      m.put("PC_NAME", hostname == null ? "" : hostname);
      String remarks = getAuditRemarkText(action, auditRemarks);
      m.put("REMARKS", remarks);
      m.put(USER_ID, userId);
      m.put("AUDIT_STATUS_ID", getAuditStatus(status));
      try {
        getReadWriteProvider().execute(new IRunnableTransaction(){
          public void execute(ITransaction t) throws SQLException, AbortTransactionException {
            getReadWriteProvider().insert("insertAuditLog", m, t);
          }});
      } catch (AbortTransactionException e) {
        getLog().emit(LoginServiceLogClient.AUDIT_FAIL, e);
        throw new AuditTrailFailureException("AUDIT_TRAIL_FAILURE", e);
      } catch (SQLException e) {
        getLog().emit(LoginServiceLogClient.AUDIT_FAIL, e);
        throw new AuditTrailFailureException("AUDIT_TRAIL_FAILURE", e);
      } catch (Exception e){
        getLog().emit(LoginServiceLogClient.AUDIT_FAIL, e);
        throw new AuditTrailFailureException("AUDIT_TRAIL_FAILURE", e);
      }
    }

    private String getAuditRemarkText(String action, final String auditRemarks) {
      String remarks;
      if (auditRemarks == null) {
        remarks = action;
      } else {
        remarks = action + ": " + auditRemarks;
      }
      if (remarks.length() <= 500){
        return remarks;
      } else {
        return remarks.substring(0, 500);
      }
    }

    private Long getAuditStatus(int status) {
        final Long id;
        switch (status) {
            case 0:  // SUCCESSFUL
                id = SUCCESS; break;
                case 1: // FAILURE
                 id = FAILURE; break;
                case 2: // EXCEPTION
                id = EXCEPTION; break;
                default: return null;
        }
        return id;
    }
    
    public TransferObject getPasswordConstraints() {
      Iterator<Map<String, String>> all = null;
      try {
        all = getPrivateProvider().query(SqlMapProvider.LIST, "getSecuritySettings", null);
      } catch (SQLException e) {
        getLog().emit(LoginServiceLogClient.ERROR_FETCHING_SECURITY, e);
        return new TransferObject(TransferObject.EXCEPTION, "QUERY_SECURITY_SETTINGS_FAILED");
      }
      Map<String, String> settings = new HashMap<String, String>();
      while (all.hasNext()) {
        Map<String, String> m = all.next();
        settings.put(m.get(NAME), m.get("VALUE"));
      }
      expiryNotice = handleInt(PassUtils.PASSWORD_EXPIRYNOTICE, settings); // in days
      passwordAge = handleInt(PassUtils.PASSWORDAGE, settings); // in days
      minLength = handleInt(PassUtils.MINLENGTH, settings); //in characters
      maxLength = handleInt(PassUtils.MAXLENGTH, settings); //in characters
      historySize = handleInt(PassUtils.HISTORY, settings); //in records
      idlePeriod = handleInt(PassUtils.IDLE_PERIOD, settings); // in minutes
      if(settings.containsKey(PassUtils.MAX_ATTEMPT)){
    	  maxAttempt = handleInt(PassUtils.MAX_ATTEMPT, settings); //in characters
      }else{
    	  maxAttempt = 0;
      }
      final Map<String, Object> c = new HashMap<String, Object>(7);
      c.put(PassUtils.MINLENGTH, minLength);
      c.put(PassUtils.MAXLENGTH, maxLength);
      c.put(PassUtils.PASSWORD_EXPIRYNOTICE, expiryNotice);
      c.put(PassUtils.PASSWORDAGE, passwordAge);
      c.put(PassUtils.HISTORY, historySize);
      c.put(PassUtils.IDLE_PERIOD, Long.valueOf(idlePeriod));
      // The key is not required if we are in strict mode, otherwise we need it
      // and will verify that it exists right away.
      if (!getPasswordHashMode().equals(HashMode.Strict)) {
        c.put(PassUtils.APP_CRYPTKEY, getAppCryptKey());
      }
      c.put(PassUtils.MAX_ATTEMPT, maxAttempt);
      return new TransferObject(c);
  }

  private void resetPasswordConstraints() {
     getPasswordConstraints();
  }
  
  public TransferObject getAllPasswordSetting() {
    PasswordManagerBean bean =new PasswordManagerBean();
    TransferObject obj = getPasswordConstraints();
    Map config = (Map) obj.getBeanHolder();
    bean.setPasswordSettings(config);
    return new TransferObject(bean);
 }
  @Override
  public String getSessionUser(Long session) {
    String user = getStore().getUser(session);
    if (user == null) {
      user = getTransientStore().getUser(session);
    }
    return user;
  }


 public TransferObject updatePasswordSetting(final PasswordManagerBean bean){
   if (!isAuthorized(ServerSession.THREAD_SESSION.get(), 80571L)) {
     return new TransferObject(TransferObject.ERROR, "NOT_AUTHORIZED");
   }
    TransferObject obj = (TransferObject)getPasswordConstraints();
    Map config = (Map) obj.getBeanHolder();
    final Set key = config.keySet();
    try {
      final IReadWriteDataProvider provider = getPrivateProvider();
      IRunnableTransaction t = new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          Iterator iter = key.iterator();
          while (iter.hasNext())  {
               final Map<String, String> setting = new HashMap<String, String>();
               String primaryKey = (String)iter.next();
               if (PassUtils.APP_CRYPTKEY.equals(primaryKey)) {
                 continue;
               }
               setting.put(NAME, primaryKey);
               setting.put("VALUE", bean.getSettings(primaryKey).toString());
               provider.update("updateSecuritySetting", setting, t);
          }
        }};
       provider.execute(t);
       resetPasswordConstraints();
    } catch (Exception e) {
       return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
    }
    return new TransferObject(bean);
  }

  private int handleInt(String key, Map s) {
    final String value = (String) s.get(key);
    if (null == value) {
      throw new RuntimeException("Required key '" + key + "' not found in table " + SEC_SETTINGS_TABLE);
    } else {
      getLog().emit(LoginServiceLogClient.READ_SECURITY_SETTING, key, value);
      return Integer.parseInt(value);
    }
  }
  
  @SuppressWarnings("unchecked")
  public Map<String, Object> getUser(String userId) throws SQLException{
    return (Map<String, Object>) getPrivateProvider().queryObject(GET_USER_STATEMENT, userId);
  }
  
  private void logLoginFailure(final String userName, String message) {
    audit(userName, LOGIN, "login", null, message, TransferObject.ERROR, new Timestamp(System.currentTimeMillis()));
  }
  
  @Override
  public TransferObject loginTransient(String userId, String password) {
    return login(userId, password, false, true);
  }

  public synchronized TransferObject login(final String userId, final String password) {
    final String multipleLogon = ServiceEngine.getProp("Multiple-Logon", "false");
    boolean isSingleLoginOnly = multipleLogon.equals("false");
    return login(userId, password, isSingleLoginOnly, false);
  }
  
  private LoginAttemptTracker getAttemptTracker() {
    if (attemptTracker == null) {
      attemptTracker = new LoginAttemptTracker(maxAttempt);
    }
    return attemptTracker;
  }
  
  private TransferObject login(final String userName, final String passWord, boolean isSingleLoginOnly, boolean isTransient) {
  	Map<String, Object> user = getUserForLogin(userName);
    if (user==null)  {
        return returnNoSuchUser(userName);
    }
    final LoginContext lc;
    try {
    	lc = new LoginContext("PowerCollect", new Subject(), new UserPassHandler(userName, passWord), getConfiguration());
    } catch (LoginException e) {
        throw new RuntimeException("Login could not be initialized", e);
    }
    try {
        lc.login();
    } catch (LoginException e) {
  		if(e instanceof LdapServerNotFoundException){
  			logLoginFailure(userName, e.getMessage());
  			return new TransferObject(TransferObject.ERROR, PassUtils.LDAP_SERVER_CONNECTION_FAILED);
  		}
  		logLoginFailure(userName, e.getMessage());
  		if(getAttemptTracker().unsuccessfulLogin(userName)){
      		try{
      			getPrivateProvider().execute(new IRunnableTransaction(){
      				public void execute(ITransaction t) throws SQLException, AbortTransactionException {
      					getPrivateProvider().update("deactivateUser", userName, t);
      				}});
      		}catch(SQLException sqle){
      			throw new RuntimeException("Database error occurred deactivating user", sqle);
      		}catch(AbortTransactionException ate){
      			throw new RuntimeException("Database error occurred deactivating user", ate);
      		}
      		logLoginFailure(userName, "User deactivated");
      	}
        return getLoginFailedResult(PassUtils.ACCESS_DENIED);
    }
    return createSession(user, isSingleLoginOnly, isTransient, null);
  }

  private TransferObject createSession(Map<String, Object> user, boolean isSingleLoginOnly, 
      boolean isTransient, ISingleSignOnSession ssoSession) {
    final String userName = (String) user.get(IUser.USER_ID);
    String message = null;
    if (ssoSession != null) {
      LoginChecker loginChecker = new LoginChecker(isSingleLoginOnly, expiryNotice);
      try {
        loginChecker.checkForLogin(userName, user);
      } catch (TransferObjectException e){
        logLoginFailure(userName, e.getMessage());
        return getLoginFailedResult(e.getTransferObject().getMessage());
      }
      message = loginChecker.getLoginMessage(user);
    }
    long rnd = -1;
    if (!isTransient) {
      // Is the license in a warning mode? Has the user been warned yet?
      TransferObject warning = this.warning.getLicenseWarning(userName);
      if (warning != null){
        return warning;
        
      }
      // login succeeded - create a new Session
      rnd = getStore().createSession(userName, ssoSession);
    } else {
      rnd = getTransientStore().createSession(userName, ssoSession);
    }
    UserBean bean;
    try {
      UserRoleBean[] availableRoles = getRoles(userName);
      bean = buildUserBean(user, filterRoles(availableRoles, ssoSession));
    } catch (SQLException e){
      throw new RuntimeException("Database error occurred retrieving roles", e);
    }
    if (!isTransient) {
      try {
        updateUserLogonStatus(user, true);
      } catch (AbortTransactionException e) {
        throw new RuntimeException("Database error occurred updating login status", e);
      } catch (SQLException e) {
        throw new RuntimeException("Database error occurred updating login status", e);
      }
    }
    bean.setSession(rnd);
    audit(userName, LOGIN, "login", null, message, TransferObject.SUCCESS, new Timestamp(System
        .currentTimeMillis()));
    getAttemptTracker().successfulLogin(userName);
    return new TransferObject(bean, TransferObject.SUCCESS, message);
  }
  private UserRoleBean[] filterRoles(UserRoleBean[] availableRoles, ISingleSignOnSession ssoSession) {
    if (ssoSession == null) {
      return availableRoles;
    }
    for (int i = 0; i < availableRoles.length; i++) {
      if (availableRoles[i].getRoleId().longValue() == ssoSession.getRole()) {
        return new UserRoleBean[] {availableRoles[i]};
      }
    }
    //We return a "dummy" role if the external system doesn't offer a matching role
    return new UserRoleBean[] {new UserRoleBean(ssoSession.getRole(), "-")};
  }

  @Override
  public TransferObject resetPassword(String userName) {
    return new TransferObject(TransferObject.ERROR, "RESEST_PASSWORD_UNSUPPORTED");
  }


  private TransferObject getLoginFailedResult(String desiredCode) {
    if (isDetailedLoginFailuresEnabled()){
      return new TransferObject(TransferObject.ERROR, desiredCode);
    } else {
      return new TransferObject(TransferObject.ERROR, PassUtils.ACCESS_DENIED);
    }
  }
  
  private boolean isDetailedLoginFailuresEnabled() {
    ServiceEngine.refreshConfig();
    return ServiceEngine.getProp(DETAIL_ERROR_CODE, "true").equals("true");
  }

  protected ILoginSessionStore getTransientStore(){
    if (transientSessions == null) {
      transientSessions = buildTransientSessionStore();
    }
    return transientSessions;
  }
  
  protected ILoginSessionStore buildTransientSessionStore(){
    return new LoginSessionStore(){
      @Override
      public void handleSessionTimeout(long session, String userId, Date lastRequestTime) {
        // This will never happen with transient sessions 
      }};
  }

  private Map<String, Object> getUserForLogin(final String userName) {
    Map<String, Object> user = null;
    try {
      user = getUser(userName);
    } catch (SQLException e){
      getLog().emit(LoginServiceLogClient.ERROR_FETCHING_USER, e);
      throw new RuntimeException("Database error retrieving user", e);
    }
    return user;
  }

  private TransferObject returnNoSuchUser(final String userName) {
    logLoginFailure(userName, "User is not exist");
    return getLoginFailedResult(PassUtils.USER_NOT_EXIST);
  }
  
  public TransferObject logoffTransient(final String userName, final long passWord) {
    return logoff(userName, passWord, true);
  }
  
  public final TransferObject logoff(final String userName, final long session) {
    return logoff(userName, session, false);
  }
  
  private final TransferObject logoff(final String userName, final long sessionId, boolean isTransient) {
    Long reqSession = ServerSession.THREAD_SESSION.get();
    if (sessionId != reqSession) {
      throw new IllegalArgumentException("Logging off unmatched session Id");
    }
    final Map<String, Object> user = getUserForLogin(userName);
    if (user == null) {
        audit(userName, LOGIN, "logoff", null, "User not found", TransferObject.ERROR, new Timestamp(System.currentTimeMillis()));
        return new TransferObject(TransferObject.ERROR, null);
    }
    final int flag;
    String message = null;
    if (isValidSession(userName, sessionId, isTransient)) {
      if (isTransient) {
        getTransientStore().terminateSession(sessionId);
      } else {
        Object sessionAttachedData = getStore().getSessionAttachedData(sessionId);
        if (sessionAttachedData instanceof ISingleSignOnSession) {
          try {
          getSingleSignOnImplementation().logoff(userName, sessionAttachedData);
          } catch (SingleSignOnConfigurationException | SingleSignOnCommunicationException e) {
            getLog().emit(LoginServiceLogClient.SSO_LOGOFF_ERROR, e, userName);
          }
        }
        try {
          updateUserLogonStatus(user, false);
          getStore().terminateSession(sessionId);
        } catch (AbortTransactionException e) {
          return new TransferObject(TransferObject.ERROR, "LOGOFF_ABORTED");
        } catch (SQLException e) {
          return new TransferObject(TransferObject.ERROR, "LOGOFF_FAILED");
        }
      }
      flag = TransferObject.SUCCESS;
    } else {
        flag = TransferObject.ERROR;
        message = PassUtils.ACCESS_DENIED;
    }
    TransferObject transferObject = new TransferObject(flag, message);
    audit(userName, LOGIN, "logoff", null, transferObject.getMessage(), flag, new Timestamp(System.currentTimeMillis()));
    return transferObject;
  }
  
  private boolean isValidSession(String userName, long sessionId,
      boolean isTransient) {
    String user = null;
    if (isTransient) {
      user = getTransientStore().getUser(sessionId);
    } else {
      user = getStore().getUser(sessionId);
    }
    return user != null && user.equals(userName);
  }

  public void updateUserLogonStatus(final Map<String, Object> user, final boolean login) throws AbortTransactionException, SQLException {
    if (user!=null) {
      Character s = login ? UserBusinessBean.LOGIN_ACTIVE : UserBusinessBean.LOGIN_INACTIVE;
      user.put(LOGON_STATUS, s.toString());
      getPrivateProvider().execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          getPrivateProvider().update(UPDATE_LOGIN_STATUS, user, t);
        }});
    }
  }
  
  public UserRoleBean[] getRoles(String userName) throws SQLException {
    List<UserRoleBean> l = new ArrayList<UserRoleBean>();
    MapCar.map(new MapCar(){
      public Object map(Object o) {
        Map m = (Map) o;
        UserRoleBean b = new UserRoleBean();
        b.setDescription((String) m.get(DESCRIPTION));
        b.setName((String) m.get(NAME));
        b.setRoleId((Double) m.get("ID"));
        return b;
      }}, 
    getPrivateProvider().query(SqlMapProvider.LIST, GET_USER_ROLES, userName), l);
    return (UserRoleBean[]) l.toArray(new UserRoleBean[0]);
  }

  private UserBean buildUserBean(final Map<String, Object> user, UserRoleBean[] roles) {
    final UserBean bean = new UserBean();
    bean.putAll(user);
    bean.setUserId((String) user.get(USER_ID));
    bean.setPassword((String) user.get(PASSWORD));
    bean.setEmailAddress((String) user.get(USER_EMAIL_ADDRESS));
    bean.setRoleBeans(Arrays.asList(roles));
    bean.setPasswordExpiry((Date) user.get(PASSWD_EXP_DATE));
    return bean;
  }

  public TransferObject reloadConnections() {
    IReadWriteDataProvider p0 = getReadWriteProvider();
    p0.reload();
    return new TransferObject();
  }


  public TransferObject checkPassword(final String userName,
			final String password) {
    if (password.trim().length() < password.length()) {
			return new TransferObject(TransferObject.ERROR,
					"Password can not begin or end with whitespace");
    }

		// check that the new password is at most 32 characters long
		if (password.length() > PASSWORD_COLUMN_WIDTH) {
			return new TransferObject(
					TransferObject.ERROR,
					"Password is too long, pick a shorter password (15 characters or less recommended)");
		}

		Map user = getUserForLogin(userName);
		if (user == null) {
			return new TransferObject(); // it's new User, no history
											// checking required
		}

		List passwords = null;
		try {
			passwords = getAllUserPasswordHistory(userName);
		} catch (SQLException e) {
		  getLog().emit(LoginServiceLogClient.QUERY_PASSWORD_HISTORY_FAILED, e);
			return new TransferObject(TransferObject.EXCEPTION,
					"QUERY_PASSWORD_HISTORY_FAILED");
		}
		final List passwordHistoriesToDelete = trimPasswordHistory(passwords);
		// don't allow the same password
		for (int i = 0; i < passwords.size(); i++) {
			final Map history = (Map) passwords.get(i);
			if (password.equals(history.get(PASSWORD))) {
				return new TransferObject(TransferObject.ERROR,
						"Password is the same as a previously used password");
			}
		}
		return new TransferObject();
	}


  public TransferObject changePassword(final String userName, final String newUserPassword,
      final String currentPassword) {
    Long session = ServerSession.THREAD_SESSION.get();
    String sessionUser = getSessionUser(session);
    if (sessionUser == null || !sessionUser.equals(userName)) {
      return new TransferObject(TransferObject.ERROR,
      "Password change failed");
    }
    synchronized (this) {
      try {
        if (!isUserPassword(getUser(userName), userName, currentPassword)) {
          try {
            // This makes it impractical to brute-force a user's password using
            // this method since the pause is at the server-level due to the sync
            Thread.sleep(BAD_CHANGE_WAIT);
          } catch (InterruptedException e) { //NOPMD Nothing to do here on exception
          }
          return new TransferObject(TransferObject.ERROR,
          "Current password incorrect");
        }
      } catch (SQLException e){
        getLog().emit(LoginServiceLogClient.ERROR_FETCHING_USER, e);
        throw new RuntimeException("Database error retrieving user", e);
      }
    }
    if (newUserPassword.trim().length() < newUserPassword.length()){
      return new TransferObject(TransferObject.ERROR,
        "Password can not begin or end with whitespace");
    }
    final String newPassword = (String) encrypt(userName, newUserPassword).getBeanHolder();
    Map<String, Object> user = getUserForLogin(userName);
    if (user == null) {
        return new TransferObject(TransferObject.ERROR, "User not found");
    }
    List passwords = null;
    try {
     passwords = getAllUserPasswordHistory(userName);
    } catch (SQLException e){
      getLog().emit(LoginServiceLogClient.QUERY_PASSWORD_HISTORY_FAILED, e);
      return new TransferObject(TransferObject.EXCEPTION, "QUERY_PASSWORD_HISTORY_FAILED");
    }
    final List passwordHistoriesToDelete = trimPasswordHistory(passwords);
    // don't allow the same password
    for (int i = 0; i < passwords.size(); i++) {
        final Map history = (Map) passwords.get(i);
        if (newPassword.equals(history.get(PASSWORD))) {
            return new TransferObject(TransferObject.ERROR,
                "Password is the same as a previously used password");
        }
    }

    // check that the new password is at most 32 characters long
    if (newPassword.length() > PASSWORD_COLUMN_WIDTH) {
        return new TransferObject(TransferObject.ERROR,
            "Password is too long, select a shorter password");
    }
    
    final Map<String, Object> u = user;
    final Calendar now = Calendar.getInstance();
    try {
      getPrivateProvider().execute(new IRunnableTransaction(){

        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          for(Iterator i = passwordHistoriesToDelete.iterator(); i.hasNext();){
            Map ph = (Map) i.next();
            getPrivateProvider().delete(DELETE_PASSWORD_HISTORY, ph, t);
          }
          
          Map<String, Object> ph = new HashMap<String, Object>();
          ph.put(USER_ID, userName);
          ph.put(PASSWORD, newPassword);
          ph.put(PASSWORD_DATE, new Timestamp(now.getTimeInMillis()));
          getPrivateProvider().insert(INSERT_PASSWORD_HISTORY, ph, t);
          u.put(PASSWORD, newPassword);
          
          now.add(Calendar.DAY_OF_YEAR, passwordAge);
          if (u.get(PASSWD_EXP_DATE)!=null) {  //if password exp date is previously null then this password never expired
             final Timestamp passStamp = new Timestamp(now.getTimeInMillis());
             u.put(PASSWD_EXP_DATE, passStamp);
          }
          getPrivateProvider().update(UPDATE_PASSWORD, u, t);
        }
        
      });
    } catch (AbortTransactionException e) {
      getLog().emit(LoginServiceLogClient.PASSWORD_CHANGE_FAILED, e, userName);
      return new TransferObject(TransferObject.ERROR, "PASSWORD_CHANGE_ABORTED");
    } catch (SQLException e) {
      getLog().emit(LoginServiceLogClient.PASSWORD_CHANGE_FAILED, e, userName);
      return new TransferObject(TransferObject.ERROR, "PASSWORD_CHANGE_FAILED"); 
    }
    return new TransferObject(now.getTime());
  }

  private List<Map<String, Object>> trimPasswordHistory(List<Map<String, Object>> passwords) {
    final List<Map<String, Object>> passwordHistoriesToDelete = new ArrayList<Map<String, Object>>();
    if (passwords.size() > historySize) { // do not allow user's password history to grow larger than history size limit
      List<Map<String, Object>> sorted = MapListUtil.sortBy(new String[]{PASSWORD_DATE}, true, passwords);
      if (historySize < passwords.size()) { // Remove all old passwords
        for (int i = 0; sorted.size() > historySize;) {
          Map<String, Object> history = sorted.remove(i);
          passwordHistoriesToDelete.add(history);
        }
      }
    }
    passwords.removeAll(passwordHistoriesToDelete);
    return passwordHistoriesToDelete;
  }

  private List getAllUserPasswordHistory(final String userName) throws SQLException {
    List l = new ArrayList();
    MapCar.map(new MapCar(){
      public Object map(Object o) {
        return o;
      }}, getPrivateProvider().query(SqlMapProvider.LIST, GET_USER_PASSWORD_HISTORY, userName), l);
    return l;
  }


  public TransferObject getRoleRights(long roleId) {
    Long session = ServerSession.THREAD_SESSION.get();
    Object sessionAttachedData = getStore().getSessionAttachedData(session);
    
    if (!(sessionAttachedData instanceof ISingleSignOnSession) && getRoleForSession(session) != roleId) {
      throw new RuntimeException("Invalid session for role access rights retrieval");
    }
    if (sessionAttachedData instanceof ISingleSignOnSession) {
      ISingleSignOnSession s = (ISingleSignOnSession) sessionAttachedData;
      try {
        List<AccessRightsBean> l = getSingleSignOnImplementation().getAccessRights(sessionAttachedData);
        return new TransferObject(l);
      } catch (SingleSignOnCommunicationException|SingleSignOnConfigurationException e1) {
        getLog().emit(LoginServiceLogClient.ERROR_FETCHING_ACCESS_RIGHTS, e1);
        return new TransferObject(TransferObject.EXCEPTION, "SSO_ERROR");
      }
    } else {
      try {
        List<AccessRightsBean> l = getAccessRights(roleId);
        return new TransferObject(l);
      } catch (SQLException e1) {
        getLog().emit(LoginServiceLogClient.ERROR_FETCHING_ACCESS_RIGHTS, e1);
        return new TransferObject(TransferObject.EXCEPTION, "DATABASE_ERROR");
      }
    }
  }

  public List<AccessRightsBean> getAccessRights(long roleId) throws SQLException {
    Iterator iterator = getPrivateProvider().query(SqlMapProvider.LIST, "getRoleAccessRights", roleId);
    List<AccessRightsBean> l = new ArrayList<AccessRightsBean>();
    final Map parentMapping = new HashMap();
    final Map objects = new HashMap();
    MapCar.map(new MapCar(){
      public Object map(Object o) {
        Map db = (Map) o;
        AccessRightsBean b = new AccessRightsBean();
        b.setAccessRightsId(new Double(((Number)db.get("ID")).doubleValue()));
        b.setAccessRightsDesc((String) db.get(DESCRIPTION));
        b.setParentBean(null);
        b.setDisabled(((Boolean)db.get("DISABLE")).booleanValue());
        Object s = db.get("SORT_PRIORITY");
        b.setSortPriority(s == null ? 0 : ((Number)s).intValue());
        parentMapping.put(b.getAccessRightsId(), db.get("PARENT_ID"));
        objects.put(b.getAccessRightsId(), b);
        return b;
      }}, iterator, l);
    for (Iterator iter = l.iterator(); iter.hasNext();) {
      AccessRightsBean b = (AccessRightsBean) iter.next();
      Number parent = (Number) parentMapping.get(b.getAccessRightsId());
      if (parent != null){
        b.setParentBean((AccessRightsBean) objects.get(new Double(parent.doubleValue())));
      }
    }
    return l;
  }
  
  public TransferObject getSessionRole(long session) {
    return new TransferObject(getRoleForSession(session));
  }

  private Long getRoleForSession(long session) {
    return getStore().getSessionRole(session);
  }
  
  public TransferObject setSessionRole(long session, long roleId) {
    ILoginSessionStore store = getStore();
    String user = store.getUser(session);
    if (user == null) {
      store = getTransientStore();
      user = store.getUser(session);
    }
    try {
      UserRoleBean[] roles = getRoles(user);
      for (int i = 0; i < roles.length; i++) {
        long role = roles[i].getRoleId().longValue();
        if (role == roleId) {
          store.setSessionRole(session, roleId);
          return new TransferObject();    
        }
      }
      return new TransferObject(new Object[]{user, roleId}, TransferObject.ERROR, "USER_NOT_ASSIGNED_ROLE");
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    
  }

  public static class PowerAppsModule implements LoginModule {

  	private final PowerLoginModule plm = new PowerLoginModule();
  	
		public boolean abort() throws LoginException {
			return plm.abort();
		}

		public boolean commit() throws LoginException {
			return plm.commit();
		}

		public void initialize(Subject subject, CallbackHandler callbackHandler,
				Map<String, ?> sharedState, Map<String, ?> options) {
			plm.initialize(subject, callbackHandler, sharedState, options);
		}

		public boolean login() throws LoginException {
			return plm.login();
		}

		public boolean logout() throws LoginException {
			return plm.logout();
		}
  
  }
  
  public static class LdapPowerAppsModule implements LoginModule{
  	private final PowerLoginModule plm = new PowerLoginModule(PowerLoginModule.LDAP);
  	
		public boolean abort() throws LoginException {
			return plm.abort();
		}

		public boolean commit() throws LoginException {
			return plm.commit();
		}

		public void initialize(Subject subject, CallbackHandler callbackHandler,
				Map<String, ?> sharedState, Map<String, ?> options) {
			plm.initialize(subject, callbackHandler, sharedState, options);
		}

		public boolean login() throws LoginException {
			return plm.login();
		}

		public boolean logout() throws LoginException {
			return plm.logout();
		}

  }

  private Configuration getConfiguration() throws LoginException{
  	final String moduleName = ServiceEngine.getProp("system.loginmodule.class", PowerAppsModule.class.getName());
  	try{
  		Reflect.invokeConstructor(moduleName, new Class[0], new Object[0]);
  	}catch(Exception e){
  		throw new LoginException(e.getMessage());
  	}
  	final LoginModuleControlFlag flag = LoginModuleControlFlag.REQUIRED; 
  	final Map<String, String> options = new HashMap<String, String>();
  	String domains = ServiceEngine.getProp("system.loginmodule.ldap.domain");
  	if(domains!=null){
  		options.put("domain",domains);
  	}
  	return new Configuration(){
			public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
				return new AppConfigurationEntry[]{new AppConfigurationEntry(moduleName, flag, options)};
			}

			public void refresh() {
			  // We don't refresh anything here
			}	
  	};
  }
 
  public TransferObject encrypt(String u, String cleartext) {
    if (getPasswordHashMode().equals(HashMode.None)) {
      return new TransferObject(getDes3Encrypted(cleartext));
    } else {
      return new TransferObject(PasswordHasher.hash(u, cleartext));
    }
  }

  protected String getDes3Encrypted(String cleartext) {
    Map m = (Map) getPasswordConstraints().getBeanHolder();
    String encryptionKey = (String) m.get(PassUtils.APP_CRYPTKEY);
    return PassUtils.encrypt(cleartext.toCharArray(), encryptionKey);
  }
  
  public TransferObject getEncrypted(String u) {
    try {
      Map user = getUser(u);
      return new TransferObject(user.get("PASSWORD"));
    } catch (SQLException e) {
      throw new RuntimeException(e); 
    }
  }
  
  private String getAppCryptKey() {
    final String file = ServiceEngine.getConfig(false).getProperty(PassUtils.APP_CRYPTKEY);
    if (null == file) {
      getLog().emit(LoginServiceLogClient.APP_KEY_NOT_SPECIFIED);
      throw new RuntimeException("Application crypt key file is not specified in server configuration");
    }
    File f = new File(file);
    getLog().emit(LoginServiceLogClient.READING_APP_KEY, file);
    try {
      return FileUtil.readFirstLine(f);
    } catch (Exception ex) {
      getLog().emit(LoginServiceLogClient.APP_KEY_READ_ERROR, ex, file, f.getAbsolutePath());
      throw new RuntimeException("Unable to read encryption key from file " + file, ex);
    }
  }

  public boolean isUsingHashedPasswords() {
    HashMode mode = getPasswordHashMode();
    return mode != HashMode.None;
  }

  public HashMode getPasswordHashMode() {
    final String modeProp = ServiceEngine.getConfig(false).getProperty(PASSWORD_HASH, "strict");
    if (modeProp.equals("none")) {
      return HashMode.None;
    } else if (modeProp.equals("transitional")) {
      return HashMode.Transitional;
    } else if (modeProp.equals("strict")) {
      return HashMode.Strict;
    } else {
      throw new IllegalArgumentException("Illegal value for " + PASSWORD_HASH + " of " + modeProp + " expected one of none, transitional, or strict");
    }
  }

  public boolean isUserPassword(Map user, String name, String cleartext) {
    String encrypted = (String) encrypt(name, cleartext).getBeanHolder();
    final String userPass = (String) user.get(PASSWORD);
    boolean isMatching = encrypted.equals(userPass);
    if (!isMatching && getPasswordHashMode() == MapLoginService.HashMode.Transitional) {
      isMatching = getDes3Encrypted(cleartext).equals(userPass);
    }
    return isMatching;
  }

  public boolean isAuthorized(long session, long authorizationId) {
    Long sessionRole = getRoleForSession(session);
    try {
      List<AccessRightsBean> accessRights = getAccessRights(sessionRole);
      for (AccessRightsBean a : accessRights) {
        if (a.getAccessRightsId().longValue() == authorizationId) {
          return true;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return false;
  }

  @Override
  protected ILoginSessionStore buildSessionStore() {
    return new LoginSessionStore() {
      @Override
      public void handleSessionTimeout(long session, String userId, Date lastRequestTime) {
        triggerServerTimeoutLogoutEvent(session);
      }
    };
  }

  protected void triggerServerTimeoutLogoutEvent(long session) {
    LookupManager lm = LookupManager.getInstance();
    IMessageHandler h = (IMessageHandler) lm.getLookupItem(LookupManager.SYSTEM, "MessageHandler");
    Map<String, Object> context = new HashMap<String, Object>();
    context.put("session", session);
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("TIMEOUT", true);
    Class[] types = new Class[] {String.class, Map.class};
    Object[] args = new Object[] {"login.logoff", params};
    h.handleMessage(null, "EventService", "sendEvent", types, args, context);
  }

  @Override
  public TransferObject loginViaSingleSignOnTicket(String ticket) {
    try {
      ISingleSignOnImplementation sso = getSingleSignOnImplementation();
      ISingleSignOnSession ssoSession = sso.getAuthenticatedUser(ticket, getPrivateProvider());
      if (ssoSession == null) {
        return new TransferObject(TransferObject.ERROR, "SSO_LOGIN_FAILED");
      }
      Map<String, Object> user = getUserForLogin(ssoSession.getUserName());
      TransferObject to = createSession(user, false, false, ssoSession);
      // This should never result in a login failure.
      sso.acknowledgeLogin(ssoSession);
      return to;
    } catch (SingleSignOnCommunicationException | SingleSignOnConfigurationException e) {
      getLog().emit(LoginServiceLogClient.SSO_LOGIN_FAILED, e, ticket);
      return new TransferObject(TransferObject.ERROR, "SSO_LOGIN_FAILED");
    }
  }

  private ISingleSignOnImplementation getSingleSignOnImplementation() throws SingleSignOnConfigurationException {
    String impl = ServiceEngine.getProp(SSO_IMPLEMENTATION, "com.profitera.services.business.sso.SilverLakeAccessItNowSingleSignOn");
    try {
      ISingleSignOnImplementation i = (ISingleSignOnImplementation) Reflect.invokeConstructor(impl, null, null);
      i.setProperties(ServiceEngine.getConfig(true));
      return i;
    } catch (ReflectionException e) {
      getLog().emit(LoginServiceLogClient.SSO_CONFIG_ERROR, e, impl, SSO_IMPLEMENTATION);
      throw new SingleSignOnConfigurationException("Unable to instantiate SSO provider defined by " + SSO_IMPLEMENTATION + ": " + impl, e);
    }
  }

  public Object getSessionAttachedData(long session) {
    return getStore().getSessionAttachedData(session);
  }
}