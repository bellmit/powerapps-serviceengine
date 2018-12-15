/*
 * Created on Jul 21, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.profitera.rpm;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.rpm.expression.InvalidExpressionException;
import com.profitera.util.reflect.Reflect;
import com.profitera.util.reflect.ReflectionException;

/**
 * @author jamison
 *
 * A factory for creating agents based on the agent code.
 * This is also where the chosen JessRule implemenation is 
 * loaded from the property file.
 * 
 */
public class RuleAgentFactory {
  private static final Log LOG = LogFactory.getLog(RuleAgentFactory.class);
	/**
	 * @return A particular RuleAgent that uses the inference engine ie based on the
	 * agentCode passed in. 
	 */

	public static RuleAgent getRuleAgent(String agentCode, RuleFiredListener l) throws NoSuchAgentException {
		Class agentClass = getAgentClass(agentCode);
		if (agentClass == null){
			throw new NoSuchAgentException("The agent code supplied does not correspond to an existing agent.", agentCode);
		}
		RuleAgent agent = null;
		
			Constructor constr;
			try {
				constr = agentClass.getConstructor(null);
			} catch (SecurityException e) {
				throw new NoSuchAgentException("The agent code supplied is not accessible.", agentCode);
			} catch (NoSuchMethodException e) {
				throw new NoSuchAgentException("The agent code supplied does not correspond to an existing agent.", agentCode);
			}
			try {
				agent = (RuleAgent)constr.newInstance(null);
				agent.setCode(agentCode);
				if (l != null)
				    agent.addRuleFiringLogger(l);
				return agent;
			} catch (IllegalArgumentException e1) {
			} catch (InstantiationException e1) {
			} catch (IllegalAccessException e1) {
			} catch (InvocationTargetException e1) {
			}		
		throw new NoSuchAgentException("The agent constructor could not be invoked.", agentCode);
		
	}
	
	private static Class getAgentClass(String agentCode) {
		ResourceBundle b = ResourceBundle.getBundle(RPM.RULE_PROPERTIES_NAME);
		String className = b.getString("AGENT_" + agentCode);
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Agent implimentation " + className + " could not be loaded", e);
		}
	}

    public static Vector getAgents() throws NoSuchAgentException, InvalidExpressionException, RuleEngineException {
		Vector agents = new Vector();
		for (int i = 0; i < RuleAgentConstants.AGENT_CODES.length; i++) {
			// this used to share a global inference engine, but that is silly, the rules and data
			// were all shared, causing utter confusion
			RuleAgent newAgent =
				RuleAgentFactory.getRuleAgent(RuleAgentConstants.AGENT_CODES[i], null);
			agents.add(newAgent);
			loadAgentRules(newAgent);
		}
		return agents;
	}
	
	private static void loadAgentRules(RuleAgent newAgent) throws InvalidExpressionException, RuleEngineException {
		AbstractRule[] rules = null;
		rules = getRuleArray(newAgent.getCode());
		try {
			newAgent.loadRules(rules);
		} catch (InvalidRuleException e) {
			e.printStackTrace();
			throw new InvalidExpressionException(
					"The agent " + newAgent.getCode() + " encountered a bad rule: " + e.getMessage(),e);				
		}
	}
	
	public static RuleAgent getRuleLoadedAgent(String agentCode, RuleFiredListener l) throws NoSuchAgentException, InvalidExpressionException, RuleEngineException{
		RuleAgent a = getRuleAgent(agentCode, l);
		loadAgentRules(a);
		return a;
	}

	public static RuleAgent getPreProcessorAgent() throws NoSuchAgentException, InvalidExpressionException, RuleEngineException{
		Vector v = getAgents();
		return (RuleAgent) v.get(RuleAgentConstants.PPA_AGENT);
	}
	
	public static RuleAgent getProfilingAgent() throws NoSuchAgentException, InvalidExpressionException, RuleEngineException{
		Vector v = getAgents();
		return (RuleAgent) v.get(RuleAgentConstants.PA_AGENT);
	}

	private static AbstractRule[] getRuleArray(String agentCode) throws InvalidExpressionException, RuleEngineException {
		Class ruleImpl = getJessRuleImplimentation(agentCode);
		boolean loggingEnabled = getLoggingEnabled();
		Hashtable rulesHash = ObjectPoolManager.getRules(agentCode);
//		Always returns non-null array.
		if (rulesHash == null)
			return new AbstractRule[0];
		ArrayList rules = new ArrayList();
		Iterator i = rulesHash.entrySet().iterator(); 
		while(i.hasNext()){
		    Rule r = null;
      Map.Entry entry = (Entry) i.next();
			String content = (String) entry.getValue();
			try {
				r = (Rule) Reflect.invokeConstructor(ruleImpl,null, null);
			} catch (ReflectionException e) {
				throw new RuleEngineException("Failed to invoke Rule implimentation class default constructor " + ruleImpl.getName(), e);
			}
			r.configureRule(agentCode, content, entry.getKey().toString(), loggingEnabled);
			if (loggingEnabled && rules.size() == 1){ // log out declarations after 1st rule created
				String[] declares = r.getDeclarations();
				for (int j = 0; j < declares.length; j++) {
          LOG.debug(declares[j]);
				}
			}
			rules.add(r);
			if (loggingEnabled){
			    LOG.debug(content);
      }
		}
		return (AbstractRule[]) rules.toArray(new AbstractRule[0]);
	}
	
	public static Rule constructRule(String agentCode, String name, String expressionContent)  throws InvalidExpressionException, RuleEngineException {
	    Class ruleImpl = getJessRuleImplimentation(agentCode);
		boolean loggingEnabled = getLoggingEnabled();
		Rule r = null;
		try {
			r = (Rule) Reflect.invokeConstructor(ruleImpl,null, null);
		} catch (ReflectionException e) {
			throw new RuleEngineException("Failed to invoke JessRule implimentation class default constructor " + ruleImpl.getName(), e);
		}
		r.configureRule(agentCode, expressionContent, name, loggingEnabled);
		return r;
	}

	/**
	 * @return
	 */
	private static boolean getLoggingEnabled() {
		ResourceBundle b = ResourceBundle.getBundle(RPM.RULE_PROPERTIES_NAME);
		try{
			return b.getString("logCode").equalsIgnoreCase("true");
		}catch(MissingResourceException e){
			return false;
		}catch(NullPointerException e){
			return false;
		}
	}

	/**
	 * @return
	 */
	private static Class getJessRuleImplimentation(String agentCode) {
		ResourceBundle b = ResourceBundle.getBundle(RPM.RULE_PROPERTIES_NAME);
		String className = b.getString("JESS_RULE_" + agentCode);
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("JessRule implimentation " + className + " could not be loaded", e);
		}
	}

}
