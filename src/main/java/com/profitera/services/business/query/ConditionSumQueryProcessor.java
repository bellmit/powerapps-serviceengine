package com.profitera.services.business.query;

import java.util.List;
import java.util.Map;

import com.profitera.math.MathUtil;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class ConditionSumQueryProcessor extends SumQueryProcessor {

	private static final String CONDITION_VALUE = "CONDITION_VALUE";
  private static final String CONDITION_KEY = "CONDITION_KEY";
  private String conditionKey;
  private String conditionValue;
	
	public ConditionSumQueryProcessor() {
		addRequiredProperty(CONDITION_KEY, String.class, "The key to check the condition", "The key to check the condition");
	  addRequiredProperty(CONDITION_VALUE, String.class, "The condition that need to met", "The condition that need to met");
	}

	protected void configureProcessor() {
		super.configureProcessor();
		conditionKey = (String) getProperty(CONDITION_KEY);
		conditionValue = (String) getProperty(CONDITION_VALUE);
	}

	protected void processResultRow(Map row, IQueryService qs) {
		Object cond = row.get(conditionKey);
		if (cond == null || !cond.toString().equals(conditionValue))
			return;
		super.processResultRow(row, qs);
  }
	
	protected String getDocumentation() {
		return "Same like SumQueryProcessor but sum up the values if and only if the condition met";
	}
	
	protected String getSummary() {
		return getDocumentation();
	}

}
