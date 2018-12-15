/**
 * @author mark
 */
package com.profitera.services.system.rpm;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.db.account.Account;
import com.profitera.descriptor.db.rule.Rule;
import com.profitera.descriptor.rpm.*;
import com.profitera.persistence.SessionManager;
import com.profitera.rpm.*;
import com.profitera.rpm.expression.InvalidExpressionException;
import com.profitera.services.system.SystemService;
import com.profitera.services.system.dataaccess.RuleQueryManager;
import com.profitera.services.system.lookup.LookupManager;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import oracle.toplink.queryframework.ReadAllQuery;
import oracle.toplink.queryframework.ReadObjectQuery;

public class RuleTest extends SystemService {
    private static final Log LOG = LogFactory.getLog(RuleTest.class);
    public RuleTest() {
    }
    
    /**
     * Returns a vector with 3 elements, a hashtable array containing the descriptors
     * (same as calling fetchAccountStates()), a 2 dim Long array containing the
     * rules foired for each descriptor, and another Hashtable array containing
     * the descriptors after the changes made by the rules have take effect.
     * TODO: This doesn't pass the results from one stage to another properly.
     * TODO: More code reuse, this implementation is embarassing!
     * @see com.profitera.deployment.rmi.RPMEngineTestServiceIntf#testRules(int, java.lang.Long[])
     */
    public TransferObject testRules(int accountCount, Long[] ruleIds) {
        TransferObject to = new TransferObject(TransferObject.ERROR, null);
        try {
          Vector accounts = getAccountVector(accountCount);
          Rule[] rules = sortRulesByAgent(getRules(ruleIds));
          Hashtable[] beforeDescriptors = getAccountDescriptors(accounts, rules);
          // getRPMDescriptors returns a vector of Object[]
          Vector rpmDescriptors = getRPMDescriptors(accounts, getAgentCodeList(rules));
          Long[][] ruleFirings = fireRules(rpmDescriptors, rules);
          Hashtable[] afterDescriptors = extractAccountDescriptors(accounts, rules, rpmDescriptors);
          Vector container = new Vector();
          container.add(beforeDescriptors);
          container.add(ruleFirings);
          container.add(afterDescriptors);
          to = new TransferObject(container);
        } catch (NoSuchRuleException e) {
          to = new TransferObject(TransferObject.ERROR, e.getMessage());
          e.printStackTrace();
        } catch (IllegalArgumentException e) {
          to = new TransferObject(TransferObject.ERROR, e.getMessage());
          e.printStackTrace();
        } catch (InvalidExpressionException e) {
          to = new TransferObject(TransferObject.ERROR, e.getMessage());
           e.printStackTrace();
        } catch (NoSuchAgentException e) {
          to = new TransferObject(TransferObject.ERROR, e.getMessage());
          e.printStackTrace();
        } catch (UnsupportedAttributeException e) {
          to = new TransferObject(TransferObject.ERROR, e.getMessage());
          e.printStackTrace();
		} catch (RuleEngineException e) {
      to = new TransferObject(TransferObject.ERROR, e.getMessage());
      e.printStackTrace();
		}
        return to;
    }
    private Long[][] fireRules(Vector rpmDescriptors, Rule[] rules)
        throws InvalidExpressionException, NoSuchAgentException, RuleEngineException, UnsupportedAttributeException {
        Long[][] firings = new Long[rpmDescriptors.size()][0];
        String[] agentCodes = getAgentCodeList(rules);
        RulesFiredIdLogger fireListener = new RulesFiredIdLogger();
        RuleAgent[] agents = createAgents(agentCodes, fireListener);
        loadAgents(agents, rules);
        for (int i = 0; i < firings.length; i++) {
            Descriptor[] objs = (Descriptor[]) rpmDescriptors.get(i);
            LOG.debug("Executing agents for: " + objs[0]);
            firings[i] = executeAgents(objs, agents, fireListener);
        }
        return firings;
    }

    private Long[] executeAgents(Descriptor[] descriptors, RuleAgent[] agents, RulesFiredIdLogger logger)
        throws NoSuchAgentException, RuleEngineException, UnsupportedAttributeException {
        Vector ruleIds = new Vector();
        for (int i = 0; i < agents.length; i++) {
            logger.clearLog();
            executeAgent(descriptors, agents[i]);
            ruleIds.addAll(logger.getFiredRuleIds());
        }
        Long[] ids = new Long[ruleIds.size()];
        ruleIds.copyInto(ids);
        return ids;
    }

    private Object findByClass(Object[] list, Class clazz) {
        for (int i = 0; i < list.length; i++)
            if (list[i].getClass().equals(clazz))
                return list[i];
        return null;
    }

    private Descriptor getRPMDescriptorForAgent(String agentCode, Object[] list) throws NoSuchAgentException {
        //		TODO: Generalize this! SEE RuleAgentConstants
        if (agentCode.equals("TRTCL")) {
            return (Descriptor) findByClass(list, AccountTreatment.class);
        }
        if (agentCode.equals("DELQM")) {
            return (Descriptor) findByClass(list, AccountDelinquency.class);
        }
        if (agentCode.equals("WLGEN")) {
            //return findByClass(list,AccountWorklist.class);
            throw new NoSuchAgentException(agentCode + " currently unsupported.", agentCode);
        }
        if (agentCode.equals("PPA")) {
            return (Descriptor) findByClass(list, AccountPreprocessor.class);
        }
        if (agentCode.equals("PA")) {
            return (Descriptor) findByClass(list, AccountProfile.class);
        }
        return null;
    }

    private void executeAgent(Descriptor[] descriptors, RuleAgent agent) throws NoSuchAgentException, RuleEngineException, UnsupportedAttributeException {
        String agentCode = agent.getCode();
        Descriptor o = getRPMDescriptorForAgent(agentCode, descriptors);
        if (o == null)
            throw new NoSuchAgentException("Agent not found.", agentCode);
        agent.loadObject(o);
        agent.execute();
    }

    private RuleAgent[] createAgents(String[] agentCodes, RuleFiredListener l)
        throws NoSuchAgentException {
        if (agentCodes.length == 0)
            return new RuleAgent[0];
        RuleAgent[] agents = new RuleAgent[agentCodes.length];
        for (int i = 0; i < agents.length; i++)
            agents[i] = RuleAgentFactory.getRuleAgent(agentCodes[i], l);
        return agents;
    }
    private void loadAgents(RuleAgent[] agents, Rule[] rules) throws InvalidExpressionException, RuleEngineException {
        int currentAgent = 0;
        for (int i = 0; i < rules.length; i++) {
            if (!rules[i].getRuleBom().getAgentCode().equals(agents[currentAgent].getCode()))
                currentAgent++;
            com.profitera.rpm.Rule rpmRule = RuleAgentFactory.constructRule("TESTING", rules[i].getRuleId().toString(), rules[i].getContent());
            agents[currentAgent].loadRule(rpmRule);
        }
    }
    /**
     * This method will return a transfer object containing an array of hashtables,
     * the keys are the names of the methods used by the rules and the data is a 
     * Vector of Strings that represents the data passed in followed by the resulting value.
     * If the method is called more than once the params are just appended to the end of 
     * the vector. The descriptors/dictionaries should never have polymophic methods anyway.
     * The client side can use the dictionary to pull these values and display 
     * them properly. For the time being we assume all the rules belong to the 
     * same dict. but this might change.
     * <br/>
     * This method relys on the assumptions about rule method names, which is a BAD THING.
     * This method relys on the internal operations of JessRule, which is a BAD THING.
     * Basically, it will exclude 'set' methods.
     * TODO: Generalize the method of extracting data from rule.
     *  
     * @see com.profitera.deployment.rmi.RPMEngineTestServiceIntf#fetchAccountStates(int, java.lang.Long[])
     */
    public TransferObject fetchAccountStates(int accountCount, Long[] ruleIds) {
        TransferObject to = new TransferObject(TransferObject.ERROR, null);
        try {
          Vector accounts = getAccountVector(accountCount);
          Rule[] rules = getRules(ruleIds);
          Hashtable[] descriptors = getAccountDescriptors(accounts, rules);
          to = new TransferObject(descriptors, TransferObject.SUCCESS, null);
        } catch (NoSuchRuleException e) {
        	to = new TransferObject(TransferObject.ERROR, e.getMessage());
        } catch (IllegalArgumentException e) {
        	to = new TransferObject(TransferObject.ERROR, e.getMessage());
          e.printStackTrace();
        } catch (UnsupportedAttributeException e) {
        	to = new TransferObject(TransferObject.ERROR, e.getMessage());
        	e.printStackTrace();
		}
        return to;
    }

    private Hashtable[] getAccountDescriptors(Vector accounts, Rule[] rules) throws UnsupportedAttributeException {
        Hashtable[] descriptors = new Hashtable[accounts.size()];
        for (int i = 0; i < descriptors.length; i++) {
            Account account = (Account) accounts.get(i);
            descriptors[i] = getAttributesUsed(account.getAccountId(), rules);
            //Add the account number
            appendAccountNumber(descriptors[i], account);
        }
        return descriptors;
    }
    private void appendAccountNumber(Hashtable ht, Account account) {
        Vector v = new Vector();
        Vector v2 = new Vector();
        v2.add(account.getAccountNumber());
        v.add(v2);
        ht.put("getAccountNumber", v);
    }

    private Hashtable[] extractAccountDescriptors(Vector accounts, Rule[] rules, Vector rpmDescriptors) 
				throws NoSuchAgentException, InvalidExpressionException, UnsupportedAttributeException, RuleEngineException {
        Hashtable[] descriptors = new Hashtable[accounts.size()];
        for (int i = 0; i < descriptors.length; i++) {
            Account account = (Account) accounts.get(i);
            Object[] accountRPMDescriptors = (Object[]) rpmDescriptors.get(i);
            descriptors[i] = extractAttributesUsed(rules, accountRPMDescriptors);
            //Add the account number
            appendAccountNumber(descriptors[i], account);
        }
        return descriptors;

    }

    private Hashtable getAttributesUsed(Double accountId, Rule[] rules) throws UnsupportedAttributeException{
        oracle.toplink.expressions.ExpressionBuilder eb = new oracle.toplink.expressions.ExpressionBuilder();
        oracle.toplink.expressions.Expression exp1 = eb.get("accountId").equal(accountId);
        ReadObjectQuery roq = new ReadObjectQuery(Account.class, exp1);
        Account account = (Account) SessionManager.getClientSession().executeQuery(roq);

        Hashtable attribs = new Hashtable();
        for (int i = 0; i < rules.length; i++) {
            String agentCode = rules[i].getRuleBom().getAgentCode();
            Descriptor descriptor;
			try {
				descriptor = getDescriptor(account, agentCode);
				attribs.putAll(getUsedAttributesValues(descriptor, rules[i]));
			} catch (Exception e) {
				e.printStackTrace();
				throw new UnsupportedAttributeException("Unable to get attrbutes for account " + account.getAccountNumber(), e);
			}
			
        }
        return attribs;
    }

    private Hashtable extractAttributesUsed(Rule[] rules, Object[] rpmDescriptors) throws NoSuchAgentException, InvalidExpressionException, UnsupportedAttributeException, RuleEngineException {
        Hashtable attribs = new Hashtable();
        for (int i = 0; i < rules.length; i++) {
            String agentCode = rules[i].getRuleBom().getAgentCode();
			Descriptor descriptor = getRPMDescriptorForAgent(agentCode, rpmDescriptors);
            attribs.putAll(getUsedAttributesValues(descriptor, rules[i]));
        }
        return attribs;
    }

    private Map getUsedAttributesValues(Descriptor descriptor, Rule rule)
        throws
            InvalidExpressionException, UnsupportedAttributeException, RuleEngineException {
        // TESTING is used for the sake of getting the testing implemenation configured
        com.profitera.rpm.Rule rpmRule = RuleAgentFactory.constructRule("TESTING", rule.getRuleId().toString(), rule.getContent());
        return rpmRule.getUsedAttributesValues(descriptor);
    }

    private Descriptor getDescriptor(Account account, String agentCode) throws NoSuchAgentException {
        // TODO: Generalize this! SEE RuleAgentConstants
        if (agentCode.equals("TRTCL")) {
            return new AccountTreatment(account,SessionManager.getClientSession());
        }
        if (agentCode.equals("DELQM")) {
            return new AccountDelinquency(account,SessionManager.getClientSession());
        }
        if (agentCode.equals("WLGEN")) {
            //new AccountWorklist(accountId.toString());
            throw new NoSuchAgentException(agentCode + " currently unsupported.", agentCode);
        }
        if (agentCode.equals("PPA")) {
            return new AccountPreprocessor(account,SessionManager.getClientSession());
        }
        if (agentCode.equals("PA")) {
            return new AccountProfile(account,SessionManager.getClientSession());
        }
        throw new NoSuchAgentException("Agent not found.", agentCode);
    }

    private String[] getAgentCodeList(Rule[] rules) {
        Vector codes = new Vector();
        for (int i = 0; i < rules.length; i++) {
            String thisCode = rules[i].getRuleBom().getAgentCode();
            if (!codes.contains(thisCode))
                codes.add(thisCode);
        }
        String[] retrn = new String[codes.size()];
        codes.copyInto(retrn);
        return retrn;
    }
    private Vector getRPMDescriptors(Vector accounts, String[] agentCodes) throws NoSuchAgentException {
        Vector descriptors = new Vector();
        Iterator i = accounts.iterator();
        while (i.hasNext()) {
            descriptors.add(getRPMDescriptors((Account) i.next(), agentCodes));
        }
        return descriptors;
    }
    private Descriptor[] getRPMDescriptors(Account account, String[] agentCodes) throws NoSuchAgentException {
		Descriptor[] d = new Descriptor[agentCodes.length];
        for (int i = 0; i < agentCodes.length; i++) {
            d[i] = getDescriptor(account, agentCodes[i]);
        }
        return d;
    }

    private Rule[] sortRulesByAgent(Rule[] rules) {
        Vector ruleVector = new Vector();
        for (int i = 0; i < RuleAgentConstants.AGENT_CODES.length; i++) {
            ruleVector.addAll(getRulesWithCode(RuleAgentConstants.AGENT_CODES[i], rules));
        }
        Rule[] sortedRules = new Rule[rules.length];
        ruleVector.copyInto(sortedRules);
        return sortedRules;
    }

    private Vector getRulesWithCode(String code, Rule[] rules) {
        Vector ruleVector = new Vector();
        for (int i = 0; i < rules.length; i++) {
            if (rules[i].getRuleBom().getAgentCode().equals(code))
                ruleVector.add(rules[i]);

        }
        return ruleVector;
    }

    private Rule[] getRules(Long[] ids) throws NoSuchRuleException {
        RuleQueryManager rqm =
            (RuleQueryManager) (LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "RuleQueryManager"));
        Rule[] rules = new Rule[ids.length];
        for (int i = 0; i < rules.length; i++) {
            rules[i] = rqm.getRule(ids[i]);
            if (rules[i] == null)
                throw new NoSuchRuleException("Rule " + ids[i].toString() + " was not found.");
        }
        return rules;
    }

    public static Vector getAccountVector(int _totalCount) {
        ReadAllQuery query = new ReadAllQuery(Account.class);
        query.setMaxRows(_totalCount);
        Vector acctVector = (Vector) SessionManager.getClientSession().executeQuery(query);
        acctVector.setSize(_totalCount);
        return acctVector;
    }
    // Jamison's unused code, might be useful in the future!
    //	private Expression[] getMethodCalls(Expression e){
    //		Vector exps = getMethodCallVector(e,new Vector());
    //		Expression[] expressions = new Expression[exps.size()];
    //		exps.copyInto(expressions);
    //		return expressions;
    //	}
    //	
    //	private Vector getMethodCallVector(Expression e, Vector v){
    //		if (e.isMethod()){
    //			v.add(e);
    //			return v;
    //		}else if (e.isCompound()){
    //			v = getMethodCallVector(e.getLeftExpression(),v);
    //			return getMethodCallVector(e.getRightExpression(),v);
    //		}else
    //			return v;
    //	}

}