package com.profitera.services.business.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class QueryArgumentInjectionProcessor extends AbstractListQueryProcessor {

  private static final String REFRESH_INTERVAL = "REFRESH_INTERVAL";
  private static final String ARGUMENT_QUERY = "ARGUMENT_QUERY";
  private static final String ARGUMENT_KEY = "ARGUMENT_KEY";
  private static final String RESULT_KEY = "RESULT_KEY";
  private String resultKey;
  private String argumentKey;
  private String argumentQuery;
  private long refreshInterval;
  private Object resultValue;
  //
  private long lastRefreshTime = 0;

  protected void configureProcessor() {
    resultKey = getRequiredProperty(RESULT_KEY);
    argumentKey = getRequiredProperty(ARGUMENT_KEY);
    argumentQuery = getRequiredProperty(ARGUMENT_QUERY);
    refreshInterval = 5 * 60 * 1000;
    try {
      // Long only throws NFE, even if the value is null
      refreshInterval = new Long(getProperties().getProperty(REFRESH_INTERVAL)).longValue();
    } catch (NumberFormatException e){
      
    }
  }
  
  private Object getValue(IQueryService s) {
    if (Math.abs(System.currentTimeMillis() - lastRefreshTime) > refreshInterval){
        if (s == null){
          throw new RuntimeException("Unable to load query service to refresh cache for " + getQueryName());
        }
        List l = (List) s.getQueryList(argumentQuery, new HashMap()).getBeanHolder();
        if (l == null || l.size() == 0)
          resultValue = null;
        else
          resultValue = ((Map)l.get(0)).get(resultKey);
    }
    return resultValue;
  }

  public Map preprocessArguments(Map arguments, IQueryService qs) throws TransferObjectException {
    arguments.put(argumentKey, getValue(qs));
    return arguments;
  }

}
