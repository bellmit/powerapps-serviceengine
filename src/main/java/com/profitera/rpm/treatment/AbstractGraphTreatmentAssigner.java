package com.profitera.rpm.treatment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import oracle.toplink.sessions.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.descriptor.business.reference.TreatmentProcessTypeStatusRefBusinessBean;
import com.profitera.descriptor.business.treatment.admin.ActionNode;
import com.profitera.descriptor.business.treatment.admin.Transition;
import com.profitera.descriptor.business.treatment.admin.TreatmentGraph;
import com.profitera.descriptor.business.treatment.admin.TreatmentGraphs;
import com.profitera.descriptor.db.reference.TreatmentStageRef;
import com.profitera.descriptor.rpm.TreatmentDescriptor;
import com.profitera.persistence.SessionManager;
import com.profitera.rpm.AgentFailureException;
import com.profitera.rpm.IncompleteAccountDataException;
import com.profitera.rpm.TreatmentAssigner;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.treatment.TreatmentAdminService;
import com.profitera.services.system.dataaccess.DefaultTreatmentProcessManager;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.RPMDataManager;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.PrimitiveValue;
import com.profitera.util.Stringifier;
import com.profitera.util.TopLinkQuery;

/**
 * @author jamison
 */
public abstract class AbstractGraphTreatmentAssigner implements TreatmentAssigner {
  private static final String ACCOUNT_ID = "ACCOUNT_ID";
  private static final String ACCOUNT_ID_LOWER = ACCOUNT_ID.toLowerCase();
  private static final String TREATMENT_START_DATE = "TREATMENT_START_DATE";
  private static final String TREATMENT_START_DATE_LOWER = TREATMENT_START_DATE.toLowerCase();
  private static final String TREATMENT_STAGE_START_DATE = "TREATMENT_STAGE_START_DATE";
  private static final String TREATMENT_STAGE_START_DATE_LOWER = TREATMENT_STAGE_START_DATE.toLowerCase();
  private static final String ACTUAL_END_DATE = "ACTUAL_END_DATE";
  private static final String ACTUAL_END_DATE_LOWER = ACTUAL_END_DATE.toLowerCase();
  private static final String EXPECTED_START_DATE = "EXPECTED_START_DATE";
  private static final String EXPECTED_START_DATE_LOWER = EXPECTED_START_DATE.toLowerCase();
  private static final String PLAN_ATTEMPT_NUMBER = "PLAN_ATTEMPT_NUMBER";
  private static final String PLAN_ATTEMPT_NUMBER_LOWER = PLAN_ATTEMPT_NUMBER.toLowerCase();
  private static final String TREATMENT_STREAM_ID = "TREATMENT_STREAM_ID";
  private static final String TREATMENT_STREAM_ID_LOWER = TREATMENT_STREAM_ID.toLowerCase();
  private static final String PLAN_PROCESS_STATUS_ID = "PLAN_PROCESS_STATUS_ID";
  private static final String PLAN_PROCESS_STATUS_ID_LOWER = PLAN_PROCESS_STATUS_ID.toLowerCase();
  private static final String NODE_LOCATION = "NODE_LOCATION";
  private static final String NODE_LOCATION_LOWER = NODE_LOCATION.toLowerCase();
  private static final String ACTION_PROCESS_TYPE_ID = "ACTION_PROCESS_TYPE_ID";
  private static final String ACTION_PROCESS_TYPE_ID_LOWER = ACTION_PROCESS_TYPE_ID.toLowerCase();
  private static final String PLAN_PROCESS_ID = "PLAN_TREATMENT_PROCESS_ID";
  private static final String PLAN_PROCESS_ID_LOWER = PLAN_PROCESS_ID.toLowerCase();
  private static Log log = LogFactory.getLog(AbstractGraphTreatmentAssigner.class);
  private static final String LOGICAL_JOIN ="LOGICAL_JOIN";
  private static final String SERVICE_ENGINE_PROP_NAME = "treatmentassignmentservice";
  private static final String GET_DELINQUENT_ACCOUNTS_WITHOUT_PLAN_QUERY_NAME = "delinquentaccountswithoutplansquery";
  private static final String GET_DELINQUENT_ACCOUNTS_WITH_PLAN_QUERY_NAME = "delinquentaccountplansquery";
	private static Integer IN_PROGRESS_TRANS_KEY = new Integer(TreatmentProcessTypeStatusRefBusinessBean.IN_PROGRESS_TREATMENT_PROCESS_STATUS.intValue());
	private int commitSize = 100;
	private String queryStrategy = TopLinkQuery.STREAM;
	private HashMap treatmentGraphs;
	private TransactionTreatmentProducer producer;
  private DefaultTreatmentProcessManager manager;
  private Map stageEntryConditions;
  private Map stageExitConditions;
	private static final long MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000;
	
	
	public AbstractGraphTreatmentAssigner() {
		ServiceEngine.getConfig(true);
		commitSize = ServiceEngine.getIntProp("TREATMENT_COMMIT", commitSize);
		queryStrategy = ServiceEngine.getProp("TREATMENT_QUERY_STRATEGY", queryStrategy);
		log.info("Treatment Assignment commit size: " + commitSize);
		log.info("Treatment Assignment query strategy:  " + queryStrategy);
		IReadWriteDataProvider p = getReadWriteProvider();
		producer = new TransactionTreatmentProducer(p);
	}

	
	
	
	
  protected abstract Integer getDesiredStreamId(Map plan, Date d, String rootID);
  protected abstract TreatmentPlanner getTreatmentPlanner();

  protected Log getLog(){
    return log;
  }
	private void assignTreatmentsWithoutDelqDetermination(String startId, String endId, final Date d, String rootID) {
		buildStageConditions(rootID);
		// Mass-close open plans for non-delqs. so we won't rip through them for no reason here.
		closeCompletePlans(startId, endId, d);
		// Create plans for all the idiots who just went delinquent
    Map args = new HashMap();
    args.put("START_CUSTOMER", startId);
    args.put("END_CUSTOMER", endId);
    args.put("EFFECTIVE_DATE", d);
    Iterator accounts = null;
    try {
    	String queryName;
    	if(rootID!=null){
    		queryName = ServiceEngine.getProp(SERVICE_ENGINE_PROP_NAME + "." + rootID + "." + GET_DELINQUENT_ACCOUNTS_WITHOUT_PLAN_QUERY_NAME);
    		if(queryName==null){
    			String msg = "Unable to get query name using '" + SERVICE_ENGINE_PROP_NAME + "." + rootID + "." + GET_DELINQUENT_ACCOUNTS_WITHOUT_PLAN_QUERY_NAME + "' in server.properties";
          log.fatal(msg);
          throw new RuntimeException(msg);
    		}
    	}
    	else
    		queryName = "getDelinquentAccountsWithoutPlansByCustomer";
      accounts = getReadWriteProvider().query(IReadWriteDataProvider.STREAM_RESULTS, queryName, args);
    } catch(SQLException e){
      String msg = "Unable to query delinquent accounts for creating treatment plans for customers " + startId + " to " + endId;
      log.fatal(msg, e);
      throw new RuntimeException(msg);
    }
    try {
      IRunnableTransaction[] trans = new IRunnableTransaction[commitSize];
      int i = -1;
      while(accounts.hasNext()){
        Map account = (Map) accounts.next();
        final Long accountId = (Long) account.get("ACCOUNT_ID");
        i++;
        trans[i] = new IRunnableTransaction(){
          public void execute(ITransaction t) throws SQLException, AbortTransactionException {
            getManager().getTreatmentPlanForProcess(new HashMap(), accountId, d, null, null, null, t, getReadWriteProvider());
          }};
        if (i == trans.length - 1){
          getReadWriteProvider().execute(new RunnableTransactionSet(trans));
          Arrays.fill(trans, null);
          i = -1;
        }
      }
      getReadWriteProvider().execute(new RunnableTransactionSet(trans));
    } catch (AbortTransactionException e) {
      // TODO: Better info
      log.error("Failed to create treatment plans", e);
    } catch (SQLException e) {
      // TODO: Better info
      log.error("Failed to create treatment plans", e);
    }
		// Now the plan stream will have everybody in it.
    Iterator plans = null;
    try {
    	String queryName;
    	if(rootID!=null){
    		queryName = ServiceEngine.getProp(SERVICE_ENGINE_PROP_NAME + "." + rootID + "." + GET_DELINQUENT_ACCOUNTS_WITH_PLAN_QUERY_NAME);
    		if(queryName==null){
    			String msg = "Unable to get query name using '" + SERVICE_ENGINE_PROP_NAME + "." + rootID + "." + GET_DELINQUENT_ACCOUNTS_WITH_PLAN_QUERY_NAME + "' in server.properties";
           log.fatal(msg);
          throw new RuntimeException(msg);
    		}
    	}
    	else
    		queryName = "getDelinquentAccountPlansByCustomer";
    	
      plans = getReadWriteProvider().query(queryStrategy.equals(TopLinkQuery.LIST) ? IReadWriteDataProvider.LIST_RESULTS : IReadWriteDataProvider.STREAM_RESULTS, queryName, args);
    } catch (SQLException e1) {
      String msg = "Unable to query treatment plans for customers " + startId + " to " + endId;
      log.fatal(msg, e1);
      throw new RuntimeException(msg);
    }
    IRunnableTransaction[] transactions = new IRunnableTransaction[commitSize];
    List planList = new ArrayList();
    int count = -1;
    while (plans.hasNext()){
      count++;
      try {
        Map p = (Map) plans.next();
        planList.add(p);
        final Map row = (Map) getReadWriteProvider().queryObject("getAccountTreatableInformation", (Long) p.get("ACCOUNT_ID"));
        if(row==null) continue;
        transactions[count] = continuePlan(p, buildTreatmentDescriptor(row, d),row, d,rootID);
        if (count == transactions.length - 1){
          getReadWriteProvider().execute(new RunnableTransactionSet(transactions));
          planList.clear();
          Arrays.fill(transactions, null);  
          count = -1;
        }
      } catch (Exception e){
        log.error("Treatment transaction failed for the following accounts:");
        for (Iterator i = planList.iterator();i.hasNext();){
          Map a = (Map)i.next();
          log.error(a);
        }
        log.error("Exception information:", e);
        planList.clear();
        Arrays.fill(transactions, null);
        count = -1;
      }
    }
    try {
      getReadWriteProvider().execute(new RunnableTransactionSet(transactions));
      planList.clear();
      Arrays.fill(transactions, null);
      count = -1;
    } catch (Exception e){
      log.error("Treatment transaction failed for the following accounts:");
      for (Iterator i = planList.iterator();i.hasNext();){
        Map a = (Map)i.next();
        log.error(a);
      }
      log.error("Exception information:", e);
      planList.clear();
      Arrays.fill(transactions, null);
      count = -1;
    }
	}

	protected IRunnableTransaction continuePlan(Map plan, TreatmentDescriptor descriptor, Map treatableAccountInfo,final Date d, final String rootID) {
    final Long accountId = (Long) plan.get("ACCOUNT_ID");
    producer.setAccountId(accountId);
    producer.setPlan(plan);
    producer.setUserId(RPMDataManager.SYSTEM_USER_ID);
    
    // Force checking exit and entry for stage conditions.
    Long currentStageId = (Long) treatableAccountInfo.get("TREATMENT_STAGE_ID");
    Long newStageId = checkStageConditionsAgainstPlan(treatableAccountInfo,getStageExitConditions(),currentStageId);	
    
    if(currentStageId!=null&&currentStageId.equals(newStageId)){
		log.info("Treatable '" + descriptor.getId() + "' met all exit conditions for stage '" + newStageId + "'. Will exit stage: " + newStageId);
		plan.put("TREATMENT_STAGE_ID", null);
	    plan.put("TREATMENT_STAGE_START_DATE", null);
		plan.put("TREATMENT_STREAM_ID", null);
		plan.put("DELINQUENT_STAGE_ID", null);
	    plan.put("NODE_LOCATION", null);	
	    producer.doStageTraversal(plan);
	}
	
	newStageId= checkStageConditionsAgainstPlan(treatableAccountInfo,getStageEntryConditions());
	if(currentStageId==null||(newStageId!=null&&!newStageId.equals(currentStageId))){
		log.info("Treatable '" + descriptor.getId() + "' met all entry conditions for stage '" + newStageId + "'. Will enter stage: " + newStageId);
		plan.put("TREATMENT_STAGE_ID", newStageId);
	    plan.put("TREATMENT_STAGE_START_DATE", d);
	    plan.put("TREATMENT_STREAM_ID", null);
	    plan.put("DELINQUENT_STAGE_ID", newStageId);
	    plan.put("NODE_LOCATION", null);
	    producer.doStageTraversal(plan);
	    descriptor.setCurrentLocation(null);
    	descriptor.setCurrentStreamId(null); 
	    descriptor.setDaysAtCurrentPlanActionStatus(0);
	} 
	
    
   // If Account has matching stage
    if(newStageId!=null&&newStageId.longValue()!=0){
	    TreatmentGraph graph = getGraph(newStageId.longValue());
	    // We do not need to assess the desired stream if one is already selected.
	    //i.e. we are already mid-stream and thus have a location.
	    if (descriptor.getCurrentLocation() == null){
	      updateDesiredStream(plan, descriptor, d,rootID);
	    }
			getTreatmentPlanner().treat(descriptor, graph, producer, d);
    }
    return producer.dumpTransaction();
	}

	private TreatmentGraph getGraph(long stageId) {
		if (treatmentGraphs == null){
			List stages = producer.getTreatmentStageInOrder(getReadWriteProvider());
			List graphs = (List) new TreatmentAdminService().getStreamGraphs().getBeanHolder();
			treatmentGraphs = new HashMap();
			for (Iterator iter = stages.iterator(); iter.hasNext();) {
				TreatmentStageRef stage = (TreatmentStageRef) iter.next();
				for (Iterator gIter = graphs.iterator();gIter.hasNext();){
					TreatmentGraph g = (TreatmentGraph) gIter.next();
					if (((Number)g.getStageId()).intValue() == stage.getTreatmentStageId().intValue()){
						addInprogressWaiting(g);
						treatmentGraphs.put(new Long(stage.getTreatmentStageId().longValue()), g);
					}
				}
			}
		}
		return (TreatmentGraph) treatmentGraphs.get(new Long(stageId));
	}

	/**
	 * Adds the transitions that make sure that when actions are in progress
	 * the treatment stream does not proceed.
	 * @param g
	 * @return
	 */
	private void addInprogressWaiting(TreatmentGraph g) {
		Transition inprogressTrans = new Transition();
		inprogressTrans.setDestination(null);
		inprogressTrans.setEntryStatus(IN_PROGRESS_TRANS_KEY);
		for (Iterator i = TreatmentGraphs.getAllNodes(g).values().iterator();i.hasNext();){
			ActionNode a = (ActionNode) i.next();
			if (a.getTransition(IN_PROGRESS_TRANS_KEY) == null)
				a.addTransition(inprogressTrans);
		}
	}

	private void updateDesiredStream(Map plan, TreatmentDescriptor descriptor, Date d, String rootID) {
		Integer stream = getDesiredStreamId(plan, d, rootID);
    descriptor.setDesiredStreamId(stream);
	}

  
	

  private Object getColumnValue(String col, String colUpper, String colLower, Map row){
	  if (row.containsKey(col))
	    return row.get(col);
	  if (row.containsKey(colUpper))
	    return row.get(colUpper);
	  if (row.containsKey(colLower))
	    return row.get(colLower);
	  return null;
	}
	
	private Object getColumnValue(String colUpper, String colLower, Map row){
	  return getColumnValue(colUpper, colUpper, colLower, row);
	}

	private TreatmentDescriptor buildTreatmentDescriptor(Map row, Date evalDate) throws SQLException {
		TreatmentDescriptor td = new TreatmentDescriptor();
		td.setCurrentPlanActionId((Number) getColumnValue(PLAN_PROCESS_ID,PLAN_PROCESS_ID_LOWER, row));
		td.setCurrentActionType(new Integer(PrimitiveValue.intValue((Number) getColumnValue(ACTION_PROCESS_TYPE_ID,ACTION_PROCESS_TYPE_ID_LOWER, row), RPMDataManager.NONE_TREATMENT_PROCESS)));
		td.setCurrentLocation(PrimitiveValue.stringValue(getColumnValue(NODE_LOCATION, NODE_LOCATION_LOWER, row), null));
		td.setCurrentPlanActionStatus(new Integer(PrimitiveValue.intValue((Number)getColumnValue(PLAN_PROCESS_STATUS_ID, PLAN_PROCESS_STATUS_ID_LOWER, row), 0)));
		td.setCurrentStreamId(PrimitiveValue.stringValue(getColumnValue(TREATMENT_STREAM_ID, TREATMENT_STREAM_ID_LOWER, row), null));
		td.setCurrentPlanActionAttempts(PrimitiveValue.intValue((Number)getColumnValue(PLAN_ATTEMPT_NUMBER, PLAN_ATTEMPT_NUMBER_LOWER, row), 1));
		long evalTime = evalDate.getTime();
		int daysSince = -1;
		Date planActionEndDate = (Date) getColumnValue(ACTUAL_END_DATE, ACTUAL_END_DATE_LOWER, row);
		Date planActionExpectedStartDate = (Date) getColumnValue(EXPECTED_START_DATE, EXPECTED_START_DATE_LOWER, row);
        if (planActionEndDate != null){
		  daysSince = (int) ((evalTime - planActionEndDate.getTime())/MILLISECONDS_IN_DAY);
		} else if (planActionExpectedStartDate != null){
		  daysSince = (int) ((evalTime - planActionExpectedStartDate.getTime())/MILLISECONDS_IN_DAY);
		}
		int daysSinceStartOfStage = -1;
		Date stageStartDate = (Date) getColumnValue(TREATMENT_STAGE_START_DATE, TREATMENT_STAGE_START_DATE_LOWER, row);
		if (stageStartDate != null){
		  daysSinceStartOfStage = (int) ((evalTime - stageStartDate.getTime())/MILLISECONDS_IN_DAY);
		}
		if (daysSince == -1) {
		  Date planStartDate = (Date) getColumnValue(TREATMENT_START_DATE, TREATMENT_START_DATE_LOWER, row);
		  daysSince = (int) ((evalTime - planStartDate.getTime())/MILLISECONDS_IN_DAY);
		}
		if (daysSinceStartOfStage > -1 && (daysSinceStartOfStage < daysSince || daysSince == -1)) daysSince = daysSinceStartOfStage;
		td.setDaysAtCurrentPlanActionStatus(daysSince);
		td.setDesiredStreamId(null);
		td.setId(getColumnValue(ACCOUNT_ID, ACCOUNT_ID_LOWER, row));
    td.setStageId((Long) row.get("TREATMENT_STAGE_ID"));
		if (log.isTraceEnabled()){
		  log.trace(Stringifier.stringify(td)); 
		}
		return td;
	}

	public void assignTreatments(String startId, String endId, Date d, boolean useDelqDetermination, String rootID){
		if (useDelqDetermination)
			throw new RuntimeException("The use of Delq determiniation is currently not supported");
		else
			assignTreatmentsWithoutDelqDetermination(startId, endId, d, rootID);
	}

    private void closeCompletePlans(String startId, String endId, final Date evalDate){
      final IReadWriteDataProvider p = getReadWriteProvider();
      Map m = new HashMap();
      m.put("START_CUSTOMER", startId);
      m.put("END_CUSTOMER", endId);
      m.put("EFFECTIVE_DATE", evalDate);
      List trans = new ArrayList();
      try {
        int count = 0;
        Iterator i = p.query(IReadWriteDataProvider.STREAM_RESULTS, "getAccountTreatmentPlansForClosing", m);
        while(i.hasNext()){
          count++;
          Map plan = (Map) i.next();
          final Long accountId = (Long) plan.get("ACCOUNT_ID");
          final Long planId = (Long) plan.get("TREATMENT_PLAN_ID");
          IRunnableTransaction t = new IRunnableTransaction(){
            public void execute(ITransaction t) throws SQLException, AbortTransactionException {
              p.update("resetAccountTurnedNonDelinquent", accountId, t);
              Map args = new HashMap();
              args.put("TREATMENT_PLAN_ID", planId);
              args.put("TREATMENT_END_DATE", evalDate);
              p.update("closeAccountTreatmentPlan", args, t);
              p.update("updateAccountCustomerStage", accountId, t);
              p.update("cancelPendingAccountTreatmentPlanActions", args, t);
            }};
          trans.add(t);
          if(count % commitSize == 0){
            IRunnableTransaction[] ta = (IRunnableTransaction[]) trans.toArray(new IRunnableTransaction[0]);
            p.execute(new RunnableTransactionSet(ta));
            trans.clear();
          }
        }
        if (trans.size() > 0){
          IRunnableTransaction[] ta = (IRunnableTransaction[]) trans.toArray(new IRunnableTransaction[0]);
          p.execute(new RunnableTransactionSet(ta));
        }
      } catch (SQLException e) {
        log.fatal("Failed to close account treatment plans", e);
        throw new RuntimeException(e);
      } catch (AbortTransactionException e) {
        log.fatal("Failed to close account treatment plans", e);
        throw new RuntimeException(e);
      } 		
    }
    
    
    
    protected static IReadWriteDataProvider getReadWriteProvider() {
      final IReadWriteDataProvider provider = (IReadWriteDataProvider) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "ReadWriteProvider");
      return provider;
    }
    
    /**
	 * This method is intended for single customer command line execution only
	 */
	public void assignCustomerTreatments(String customerId, Date d, boolean useDelinquencyDetermination)
		throws AgentFailureException, IncompleteAccountDataException {
		assignTreatments(customerId, customerId, d, useDelinquencyDetermination, null);
		log.debug("Treatments assigned for customer: " + customerId);
	}
  
  protected DefaultTreatmentProcessManager getManager() {
    if (manager == null){
      manager = new DefaultTreatmentProcessManager();      
    }
    return manager;
  }
  
  private Map getStageExitConditions() {
		return stageExitConditions;
  }
  
  private Map getStageEntryConditions() {
		return stageEntryConditions;
  }
    
  private void buildStageConditions(String rootID) {
		stageEntryConditions = new LinkedHashMap();
		stageExitConditions = new LinkedHashMap();
		Iterator iter;
		try {
			Map m= new HashMap(1);
			m.put("ROOT_ID",rootID);
			iter = getReadWriteProvider().query(IReadOnlyDataProvider.LIST_RESULTS,"getTreatmentStageCondition",m);
		} catch (SQLException e) {
			log.error(e.getMessage(),e);
			throw new RuntimeException(e.getMessage(),e);
		}
		
		while(iter.hasNext()){
			Map m = (Map) iter.next();
			Object stageId = m.get("TREATMENT_STAGE_ID");
			if(((Boolean) m.get("IS_ENTRY")).booleanValue()){
				if(stageEntryConditions.containsKey(stageId)){
					List conditions = (List) stageEntryConditions.get(stageId);
					conditions.add(m);
					stageEntryConditions.put(stageId,conditions);
				} else {
					List conditions = new ArrayList();
					conditions.add(m);
					stageEntryConditions.put(stageId,conditions);
				}
			} else {
				if(stageExitConditions.containsKey(stageId)){
					List conditions = (List) stageExitConditions.get(stageId);
					conditions.add(m);
					stageExitConditions.put(stageId,conditions);
				} else {
					List conditions = new ArrayList();
					conditions.add(m);
					stageExitConditions.put(stageId,conditions);
				}
			}
		}
	}
  
  private Long checkStageConditionsAgainstPlan(Map accountInfo,Map condition,Long preferredStageId){
	  	List conditions = (List) condition.get(preferredStageId);
	  	if(isStageDisabled(conditions)){
	  		return preferredStageId;
	  	}
		if(conditions!=null&&isConditionMet(accountInfo,conditions))
			return preferredStageId;
		
		return checkStageConditionsAgainstPlan(accountInfo,condition);	  
  }
  private Long checkStageConditionsAgainstPlan(Map accountInfo,Map condition){
		Iterator stageIdIterator = condition.keySet().iterator();
		
		while(stageIdIterator.hasNext()){
			Long stageId = (Long) stageIdIterator.next();
			List conditions = (List) condition.get(stageId);
			if(isConditionMet(accountInfo,conditions))
				return stageId;
		}
		return null;
  }

  private boolean isStageDisabled(List conditions){
	  if(conditions==null||conditions.size()==0)
		  return false;
	  Map condition = (Map) conditions.get(0);
	  if(Boolean.TRUE.equals(condition.get("DISABLE")))
			 return true;
	  
	  return false;
		 
  }
  private boolean isConditionMet(Map plan, List conditions){
	  boolean isConditionMet = true;
	  
	  for(int i=0;i<conditions.size();i++){
		 Map condition = (Map) conditions.get(i);
		 if(Boolean.TRUE.equals(condition.get("DISABLE")))
			 return false;
		 condition.put("ROW_VALUE",Boolean.valueOf(compare(plan,condition)));
	  }
	  evaluateLogicalAndConditions(conditions);
	  
	  for(int i=0;i<conditions.size();i++){
		  if(!((Boolean) ((Map) conditions.get(i)).get("ROW_VALUE")).booleanValue()){
			  isConditionMet=false;
			  break;
		  }
	  }
	  return isConditionMet;
		
	}
	
  private void evaluateLogicalAndConditions(List conditions){
	  
	  int startingCount=conditions.size();
	  boolean conditionMet = true;
	  boolean previousConditionMet = true;
	  for(int i=0;i<conditions.size();i++){
		  Map value = (Map) conditions.get(i);
		  
		  if(conditionMet){
			  conditionMet = ((Boolean) value.get("ROW_VALUE")).booleanValue();
		  }
		  if("AND".equalsIgnoreCase((String) value.get(LOGICAL_JOIN))){
			  if(startingCount==conditions.size())
				  startingCount = i;
			  previousConditionMet = conditionMet;
			  continue;
		  } else if("OR".equalsIgnoreCase((String) value.get(LOGICAL_JOIN))){
			  for(int x=startingCount;x<i;x++){
				  Map val = (Map) conditions.get(x);
				  val.put("ROW_VALUE",Boolean.valueOf(previousConditionMet));
			  }
			  Map nextValue =(Map) conditions.get(i+1);
			  conditionMet = ((Boolean) value.get("ROW_VALUE")).booleanValue() || ((Boolean) nextValue.get("ROW_VALUE")).booleanValue();
			  value.put("ROW_VALUE",Boolean.valueOf(conditionMet));
			  nextValue.put("ROW_VALUE",Boolean.valueOf(conditionMet));
			  startingCount = conditions.size();
		  }  else {
			  for(int x=startingCount;x<=i;x++){
				  Map val = (Map) conditions.get(x);
				  val.put("ROW_VALUE",Boolean.valueOf(conditionMet));
			  }
			  startingCount=conditions.size();
			  conditionMet=true;
		  }
		  
		  previousConditionMet = conditionMet;
	}
  }
  
  private boolean compare(Map plan, Map condition){
	  
	  Object columnName = condition.get("COLUMN_NAME");
	  
	  if(!plan.containsKey(columnName))
			return false;
	  
	  String operator = (String) condition.get("OPERATOR");
	  Long rightOperand = (Long) condition.get("COLUMN_VALUE");
	  Long leftOperand = (Long) plan.get(columnName);
	  
	  if(leftOperand==null||rightOperand==null)
			return false;
		
		if("=".equals(operator)){
			return leftOperand.equals(rightOperand);
		} else if("<>".equals(operator)){
			return !leftOperand.equals(rightOperand);
		} else if("<".equals(operator)){
			return leftOperand.longValue()<rightOperand.longValue();
		} else if("<=".equals(operator)){
			return (leftOperand.longValue()<=rightOperand.longValue());
		} else if(">".equals(operator)){
			return (leftOperand.longValue()>rightOperand.longValue());
		} else if(">=".equals(operator)){
			return (leftOperand.longValue()>=rightOperand.longValue());
		} else 
			return false;
	}

}
