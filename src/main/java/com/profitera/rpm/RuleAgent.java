package com.profitera.rpm;

import java.util.List;

import com.profitera.descriptor.rpm.Descriptor;
import com.profitera.descriptor.rpm.UnsupportedAttributeException;

public interface RuleAgent {
    
    
	/**
	 * This method retrieves the id code from an agent. The id comes from the database from
	 * ptrrule_bom.
	 * @return String the id code in string
	 */
	public String getCode();
	public void setCode(String agentCode);
	
	/**
	 * This method can be used to load a rule into Jess.
	 * @param r This is the rule
	 * @throws JessException
	 */
	public void loadRule(Rule r) throws InvalidRuleException;
	public void loadRules(Rule[] rules) throws InvalidRuleException;
    public void loadObjects(Descriptor[] _obj) throws InvalidRuleException, UnsupportedAttributeException;
	/**
	 * Loads the descriptor into the inference engine using the rules loaded
	 * to get the assertions.
	 * @param obj
	 * @throws InvalidRuleException
	 * @throws UnsupportedAttributeException
	 */
	public void loadObject(Descriptor obj) throws InvalidRuleException, UnsupportedAttributeException;
	public void execute() throws RuleEngineException;
	/**
	 * Use this method to find out if the agent/rules implementation
	 * supports the loading of multiple descriptors at the same time.
	 * @return
	 */
	
	/**
	 * Returns a List of object arrays, each array is composed of a 
	 * String/Object[] pair that embodies the access of a given property
	 * of a descriptor using the arguments supplied in the array
	 * <br/>
	 * NOTE: This method should only be called after all rules are loaded.
	 * If all rules are not loaded not all properties used will be known. Also,
	 * if no rules are loaded the behaviour is undefined. 
	 * @return
	 */
	public List getPropertiesUsed();
	
	public boolean isMultiDescriptorEnabled();
	
	public void addRuleFiringLogger(RuleFiredListener l);


}