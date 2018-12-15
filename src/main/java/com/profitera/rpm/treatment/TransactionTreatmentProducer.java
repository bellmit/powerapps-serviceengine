package com.profitera.rpm.treatment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.deployment.rmi.TreatmentWorkpadServiceIntf;
import com.profitera.descriptor.business.meta.ITreatmentProcess;
import com.profitera.descriptor.business.reference.TreatmentProcessTypeStatusRefBusinessBean;
import com.profitera.descriptor.business.treatment.admin.ActionNode;
import com.profitera.descriptor.business.treatment.admin.Transition;
import com.profitera.descriptor.db.reference.NotifierCodeRef;
import com.profitera.descriptor.db.reference.TemplateTypeRef;
import com.profitera.descriptor.db.reference.TreatmentStageRef;
import com.profitera.descriptor.db.reference.TreatmentStreamRef;
import com.profitera.descriptor.db.reference.TreatprocSubtypeRef;
import com.profitera.descriptor.db.treatment.Template;
import com.profitera.descriptor.db.treatment.TreatprocTemplate;
import com.profitera.descriptor.rpm.Treatable;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.ITreatmentProcessService;
import com.profitera.services.system.dataaccess.TreatmentProcessCreationException;
import com.profitera.services.system.dataaccess.TreatmentProcessUpdateException;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.MapCar;

/**
 * 
 *
 * @author jamison
 */
public class TransactionTreatmentProducer implements TreatmentProducer {
  private static final String GET_TREATMENT_STAGE_REF_QUERY_NAME = "getTreatmentStageRefForTreatmentAssignment";
	private Log log = LogFactory.getLog(TransactionTreatmentProducer.class);
	private Map plan;
	private Map subTypes = new HashMap();
	private Map templates  = new HashMap();
	private List treatmentStages;
	private Map streams;
	private Map cancelledTypeStatii  = new HashMap();
    private Long accountId;
    private String userId;
    private List transactions = new ArrayList();
    private IReadWriteDataProvider provider;
	
	public TransactionTreatmentProducer(IReadWriteDataProvider p){
	  provider = p;
	  List allStreams = getTreatmentStreamRef(p);
	  List stages = getTreatmentStageInOrder(p);
	  List[] subTypesAndTemplates = getTreatmentSubtypeTemplates(p);
	  List processSubtypes = subTypesAndTemplates[0];
	  List templateList = subTypesAndTemplates[1];
		treatmentStages = stages;
		for (Iterator iter = processSubtypes.iterator(); iter.hasNext();) {
			TreatprocSubtypeRef subtype = (TreatprocSubtypeRef) iter.next();
			subTypes.put(new Integer(subtype.getTreatprocSubtypeId().intValue()), subtype);			
		}
		for (Iterator iter = templateList.iterator(); iter.hasNext();) {
			TreatprocTemplate t = (TreatprocTemplate) iter.next();
			if (t!= null)
				templates.put(new Integer(t.getTreatprocSubtypeId().intValue()), t);
		}
		streams = new HashMap();
		for (Iterator iter = allStreams.iterator(); iter.hasNext();) {
			TreatmentStreamRef t = (TreatmentStreamRef) iter.next();
			streams.put("" + t.getTreatmentStreamId().intValue(), t);
		}
	}
	 class StageComparator implements Comparator {

	    public int compare(Object o1, Object o2) {
	      TreatmentStageRef ref1 = (TreatmentStageRef) o1;
	      TreatmentStageRef ref2 = (TreatmentStageRef) o2;
	      return ref1.getTreatmentStageId().compareTo(ref2.getTreatmentStageId());
	    }   
	  };
	 private StageComparator stageComparator = new StageComparator();
	  
	  private List getTreatmentStreamRef(IReadWriteDataProvider p){
	    try {
	      Iterator iterator = p.query(IReadOnlyDataProvider.LIST_RESULTS,GET_TREATMENT_STREAM_REF_QUERY_NAME,null);
	      List streams = new ArrayList();     
	      MapCar.map(TREATMENT_STREAM_REF_MAPCAR,iterator, streams);    
	      return streams;
	    } catch (SQLException e) {
	      log.fatal(e.getMessage(),e);
	      throw new RuntimeException(e);
	    }   
	  }
	  
	  private static final String GET_TREATMENT_STREAM_REF_QUERY_NAME = "getTreatmentStreamRefForTreatmentAssignment";


	  private final MapCar TREATMENT_STREAM_REF_MAPCAR = new MapCar() {
	    public Object map(Object o) {
	      return buildTreatmentStreamRef((HashMap) o);
	    }
	  };
	  
	  private TreatmentStreamRef buildTreatmentStreamRef(Map m){
	    TreatmentStreamRef ref = new TreatmentStreamRef();
	    ref.setDisable((Double) m.get("DISABLE"));
	    ref.setSortPriority((Double) m.get("SORT_PRIORITY"));
	    try {
	      Map map = (Map) provider.queryObject(GET_TREATMENT_STAGE_REF_QUERY_NAME,m);
	      ref.setTreatmentStageRef(buildTreatmentStageRef(map));
	    } catch (SQLException e) {
	      log.fatal(e.getMessage(),e);
	      throw new RuntimeException(e);
	    }
	    ref.setTreatmentStreamCode((String) m.get("TREATMENT_STREAM_CODE"));
	    ref.setTreatmentStreamDesc((String) m.get("TREATMENT_STREAM_DESC"));
	    ref.setTreatmentStreamId((Double) m.get("TREATMENT_STREAM_ID"));
	    return ref;
	  }


	  private static final MapCar TREATMENT_STAGE_REF_MAPCAR = new MapCar() {
	    public Object map(Object o) {
	      return buildTreatmentStageRef((HashMap) o);
	    }
	  };
	  
	  private static TreatmentStageRef buildTreatmentStageRef(Map m){
	    TreatmentStageRef ref = new TreatmentStageRef();
	    ref.setDisable((Double) m.get("DISABLE"));
	    ref.setSortPriority((Double) m.get("SORT_PRIORITY"));
	    ref.setTreatmentStageCode((String) m.get("TREATMENT_STAGE_CODE"));
	    ref.setTreatmentStageDesc((String) m.get("TREATMENT_STAGE_DESC"));
	    ref.setTreatmentStageId((Double) m.get("TREATMENT_STAGE_ID"));
	    return ref;
	  }

	
	public List getTreatmentStageInOrder(IReadWriteDataProvider p) {
    try {
      Iterator iterator = p.query(IReadOnlyDataProvider.LIST_RESULTS,GET_TREATMENT_STAGE_REF_QUERY_NAME,null);
      List stages = new ArrayList();      
      MapCar.map(TREATMENT_STAGE_REF_MAPCAR,iterator, stages);    
      Collections.sort(stages,stageComparator);
      return stages;
    } catch (SQLException e) {
      log.fatal(e.getMessage(),e);
      throw new RuntimeException(e);
    }   
  }

	
	private static final MapCar TREATMENT_SUBTYPE_REF_MAPCAR = new MapCar() {
    public Object map(Object o) {
      return buildTreatmentSubtypeRef((HashMap) o);
    }
  };
  private static TreatprocSubtypeRef buildTreatmentSubtypeRef(Map m){
    TreatprocSubtypeRef ref = new TreatprocSubtypeRef();
    Object o = m.get("DISABLE");
    if (o instanceof Boolean && ((Boolean)o).booleanValue()){
      o = new Double(1);
    }else if (o instanceof Boolean && !((Boolean)o).booleanValue()){
      o = new Double(0);
    }
    ref.setDisable((Double)o);
    ref.setSortPriority((Double) m.get("SORT_PRIORITY"));
    ref.setTreatprocSubtypeId((Double) m.get("TREATPROC_SUBTYPE_ID"));
    ref.setTreatprocTypeCode((String) m.get("TREATPROC_TYPE_CODE"));
    ref.setTreatprocTypeDesc((String) m.get("TREATPROC_TYPE_DESC"));
    ref.setTreatprocTypeId((Double) m.get("TREATPROC_TYPE_ID"));
    return ref;
  }
  private final MapCar TREATPROC_TEMPLATE_MAPCAR = new MapCar() {
    public Object map(Object o) {
      return buildTreatprocTemplate((HashMap) o);
    }
  };
  private TreatprocTemplate buildTreatprocTemplate(Map m){
    if(m==null) return null;
    TreatprocTemplate template = new TreatprocTemplate();
    template.setCost((Double) m.get("COST"));
    template.setDaysDuration((Integer) m.get("DAYS_DURATION"));
    template.setDocumentTemplate(buildDocumentTemplateRef((Long) m.get("DOCUMENT_TEMPLATE_ID")));
    template.setLeadTimeHours((Integer) m.get("LEAD_TIME_HOURS"));
    template.setNotifierCodeRef(buildNotifierCodeRef((Long) m.get("NOTIFIER_CODE_ID")));
    template.setNotifierProcess((Boolean) m.get("NOTIFIER_PROCESS"));
    template.setTreatprocSubtypeId((Double) m.get("TREATPROC_SUBTYPE_ID"));
    template.setTreatprocTemplateId((Double) m.get("TREATPROC_TEMPLATE_ID"));
    template.setTreatprocTypeId((Double) m.get("TREATPROC_TYPE_ID"));
    template.setUpdateHost((Boolean) m.get("UPDATE_HOST"));   
    return template;
  }
  
  private Template buildDocumentTemplateRef(Long templateId){
    if(templateId==null) return null;
    try {
      Map temp = (Map) provider.queryObject(GET_TEMPLATE_QUERY_NAME,templateId);
      if(temp==null) throw new RuntimeException("Template not found: "+templateId);
      Template template = new Template();
      template.setContent((String) temp.get("CONTENT"));
      template.setDescription((String) temp.get("DESCRIPTION"));
      template.setDisable((Boolean) temp.get("DISABLE"));
      template.setTemplateId((Double) temp.get("TEMPLATE_ID"));
      template.setTemplateName((String) temp.get("TEMPLATE_NAME"));
      template.setTypeRef(buildTemplateTypeRef((Long) temp.get("TEMPLATE_TYPE_ID")));
      return template;
    } catch (SQLException e) {
      log.fatal(e.getMessage(),e);
      throw new RuntimeException(e);
    }   
  }
  private static final String GET_TEMPLATE_TYPE_REF_QUERY_NAME = "getTemplateTypeRefByTemplateTypeID";
  private TemplateTypeRef buildTemplateTypeRef(Long templateTypeID){
    if(templateTypeID==null) return null;
    try {
      Map temp = (Map) provider.queryObject(GET_TEMPLATE_TYPE_REF_QUERY_NAME,templateTypeID);
      TemplateTypeRef ref = new TemplateTypeRef();
      ref.setDisable((Double) temp.get("DISABLE"));
      ref.setSortPriority((Double) temp.get("SORT_PRIORITY"));
      ref.setTemplateTypeCode((String) temp.get("TEMPLATE_TYPE_CODE"));
      ref.setTemplateTypeDesc((String) temp.get("TEMPLATE_TYPE_DESC"));
      ref.setTemplateTypeId((Double) temp.get("TEMPLATE_TYPE_ID"));
      return ref;
    } catch (SQLException e) {
      log.fatal(e.getMessage(),e);
      throw new RuntimeException(e);
    }   
  }

  
  private static final String GET_NOTIFIER_CODE_REF_QUERY_NAME = "getNotifierCodeRefbyRefID";
  private static final String GET_TEMPLATE_QUERY_NAME = "getTemplatebyTemplateID";
  private NotifierCodeRef buildNotifierCodeRef(Long notifierCodeID){
    if(notifierCodeID==null) return null;
    try {
      Map notifier = (Map) provider.queryObject(GET_NOTIFIER_CODE_REF_QUERY_NAME,notifierCodeID);
      NotifierCodeRef ref = new NotifierCodeRef();
      ref.setId(notifierCodeID);
      ref.setCode((String) notifier.get("CODE"));
      ref.setDescription((String) notifier.get("DESCRIPTION"));
      ref.setDisable(new Short(((Number) notifier.get("DISABLE")).shortValue()));
      ref.setSortPriority((Integer) notifier.get("SORT_PRIORITY"));
      return ref;
    } catch (SQLException e) {
      log.fatal(e.getMessage(),e);
      throw new RuntimeException(e);
    }   
  }

  
  private static final String GET_TREATMENT_SUBTYPE_REF_QUERY_NAME = "getTreatmentSubtypeRef";
  private static final String GET_TREATMENT_PROC_TEMPLATE_QUERY_NAME = "getTreatprocTemplateBySubtypeID";
	 private List[] getTreatmentSubtypeTemplates(IReadWriteDataProvider p) {
	    try {
	      Iterator iterator = p.query(IReadOnlyDataProvider.LIST_RESULTS,GET_TREATMENT_SUBTYPE_REF_QUERY_NAME,null);
	      List subtypes = new ArrayList();
	      List temp = new ArrayList();
	      MapCar.map(TREATMENT_SUBTYPE_REF_MAPCAR,iterator, subtypes);      
	      for(int i=0;i<subtypes.size();i++){
	        TreatprocSubtypeRef ref = (TreatprocSubtypeRef) subtypes.get(i);
	        temp.add(p.queryObject(GET_TREATMENT_PROC_TEMPLATE_QUERY_NAME,ref.getTreatprocSubtypeId()));
	      }
	      List templates = MapCar.map(TREATPROC_TEMPLATE_MAPCAR,temp);
	      return new List[]{subtypes, templates};
	    } catch (SQLException e) {
	      log.fatal(e.getMessage(),e);
	      throw new RuntimeException(e);
	    }
	  }
	
	
	

	/**
	 * @see com.profitera.rpm.treatment.TreatmentProducer#doActionRetry(com.profitera.descriptor.rpm.Treatable, com.profitera.rpm.ActionNode, Date)
	 */
	public void doActionRetry(Treatable t, ActionNode an, Date date) {
		int attemptNumber = t.getCurrentPlanActionAttempts() + 1;
		log.debug(t.getId() + ": Retrying node " + an.getId() + ", attempt number " + attemptNumber);
		addTreatmentProcess(an, attemptNumber, date);
	}

	/**
	 * @see com.profitera.rpm.treatment.TreatmentProducer#doTraversal(com.profitera.descriptor.rpm.Treatable, com.profitera.rpm.ActionNode, com.profitera.rpm.ActionNode, com.profitera.rpm.Transition, Date)
	 */
	public void doTraversal(Treatable t, ActionNode an, ActionNode nextAction, Transition trans, Date date) {
		if (trans != null)
		  log.debug(t.getId() + ": From " + an.getId() + " to " + nextAction.getId() + " via " + trans.getEntryStatus()+"(Weight: "+trans.getWeight()+")");
		else
		  log.debug(t.getId() + ": Direct transfer from from " + an.getId() + " to " + nextAction.getId());
		addTreatmentProcess(nextAction, 1, date);
	}

	private void addTreatmentProcess(ActionNode nextAction, int attemptNumber, Date date) {
    ITreatmentProcessService service = getTreatmentProcessService();
    TreatprocSubtypeRef st = (TreatprocSubtypeRef)subTypes.get(nextAction.getProcessSubtype());
    TreatmentStreamRef stream = null;
    if (nextAction.getStreamId() != null){
      String newStream = nextAction.getStreamId().toString();
      stream = (TreatmentStreamRef) streams.get(newStream);
    }
    Map process = new HashMap();
    process.put(ITreatmentProcess.TREATMENT_PLAN_ID, plan.get(ITreatmentProcess.TREATMENT_PLAN_ID));
    process.put(ITreatmentProcess.PROCESS_SUBTYPE_ID, new Long(st.getTreatprocSubtypeId().longValue()));
    Long typeId = new Long(st.getTreatprocTypeId().longValue());
    process.put(ITreatmentProcess.PROCESS_TYPE_ID, typeId);
    process.put(ITreatmentProcess.ATTEMPT_NUMBER, new Long(attemptNumber));
    process.put(ITreatmentProcess.PROCESS_STATUS_ID, new Long(TreatmentProcessTypeStatusRefBusinessBean.IN_PROGRESS_TREATMENT_PROCESS_STATUS.longValue()));
    Long streamId = stream == null ? null : new Long(stream.getTreatmentStreamId().longValue());
    
    try {
      plan.put("NODE_LOCATION", nextAction.getId());
      plan.put("TREATMENT_STREAM_ID", streamId);
      IRunnableTransaction[] t = service.createSystemProcess(process, accountId, plan, date, typeId, !nextAction.getProcessType().equals(ActionNode.PIT_STOP_ID));
      transactions.addAll(Arrays.asList(t));
    } catch (TreatmentProcessCreationException e) {
      throw new RuntimeException(e);
    }
		//	If the action node has a stream, set our current stream to that.
    if (streamId != null){
      final Long accountStream = streamId;
      final Long accountId = this.accountId;
      transactions.add(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          Map args = new HashMap();
          args.put("ACCOUNT_ID", accountId);
          args.put(ITreatmentProcess.TREATMENT_STREAM_ID, accountStream);
          provider.update("updateAccountTreatmentStream", args, t);
        }});
      log.debug(accountId + ": Stream will be set to " + stream.getTreatmentStreamDesc());
    }
	}

  /**
	 * @see com.profitera.rpm.treatment.TreatmentProducer#doStageTraversal(com.profitera.descriptor.rpm.Treatable, com.profitera.rpm.ActionNode, com.profitera.rpm.Transition, Date)
	 */
	public long doStageTraversal(Treatable treatable, ActionNode an, Transition trans, Date date) {
    Object treatableId = treatable.getId();
    Long stageId = treatable.getStageId();
		int stage = stageId == null ? Integer.MIN_VALUE : stageId.intValue();
		TreatmentStageRef newStage = getNextStage(stage);
    String nodeId = an == null ? null : an.getId();
		log.debug(treatableId + ": from " + nodeId + " advancing to stage " + newStage.getTreatmentStageDesc());
    final Long newStageId = new Long(newStage.getTreatmentStageId().longValue());
    plan.put("TREATMENT_STAGE_ID", newStageId);
    plan.put("TREATMENT_STAGE_START_DATE", date);
    plan.put("TREATMENT_STREAM_ID", null);
    plan.put("NODE_LOCATION", null);
    final Map transactionPlan = plan;
    transactions.add(new IRunnableTransaction(){
      public void execute(ITransaction t) throws SQLException, AbortTransactionException {
        provider.update("updateTreatmentPlanTreatmentStageInformation", transactionPlan, t);
        provider.update("updateAccountTreatmentStageInformation", transactionPlan, t);
        provider.update("updateCustomerTreatmentStageInformation", transactionPlan, t);
      }});
    return newStageId.longValue();
	}
	
	public void doStageTraversal(final Map transactionPlan) {
		try{
			provider.execute(new IRunnableTransaction(){
			      public void execute(ITransaction t) throws SQLException, AbortTransactionException {
			        provider.update("updateTreatmentPlanTreatmentStageInformation", transactionPlan, t);
			        provider.update("updateAccountTreatmentStageInformation", transactionPlan, t);
			        provider.update("updateCustomerTreatmentStageInformation", transactionPlan, t);
			      }});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private TreatmentStageRef getNextStage(int prev) {
		for (Iterator i = treatmentStages.iterator(); i.hasNext();) {
			TreatmentStageRef s = (TreatmentStageRef) i.next();
			if (s.getTreatmentStageId().intValue() > prev)
				return s;
		} // If there is no greater stage return the last one again, what else?!
		return (TreatmentStageRef) treatmentStages.get(treatmentStages.size() - 1);
	}
	
	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}
	public void setPlan(Map plan) {
		this.plan = plan;
	}
	public void setUserId(String user) {
		this.userId = user;
	}

  /**
   * @see com.profitera.rpm.treatment.TreatmentProducer#doCancelCurrentAction(com.profitera.descriptor.rpm.Treatable, com.profitera.descriptor.business.treatment.admin.ActionNode, Date)
   */
  public void doCancelCurrentAction(Treatable treatable, ActionNode an, Date date) {
    Number processId = treatable.getCurrentPlanActionId();
    TreatmentWorkpadServiceIntf twp = getTreatmentWorkpadService();
    Map tp = (Map) twp.getTreatmentProcessForEditing(new Long(processId.longValue()), null).getBeanHolder();
    if (tp == null) {
      log.error(treatable.getId() + " attempted to cancel non-existent process " + processId);
      return;
    }
    Long cancelled = getCancelledStatus();
    tp.put(ITreatmentProcess.PROCESS_STATUS_ID, cancelled);
    tp.put(ITreatmentProcess.ACTUAL_END_DATE, date);
    tp.put(ITreatmentProcess.USER_ID, userId);
    try {
      Long cancelledTypeStatus = getCancelledTypeStatus((Number) tp.get(ITreatmentProcess.PROCESS_TYPE_ID));
      tp.put(ITreatmentProcess.PROCESS_TYPE_STATUS_ID, cancelledTypeStatus);
      transactions.add(getTreatmentProcessService().updateTreatmentProcess(tp, date, userId));
    } catch (TreatmentProcessUpdateException e) {
      throw new RuntimeException(e);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private TreatmentWorkpadServiceIntf getTreatmentWorkpadService() {
    Object o = LookupManager.getInstance().getLookupItem(LookupManager.BUSINESS, "TreatmentWorkpadService");
    TreatmentWorkpadServiceIntf service = (TreatmentWorkpadServiceIntf) o;
    return service;
  }
  
  private Long getCancelledTypeStatus(Number treatprocTypeId) throws SQLException {
    if (!(treatprocTypeId instanceof Long)){
      treatprocTypeId = new Long(treatprocTypeId.longValue());
    }
    Long cancelledTypeStatus = (Long) cancelledTypeStatii.get(treatprocTypeId);
    if (cancelledTypeStatus == null){
      cancelledTypeStatus = (Long) provider.queryObject("getTreatmentCanceledTypeStatus", treatprocTypeId);
      cancelledTypeStatii.put(treatprocTypeId, cancelledTypeStatus);
    }
    return cancelledTypeStatus;
  }

  private Long getCancelledStatus() {
    return new Long(TreatmentProcessTypeStatusRefBusinessBean.CANCEL_TREATMENT_PROCESS_STATUS.longValue());    
  }

  public IRunnableTransaction dumpTransaction() {
    IRunnableTransaction[] array = (IRunnableTransaction[]) transactions.toArray(new IRunnableTransaction[0]);
    transactions.clear();
    return new RunnableTransactionSet(array);
  }
  
  private ITreatmentProcessService getTreatmentProcessService() {
    ITreatmentProcessService processService = (ITreatmentProcessService) LookupManager.getInstance()
        .getLookupItem(LookupManager.SYSTEM, "TreatmentProcessService");
    return processService;
  }
  

}
