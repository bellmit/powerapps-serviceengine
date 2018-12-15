package com.profitera.rpm.treatment;

import com.profitera.descriptor.business.reference.TreatmentStreamReferenceBusinessBean;
import com.profitera.descriptor.db.account.Account;
import com.profitera.descriptor.db.account.AccountTreatmentPlan;
import com.profitera.descriptor.db.account.Customer;
import com.profitera.descriptor.db.reference.TreatmentStageRef;
import com.profitera.descriptor.db.reference.TreatmentStreamRef;
import com.profitera.descriptor.db.reference.TreatprocSubtypeRef;
import com.profitera.descriptor.db.treatment.TreatmentProcess;
import com.profitera.descriptor.db.treatment.TreatprocTemplate;
import com.profitera.descriptor.db.user.User;
import com.profitera.descriptor.rpm.AccountDelinquency;
import com.profitera.descriptor.rpm.AccountTreatment;
import com.profitera.descriptor.rpm.UnsupportedAttributeException;
import com.profitera.persistence.PersistenceManager;
import com.profitera.rpm.AccountIterator;
import com.profitera.rpm.AgentFailureException;
import com.profitera.rpm.IncompleteAccountDataException;
import com.profitera.rpm.InvalidRuleException;
import com.profitera.rpm.NoSuchAgentException;
import com.profitera.rpm.RPM;
import com.profitera.rpm.RuleAgent;
import com.profitera.rpm.RuleAgentConstants;
import com.profitera.rpm.RuleAgentFactory;
import com.profitera.rpm.RuleEngineException;
import com.profitera.rpm.TreatmentAssigner;
import com.profitera.rpm.expression.InvalidExpressionException;
import com.profitera.services.system.dataaccess.RPMDataManager;
import com.profitera.util.*;
import java.sql.Timestamp;
import java.util.*;
import oracle.toplink.queryframework.CursoredStream;
import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.UnitOfWork;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author jamison
 */
public class RuleTreatmentAssigner implements TreatmentAssigner {
	private static Log log = LogFactory.getLog(RuleTreatmentAssigner.class);
	private RuleAgent[] delAgents;
	private RuleAgent[] treatAgents;
	private RuleAgent[] streamAgents;
	private int agentCount;
	private List stagesInOrder;
	private Map streamMap;
	private User systemUser;
	private int commitSize;
	private int batchSize = 2000;
	private Map subtypeMap;
	private Map templateMap;
	private boolean debugMode = false;

	private class TreatmentThread extends IteratorTransactionThread {
		private RuleAgent streamAgent;
		private RuleAgent treatAgent;
		private RuleAgent delAgent;
		private Date date;

		public TreatmentThread(
			Iterator i,
			RuleAgent delAgent,
			RuleAgent treatAgent,
			RuleAgent streamAgent,
			Date d) {
			super(i, commitSize);
			this.delAgent = delAgent;
			this.treatAgent = treatAgent;
			this.streamAgent = streamAgent;
			this.date = d;
		}

		protected void process(Object o, UnitOfWork uow) {
            if (o instanceof Account){
                Account a = (Account) uow.registerExistingObject(o);
                assignTreatment(a, null, delAgent, streamAgent, treatAgent, date, uow);
            }else{
                AccountTreatmentPlan p = (AccountTreatmentPlan) uow.registerExistingObject(o);
                Account a = (Account) uow.registerExistingObject(p.getAccount());
                assignTreatment(a, p, delAgent, streamAgent, treatAgent, date, uow);
            }
        }

		protected void handleException(RuntimeException e, List transactionObjects) {
			// The objects will all be accounts, print out their accoutids and numbers:
			log.error("Treatment transaction failed for the following accounts:");
			for (Iterator i = transactionObjects.iterator();i.hasNext();){
				Object o = i.next();
				Account a = null;
				if (o instanceof Account)
					a = (Account) o;
				else
					a = ((AccountTreatmentPlan)o).getAccount();
				log.error(a.getAccountId() + "/" + a.getAccountNumber());
			}
			log.error(e);
		}
	}
	
	private class PlanStreamIterator extends KeyedListIterator{
		public PlanStreamIterator(Iterator stream) {
			super(stream);
		}
		protected Comparable getNextKey(Object nextObject) {
			return ((AccountTreatmentPlan)nextObject).getTreatmentPlanId();
		}
	}

	public RuleTreatmentAssigner() throws NoSuchAgentException, InvalidExpressionException, RuleEngineException {
		ResourceBundle b = ResourceBundle.getBundle("server");
		agentCount = Integer.parseInt(b.getString("TREATMENT_THREADS"));
		commitSize = Integer.parseInt(b.getString("TREATMENT_COMMIT"));
		log.debug("Treatment Assignment threads/commit size: " + agentCount + "/"+commitSize);
		delAgents = new RuleAgent[agentCount];
		streamAgents = new RuleAgent[agentCount];
		treatAgents = new RuleAgent[agentCount];
		for (int i = 0; i < delAgents.length; i++) {
			delAgents[i] =
				(RuleAgent) RuleAgentFactory.getAgents().get(RuleAgentConstants.DELQM_AGENT);
			treatAgents[i] =
				(RuleAgent) RuleAgentFactory.getAgents().get(RuleAgentConstants.TRTCL_AGENT);
			streamAgents[i] = (RuleAgent) RuleAgentFactory.getAgents().get(RuleAgentConstants.TRTSM_AGENT);
		}
	}

	public void assignTreatments(
		Customer c,
		RuleAgent delAgent,
		RuleAgent streamAgent,
		RuleAgent treatAgent,
		Date d,
		UnitOfWork uow)
		throws AgentFailureException, IncompleteAccountDataException {
		List accounts = RPMDataManager.getAllCustomerPrimaryAccounts(c, uow);
		Iterator i = accounts.iterator();
		int maxStage = RPMDataManager.NONE_TREATMENT_STAGE;
		while (i.hasNext()) {
			Account a = (Account) uow.registerExistingObject(i.next());
			// This returns the new process, but we won't do anything with it anyway...
			assignTreatment(a, RPMDataManager.getCurrentTreatmentPlan(a,uow), delAgent, streamAgent, treatAgent, d, uow);
			if (maxStage == RPMDataManager.NONE_TREATMENT_STAGE && a.getTreatmentStageRef() != null)
				maxStage = a.getTreatmentStageRef().getTreatmentStageId().intValue();
			else if (
				a.getTreatmentStageRef() != null
					&& a.getTreatmentStageRef().getTreatmentStageId().doubleValue() > maxStage)
				maxStage = a.getTreatmentStageRef().getTreatmentStageId().intValue();
		}
		if (maxStage == RPMDataManager.NONE_TREATMENT_STAGE)
			log.debug(c.getCustomerId() + ": has no treatments, stage will be null");
		else
			log.debug(c.getCustomerId() + ": maximum stage ID is " + maxStage);
		RPMDataManager.updateCustomerTreatmentStage(c, maxStage, uow);
	}

	/**
	 * @param account - should already be reg'd with uow.
	 * @param delAgent
	 * @param streamAgent
	 * @param treatAgent
	 * @param d
	 * @param uow
	 * @return
	 */
	private TreatmentProcess assignTreatment(
		Account account,
		AccountTreatmentPlan currentPlan,
		RuleAgent delAgent,
		RuleAgent streamAgent,
		RuleAgent treatAgent,
		Date d,
		UnitOfWork uow) {
		boolean useDelqDet = delAgent != null;
		TreatmentProcess newProcess = null;
		AccountDelinquency delinquencyDescriptor = null;
		if (useDelqDet) {
			// 	Use the delinquency management agent to determine if the account is delq or potentially delq.
			delinquencyDescriptor = new AccountDelinquency(account, uow);
			try {
				delAgent.loadObject(delinquencyDescriptor);
				delAgent.execute();
			} catch (InvalidRuleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedAttributeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RuleEngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// If we use determination check the descriptor, otherwise check the account object itself
		if (useDelqDet
			&& delinquencyDescriptor.getCurrentCyclesDelinquent() < 1
			|| account.getCycDelId() != null
			&& account.getCycDelId().intValue() < 1) {
			// if there are any open treatment plans for this account they should be cancelled.
			// This will close the plan and cancel any in progress processes
			RPMDataManager.cancelCurrentTreatmentPlan(account, uow);
		}
		boolean delinquent =
			useDelqDet
				&& (delinquencyDescriptor.getCurrentCyclesDelinquent() > 0
					|| delinquencyDescriptor.getPotentiallyDelinquent());
		if (!useDelqDet)
			delinquent = account.getCycDelId() != null && account.getCycDelId().intValue() > 0;

		// If this account is freshly delinquent we need to set the stage start to today
		// and set the stream, maybe
		if (delinquent) {
			if (currentPlan == null)
				currentPlan = RPMDataManager.createNewAccountTreatmentPlan(account, uow);
			currentPlan = (AccountTreatmentPlan) uow.registerObject(currentPlan);
			int treatmentAction = getNextTreatmentProcessType(account, currentPlan, streamAgent, treatAgent, d, uow);
			if (treatmentAction != RPMDataManager.TREATMENT_PROCESS_TYPE_NONE) {
				TreatprocSubtypeRef subType = getSubtype(treatmentAction, uow);
				TreatprocTemplate template = getTemplate(subType.getTreatprocSubtypeId(), uow);
				newProcess =
					RPMDataManager.addTreatmentProcess(account, currentPlan, subType, template, getSystemUser(uow), 1, new Date(), uow);
			}
		}
		return newProcess;
	}

	/**
	 * @param subtypeId
	 * @param uow
	 * @return
	 */
	private TreatprocTemplate getTemplate(Double subtypeId, UnitOfWork uow) {
		if (templateMap == null)
			getSubtype(subtypeId.intValue(), uow);
		return (TreatprocTemplate) templateMap.get(subtypeId);
	}

	/**
	 * @param treatmentAction
	 * @return
	 */
	private TreatprocSubtypeRef getSubtype(int treatmentAction, Session s) {
		if (subtypeMap == null){
			List[] subTypesAndTemplates = RPMDataManager.getTreatmentSubtypeTemplates(s);
			Map idToSubtype = new HashMap();
			Map idToTemplate = new HashMap();
			for (int i = 0 ; i < subTypesAndTemplates[0].size(); i++){
				TreatprocSubtypeRef str = (TreatprocSubtypeRef) subTypesAndTemplates[0].get(i);
				idToSubtype.put(str.getTreatprocSubtypeId(), str);
				// template might be null, but that's OK.
				idToTemplate.put(str.getTreatprocSubtypeId(), subTypesAndTemplates[1].get(i));
			}
			subtypeMap = idToSubtype;
			templateMap = idToTemplate;
		}
		return (TreatprocSubtypeRef) subtypeMap.get(new Double(treatmentAction));
	}

	/**
	 * @return
	 */
	private User getSystemUser(Session s) {
		if (systemUser == null)
			systemUser = RPMDataManager.getSystemUser(s);
		return systemUser;
	}

	/**
	 * @param account
	 * @param currentPlan
	 * @param streamAgent
	 * @return
	 */
	private int getNextTreatmentProcessType(
		Account account,
		AccountTreatmentPlan currentPlan,
		RuleAgent streamAgent,
		RuleAgent treatAgent,
		Date d,
		UnitOfWork uow) {
		//		Get the treatment descriptor and use it to determine the current stream and action 
		boolean alreadyAdvanced = false;
		if (account.getTreatmentStageStartDate() == null)
			account.setTreatmentStageStartDate(new Timestamp(d.getTime()));
		AccountTreatment at = new AccountTreatment(account, currentPlan, d, uow);
		while (true) {
			if (debugMode) log.debug(at);
			int currentTreatment = at.getCurrentTreatmentActionType();
			int currentStream = at.getTreatmentStream();
			try {
				// Do stream assignment here.
				streamAgent.loadObject(at);
				streamAgent.execute();
				log.debug("Stream is : " + at.getTreatmentStream());
				if (at.getTreatmentStream() != currentStream) {
					// Advance if the new stream is more harsh than the current stream, making sure tha we don't advance if there was no current stream
					boolean advance =
						currentStream != TreatmentStreamReferenceBusinessBean.TREATMENT_STREAM_NONE
							&& currentStream < at.getTreatmentStream()
							&& !alreadyAdvanced;
					// if we need to advance, set the current stream to null and reset the stream start date.
					// Then rerun stream assignment to assign a new stream for the stage
					if (advance) {
						account.setTreatmentStageRef(
							(TreatmentStageRef) uow.registerExistingObject(
								getNextStage(at.getTreatmentStreamStage(), uow)));
						account.setTreatmentStageStartDate(new Timestamp(d.getTime()));
						log.debug("Advancing to stage: " + account.getTreatmentStageRef().getTreatmentStageDesc());
						account.setTreatmentStreamRef(null);
						currentPlan.setTreatmentStreamRef(null);
						// Now rerun but ensure that the last action was none for the new stage.
						at = new AccountTreatment(account, currentPlan, d, uow);
						at.overrideValue("getLastTreatmentActionType",RPMDataManager.TREATMENT_PROCESS_TYPE_NONE+"");
						continue; // rerun and assign a new stream and (possibly) treatment based on new stage
					}
					// If the account has no current stream, then assign a new stream, if there is a current stream 
					// then we refuse to change the stream.
					if (currentStream == TreatmentStreamReferenceBusinessBean.TREATMENT_STREAM_NONE && at.getTreatmentStream() != TreatmentStreamReferenceBusinessBean.TREATMENT_STREAM_NONE){
						Double stream = new Double(at.getTreatmentStream());
						account.setTreatmentStreamRef(
								(TreatmentStreamRef) uow.registerExistingObject(getStream(stream, uow)));
						currentPlan.setTreatmentStreamRef(account.getTreatmentStreamRef());
						log.debug("Stream changed to " + account.getTreatmentStreamRef().getTreatmentStreamDesc());
						// we need to create a new descriptor since the stream has changed and make sure its last action is none:
						at = new AccountTreatment(account, currentPlan, d, uow);
						at.overrideValue("getLastTreatmentActionType",RPMDataManager.TREATMENT_PROCESS_TYPE_NONE+"");
					}else{
						log.debug("Stream not changed (" + at.getTreatmentStream() + " requested).");
						at = new AccountTreatment(account, currentPlan, d, uow);
					}
				}
				// treatment only does treatment...
				treatAgent.loadObject(at);
				treatAgent.execute();
			} catch (InvalidRuleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedAttributeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RuleEngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (at.getAdvanceToNextStage()) {
				account.setTreatmentStageRef(
					(TreatmentStageRef) uow.registerExistingObject(getNextStage(at.getTreatmentStreamStage(), uow)));
				log.debug("Advancing to stage: " + account.getTreatmentStageRef().getTreatmentStageDesc());
				log.debug("Setting current stream to null so it can be reset for new stage");
				account.setTreatmentStreamRef(null);
				currentPlan.setTreatmentStreamRef(null);
				alreadyAdvanced = true;
				// Now rerun but ensure that the last action was none for the new stage.
				at = new AccountTreatment(account, currentPlan, d, uow);
				at.overrideValue("getLastTreatmentActionType",RPMDataManager.TREATMENT_PROCESS_TYPE_NONE+"");
			} else {
				if (at.getNextTreatmentActionType() != currentTreatment)
					return at.getNextTreatmentActionType();
				else
					return RPMDataManager.TREATMENT_PROCESS_TYPE_NONE;
			}
		}
	}

	/**
	 * @param stream
	 * @param uow
	 * @return
	 */
	private TreatmentStreamRef getStream(Double stream, UnitOfWork uow) {
		if (streamMap == null) {
			// Assign to streamMap at the end so no need for sychronization
			Map m = new HashMap();
			for (Iterator i = RPMDataManager.getTreatmentStreams(uow).iterator(); i.hasNext();) {
				TreatmentStreamRef r = (TreatmentStreamRef) i.next();
				m.put(r.getTreatmentStreamId(), r);
			}
			streamMap = m;
		}
		return (TreatmentStreamRef) streamMap.get(stream);
	}

	/**
	 * The searching doesn't need to be fast, we're talking
	 * about 3 or 4 items...
	 * @param prev
	 * @return
	 */
	private TreatmentStageRef getNextStage(int prev, Session session) {
		if (stagesInOrder == null) {
			stagesInOrder = RPMDataManager.getTreatmentStagesInOrder(session);
		}
		for (Iterator i = stagesInOrder.iterator(); i.hasNext();) {
			TreatmentStageRef s = (TreatmentStageRef) i.next();
			if (s.getTreatmentStageId().intValue() > prev)
				return s;
		} // If there is no greater stage return the last one again, what else?!
		return (TreatmentStageRef) stagesInOrder.get(stagesInOrder.size() - 1);
	}

	private void assignTreatmentsWithDelqDetermination(String startId, String endId, final Date d) {
		debugMode = false;
		Session session = PersistenceManager.getClientSession();
		CursoredStream stream;
		stream = RPMDataManager.getCustomerStream(startId, endId, 1000, session);
		while (stream.hasMoreElements()) {
			final UnitOfWork uow = session.acquireUnitOfWork();
			Thread[] threads = new Thread[agentCount];
			for (int i = 0; i < agentCount && stream.hasMoreElements(); i++) {
				final Customer c = (Customer) stream.read();
				final int index = i;
				stream.releasePrevious();
				threads[i] = new Thread(new Runnable() {
					public void run() {
						try {
							assignTreatments(c, delAgents[index], streamAgents[index], treatAgents[index], d, uow);
						} catch (AgentFailureException e) {
							// TODO: jamison Fix brain-dead catch
							e.printStackTrace();
						} catch (IncompleteAccountDataException e) {
							// 	TODO: jamison Fix brain-dead catch
							e.printStackTrace();
						}
					}
				});
				threads[i].start();
			}
			for (int i = 0; i < agentCount; i++)
				try {
					threads[i].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			uow.commit();
		}
	}

	private void assignTreatmentsWithoutDelqDetermination(String startId, String endId, final Date d) {
		debugMode = false;
		Session session = PersistenceManager.getClientSession();
		Iterator stream = RPMDataManager.getDelinquentAccountPlansByCustomer(TopLinkQuery.STREAM, startId, endId, batchSize, session);
		TreatmentThread[] streamThreads = new TreatmentThread[agentCount];
		for (int i = 0; i < streamThreads.length; i++) {
			streamThreads[i] = new TreatmentThread(new PlanStreamIterator(stream), null, treatAgents[i], streamAgents[i], d);
			streamThreads[i].start();
		}
		for (int i = 0; i < streamThreads.length; i++) {
			try {
				streamThreads[i].join();
			} catch (InterruptedException e) {
				throw new RuntimeException("One or more treatment processing threads have died.");
			}
		}
		
		stream = RPMDataManager.getDelinquentAccountStreamWithoutPlansByCustomer(TopLinkQuery.STREAM, startId, endId, session);
		streamThreads = new TreatmentThread[agentCount];
		for (int i = 0; i < streamThreads.length; i++) {
			streamThreads[i] = new TreatmentThread(new AccountIterator(stream), null, treatAgents[i], streamAgents[i], d);
			streamThreads[i].start();
		}
		for (int i = 0; i < streamThreads.length; i++) {
			try {
				streamThreads[i].join();
			} catch (InterruptedException e) {
				throw new RuntimeException("One or more treatment processing threads have died.");
			}
		}
		//Mass-close open plans for non-delqs.
		closeCompletePlans(startId, endId, session);
		//Mass-upadate the Customer max stage.
		updateCustomerStages(startId, endId, session); 
	}
	
	public void assignTreatments(String startId, String endId, Date d, boolean useDelqDetermination, String rootID){
		if (useDelqDetermination)
			assignTreatmentsWithDelqDetermination(startId, endId, d);
		else
			assignTreatmentsWithoutDelqDetermination(startId, endId, d);
	}

    /**
	 * TODO: This is a copy from AgingService.java, merge this somehow
     * @param session
     */
    private void closeCompletePlans(String startId, String endId, Session session){
    	QueryBundle qb = new QueryBundle(RPM.TREATMENT_QUERY_FILE);
		// Note, the order of these queries may be significant!
    	executeQuery(qb,"resetAccountsTurnedNonDelq", new Object[]{startId, endId}, session);
		executeQuery(qb,"closeCompletePlans", new Object[]{startId, endId}, session);
		executeQuery(qb,"cancelPendingActionsForClosedPlans", new Object[]{startId, endId}, session);
	}

	private void updateCustomerStages(String startId, String endId, Session session){
		QueryBundle qb = new QueryBundle(RPM.TREATMENT_QUERY_FILE);
    	executeQuery(qb,"updateCustomerStage", new Object[]{startId, endId}, session);
	}
    
    private void executeQuery(QueryBundle qb, String propName, Object[] args, Session session){
    	String query = qb.getQuery(propName, args);    	
    	log.info("Starting " + propName);
    	log.info("Query text: " + query);
    	TopLinkQuery.executeUpdateSQL(propName, query, session);
    }
}
