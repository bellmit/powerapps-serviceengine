package com.profitera.services.business.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.deployment.rmi.ListQueryServiceIntf;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class ListArgumentInjectionQueryProcessor extends AbstractListQueryProcessor {

  private static final String ARGUMENT_QUERY = "ARGUMENT_QUERY";
  private static final String ARGUMENT_KEY = "ARGUMENT_KEY";
  private static final String RESULT_KEY = "RESULT_KEY";
  private String resultKey;
  private String argumentKey;
  private String argumentQuery;
  //

  protected void configureProcessor() {
    resultKey = getRequiredProperty(RESULT_KEY);
    argumentKey = getRequiredProperty(ARGUMENT_KEY);
    argumentQuery = getRequiredProperty(ARGUMENT_QUERY);
  }
  
  private List getValue(Map args, IQueryService s) {
        if (s == null){
          throw new RuntimeException("Unable to load query service for " + getQueryName());
        }
        List l = (List) s.getQueryList(argumentQuery, args).getBeanHolder();
        List arg = new ArrayList(l.size());
        for (Iterator i = l.iterator(); i.hasNext();) {
          Map result = (Map) i.next();
          Object v = result.get(resultKey);
          // Nulls are not added to list
          if (v != null){
            arg.add(v);
          }
        }
        
    return arg;
  }

  public Map preprocessArguments(Map arguments, IQueryService qs) throws TransferObjectException {
  	List values = getValue(arguments, qs);
  	if (arguments.containsKey(argumentKey)) {
  		List oldValues = (List)arguments.get(argumentKey);
  		oldValues.addAll(values);
  		arguments.put(argumentKey, oldValues);
  	} else
  		arguments.put(argumentKey, values);
    return arguments;
  }
}