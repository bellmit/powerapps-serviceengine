package com.profitera.services.system.report;

import com.profitera.util.exception.GenericException;

public class ReportCompilationException extends GenericException {
  
  private static final String ERROR_CODE = "RPT_COMPILATION_ERROR";

  public ReportCompilationException() {
    super();
  }

  public ReportCompilationException(String message, Throwable cause) {
    super(message, cause);
  }

  public ReportCompilationException(String message) {
    super(message);
  }

  public ReportCompilationException(Throwable cause) {
    super(cause);
  }
  
  public String getErrorCode(){
    return ERROR_CODE;
  }

}
