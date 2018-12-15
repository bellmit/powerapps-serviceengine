package com.profitera.services.business.login.impl;

public class LoginDatabaseException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  public LoginDatabaseException(String message, Exception e) {
    super(message, e);
  }
}
