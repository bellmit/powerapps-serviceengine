package com.profitera.services.business.login;

public class SessionRequiredException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  public SessionRequiredException(String message, Throwable cause){
    super(message, cause);
  }
}
