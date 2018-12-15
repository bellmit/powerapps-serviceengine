package com.profitera.services.system.dataaccess;

import java.util.Vector;

import oracle.toplink.expressions.Expression;
import oracle.toplink.expressions.ExpressionBuilder;
import oracle.toplink.queryframework.ReadAllQuery;
import oracle.toplink.queryframework.ReadObjectQuery;

import com.profitera.descriptor.db.rule.Rule;
import com.profitera.descriptor.db.rule.RuleBom;
import com.profitera.descriptor.rpm.RuleStatusDescriptors;

public class RuleQueryManager extends QueryManager {
    public static final String STREAM_RULE_NAME = "STREAM";
    
	public Rule getRule(Long ruleId) {
        Expression exp = new ExpressionBuilder().get(Rule.RULE_ID).equal(ruleId);
        ReadObjectQuery query = new ReadObjectQuery(Rule.class, exp);
        return (Rule) getObject(query);
    }

    public Vector getAllDeployedRulesByAgent(String agentCode) {
        final ExpressionBuilder builder = new ExpressionBuilder();
        Expression status = builder.get(Rule.STATUS).equal(RuleStatusDescriptors.DEPLOYED);
        Expression agent = builder.get(Rule.RULE_BOM).get(RuleBom.AGENT_CODE).equal(agentCode);
        Expression notDeleted = builder.get(Rule.DELETED).equal(false);
        ReadAllQuery query = new ReadAllQuery(Rule.class, notDeleted.and(status.and(agent).and(getNonRuleExclusion(builder))));
        return get(query);
    }
    
    public static Expression getNonRuleExclusion(ExpressionBuilder b){
    	return b.get(Rule.RULE_NAME).equal(STREAM_RULE_NAME).not();
    }
}