package com.profitera.server.impl;

import com.profitera.log.ILogClient;
import com.profitera.log.ILogProvider;
import com.profitera.log.ILogProvider.Level;

public class ServiceEngineLog implements ILogClient {

  public static final String BUILD_LOGIN = "BUILD_LOGIN";
  public static final String BUILD_LOGIN_FAILURE = "BUILD_LOGIN_FAILURE";
  public static final String BOUND = "BOUND";
  public static final String NOT_BOUND = "NOT_BOUND";
  public static final String STARTED = "SERVER_STARTED";
  public static final String START_FAILED = "SERVER_START_FAILED";
  public static final String REFRESH_PROPS = "RELOAD_SERVER_PROPS";
  public static final String REFRESH_PROPS_FAIL = "RELOAD_SERVER_PROPS_FAIL";
  public static final String INTERCEPTOR_ERROR = "INTERCEPTOR_ERROR";
  public static final String VERIFY_MEM_FAIL = "VERIFY_MEM_FAIL";
  public static final String NO_VERIFY_MEM = "VERIFY_MEM_DISABLED";
  public static final String DB_VERSION = "DB_VERSION";
  public static final String DB_DRIVER_VERSION = "DB_DRIVER_VERSION";
  public static final String UNCAUGHT_SERVER_ERROR = "UNCAUGHT_SERVER_ERROR";
  public static final String DB_CONNECTION_CHECK = "DB_CONNECTION_CHECK";

  public String getName() {
    return "Server";
  }

  public void registerMessages(ILogProvider p) {
    p.registerMessage(this, BUILD_LOGIN, Level.I, "Building login service {0}", 
        "Login service is about to be constructed using the configured implementation.");
    p.registerMessage(this, BUILD_LOGIN_FAILURE, Level.E, "Failed to build login service \"{0}\" as specified by server property \"{1}\"", 
    "Login service could not be constructed using the configured implementation.");
    p.registerMessage(this, BOUND, Level.I, "Successfullly bound service {0}", 
    "Indicates that a service was registered successfully.");
    p.registerMessage(this, NOT_BOUND, Level.E, "Trouble binding service {0}", 
    "Indicates that a service was not registered successfully.");
    p.registerMessage(this, STARTED, Level.I, "### PROFITERA SERVER STARTED SUCCESSFULLY ###", 
    "Indicates that the server was started successfully.");
    p.registerMessage(this, REFRESH_PROPS, Level.D, "Refreshing server properties", 
        "An internal request was made to refresh the server properties by reloading them.");
    p.registerMessage(this, REFRESH_PROPS_FAIL, Level.E, "Exception occured while refreshing server properties", 
    "An internal request was made to refresh the server properties by reloading them and the attempt to reload them failed.");
    p.registerMessage(this, INTERCEPTOR_ERROR, Level.E, "Failed to load server request interceptor {0}",
        "The server startup procedure was unable to build a required request interceptor causing a general failure.");
    p.registerMessage(this, VERIFY_MEM_FAIL, Level.E, 
        "Memory settings verification failed for server {0} . Verification can be temporarily disabled by setting server property \"{1}\" to \"false\"'",
        "Memory verification failed for this server for the reason indicated, this must either be rectified or the verification turned off by setting the property indicated.");
    p.registerMessage(this, NO_VERIFY_MEM, Level.W, 
        "Memory settings verification disabled for server",
        "Memory verification is disabled and the memory settings will not be verified at start up.");
    p.registerMessage(this, DB_VERSION, Level.I, 
        "Database version {0}",
        "Database Version");
    p.registerMessage(this, DB_DRIVER_VERSION, Level.I, 
        "Database driver version {0}",
        "Database Driver Version");
    p.registerMessage(this, DB_CONNECTION_CHECK, Level.D, 
        "Attempting database connection for data source {0}",
        "A start-up time check to ensure database connectivity so that a problem with the database can be addressed immediately.");
    p.registerMessage(this, UNCAUGHT_SERVER_ERROR, Level.E, 
        "Error or exception in thread {0}",
        "An error or exception was thrown and uncaught by service executing.");
    p.registerMessage(this, START_FAILED, Level.E, 
        "Fatal exception starting server",
        "An error or exception was thrown and caused server startup to abort.");
    
  }

}
