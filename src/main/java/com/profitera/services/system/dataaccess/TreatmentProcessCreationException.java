package com.profitera.services.system.dataaccess;

import com.profitera.util.exception.GenericException;

public class TreatmentProcessCreationException extends GenericException {

  public TreatmentProcessCreationException() {
    super();
  }

  public TreatmentProcessCreationException(String message) {
    super(message);
  }

  public TreatmentProcessCreationException(String message, String errorCode) {
    super(message, errorCode);
  }

  public TreatmentProcessCreationException(Throwable cause) {
    super(cause);
  }

  public TreatmentProcessCreationException(String message, Throwable cause) {
    super(message, cause);
  }

}
