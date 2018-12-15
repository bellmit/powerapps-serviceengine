package com.profitera.services.business.query;

import java.util.List;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class ConditionalMergeQueryProcessor extends MergeQueryProcessor {

  protected String getDocumentation() {
		return "Same like MergeQueryProcessor but the query execute if and only if the condition met";
	}
  protected String getSummary(){
	  return "TreatmentWorkpad now loads processes for editing by querying them out of \n"
	  + "the database again. This allows the process list loading to be more efficient \n"
	  + "because it can request only the fields needed to display the process in the tree,\n"
	  + "instead of all the fields for editing. \n"
	  + "A new QP was added that does merging when a condition is satisfied, which allows 1 query \n"
	  + "to retrieve all the processes, selecting the merger query for each type based on the \n"
	  + "arguments provided.";
  }

  private static final String CONDITION_VALUE = "CONDITION_VALUE";
  private static final String CONDITION_KEY = "CONDITION_KEY";
  private String conditionKey;
  private String conditionValue;

  public ConditionalMergeQueryProcessor(){
	  addRequiredProperty(CONDITION_KEY, String.class, "The key to check the condition", "The key to check the condition");
	  addRequiredProperty(CONDITION_VALUE, String.class, "The condition that need to met", "The condition that need to met");
  }
  
  protected void configureProcessor() {
    super.configureProcessor();
    conditionKey = (String)getProperty(CONDITION_KEY);
    conditionValue = (String)getProperty(CONDITION_VALUE);
  }

  public List postProcessResults(Map arguments, List result, IQueryService qs) throws TransferObjectException {
    Object cond = arguments.get(conditionKey);
    if (cond == null || !cond.toString().equals(conditionValue))
      return result;
    return super.postProcessResults(arguments, result, qs);
  }
  
  

}
