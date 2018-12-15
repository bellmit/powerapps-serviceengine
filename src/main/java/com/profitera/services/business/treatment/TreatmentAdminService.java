/*
 * Created on Sep 3, 2003
 */
package com.profitera.services.business.treatment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import oracle.toplink.expressions.Expression;
import oracle.toplink.expressions.ExpressionBuilder;
import oracle.toplink.queryframework.ObjectLevelReadQuery;
import oracle.toplink.queryframework.ReadAllQuery;
import oracle.toplink.queryframework.ReportQuery;
import oracle.toplink.sessions.Session;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.deployment.rmi.TreatmentAdminServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.ITreatmentProcess;
import com.profitera.descriptor.business.reference.ReferenceBeanConverter;
import com.profitera.descriptor.business.reference.ReferenceBusinessBean;
import com.profitera.descriptor.business.reference.TreatmentStreamReferenceBusinessBean;
import com.profitera.descriptor.business.treatment.admin.ActionNode;
import com.profitera.descriptor.business.treatment.admin.DefaultTreatmentGraph;
import com.profitera.descriptor.business.treatment.admin.TreatmentGraph;
import com.profitera.descriptor.business.treatment.admin.TreatmentSubtypeBusinessBean;
import com.profitera.descriptor.db.account.AccountTreatmentPlan;
import com.profitera.descriptor.db.reference.NotifierCodeRef;
import com.profitera.descriptor.db.reference.TreatProcessTypeStatusRef;
import com.profitera.descriptor.db.reference.TreatmentStageRef;
import com.profitera.descriptor.db.reference.TreatprocSubtypeRef;
import com.profitera.descriptor.db.treatment.Template;
import com.profitera.descriptor.db.treatment.TreatprocTemplate;
import com.profitera.descriptor.db.user.User;
import com.profitera.descriptor.rpm.Treatable;
import com.profitera.persistence.SessionManager;
import com.profitera.rpm.treatment.TransactionTreatmentProducer;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.business.reference.ReferenceService;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.MapVerifyingMapCar;
import com.profitera.services.system.dataaccess.RPMDataManager;
import com.profitera.services.system.dataaccess.TreatmentAdminDataManager;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.MapCar;
import com.profitera.util.TopLinkQuery;
import com.profitera.util.reflect.Reflect;
import com.profitera.util.reflect.ReflectionException;

/**
 * @author jamison
 * 
 */
public class TreatmentAdminService extends ProviderDrivenService
		implements
			TreatmentAdminServiceIntf {
	private static final String LOADER_PROP = "treatmentGraphLoader";
	private static final String SAVER_PROP = "treatmentGraphSaver";

	/**
   * Null ID means insertion
   * 
   * @param subtypeBean
   * @return
   */
	public TransferObject updateTreatmentSubtype(TreatmentSubtypeBusinessBean subtypeBean) {
		// The subtype bean is actually 2 objects in one
		TreatprocSubtypeRef subTypeRef = new TreatprocSubtypeRef();
		TreatprocTemplate template = new TreatprocTemplate();
		dumpBean(subtypeBean, subTypeRef);
		dumpBean(subtypeBean, template);
		Session session = TreatmentAdminDataManager.getSession();
		subTypeRef = TreatmentAdminDataManager.updateSubtype(subTypeRef, template, session);
		// Use the service so I get the TransferObject for free...
		return getTreatmentSubtype(subTypeRef.getTreatprocSubtypeId());
	}

	public TransferObject getTreatmentSubtype(Double subtypeId) {
		Session session = TreatmentAdminDataManager.getSession();
		TreatprocSubtypeRef subType = getTreatprocSubtypeRef(subtypeId, session);
		TreatprocTemplate template = TreatmentAdminDataManager
				.getTemplateForSubtype(subtypeId, session);
		return new TransferObject(createBean(subType, template));
	}

	private TreatprocSubtypeRef getTreatprocSubtypeRef(Number id, Session s) {
		return (TreatprocSubtypeRef) TopLinkQuery.getObject(TreatprocSubtypeRef.class,
				new String[]{TreatprocSubtypeRef.TREATPROC_SUBTYPE_ID}, id, s);
	}

	public TransferObject getAllTreatmentSubtypes() {
		Vector beans = new Vector();
		try {
			Iterator iterator = getReadWriteProvider().query(IReadOnlyDataProvider.LIST_RESULTS,
					"getTreatmentSubtypes", null);
			while (iterator.hasNext()) {
				Map m = (Map) iterator.next();
				if (RPMDataManager.TREATMENT_PROCESS_TYPE_NONE == ((Number) m.get("TREATPROC_SUBTYPE_ID"))
						.intValue())
					continue;
				TreatmentSubtypeBusinessBean bean = new TreatmentSubtypeBusinessBean();
				bean.setId(new Double(((Long) m.get("TREATPROC_SUBTYPE_ID")).doubleValue()));
				bean.setCode((String) m.get("TREATPROC_TYPE_CODE"));
				bean.setDesc((String) m.get("TREATPROC_TYPE_DESC"));
				bean.setDisabled(((Boolean) m.get("DISABLE")).booleanValue());
				bean.setSortPriority(((Number) m.get("SORT_PRIORITY")).intValue());
				bean.setProcessTypeId(new Double(((Long) m.get("TREATPROC_TYPE_ID")).doubleValue()));
				Map template = getTreatProcTemplate((Long) m.get("TREATPROC_SUBTYPE_ID"));
				if (template != null) {
					bean.setDocumentTemplateId(m.get("DOCUMENT_TEMPLATE_ID") == null ? null : new Double(
							((Long) m.get("DOCUMENT_TEMPLATE_ID")).doubleValue()));
					bean.setDuration((Integer) template.get("DAYS_DURATION"));
					bean.setLeadTime((Integer) template.get("LEAD_TIME_HOURS"));
					bean.setIsNotifierProcess((Boolean) template.get("NOTIFIER_PROCESS"));
					bean.setNotiferId(template.get("NOTIFIER_CODE_ID") == null ? null : new Double(
							(((Long) template.get("NOTIFIER_CODE_ID")).doubleValue())));
					bean.setUpdateHost((Boolean) template.get("UPDATE_HOST"));
					bean.setCost((Double) template.get("COST"));
				}
				beans.add(bean);
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
		}

		return new TransferObject(beans);
	}

	private TransferObject asTreatmentSubtypes(Vector subTypes, Session session) {
		Vector beans = new Vector();
		Iterator i = subTypes.iterator();
		while (i.hasNext()) {
			TreatprocSubtypeRef subtype = (TreatprocSubtypeRef) i.next();
			TreatprocTemplate template = TreatmentAdminDataManager.getTemplateForSubtype(subtype
					.getTreatprocSubtypeId(), session);
			beans.add(createBean(subtype, template));
		}
		return new TransferObject(beans);
	}

	public TransferObject getAllTreatmentTypes() {
		return new ReferenceService().getTreatmentProcessTypeRef();
	}

	private TreatmentSubtypeBusinessBean createBean(TreatprocSubtypeRef subType,
			TreatprocTemplate template) {
		TreatmentSubtypeBusinessBean bean = new TreatmentSubtypeBusinessBean();
		bean.setId(subType.getTreatprocSubtypeId());
		bean.setCode(subType.getTreatprocTypeCode());
		bean.setDesc(subType.getTreatprocTypeDesc());
		bean.setDisabled(subType.getDisable().intValue() != 0);
		bean.setSortPriority(subType.getSortPriority().intValue());
		bean.setProcessTypeId(subType.getTreatprocTypeId());
		if (template != null) {
			if (template.getDocumentTemplate() != null)
				bean.setDocumentTemplateId(template.getDocumentTemplate().getTemplateId());
			bean.setDuration(template.getDaysDuration());
			bean.setLeadTime(template.getLeadTimeHours());
			bean.setIsNotifierProcess(template.getNotifierProcess());
			if (template.getNotifierCodeRef() != null)
				bean.setNotiferId(new Double(template.getNotifierCodeRef().getId().doubleValue()));
			else
				bean.setNotiferId(null);
			bean.setUpdateHost(template.getUpdateHost());
			bean.setCost(template.getCost());
		}
		return bean;
	}

	private void dumpBean(TreatmentSubtypeBusinessBean bean, TreatprocSubtypeRef subtypeRef) {
		subtypeRef.setTreatprocSubtypeId(bean.getId());
		subtypeRef.setTreatprocTypeCode(bean.getCode());
		subtypeRef.setTreatprocTypeDesc(bean.getDesc());
		subtypeRef.setTreatprocTypeId(bean.getProcessTypeId());
		subtypeRef.setDisable(bean.isDisabled() ? new Double(1) : new Double(0));
		subtypeRef.setSortPriority(new Double(bean.getSortPriority()));
	}

	private void dumpBean(TreatmentSubtypeBusinessBean bean, TreatprocTemplate template) {
		template.setTreatprocSubtypeId(bean.getId());
		template.setDaysDuration(bean.getDuration());
		template.setDocumentTemplate(new Template());
		template.getDocumentTemplate().setTemplateId(bean.getDocumentTemplateId());
		template.setLeadTimeHours(bean.getLeadTime());
		template.setNotifierProcess(bean.getIsNotifierProcess());
		NotifierCodeRef r = null;
		if (bean.getNotiferId() != null) {
			r = new NotifierCodeRef();
			r.setId(new Long(bean.getNotiferId().longValue()));
		}
		template.setNotifierCodeRef(r);
		template.setTreatprocTypeId(bean.getProcessTypeId());
		template.setCost(bean.getCost());
		template.setUpdateHost(bean.getUpdateHost());
	}

	/**
   * @see com.profitera.deployment.rmi.TreatmentAdminServiceIntf#getTreatmentSubtypes(java.lang.Class)
   */
	public TransferObject getTreatmentSubtypes(Class beanClass) {
	  throw new UnsupportedOperationException();
	}

	public TransferObject getTreatmentSubtypes(Double typeId) {
		Session session = TreatmentAdminDataManager.getSession();
		Vector subTypes = TreatmentAdminDataManager.getSubtypes(typeId, session);
		return asTreatmentSubtypes(subTypes, session);
	}

	/**
   * @see com.profitera.deployment.rmi.TreatmentAdminServiceIntf#getTreatmentTypeStatuses(java.lang.Class)
   */
	public TransferObject getTreatmentTypeStatuses(Class beanClass) {
	  throw new UnsupportedOperationException();
	}

	public TransferObject getTreatmentTypeStatuses(Double typeId) {
		Session session = TreatmentAdminDataManager.getSession();
		Vector statuses = (Vector) session.executeQuery(new ReadAllQuery(
				TreatProcessTypeStatusRef.class, new ExpressionBuilder().get(
						TreatProcessTypeStatusRef.TREATPROC_TYPE_ID).equal(typeId)));
		return asTreatmentTypeStatuses(statuses);
	}

	/**
   * @param statuses
   * @return
   */
	private TransferObject asTreatmentTypeStatuses(Vector statuses) {
		Vector result = new Vector();
		Iterator i = statuses.iterator();
		while (i.hasNext()) {
			TreatProcessTypeStatusRef s = (TreatProcessTypeStatusRef) i.next();
			result.add(ReferenceBeanConverter.convertToBusinessBean(s));
		}
		return new TransferObject(result);
	}

	public TransferObject getStreamGraphs() {
		TreatmentGraphLoader loader = getTreatmentGraphLoader();
		List allStages = getTreatmentStages();
		List l = new ArrayList();
		for (Iterator i = allStages.iterator(); i.hasNext();) {
			TreatmentGraph g = loader.loadGraph((Map) i.next(), getReadWriteProvider());
			if (g != null)
				l.add(g);
		}
		return new TransferObject(l);
	}

	/**
   * 
   * @see com.profitera.deployment.rmi.TreatmentAdminServiceIntf#updateStreamGraph(com.profitera.descriptor.business.reference.ReferenceBusinessBean,
   *      com.profitera.descriptor.business.treatment.admin.TreatmentGraph)
   */
	public TransferObject updateStreamGraph(ReferenceBusinessBean stage, final TreatmentGraph graph,
			final List conditions) {
		final Map args = new HashMap(1);
		args.put("TREATMENT_STAGE_ID", stage.getId());
		final TreatmentStageRef stageRef = new TreatmentStageRef();
		stageRef.setTreatmentStageId(stage.getId());
		stageRef.setTreatmentStageCode(stage.getCode());
		stageRef.setTreatmentStageDesc(stage.getDesc());
		stageRef.setDisable(stage.isDisabled() ? new Double(1) : new Double(0));
		stageRef.setSortPriority(new Double(stage.getSortPriority()));
		try {
		  getReadWriteProvider().execute(new IRunnableTransaction() {

				public void execute(ITransaction t) throws SQLException, AbortTransactionException {
					getTreatmentGraphSaver().saveTreatmentGraph(graph, stageRef, getReadWriteProvider(), t);
					for (int i = 0; i < conditions.size(); i++) {
						if (i == 0) {
						  getReadWriteProvider().delete("deleteTreatmentStageCondition", conditions.get(i), t);
						}
						getReadWriteProvider().insert("insertTreatmentStageCondition", conditions.get(i), t);
					}
				}
			});
		} catch (AbortTransactionException e) {
			return new TransferObject(TransferObject.ERROR, e.getMessage());
		} catch (SQLException e) {
			return new TransferObject(TransferObject.ERROR, e.getMessage());
		}
		return new TransferObject(getStreamGraph(stageRef));
	}

	private TreatmentGraphSaver getTreatmentGraphSaver() {
		try {
			ServiceEngine.getConfig(true);
			return (TreatmentGraphSaver) Reflect.invokeConstructor(ServiceEngine
					.getProp(SAVER_PROP, null), null, null);
		} catch (ReflectionException e) {
			throw new RuntimeException("Unable to load class define by property: " + SAVER_PROP, e);
		}
	}

	private TreatmentGraphLoader getTreatmentGraphLoader() {
			return new com.profitera.services.business.treatment.ObjectMappedGraphStorage();
	}

	private DefaultTreatmentGraph getStreamGraph(TreatmentStageRef stageRef) {
		Map stage = new HashMap(5);
		stage.put("TREATMENT_STAGE_ID", stageRef.getTreatmentStageId());
		stage.put("TREATMENT_STAGE_CODE", stageRef.getTreatmentStageCode());
		stage.put("TREATMENT_STAGE_DESC", stageRef.getTreatmentStageDesc());
		stage.put("SORT_PRIORITY", stageRef.getSortPriority());
		stage.put("DISABLE", stageRef.getDisable());
		return getTreatmentGraphLoader().loadGraph(stage, getReadWriteProvider());
	}

	public TransferObject getOpenPlansAtNodeCount(String nodeId) {
		Session s = SessionManager.getClientSession();
		ReportQuery rq = new ReportQuery();
		configurePlanAtNodeQuery(rq, nodeId);
		rq.addCount();
		rq.setName("Get Open Plans At Node Count");
		return new TransferObject(new Double(TopLinkQuery.getSingleRowQueryResult(0, rq, s)));
	}

	public TransferObject getAccountsAtNode(String nodeId) {
		try {
			return new TransferObject(executeListQuery(IReadOnlyDataProvider.LIST_RESULTS,
					"getAccountsAtNode", nodeId, new MapVerifyingMapCar(new String[]{"ACCOUNT_ID"}),
					getReadOnlyProvider()));
		} catch (TransferObjectException e) {
			return e.getTransferObject();
		}
	}

	private void configurePlanAtNodeQuery(ObjectLevelReadQuery rq, String nodeId) {
		ExpressionBuilder eb = new ExpressionBuilder();
		Expression e = eb.get(AccountTreatmentPlan.TREATMENT_END_DATE).isNull().and(
				eb.get(AccountTreatmentPlan.NODE_LOCATION).equal(nodeId));
		rq.setReferenceClass(AccountTreatmentPlan.class);
		rq.setSelectionCriteria(e);
	}

	/**
   * @see com.profitera.deployment.rmi.TreatmentAdminServiceIntf#transferTreatmentPlans(com.profitera.descriptor.business.treatment.admin.ActionNode,
   *      com.profitera.descriptor.business.treatment.admin.ActionNode)
   */
	public TransferObject transferTreatmentPlans(final ActionNode sourceNode,
			final ActionNode transferTo) {
		try {
			checkNodes(sourceNode, transferTo);
		} catch (RuntimeException e) {
			return new TransferObject(TransferObject.ERROR, e.getMessage());
		}
		ReadAllQuery rq = new ReadAllQuery();
		rq.setName("Get All Open Plans At Node");
		configurePlanAtNodeQuery(rq, sourceNode.getId());
		rq.addJoinedAttribute(AccountTreatmentPlan.ACCOUNT);
		return transferResults(sourceNode, transferTo, rq);
	}

	public TransferObject transferAccounts(final ActionNode sourceNode, final ActionNode transferTo,
			Double[] accountIds) {
		try {
			checkNodes(sourceNode, transferTo);
		} catch (RuntimeException e) {
			return new TransferObject(TransferObject.ERROR, e.getMessage());
		}
		return transferResults(sourceNode, transferTo, accountIds );
	}

	private TransferObject transferResults(final ActionNode sourceNode, final ActionNode transferTo,Double[] accountIds) {
		Session s = SessionManager.getClientSession();
		final User user = RPMDataManager.getSystemUser(s);
		final TransactionTreatmentProducer producer = new TransactionTreatmentProducer(getReadWriteProvider());
		producer.setUserId(user.getUserId());
		final TransferObject[] tos = new TransferObject[1];
		tos[0] = new TransferObject("Success");
		final Date date = new Date();
		for(int i=0;i<accountIds.length;i++){
			Long accountId = new Long(accountIds[i].longValue());
			Map treatmentProcess=null;
			try {
				treatmentProcess = (Map) getReadWriteProvider().queryObject("getLastInProgressSystemTreatmentProcess",accountId);
			} catch (SQLException e1) {
				log.error("Unable to get last in progress treatment process for account " + accountId + ". Account will not be transferred.",e1);
				tos[0] = new TransferObject(TransferObject.ERROR,
	          "One or more accounts where not successfully transferred");
				continue;
			}
			Treatable treatable = getTreatable(accountId, treatmentProcess==null?null:(Number) treatmentProcess.get(ITreatmentProcess.TREATMENT_PROCESS_ID));
			if(treatmentProcess!=null){
				producer.doCancelCurrentAction(treatable,null,date);
			}
			producer.setAccountId(new Long(accountId.longValue()));
			Map row = null;
			try {
				row = (Map) getReadWriteProvider().queryObject("getDelinquentAccountPlansByAccountId",
						accountId);
			} catch (SQLException e) {
				throw new RuntimeException("Unable to retrieve treatment plan information for account: "
						+ accountId, e);
			}
			producer.setPlan(row);
			producer.doTraversal(treatable, sourceNode, transferTo, null, date);
		}
		try {
			getReadWriteProvider().execute(producer.dumpTransaction());
		} catch (AbortTransactionException e) {
			tos[0] = new TransferObject(TransferObject.ERROR,
					"One or more accounts where not successfully transferred:\n" + e.getMessage());
		} catch (SQLException e) {
			tos[0] = new TransferObject(TransferObject.ERROR,
					"One or more accounts where not successfully transferred:\n" + e.getMessage());
		}
		return tos[0];
	}

	private Treatable getTreatable(final Object accountID, final Number treatmentProcessID){
		return new Treatable(){
			public String getCurrentLocation() {
				return null;
			}
			public Integer getCurrentActionType() {
				return null;
			}
			public Long getStageId() {
				return null;
			}
			public Object getId() {
				return accountID;
			}
			public Integer getCurrentPlanActionStatus() {
				return null;
			}
			public int getDaysAtCurrentPlanActionStatus() {
				return 0;
			}
			public boolean getCurrentPlanActionNotSuccessful() {
				return false;
			}
			public int getCurrentPlanActionAttempts() {
				return 0;
			}
			public Object getDesiredStreamId() {
				return null;
			}
			public Object getCurrentStreamId() {
				return null;
			}
			public Number getCurrentPlanActionId() {
				return treatmentProcessID;
			}
			
		}; 
	}
	private void checkNodes(ActionNode sourceNode, ActionNode transferTo) {
		if (sourceNode == null || sourceNode.getId() == null) {
			if (sourceNode == null)
				log.info("Attemped transfer failed: source node specified null(Destination was: "
						+ transferTo + ")");
			else
				log.info("Attemped transfer failed: source node specified has null id (Source was: "
						+ sourceNode + " - Destination was: " + transferTo + ")");
			throw new RuntimeException("Source action for transfer invalid");
		}
	}

	private TransferObject transferResults(final ActionNode sourceNode, final ActionNode transferTo,
			ReadAllQuery rq) {
	  throw new UnsupportedOperationException();
	}
	/**
   * @see com.profitera.deployment.rmi.TreatmentAdminServiceIntf#getStreamGraph(com.profitera.descriptor.business.reference.ReferenceBusinessBean)
   */
	public TransferObject getStreamGraph(ReferenceBusinessBean stage) {
		List l = (List) getStreamGraphs().getBeanHolder();
		for (Iterator i = l.iterator(); i.hasNext();) {
			DefaultTreatmentGraph g = (DefaultTreatmentGraph) i.next();
			if (((Number) g.getStageId()).longValue() == stage.getId().longValue())
				return new TransferObject(g);
		}
		return new TransferObject(TransferObject.ERROR, "No treatment streams defined for stage: "
				+ stage.getDesc());
	}

	/**
   * @see com.profitera.deployment.rmi.TreatmentAdminServiceIntf#addTreatmentStream(com.profitera.descriptor.business.reference.TreatmentStreamReferenceBusinessBean)
   */
	public TransferObject addTreatmentStream(final TreatmentStreamReferenceBusinessBean stream) {
		try {
		  getReadWriteProvider().execute(new IRunnableTransaction() {

				public void execute(ITransaction t) throws SQLException, AbortTransactionException {
					Map parameter = new HashMap();
					parameter.put("TREATMENT_STREAM_CODE", stream.getCode());
					parameter.put("TREATMENT_STREAM_DESC", stream.getDesc());
					parameter.put("TREATMENT_STAGE_ID", stream.getStage().getId());
					parameter.put("DISABLE", stream.isDisabled() ? new Integer(1) : new Integer(0));
					parameter.put("SORT_PRIORITY", new Integer(stream.getSortPriority()));
					Object id = getReadWriteProvider().insert("insertTreatmentStream", parameter, t);
					stream.setId(new Double(((Long) id).doubleValue()));
				}
			});
			return new TransferObject(stream);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return new TransferObject(TransferObject.ERROR, e.getMessage());
		}
	}

	public TransferObject updateTreatmentStream(final TreatmentStreamReferenceBusinessBean stream) {
		try {
		  getReadWriteProvider().execute(new IRunnableTransaction() {

				public void execute(ITransaction t) throws SQLException, AbortTransactionException {
					Map parameter = new HashMap();
					parameter.put("TREATMENT_STREAM_ID", stream.getId());
					parameter.put("TREATMENT_STREAM_CODE", stream.getCode());
					parameter.put("TREATMENT_STREAM_DESC", stream.getDesc());
					parameter.put("TREATMENT_STAGE_ID", stream.getStage().getId());
					parameter.put("DISABLE", stream.isDisabled() ? new Integer(1) : new Integer(0));
					parameter.put("SORT_PRIORITY", new Integer(stream.getSortPriority()));
					getReadWriteProvider().update("updateTreatmentStream", parameter, t);
				}
			});
			return new TransferObject(stream);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return new TransferObject(TransferObject.ERROR, e.getMessage());
		}
	}

	/**
   * @see com.profitera.deployment.rmi.TreatmentAdminServiceIntf#getNotifierCodes()
   */
	public TransferObject getNotifierCodes() {
		ReadAllQuery q = new ReadAllQuery(NotifierCodeRef.class);
		q.setName("Get Notifier Codes");
		return new TransferObject(MapCar.map(new MapCar() {
			public Object map(Object o) {
				NotifierCodeRef n = (NotifierCodeRef) o;
				return ReferenceBeanConverter.convertToBusinessBean(n);
			}
		}, TopLinkQuery.asList(q, SessionManager.getClientSession())));
	}

	public TransferObject updateStageOrder(final List stages) {
		try {
		  getReadWriteProvider().execute(new IRunnableTransaction() {

				public void execute(ITransaction t) throws SQLException, AbortTransactionException {
					for (int i = 0; i < stages.size(); i++) {
					  getReadWriteProvider().update("updateTreatmentStageSortPriority", stages.get(i), t);
					}

				}
			});
		} catch (AbortTransactionException e) {
			return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
		} catch (SQLException e) {
			return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
		}

		return new TransferObject("SUCCESS");
	}

	public TransferObject addTreatmentStage(final ReferenceBusinessBean stage, final String rootID) {
		try {
		  getReadWriteProvider().execute(new IRunnableTransaction() {

				public void execute(ITransaction t) throws SQLException, AbortTransactionException {
					Map m = new HashMap(5);
					m.put("TREATMENT_STAGE_CODE", stage.getCode());
					m.put("TREATMENT_STAGE_DESC", stage.getDesc());
					m.put("SORT_PRIORITY", new Integer(stage.getSortPriority()));
					m.put("DISABLE", Boolean.valueOf(stage.isDisabled()));
					if (rootID != null)
						m.put("ROOT_ID", new Long(rootID));

					getReadWriteProvider().insert("insertTreatmentStage", m, t);

				}
			});
		} catch (Exception e) {
			return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
		}
		return new TransferObject("SUCCESS");
	}

	public TransferObject updateTreatmentStage(final ReferenceBusinessBean stage, final String rootId) {
		try {
		  getReadWriteProvider().execute(new IRunnableTransaction() {
				public void execute(ITransaction t) throws SQLException, AbortTransactionException {
					Map m = new HashMap(6);
					m.put("TREATMENT_STAGE_ID", stage.getId());
					m.put("TREATMENT_STAGE_CODE", stage.getCode());
					m.put("TREATMENT_STAGE_DESC", stage.getDesc());
					m.put("SORT_PRIORITY", new Integer(stage.getSortPriority()));
					m.put("DISABLE", Boolean.valueOf(stage.isDisabled()));
					if (rootId != null)
						m.put("ROOT_ID", new Long(rootId));
					getReadWriteProvider().update("updateTreatmentStage", m, t);

				}
			});
		} catch (Exception e) {
			return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
		}
		return new TransferObject("SUCCESS");
	}

	private Map getTreatProcTemplate(Long subtypeId) {
		try {
			return (Map) getReadWriteProvider().queryObject("getTreatProcTemplateBySubtypeID", subtypeId);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private List getTreatmentStages() {
		try {
			Iterator i = getReadWriteProvider().query(IReadOnlyDataProvider.LIST_RESULTS, "getTreatmentStageRef", null);
			List l = new ArrayList();
			while (i.hasNext())
				l.add(i.next());

			return l;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public TransferObject resetCustomerAccountTreatment() {
		try {
		  getReadWriteProvider().execute(new IRunnableTransaction() {

				public void execute(ITransaction t) throws SQLException, AbortTransactionException {
				  getReadWriteProvider().update("resetAccountTreatment", null, t);
					getReadWriteProvider().update("resetCustomerTreatment", null, t);
					getReadWriteProvider().update("resetAccountTreatmentPlan", null, t);

				}
			});
		} catch (Exception e) {
			return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
		}
		return new TransferObject("SUCCESS");
	}

}
