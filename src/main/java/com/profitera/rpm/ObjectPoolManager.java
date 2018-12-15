package com.profitera.rpm;

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import com.profitera.descriptor.db.reference.ProfileSegmentRef;
import com.profitera.descriptor.db.reference.RiskLevelRef;
import com.profitera.descriptor.db.rule.Rule;
import com.profitera.services.system.dataaccess.RPMDataManager;
import com.profitera.services.system.dataaccess.RuleQueryManager;
import com.profitera.services.system.lookup.LookupManager;

public class ObjectPoolManager {
	private static ProfileSegmentRef[] profileSegments = null;    
    private static RiskLevelRef[] riskLevels = null;
    private static Hashtable rules = null;
    private static RiskLevelRef defaultRiskLevel = null;
	private static ProfileSegmentRef defaultProfileSegment = null;

    private ObjectPoolManager() {
    }

    /**
     * Stock the rule cache with all the rules form the DB
     * 
     */
    private static void getAllRules() {
        Hashtable newRules = new Hashtable(200);
        for (int i = 0; i < RuleAgentConstants.AGENT_CODES.length; i++) {
            newRules.put(RuleAgentConstants.AGENT_CODES[i], getRulesCache(RuleAgentConstants.AGENT_CODES[i]));
        }
        rules = newRules;
    }

    /**
     * Get all the rules for an agent by code and plunk them into a Hash
     * @param agentCode
     * @return
     */
    private static Map getRulesCache(String agentCode) {
        Hashtable m = new Hashtable();
        RuleQueryManager rqm = (RuleQueryManager) (LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "RuleQueryManager"));
        Vector v = rqm.getAllDeployedRulesByAgent(agentCode);
        for (int i = 0; i < v.size(); i++) {
            Rule r = (Rule) v.get(i);
            m.put(r.getRuleId().toString(), r.getContent());
        }
        return m;
    }

    /**
     * get all the deployed rules for a given agent.
     * @param agentCode
     * @return
     */
    public static Hashtable getRules(String agentCode) {
        if (rules == null) {
            getAllRules();
        }
        return (Hashtable) rules.get(agentCode);
    }

    public static RiskLevelRef getDefaultRiskLevel() {
        if (defaultRiskLevel == null) {
            defaultRiskLevel = getRiskLevels()[getRiskLevels().length-1];
        }
        return defaultRiskLevel;
    }

    public static RiskLevelRef[] getRiskLevels() {
        if (riskLevels == null) {
            Vector v = RPMDataManager.getRiskLevels(RPMDataManager.getSession());
            riskLevels = new RiskLevelRef[v.size()];
            v.copyInto(riskLevels);
        }
        return riskLevels;
    }
    


	public static ProfileSegmentRef[] getProfileSegments() {
		if (profileSegments == null) {
			Vector v = RPMDataManager.getProfileSegments(RPMDataManager.getSession());
			profileSegments = new ProfileSegmentRef[v.size()];
			v.copyInto(profileSegments);
		}
		return profileSegments;
	}

    /**
     * Toasts the cache, should be called before the big 'ole
     * nightly run of the RPM.
     */
    public static void clearCache() {
        rules = null;
        defaultRiskLevel = null;
        riskLevels = null;
        
    }

    public static ProfileSegmentRef getDefaultProfileSegment() {
		if (defaultProfileSegment == null) {
			defaultProfileSegment = getProfileSegments()[getProfileSegments().length-1];
		}
		return defaultProfileSegment;
    }
}
