package com.profitera.services.business.query;

import java.util.Date;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

/**
 * @author jamison
 */
public class CurrentDateProcessor extends BaseListQueryProcessor {

  private static final String DEFAULT_CURRENT_DATE_NAME = "CURRENT_DATE";
  private String currentDateName = DEFAULT_CURRENT_DATE_NAME;
  
  public CurrentDateProcessor(){
	  addProperty("ARGUMENT_NAME", String.class, DEFAULT_CURRENT_DATE_NAME, "Argument name", "Name of the argument to assign the value to.");
  }
  

  protected void configureProcessor() {
    super.configureProcessor();
    currentDateName = (String) getProperty("ARGUMENT_NAME");
  }

  /**
   * @see com.profitera.services.business.query.IListQueryProcessor#preprocessArguments(java.util.Map, IQueryService)
   */
  public Map preprocessArguments(Map arguments, IQueryService qs) throws TransferObjectException {
    arguments.put(getDateArgumentName(), getArgumentDate());
    return arguments;
  }

  protected String getDateArgumentName() {
    return currentDateName;
  }
  protected Date getArgumentDate() {
    return new Date();
  }


protected String getDocumentation() {
	return "Adds the current date and time to query arguments using the specified key, overwrites any existing value.";
}


protected String getSummary() {
	return "Add the current date to query arguments";
}

}
