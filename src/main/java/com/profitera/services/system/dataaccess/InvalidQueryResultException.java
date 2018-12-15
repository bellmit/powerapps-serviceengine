package com.profitera.services.system.dataaccess;


/**
 * @author jamison
 */
public class InvalidQueryResultException extends RuntimeException {
  public InvalidQueryResultException() {super();}
  public InvalidQueryResultException(String message) {super(message);}
  public InvalidQueryResultException(String message, Throwable cause) {super(message, cause);}
  public InvalidQueryResultException(Throwable cause) {super(cause);}
}