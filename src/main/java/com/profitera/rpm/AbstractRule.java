package com.profitera.rpm;

import java.util.Map;

import com.profitera.descriptor.rpm.Descriptor;
import com.profitera.descriptor.rpm.UnsupportedAttributeException;
import com.profitera.rpm.expression.Expression;
import com.profitera.rpm.expression.InvalidExpressionException;
import com.profitera.rpm.expression.RuleBuilder;

/**
 * This class used to called Rule, now that is an interface,  but it
 * has the cvs history for this class associated with it if you are 
 * interested!
 * @author jamison
 *
 */
public abstract class AbstractRule implements Rule {
	private String agentCode = null;
	private Expression condition = null;
	private Expression action = null;
    
    public AbstractRule(){
    }
    
	public void configureRule(String agentCode, String ruleExpressionText, boolean log) throws InvalidExpressionException, RuleEngineException {
	    configureRule(agentCode, ruleExpressionText, null, log);
	}
	
	public void configureRule(String agentCode, String ruleExpressionText, String overrideName, boolean log) throws InvalidExpressionException, RuleEngineException {
	    this.agentCode = agentCode;
		RuleBuilder rb = new RuleBuilder(ruleExpressionText);
		String name = overrideName;
		if (overrideName == null)
			name = rb.getName();
		this.condition = rb.getCondition();
		this.action = rb.getAction();
		setClauses(name, condition, action, log);
	}
	
	public abstract void setClauses(String name, Expression cond, Expression action, boolean log);

	/**
	 * @return The agent code of this rule.
	 */
	public String getAgentCode() {
		return agentCode;
	}
	
	public abstract String[] getDeclarations();

	public Expression getCondition() {
		return condition;
	}
	public Expression getAction() {
		return action;
	}
	
	public abstract Map getUsedAttributesValues(Descriptor descriptor) throws InvalidExpressionException, UnsupportedAttributeException, RuleEngineException, UnsupportedOperationException;
}
