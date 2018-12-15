package com.profitera.services.system.financial;

import com.profitera.dataaccess.AbortTransactionException;

public class ExceptionUtil {
  public static AbortTransactionException getClassCastAbort(String statement, Class expected, Object value, ClassCastException e){
    String complainType = value == null ? "." : (", returned " + value.getClass().getName());
    return new AbortTransactionException(statement + " statment misconfigured, should return " + expected.getName() + " data type key" + complainType, e);
  }

}
