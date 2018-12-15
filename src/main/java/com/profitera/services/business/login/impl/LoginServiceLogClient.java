package com.profitera.services.business.login.impl;

import com.profitera.log.ILogClient;
import com.profitera.log.ILogProvider;
import com.profitera.log.ILogProvider.Level;
import com.profitera.util.PassUtils;

public class LoginServiceLogClient implements ILogClient {

  public static final String APP_KEY_NOT_SPECIFIED = "APP_KEY_NOT_SPECIFIED";
  public static final String AUDIT_FAIL = "AUDIT_INSERT_FAILURE";
  public static final String READ_SECURITY_SETTING = "READ_SECURITY_SETTING";
  public static final String PASSWORD_CHANGE_FAILED = "PASSWORD_CHANGE_FAILED";
  public static final String ERROR_FETCHING_SECURITY = "ERROR_FETCHING_SECURITY_SETTINGS";
  public static final String QUERY_PASSWORD_HISTORY_FAILED = "QUERY_PASSWORD_HISTORY_FAILED";
  public static final String READING_APP_KEY = "READING_APP_KEY";
  public static final String APP_KEY_READ_ERROR = "APP_KEY_READ_ERROR";
  public static final String ERROR_FETCHING_ACCESS_RIGHTS = "ERROR_FETCHING_ACCESS_RIGHTS";
  public static final String ERROR_FETCHING_USER = "ERROR_FETCHING_USER";
  public static final String SERVER_SESSION_TIMEOUT_BAD = "SESSION_TIMEOUT_INVALID";
  public static final String SSO_CONFIG_ERROR = "SSO_CONFIG_ERROR";
  public static final String SSO_LOGIN_FAILED = "SSO_LOGIN_FAILED";
  public static final String SSO_LOGOFF_ERROR = "SSO_LOGOFF_ERROR";
  @Override
  public void registerMessages(ILogProvider provider) {
    provider.registerMessage(this, AUDIT_FAIL, Level.E, "Audit trail insert failed for request", 
        "The insert of the audit log record recording a client request failed with the indicated error.");
    provider.registerMessage(this, READ_SECURITY_SETTING, Level.I, "Security setting {0}: {1}", 
        "Indicates the value for the security setting read from the database.");
    provider.registerMessage(this, PASSWORD_CHANGE_FAILED, Level.E, "An error occurred changing password for {0}", 
        "A database error occurred changing the password for a user.");
    provider.registerMessage(this, ERROR_FETCHING_SECURITY, Level.E, "An error occurred retrieving security settings", 
    "A database error occurred loading security settings, all user logins will fail.");
    provider.registerMessage(this, QUERY_PASSWORD_HISTORY_FAILED, Level.E, "An error occurred retrieving user password history", 
    "A database error occurred loading a user's password history.");
    provider.registerMessage(this, APP_KEY_NOT_SPECIFIED, Level.E, "Application crypt key file is not specified by property " + PassUtils.APP_CRYPTKEY + " in server configuration", 
        "If the legacy or mixed application password encryption mechanism is employed the application encryption key is requred and has been found to be not specified.");
    provider.registerMessage(this, READING_APP_KEY, Level.I, "Reading application crypt key file from {0}", 
    "If the legacy or mixed application password encryption mechanism is employed the application encryption key is then read from the path indicated.");
    provider.registerMessage(this, APP_KEY_READ_ERROR, Level.E, "Error reading application crypt key specified as {0}, from {1}", 
    "If the legacy or mixed application password encryption mechanism is employed the application encryption key could not be read from the path indicated.");
    provider.registerMessage(this, ERROR_FETCHING_ACCESS_RIGHTS, Level.E, "Error reading user access rights", 
    "A database occurred reading user access rights, this is most likely due to an error in the structure of the database used.");
    provider.registerMessage(this, ERROR_FETCHING_USER, Level.E, "Error reading user information from the database", 
    "A database occurred loading a user from the database, this is most likely due to an error in the structure of the database used.");
    provider.registerMessage(this, SERVER_SESSION_TIMEOUT_BAD, Level.W, "Unable to parse numeric value for property {0} with value \"{1}\", no server session timeout will be applied", 
        "A value was specified for the server-side session timeout but that value was not a valid number.");
    provider.registerMessage(this, SSO_CONFIG_ERROR, Level.E, "Unable to load or configure SSO implementation {0} defined by property {1}", 
        "A system configuration error is preventing the loading of the single sign on system to be used.");
    provider.registerMessage(this, SSO_LOGIN_FAILED, Level.E, "Unable to login user with provided SSO ticket {0}", 
        "An error is preventing the login of a user via the single sign on system to be used.");
    provider.registerMessage(this, SSO_LOGOFF_ERROR, Level.W, "Unable to logoff user {0} local logoff will proceed", 
        "An error is preventing the logoff of a user via the single sign on system, this error will not stop the logoff of the user locally but the host may have not been properly notified.");

  }

  @Override
  public String getName() {
    return "Login";
  }
}
