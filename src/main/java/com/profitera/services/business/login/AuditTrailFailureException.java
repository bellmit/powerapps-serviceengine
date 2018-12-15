package com.profitera.services.business.login;

public class AuditTrailFailureException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  public AuditTrailFailureException(String message, Throwable cause){
    super(message, cause);
  }
}
