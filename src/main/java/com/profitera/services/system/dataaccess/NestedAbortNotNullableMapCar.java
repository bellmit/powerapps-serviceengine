package com.profitera.services.system.dataaccess;

import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.util.MapCar;

public class NestedAbortNotNullableMapCar extends MapCar {
  private final Object[] keys;

  public NestedAbortNotNullableMapCar(Object[] keys) {
    this.keys = keys;
  }

  public Object map(Object o1) {
    try {
      Map o = (Map) o1;
      for (int i = 0; i < keys.length; i++) {
        if (o.get(keys[i]) == null)
          throw new AbortTransactionException("Query argument not nullable: " + keys[i]);
      }
      return o;
    } catch (AbortTransactionException e) {
      throw new RuntimeException(e);
    }
  }
}