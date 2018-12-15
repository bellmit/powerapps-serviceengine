package com.profitera.rpm;

import com.profitera.descriptor.db.account.Account;
import com.profitera.descriptor.db.reference.ProfileSegmentRef;
import com.profitera.descriptor.db.reference.RiskLevelRef;
import com.profitera.descriptor.rpm.*;
import com.profitera.persistence.PersistenceManager;
import com.profitera.persistence.SessionManager;
import com.profitera.rpm.expression.InvalidExpressionException;
import com.profitera.server.ServiceEngine;
import com.profitera.services.system.dataaccess.RPMDataManager;
import com.profitera.util.*;
import java.util.*;
import oracle.toplink.publicinterface.DatabaseRow;
import oracle.toplink.queryframework.CursoredStream;
import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.UnitOfWork;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author jamison
 */
public class AccountProfiler implements IAccountProfiler {
	private static Log log = LogFactory.getLog(AccountProfiler.class);
	private int commitSize;
	private int batchSize;
	private int threadCount;
	private Map profileSegmentMap;
	
	class ProfilingThread extends IteratorTransactionThread{
	    RuleAgent preprocessorAgent;
	    RuleAgent profiler;
		RPMPropertyCache preprocessorPropCache;
		public ProfilingThread(Iterator i, int commit, RuleAgent preprocessorAgent, RuleAgent profiler, Session session) {
			super(i, commit);
			this.preprocessorAgent = preprocessorAgent;
			this.profiler = profiler;
			List propertiesUsed = preprocessorAgent.getPropertiesUsed();
			propertiesUsed.addAll(profiler.getPropertiesUsed());
			preprocessorPropCache = getRPMPropertyCache(propertiesUsed, new BaseDescriptor[] { new AccountPreprocessor(session), new AccountProfile(session) });
		}

		protected void process(Object o, UnitOfWork uow) {
			AccountPreprocessor currentPreprocessor= new AccountPreprocessor(uow);
			AccountProfile currentProfile = new AccountProfile(uow);
			Object[] streamObjects = (Object[]) o;
			Account currentAccount = (Account) streamObjects[0];
			currentPreprocessor.setCurrentRow((DatabaseRow) streamObjects[1]);
			currentProfile.setCurrentRow((DatabaseRow) streamObjects[1]);
			if (streamObjects.length == 3){
				preprocessorPropCache.setCurrentRow((DatabaseRow) streamObjects[2]);
				currentPreprocessor.usePropertyCache(preprocessorPropCache);
				currentProfile.usePropertyCache(preprocessorPropCache);
			}
			try {
				assessAccountProfile(currentAccount, currentPreprocessor, currentProfile, preprocessorAgent, profiler, uow);
			} catch (InvalidRuleException e) {
				e.printStackTrace();
				throw new RuntimeException("Failed on account: " + currentAccount.getAccountNumber(), e);
			} catch (UnsupportedAttributeException e) {
				e.printStackTrace();
				throw new RuntimeException("Failed on account: " + currentAccount.getAccountNumber(), e);
			}
		}
		protected void handleException(RuntimeException e, List transactionObjects) {
			// 	The objects will all be accounts, print out their accoutids and numbers:
			log.error("Profiling transaction failed for the following accounts:");
			for (Iterator i = transactionObjects.iterator();i.hasNext();){
				Account a = (Account) ((Object[])i.next())[0];
				log.error(a.getAccountId() + "/" + a.getAccountNumber());
			}
			log.error("Exception raised was: ", e);
		}
	}

	/**
	 * 
	 */
	public AccountProfiler() {
		super();
		ServiceEngine.getConfig(true);
		commitSize = ServiceEngine.getIntProp("ACCOUNT_PROFILER_COMMIT", 10);
		batchSize = ServiceEngine.getIntProp("ACCOUNT_PROFILER_BATCH", 1000);
		threadCount = ServiceEngine.getIntProp("ACCOUNT_PROFILER_THREADS", 1);
	}

	public void buildCacheTables() throws AgentFailureException, NoSuchAgentException, InvalidExpressionException, RuleEngineException {
		log.info("Acquiring cache table information");
		Session session = PersistenceManager.getClientSession();
		//Vector agents = RuleAgentFactory.getAgents();
		//RuleAgent preprocessorAgent = (RuleAgent) agents.get(RuleAgentConstants.PPA_AGENT);
		//RuleAgent profiler = (RuleAgent) agents.get(RuleAgentConstants.PA_AGENT);
		RuleAgent preprocessorAgent = RuleAgentFactory.getRuleLoadedAgent(RuleAgentConstants.AGENT_CODES[RuleAgentConstants.PPA_AGENT], null);
		RuleAgent profiler = RuleAgentFactory.getRuleLoadedAgent(RuleAgentConstants.AGENT_CODES[RuleAgentConstants.PA_AGENT], null);
		AccountPreprocessor ap = new AccountPreprocessor(session);
		AccountProfile aProfile = new AccountProfile(session);
		RPMPropertyCache preprocessorPropCache;
		List propertiesUsed = profiler.getPropertiesUsed();
		propertiesUsed.addAll(preprocessorAgent.getPropertiesUsed());
		preprocessorPropCache = getRPMPropertyCache(propertiesUsed, new BaseDescriptor[] { ap, aProfile });
		log.info("Starting cache table building process");
		preprocessorPropCache.buildTemporaryTables();
		log.info("Cache tables built");
	}

	public void dropCacheTables() throws AgentFailureException, NoSuchAgentException, InvalidExpressionException, RuleEngineException {
		log.info("Acquiring cache table information");
		Session session = PersistenceManager.getClientSession();
		//Vector agents = RuleAgentFactory.getAgents();
		//RuleAgent preprocessorAgent = (RuleAgent) agents.get(RuleAgentConstants.PPA_AGENT);
		//RuleAgent profiler = (RuleAgent) agents.get(RuleAgentConstants.PA_AGENT);
		RuleAgent preprocessorAgent = RuleAgentFactory.getRuleLoadedAgent(RuleAgentConstants.AGENT_CODES[RuleAgentConstants.PPA_AGENT], null);
		RuleAgent profiler = RuleAgentFactory.getRuleLoadedAgent(RuleAgentConstants.AGENT_CODES[RuleAgentConstants.PA_AGENT], null);
		AccountPreprocessor ap = new AccountPreprocessor(session);
		AccountProfile aProfile = new AccountProfile(session);
		RPMPropertyCache preprocessorPropCache;
		List propertiesUsed = profiler.getPropertiesUsed();
		propertiesUsed.addAll(preprocessorAgent.getPropertiesUsed());
		preprocessorPropCache = getRPMPropertyCache(propertiesUsed, new BaseDescriptor[] { ap, aProfile });
		log.info("Starting to tear down cache tables");
		preprocessorPropCache.dropTempTables();
		log.info("Cache tables dropped successfully");
	}
	
	public void profileAccounts(boolean useCache, int startingAccountId, int endingAccountId)
		throws NoSuchAgentException, InvalidExpressionException, RuleEngineException {
		Vector agents = RuleAgentFactory.getAgents();
		log.info("Starting account profiling");
		log.info("Profiling batch/commit: " + batchSize + "/" + commitSize);
		Session session = PersistenceManager.getClientSession();
		RPMPropertyCache preprocessorPropCache;
		log.info("Acquiring primary account cursor");
		CursoredStream accountStream = getAccountStream(startingAccountId, endingAccountId, session);
		log.info("Acquring account database stream");
		CursoredStream dbStream = getMainQueryStream(startingAccountId, endingAccountId, session);
		CursoredStream cacheStream = null;
		if (useCache) {
			log.info("Acquiring cache table cursor");
			RuleAgent a = (RuleAgent) agents.get(RuleAgentConstants.PA_AGENT);
			List propertiesUsed = a.getPropertiesUsed();
			a = (RuleAgent) agents.get(RuleAgentConstants.PPA_AGENT);
			propertiesUsed.addAll(a.getPropertiesUsed());
			preprocessorPropCache = getRPMPropertyCache(propertiesUsed, new BaseDescriptor[] { new AccountPreprocessor(session), new AccountProfile(session) });
			cacheStream = preprocessorPropCache.open(startingAccountId, endingAccountId);
		}
		
		KeyedIteratorsIterator iterator;
		if (useCache){
			iterator = new KeyedIteratorsIterator(
				new KeyedIterator[]{new AccountIterator(new StreamIterator(accountStream)), 
				new DatabaseRowIterator(new StreamIterator(dbStream), "ACCOUNT_ID"),
				new DatabaseRowIterator(new StreamIterator(cacheStream), TableMap.JOIN_KEY_FIELD_NAME)});
		}else{
			iterator = new KeyedIteratorsIterator(
					new KeyedIterator[]{new AccountIterator(new StreamIterator(accountStream)), 
					new DatabaseRowIterator(new StreamIterator(dbStream), "ACCOUNT_ID")});
		}
		log.info("Profiling accounts");
		ProfilingThread t[] = new ProfilingThread[threadCount];
		for (int i=0;i<threadCount;i++){ 
		  RuleAgent preprocessorAgent = RuleAgentFactory.getRuleLoadedAgent(RuleAgentConstants.AGENT_CODES[RuleAgentConstants.PPA_AGENT], null);
			RuleAgent profiler = RuleAgentFactory.getRuleLoadedAgent(RuleAgentConstants.AGENT_CODES[RuleAgentConstants.PA_AGENT], null);
			t[i] = new ProfilingThread(iterator, commitSize, preprocessorAgent, profiler, session);
			t[i].start();
		}
		for (int i=0;i<threadCount;i++)
			try {
				t[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			log.info("Profiling complete");
	}

	private CursoredStream getMainQueryStream(int startingAccountId, int endingAccountId, Session session) {
		String mainQuery = new AccountPreprocessor(session).getQuery(BaseDescriptor.MAIN_QUERY_NAME, null);
		mainQuery = QueryBundle.insertQueryCondition(mainQuery, BaseDescriptor.MAIN_TABLE_ALIAS + ".account_id >= " + startingAccountId +" and " + BaseDescriptor.MAIN_TABLE_ALIAS + ".account_id <= " + endingAccountId,"AND");
		if (mainQuery == null)
			throw new RuntimeException("Main query for accounts could not be found.");
		return TopLinkQuery.asCursoredStream(mainQuery, 1000, 1000, session);
	}

	private CursoredStream getAccountStream(int startingAccountId, int endingAccountId, Session session) {
		CursoredStream accountStream =
			RPMDataManager.getPrimaryAccountStream(
				startingAccountId,
				endingAccountId,
				batchSize, false, session);
		return accountStream;
	}
	
	private void assessAccountProfile(
		Account a,
		AccountPreprocessor ap,
		AccountProfile aProf,
		RuleAgent preAgent,
		RuleAgent proAgent,
		UnitOfWork uow) throws InvalidRuleException, UnsupportedAttributeException {
		log.debug("Profiling: " + a.getAccountId());
		ap.setAccount(a);
		aProf.setAccount(a);
		try {
			ap.setScore(0);
			ap.setBehaviourScore(0);
			ap.setExternalScore(0);
			ap.setPaymentScore(0);
			ap.setProfileScore(0);
			preAgent.loadObject(ap);
			preAgent.execute();
			ap.setScore(ap.getBehaviourScore() + ap.getExternalScore() + ap.getPaymentScore() + ap.getProfileScore());
			ap.setRisk(getRiskLevelId(ap.getScore()));
			aProf.overrideRisk(ap.getRisk());
			aProf.setScore(0);
			proAgent.loadObject(aProf);
			proAgent.execute();
			log.debug(a.getAccountId() + " - score: " + aProf.getScore());
			aProf.setProfile(getProfileSegmentId(aProf.getScore()));
			log.debug(a.getAccountId() + " - profile id: " + aProf.getProfile());
			updateAccountInformation(a, ap, aProf, uow);
		} catch (RuleEngineException e) {
			log.error(e.getMessage(),e);
		}
	}

	/**
	 * @param propertiesUsed
	 * @return
	 */
	private RPMPropertyCache getRPMPropertyCache(List propertiesUsed, BaseDescriptor[] bd) {
		RPMPropertyCache cache =
			new RPMPropertyCache(
				"Select ACCOUNT_ID, ACCOUNT_PARENT_ID as ID FROM PTRACCOUNT WHERE ACCOUNT_PARENT_ID is NULL",
				"BIGINT");
		Iterator i = propertiesUsed.iterator();
		while (i.hasNext()) {
			Object[] objs = (Object[]) i.next();
			String propName = objs[0].toString();
			String query = null;
			for (int j = 0; j < bd.length; j++) {
				try{
				query = bd[j].getQuery(propName, (Object[]) objs[1]);
				}catch(IllegalArgumentException e){}
				if (query != null)
					cache.addProperty(propName, (Object[]) objs[1], query);
			}
		}
		return cache;
	}

	private void updateAccountInformation(
		Account account,
		AccountPreprocessor ap,
		AccountProfile profile,
		UnitOfWork handle) {
		Account wCopy = (Account) handle.registerObject(account);
		RiskLevelRef rlrCopy = new RiskLevelRef();
		rlrCopy.setRiskLevelId(new Double(ap.getRisk()));
		rlrCopy = (RiskLevelRef) handle.registerExistingObject(rlrCopy);
		wCopy.setRiskBehaviourScore(new Double(ap.getBehaviourScore()));
		wCopy.setRiskExternalScore(new Double(ap.getExternalScore()));
		wCopy.setRiskPaymentScore(new Double(ap.getPaymentScore()));
		wCopy.setRiskProfileScore(new Double(ap.getProfileScore()));
		wCopy.setRiskScore(new Double(ap.getScore()));
		wCopy.setRiskLevelRef(rlrCopy);
		double weightedReceivable = 0;
		ProfileSegmentRef profileSegment = null;
		if (profile.getProfile() != RPMDataManager.NONE_PROFILE) {
			profileSegment = (ProfileSegmentRef) handle.registerExistingObject(getProfileSegment(profile.getProfile()));
			double recoveryPotential = PrimitiveValue.doubleValue(profileSegment.getRecoveryPotential(), 0);
            double accountOutstanding = PrimitiveValue.doubleValue(account.getOutstandingAmt(), 0);
            weightedReceivable = (recoveryPotential * accountOutstanding)/100;
		}
		wCopy.setProfilingScore(new Double(profile.getScore()));
		wCopy.setProfileSegmentRef(profileSegment);
		if (weightedReceivable < 0)
			wCopy.setWeightedReceivableAmount(new Double(0));
		else
			wCopy.setWeightedReceivableAmount(new Double(weightedReceivable));
	}

	/**
	 * @param id
	 * @return
	 */
	private ProfileSegmentRef getProfileSegment(int id) {
		if (profileSegmentMap == null){
			Map m = new HashMap();
			ProfileSegmentRef[] profileSegments = ObjectPoolManager.getProfileSegments();
			for (int i = 0; i < profileSegments.length;i++)
				m.put(profileSegments[i].getProfileId(),profileSegments[i]);
			profileSegmentMap = m;
		}
		return (ProfileSegmentRef) profileSegmentMap.get(new Double(id));
	}

	private static int getProfileSegmentId(int score) {
		ProfileSegmentRef[] profiles = ObjectPoolManager.getProfileSegments();
		for (int i = 0; i < profiles.length; i++)
			if (profiles[i].getMinimumScore() != null && profiles[i].getMinimumScore().intValue() < score)
				return profiles[i].getProfileId().intValue();
		return ObjectPoolManager.getDefaultProfileSegment().getProfileId().intValue();
	}

	private static int getRiskLevelId(int score) {
		RiskLevelRef[] risks = ObjectPoolManager.getRiskLevels();
		for (int i = 0; i < risks.length; i++)
			if (risks[i].getMinimumScore() != null && risks[i].getMinimumScore().intValue() < score)
				return risks[i].getRiskLevelId().intValue();
		return ObjectPoolManager.getDefaultRiskLevel().getRiskLevelId().intValue();
	}
  
    public void profileAccount(Account a, RuleFiredListener l)
        throws NoSuchAgentException, InvalidExpressionException, RuleEngineException, UnsupportedAttributeException {
        profileAccount(a, l, SessionManager.getClientSession());
    }

	private void profileAccount(Account a, RuleFiredListener l, Session session)
		throws NoSuchAgentException, InvalidExpressionException, RuleEngineException, UnsupportedAttributeException {
		RuleAgent pre = RuleAgentFactory.getRuleLoadedAgent(RuleAgentConstants.AGENT_CODES[RuleAgentConstants.PPA_AGENT], l);
		RuleAgent pro = RuleAgentFactory.getRuleLoadedAgent(RuleAgentConstants.AGENT_CODES[RuleAgentConstants.PA_AGENT], l);
		UnitOfWork uow = session.acquireUnitOfWork();
		try {
			assessAccountProfile(a, new AccountPreprocessor(session), new AccountProfile(session), pre, pro, uow);
			uow.commit();
		} finally {
			uow.release();
		}
		
	}
}
