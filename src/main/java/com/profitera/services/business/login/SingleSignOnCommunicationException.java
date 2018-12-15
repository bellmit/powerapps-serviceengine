package com.profitera.services.business.login;

public class SingleSignOnCommunicationException extends Exception {
  private static final long serialVersionUID = 1L;
  public SingleSignOnCommunicationException(Exception e) {
    super(e);
  }
  public SingleSignOnCommunicationException(String message) {
    super(message);
  }
}
