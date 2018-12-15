package com.profitera.services.business.treatment;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oracle.toplink.expressions.ExpressionBuilder;
import oracle.toplink.queryframework.ReadAllQuery;
import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.UnitOfWork;

import com.profitera.dataaccess.ITransaction;
import com.profitera.descriptor.business.treatment.admin.DefaultTreatmentGraph;
import com.profitera.descriptor.business.treatment.admin.TreatmentGraph;
import com.profitera.descriptor.db.reference.TreatmentStageRef;
import com.profitera.descriptor.db.rule.Rule;
import com.profitera.descriptor.db.rule.RuleBom;
import com.profitera.rpm.treatment.StreamBuilder;
import com.profitera.rpm.treatment.StreamRenderer;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.RuleQueryManager;
import com.profitera.util.MapCar;
import com.profitera.util.TopLinkQuery;

/**
 * 
 *
 * @author jamison
 */
public class RuleTreatmentGraphStorage implements TreatmentGraphLoader, TreatmentGraphSaver {

	/**
	 * @see com.profitera.services.business.treatment.TreatmentGraphLoader#loadGraph(com.profitera.descriptor.db.reference.TreatmentStageRef, oracle.toplink.sessions.Session)
	 */
	public DefaultTreatmentGraph loadGraph(TreatmentStageRef stage,	Session session) {
		List l = getStreamGraphs(session);
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			DefaultTreatmentGraph g = (DefaultTreatmentGraph) iter.next();
			if (((Number)g.getStageId()).intValue() == stage.getTreatmentStageId().intValue())
				return g;
		}
		return null;
	}
	
	private List getStreamGraphs(Session session) {
		ExpressionBuilder eb = new ExpressionBuilder();
		ReadAllQuery q = new ReadAllQuery(Rule.class, RuleQueryManager.getNonRuleExclusion(eb).not().and(eb.get(Rule.DELETED).equal(false)));
		q.setName("Get Stream Graphs");
		List rules = TopLinkQuery.asList(q, session);
		return MapCar.map(new MapCar(){
			public Object map(Object o) {
				return buildStream((Rule) o);
			}}, rules);
	}

	private DefaultTreatmentGraph buildStream(Rule r) {
		DefaultTreatmentGraph t = new StreamBuilder(r.getContent()).getGraph();
		t.setId(r.getRuleId());
		return t;
	}
	
	public void saveTreatmentGraph(TreatmentGraph graph, TreatmentStageRef stage, UnitOfWork uow) {
		String graphXml = StreamRenderer.getXMLString(stage.getTreatmentStageId().longValue()+"", graph);
		Long ruleId = graph.getId();
		if (ruleId != null){
			Rule oldVersion = (Rule) uow.registerExistingObject(new RuleQueryManager().getRule(ruleId));
			oldVersion.setDeleted(new Boolean(true));
		}
		Rule newVersion = null;
		newVersion = (Rule) uow.registerNewObject(new Rule());
		newVersion.setRuleName(RuleQueryManager.STREAM_RULE_NAME);
		RuleBom b = new RuleBom();
		b.setBomId(new Long(16));
		newVersion.setRuleBom((RuleBom) uow.registerExistingObject(b));
		newVersion.setContent(graphXml);
		newVersion.setCreatedDate(new Timestamp(System.currentTimeMillis()));
		newVersion.setDeleted(new Boolean(false));
		newVersion.setStatus("-");
	}

	public DefaultTreatmentGraph loadGraph(Map stage,IReadWriteDataProvider p) {
		return null;
	}

	public void saveTreatmentGraph(TreatmentGraph graph, TreatmentStageRef stage, IReadWriteDataProvider p, ITransaction t) {
		
	}

}
