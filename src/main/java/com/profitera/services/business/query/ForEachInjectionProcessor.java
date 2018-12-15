package com.profitera.services.business.query;

import java.util.List;
import java.util.Map;

import com.profitera.descriptor.business.TransferObject;

public class ForEachInjectionProcessor extends AbstractForEachProcessor {
  private static final String INJECTION_QUERY = "INJECTION_QUERY";

protected String getDocumentation() {
		return "Executes the query defined for INJECTION_QUERY for each result row, \n"
		+ "from the main query, uses the result row values as arguments for the INJECTION_QUERY.";
	}

	protected String getSummary() {
		return "Use the result row values as arguments to executes the query defined for each result row.";
	}

	public ForEachInjectionProcessor(){
		addRequiredProperty(INJECTION_QUERY, String.class, "Injection query", "Query to execute for each result row");
	}
	
private String query;

  protected void configureProcessor() {
    super.configureProcessor();
    query = getProperty(INJECTION_QUERY).toString();
  }

  protected void processResultRow(Map row,IQueryService qs) {
    TransferObject to = qs.getQueryList(query, row);
    List l = (List) to.getBeanHolder();
    if (l!=null&&l.size() > 0){
      row.putAll((Map) l.get(0));
    }
  }
  

}
