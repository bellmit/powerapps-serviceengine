package com.profitera.services.business.login;

public class SingleSignOnConfigurationException extends Exception {
  private static final long serialVersionUID = 1L;
  public SingleSignOnConfigurationException(String message, Exception e) {
    super(message, e);
  }
}
