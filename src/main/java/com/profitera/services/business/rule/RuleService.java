package com.profitera.services.business.rule;

import com.profitera.deployment.rmi.RuleServiceIntf;
import com.profitera.descriptor.business.ServiceException;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.ra.RuleBean;
import com.profitera.descriptor.business.ra.RuleHistoryBean;
import com.profitera.descriptor.db.rule.Rule;
import com.profitera.descriptor.db.rule.RuleBom;
import com.profitera.descriptor.db.rule.RuleGroup;
import com.profitera.descriptor.db.rule.RuleHistory;
import com.profitera.descriptor.rpm.RuleStatusDescriptors;
import com.profitera.persistence.SessionManager;
import com.profitera.services.business.BusinessService;
import com.profitera.services.system.dataaccess.RuleQueryManager;
import com.profitera.util.MapCar;
import com.profitera.util.PrimitiveValue;
import com.profitera.util.TopLinkQuery;
import java.sql.Timestamp;
import java.util.List;
import java.util.Vector;
import oracle.toplink.expressions.Expression;
import oracle.toplink.expressions.ExpressionBuilder;
import oracle.toplink.queryframework.ReadAllQuery;
import oracle.toplink.queryframework.ReadObjectQuery;
import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.UnitOfWork;

public class RuleService extends BusinessService implements RuleServiceIntf {
    private static final int NAME_LENGTH = 50;
    private static final int DESCRIPTION_LENGTH = 250;
    private static final MapCar RULE_BEAN_MAPCAR = new MapCar() {
        public Object map(Object o) {
            return buildRuleBean((Rule) o);
        }
    };
    private static final MapCar RULE_HISTORY_BEAN_MAPCAR = new MapCar() {
        public Object map(Object o) {
            return buildRuleHistoryBean((RuleHistory) o);
        }
    };

    private void writeToHistory(Rule obseleteRule, String remarks, UnitOfWork uow) {
        Rule wcRule = (Rule) uow.registerObject(obseleteRule);
        RuleHistory ruleHistory = new RuleHistory();
        RuleHistory wcRuleHistory = (RuleHistory) uow.registerObject(ruleHistory);
        wcRuleHistory.setParentRule(wcRule);
        wcRuleHistory.setRuleName(obseleteRule.getRuleName());
        wcRuleHistory.setDescription(obseleteRule.getDescription());
        // For some reason getUpdatedBy can be null!
        if (obseleteRule.getUpdatedBy() != null) {
            wcRuleHistory.setUserId(obseleteRule.getUpdatedBy());
        } else if (obseleteRule.getCreatedBy() != null) {
            wcRuleHistory.setUserId(obseleteRule.getCreatedBy());
        } else {
            wcRuleHistory.setUserId("");
        }
        wcRuleHistory.setContent(obseleteRule.getContent());
        wcRuleHistory.setRemarks(remarks);
        wcRuleHistory.setVersionDateTime(new Timestamp(System.currentTimeMillis()));
    }

    /**
     * Now this method writes the rule history for you!
     *
     * @return A TransferObject which holds a fresh copy of an updated Rule
     *         object
     */
    public TransferObject updateRule(RuleBean updatedRule) {
        try {
            Session s = SessionManager.getClientSession();
            // Validate name and desc.
            if (updatedRule.getRuleName().length() > NAME_LENGTH) {
                updatedRule.setRuleName(updatedRule.getRuleName().substring(0, NAME_LENGTH));
            }
            if (updatedRule.getDescription().length() > DESCRIPTION_LENGTH) {
                updatedRule.setDescription(updatedRule.getDescription().substring(0, DESCRIPTION_LENGTH));
            }
            Rule rule;
            UnitOfWork uow = s.acquireUnitOfWork();
            if (updatedRule.getRuleId() != null) {
                Rule oldRule = getRule(updatedRule.getRuleId(), s);
                if (oldRule == null) throw new ServiceException("Rule to be updated could not be found");
                writeToHistory(oldRule, updatedRule.getRemarks(), uow);
                rule = (Rule) uow.registerExistingObject(oldRule);
            } else {
                rule = (Rule) uow.registerNewObject(new Rule());
                rule.setCreatedDate(new Timestamp(System.currentTimeMillis()));
            }
            rule.setRuleId(updatedRule.getRuleId());
            rule.setContent(updatedRule.getContent());
            rule.setCreatedBy(updatedRule.getCreatedBy());
            if (RuleStatusDescriptors.isDeployed(updatedRule.getStatus())) {
                rule.setDeployDate(new Timestamp(System.currentTimeMillis()));
            }
            rule.setDescription(updatedRule.getDescription());
            // Fix for [ #379 ] Rules Creation - Dictionary value in Group table not displayed
            // It was caused by using a dummy RuleBom object to do the update/create
            // but then never actually getting a real one and the buildRuleBean method
            // would use the RuleBom to pull the dict name. Now it pulls the real RuleBom
            // from the DB
            rule.setRuleBom((RuleBom) uow.registerExistingObject(getRuleBom(updatedRule.getRuleBomId())));
            // end fix
            if (updatedRule.getRuleGroupId() != null) {
                rule.setRuleGroup(new RuleGroup());
                rule.getRuleGroup().setGroupId(updatedRule.getRuleGroupId());
                rule.setRuleGroup((RuleGroup) uow.registerExistingObject(rule.getRuleGroup()));
            } else {
                rule.setRuleGroup(null);
            }
            rule.setRuleName(updatedRule.getRuleName());
            rule.setStatus(updatedRule.getStatus());
            rule.setUpdatedBy(updatedRule.getUpdatedBy());
            rule.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
            rule.setDeleted(new Boolean(PrimitiveValue.booleanValue(updatedRule.getDeleted(), false)));
            uow.commit();
            return new TransferObject(buildRuleBean(rule));
        } catch (ServiceException e) {
            return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
        }
    }

    public Rule getRule(Long ruleId, Session s) {
        ReadObjectQuery query = new ReadObjectQuery(Rule.class,
            new ExpressionBuilder().get(Rule.RULE_ID).equal(ruleId));
        return (Rule) s.executeQuery(query);
    }

    private RuleBom getRuleBom(Long bomId) {
        return new RuleBomService().getRuleBom(bomId, SessionManager.getClientSession());
    }

    private static RuleBean buildRuleBean(Rule rule) {
        RuleBean bean = new RuleBean();
        bean.setContent(rule.getContent());
        bean.setCreatedBy(rule.getCreatedBy());
        bean.setCreatedDate(rule.getCreatedDate());
        bean.setDeployedDate(rule.getDeployDate());
        bean.setDescription(rule.getDescription());
        bean.setRuleBomId(rule.getRuleBom().getBomId());
        bean.setRuleBomName(rule.getRuleBom().getBomName());
        bean.setRuleGroupId(null);
        if (rule.getRuleGroup() != null) {
            bean.setRuleGroupId(rule.getRuleGroup().getGroupId());
        }
        bean.setRuleId(rule.getRuleId());
        bean.setRuleName(rule.getRuleName());
        bean.setStatus(rule.getStatus());
        bean.setUpdatedBy(rule.getUpdatedBy());
        bean.setUpdatedDate(rule.getUpdatedDate());
        bean.setDeleted(rule.getDeleted());
        return bean;
    }

    public TransferObject refreshRuleObject(Long ruleId) {
        return new TransferObject(buildRuleBean(getRule(ruleId, SessionManager.getClientSession())));
    }

    /**
     * Deletes a rule from the db, this will generally not be used, as
     * Rule now has a 'deleted' flag that should used instead of actually deleting the rules.
     *
     * @param ruleId The id of Rule object to be deleted.
     * @return A TransferObject which holds the status of the deletion
     */
    public TransferObject deleteRule(Long ruleId) {
        UnitOfWork uow = SessionManager.getClientSession().acquireUnitOfWork();
        Rule rule = getRule(ruleId, uow);
        uow.deleteAllObjects(rule.getHistory());
        uow.deleteObject(rule);
        return new TransferObject("Rule deleted");
    }

    private TransferObject returnAsBeans(List l) {
        return new TransferObject(MapCar.map(RULE_BEAN_MAPCAR, l));
    }

    /**
     * @param id The id of the rule to retrieved
     * @return A TransferObject that holds a Rule with a certain id
     */
    public TransferObject getRuleById(Long id) {
        return new TransferObject(buildRuleBean(getRule(id, SessionManager.getClientSession())));
    }

    /**
     * @return A TransferObject that holds a Vector of all Rules
     */
    public TransferObject getAllRules() {
        ReadAllQuery query = new ReadAllQuery(Rule.class);
        query.setSelectionCriteria(RuleQueryManager.getNonRuleExclusion(new ExpressionBuilder()));
        query.addAscendingOrdering(Rule.RULE_NAME);
        query.setName("Get All Rules");
        return returnAsBeans(TopLinkQuery.asList(query, SessionManager.getClientSession()));
    }

    public TransferObject getRuleHistory(Long ruleId) {
        final ExpressionBuilder builder = new ExpressionBuilder();
        Expression exp = builder.get(RuleHistory.PARENT_RULE).get(Rule.RULE_ID).equal(ruleId);
        ReadAllQuery query = new ReadAllQuery(RuleHistory.class, exp);
        query.addAscendingOrdering(RuleHistory.VERSION_DATE_TIME);
        Vector history = (Vector) SessionManager.getClientSession().executeQuery(query);
        return new TransferObject(MapCar.map(RULE_HISTORY_BEAN_MAPCAR, history));
    }

    private static RuleHistoryBean buildRuleHistoryBean(RuleHistory history) {
        RuleHistoryBean bean = new RuleHistoryBean();
        bean.setContent(history.getContent());
        bean.setDescription(history.getDescription());
        bean.setParentRuleId(history.getParentRule().getRuleId());
        bean.setRemarks(history.getRemarks());
        bean.setRuleName(history.getRuleName());
        bean.setUserId(history.getUserId());
        bean.setVersionDateTime(history.getVersionDateTime());
        return bean;
    }

	public TransferObject getAllRules(String groupId) {
		return getAllRules();
	}
}