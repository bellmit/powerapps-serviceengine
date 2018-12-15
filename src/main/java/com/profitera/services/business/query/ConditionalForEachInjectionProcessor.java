/*
 * Created on Mar 28, 2006
 *
 */
package com.profitera.services.business.query;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.util.reflect.Reflect;

public class ConditionalForEachInjectionProcessor extends
		ForEachInjectionProcessor {
	
	private static final String INJECTION_QUERY = "INJECTION_QUERY";
	private static final String CONDITION_VALUE = "CONDITION_VALUE";
	private static final String CONDITION_KEY = "CONDITION_KEY";

	protected String getDocumentation() {
		// TODO Auto-generated method stub
		return "Same as ForEachInjectionProcessor except the query will only be executed \n"
		+ "if user defined condition is met";
	}

	protected String getSummary() {
		// TODO Auto-generated method stub
		return super.getSummary();
	}

	private String conditionKey;
	private String conditionValue;
	private String query;
	
	public ConditionalForEachInjectionProcessor(){
		addRequiredProperty(CONDITION_KEY, String.class, "Condition Key", "Condition key.");
		addRequiredProperty(CONDITION_VALUE, String.class, "Condition Value", "Condition Value");
		addRequiredProperty(INJECTION_QUERY, String.class, "Injection Query", "Query to execute for each result row");
	}
	
	protected void configureProcessor() {
		super.configureProcessor();
		conditionKey = getProperty(CONDITION_KEY).toString();
		conditionValue = getProperty(CONDITION_VALUE).toString();
		query = getProperty(INJECTION_QUERY).toString(); 
	}

	public List postProcessResults(Map arguments, List result, IQueryService qs)
			throws TransferObjectException {
		for (Iterator i = result.iterator(); i.hasNext();) {
			Map m = (Map) i.next();
			
			// check condition
			Object currentValue = m.get(conditionKey);
			if(currentValue==null) {
			  continue;
			} else {
			  currentValue = currentValue.toString();
			}
			
			if(!currentValue.equals(conditionValue))
				continue;
			
			TransferObject to = qs.getQueryList(query, m);
			List l = (List) to.getBeanHolder();
			if (l.size() > 0) {
				m.putAll((Map) l.get(0));
			}
		}
		return result;
	}

}
