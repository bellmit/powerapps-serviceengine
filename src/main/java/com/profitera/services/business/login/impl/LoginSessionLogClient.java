package com.profitera.services.business.login.impl;

import com.profitera.log.ILogClient;
import com.profitera.log.ILogProvider;
import com.profitera.log.ILogProvider.Level;

public class LoginSessionLogClient implements ILogClient {

  public static final String LOGIN_DIGESTER_FAILED = "LOGIN_DIGESTER_FAILED";
  public static final String RETRIEVE = "LOGIN_RETRIEVE_FAILED";
  public static final String STORE = "LOGIN_STORE_FAILED";
  public static final String TERMINATE = "LOGIN_TERMINATE_FAILED";
  public static final String TERMINATE_NO_SESSION = "LOGIN_TERMINATE_NO_SESSION";
  
  //
  private static final String GET_ERROR_MESSAGE = "Failed to retrieve login session";
  private static final String STORE_ERROR_MESSAGE = "Failed to store login session for user {0}";
  private static final String TERM_ERROR_MESSAGE = "Failed to remove login session for user {0}";

  @Override
  public void registerMessages(ILogProvider provider) {
    provider.registerMessage(this, LOGIN_DIGESTER_FAILED, Level.W, "Digester intialization failed for algorithm {0}, session keys can not be hashed",
        "In the unlikely event that the session digester can not be intialized this message will be displayed.");
    provider.registerMessage(this, RETRIEVE, Level.E, GET_ERROR_MESSAGE, "A database error occurred retrieving login session information");
    provider.registerMessage(this, STORE, Level.E, STORE_ERROR_MESSAGE, STORE_ERROR_MESSAGE);
    provider.registerMessage(this, TERMINATE, Level.E, TERM_ERROR_MESSAGE, TERM_ERROR_MESSAGE);
    provider.registerMessage(this, TERMINATE_NO_SESSION, Level.W, "Termination of session for {0} did not have a session in the database",
        "There was an attempt by a client application to terminate a session that either does not exist or was already terminated.");
  }

  @Override
  public String getName() {
    return "Login Session";
  }

}
