package com.profitera.services.system.report;

import com.profitera.util.exception.GenericException;

public class ReportGenerationException extends GenericException {

  private static String ERROR_CODE = "RPT_GEN_ERROR";
  
  public ReportGenerationException() {
    super();

  }

  public ReportGenerationException(String message) {
    super(message);

  }

  public ReportGenerationException(Throwable cause) {
    super(cause);
  }

  public ReportGenerationException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public String getErrorCode(){
    return ERROR_CODE;
  }

}
