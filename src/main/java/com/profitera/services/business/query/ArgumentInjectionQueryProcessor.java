/*
 * Created on Aug 4, 2006
 *
 */
package com.profitera.services.business.query;

import java.util.List;
import java.util.Map;

import com.profitera.deployment.rmi.ListQueryServiceIntf;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class ArgumentInjectionQueryProcessor  extends AbstractListQueryProcessor{
	 private static final String ARGUMENT_QUERY = "ARGUMENT_QUERY";
	  private static final String ARGUMENT_KEY = "ARGUMENT_KEY";
	  private static final String RESULT_KEY = "RESULT_KEY";
	  private String resultKey;
	  private String argumentKey;
	  private String argumentQuery;
	  
	  protected void configureProcessor() {
	    resultKey = getRequiredProperty(RESULT_KEY);
	    argumentKey = getRequiredProperty(ARGUMENT_KEY);
	    argumentQuery = getRequiredProperty(ARGUMENT_QUERY);
	  }
	  
	  private Object getValue(Map args, IQueryService s) {
	        if (s == null){
	          throw new RuntimeException("Unable to load query service for " + getQueryName());
	        }
	        List l = (List) s.getQueryList(argumentQuery, args).getBeanHolder();
	        if(l!=null&&l.size()!=0){
	        	Map result = (Map) l.get(0);
	        	Object v = result.get(resultKey);
	        	return v;
	        }
	    return null;
	  }

	  public Map preprocessArguments(Map arguments, IQueryService qs) throws TransferObjectException {
	    arguments.put(argumentKey, getValue(arguments, qs));
	    return arguments;
	  }
}
