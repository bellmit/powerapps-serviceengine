package com.profitera.services.system.dataaccess;

import java.util.Map;

import com.profitera.util.MapCar;

public class MapVerifyingMapCar extends MapCar {
  boolean verified = false;

  Object[] required;

  private static final String EXPECTED_NAME = Map.class.getName();

  public MapVerifyingMapCar(Object[] requiredKeys) {
    required = requiredKeys;
  }

  /**
   * @see com.profitera.util.MapCar#map(java.lang.Object)
   */
  public Object map(Object o) {
    if (verified)
      return o;
    try {
      Map m = (Map) o;
      for (int i = 0; i < required.length; i++) {
        if (!m.containsKey(required[i]))
          throw new InvalidQueryResultException("Missing query return column '" + required[i] + "'");
      }
    } catch (ClassCastException e) {
      String cName = o.getClass().getName();
      throw new InvalidQueryResultException("Configured query return type of '" + cName + "' is not of type "
          + EXPECTED_NAME);
    }
    verified = true;
    return o;
  }
}