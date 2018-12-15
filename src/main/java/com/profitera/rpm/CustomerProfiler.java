/*
 * Created on Nov 13, 2003
 */
package com.profitera.rpm;

import java.util.Iterator;
import java.util.Vector;

import oracle.toplink.publicinterface.DatabaseRow;
import oracle.toplink.queryframework.CursoredStream;
import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.UnitOfWork;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.descriptor.db.account.Customer;
import com.profitera.descriptor.db.reference.ProfileSegmentRef;
import com.profitera.descriptor.rpm.BaseDescriptor;
import com.profitera.descriptor.rpm.CustomerProfile;
import com.profitera.descriptor.rpm.RPMPropertyCache;
import com.profitera.descriptor.rpm.UnsupportedAttributeException;
import com.profitera.persistence.PersistenceManager;
import com.profitera.server.ServiceEngine;
import com.profitera.services.system.dataaccess.RPMDataManager;
import com.profitera.util.PrimitiveValue;

/**
 * @author Jamison Masse
 */
public class CustomerProfiler implements ICustomerProfiler {
	private static Log log = LogFactory.getLog(ICustomerProfiler.class);
	private static final int CYCLES_ASSESSMENT = 3;
	private static final int MEMBERSHIP_LENGTH_SCORE_MAX = 50;
	private static final int USAGE_RISK_SCORE_MAX = 150;
	private static final int TRANSACTION_VOLUME_SCORE_MAX = 200;
	private static final int PROFILE_SCORE_MAX = 300;
	private static final int INCOME_SCORE_MAX = 300;
	private static final int[] TRANSACTION_COUNT_STEPS = { 15, 6, 0 };
	private static final double[] TRANSACTION_COUNT_WEIGHTS = { 1.0, 0.65, 0.30 };
	private static final int GET_TOTAL_SPENT_SINCE_CALL = 0;
	private static final int GET_AVERAGE_LOC_CALL = 1;
	private static final int GET_MAX_PROFILE_SCORE_CALL = 2;
	private static final int GET_TRANSACTION_COUNT_CALL = 3;
	private static final int GET_AVERAGE_BALANCE_CARRIED_CALL = 4;
	private static final int GET_OLDEST_ACCOUNT_CALL = 5;
	private static final int GET_DUE_AMOUNT_CALL = 6;
	private static final int GET_TOTAL_OS_CALL = 7;
	private static final Vector propertiesUsed = new Vector();
	static {
		propertiesUsed.add(new Object[] { "getTotalSpentSince", new Object[] { "" + CYCLES_ASSESSMENT }});
		propertiesUsed.add(new Object[] { "getAverageLineOfCreditSince", new Object[] { "" + CYCLES_ASSESSMENT }});
		propertiesUsed.add(new Object[] { "getMaxAccountProfileScore", new Object[0] });
		propertiesUsed.add(new Object[] { "getTransactionCount", new Object[] { "" + CYCLES_ASSESSMENT }});
		propertiesUsed.add(new Object[] { "getAverageBalanceCarriedForwardSince", new Object[] { "" + CYCLES_ASSESSMENT }});
		propertiesUsed.add(new Object[] { "getOldestAccountAge", new Object[0] });
		propertiesUsed.add(new Object[] { "getDueAmount", new Object[] { "" + CYCLES_ASSESSMENT }});
		propertiesUsed.add(new Object[] { "getTotalOutstanding", new Object[0]});
	}
	private int commitSize;
	private int batchSize;

	/**
	 * 
	 */
	public CustomerProfiler() {
		super();
		ServiceEngine.getConfig(true);
		commitSize =ServiceEngine.getIntProp("CUSTOMER_PROFILER_COMMIT",100);
		batchSize =ServiceEngine.getIntProp("CUSTOMER_PROFILER_BATCH", 1000);
	}

	public void buildCacheTables() {
		Session session = PersistenceManager.getClientSession();
		CustomerProfile profile = new CustomerProfile(session);
		RPMPropertyCache customerPropCache = getCustomerPropertyCache(propertiesUsed, new BaseDescriptor[] { profile });
		log.info("Starting cache table building process");
		customerPropCache.buildTemporaryTables();
		log.info("Cache tables built");
	}

	public void dropCacheTables() {
		Session session = PersistenceManager.getClientSession();
		CustomerProfile profile = new CustomerProfile(session);
		RPMPropertyCache customerPropCache = getCustomerPropertyCache(propertiesUsed, new BaseDescriptor[] { profile });
		log.info("Starting to tear down cache tables");
		customerPropCache.dropTempTables();
		log.info("Cache tables dropped successfully");
	}

	public void profileCustomers(boolean useCache, String startingId, String endingId) {
		log.info("Starting customer profiling");
		log.info("Profiling batch/commit: " + batchSize + "/" + commitSize);
		Session session = PersistenceManager.getClientSession();
		CustomerProfile profile = new CustomerProfile(session);
		RPMPropertyCache customerPropCache = null;
		CursoredStream cacheStream = null;
		if (useCache) {
			customerPropCache = getCustomerPropertyCache(propertiesUsed, new BaseDescriptor[] { profile });
			log.info("Acquiring cache table cursor");
			cacheStream = customerPropCache.open(startingId);
		}

		log.info("Acquiring customer cursor");
		CursoredStream customerStream = RPMDataManager.getCustomerStream(startingId, endingId, batchSize, session);
		log.info("Profiling customers");
		int i = 0;
		UnitOfWork uow = session.acquireUnitOfWork();
		while (customerStream.hasMoreElements() && (!useCache || cacheStream.hasMoreElements())) {
			i++;
			try {
				Customer cust = (Customer) customerStream.read();
				customerStream.releasePrevious();
				if (useCache) {
					customerPropCache.setCurrentRow((DatabaseRow) cacheStream.read());
					cacheStream.releasePrevious();
					String cacheId = customerPropCache.getKeyValue().toString();
					while (!cust.getCustomerId().equals(cacheId)) {
						log.debug(
							"Cache is not synchronized, resynching with account stream (cache: "
								+ cacheId
								+ ", customer: "
								+ cust.getCustomerId());
						if (cacheId.compareTo(cust.getCustomerId()) > 0) {
							if (!customerStream.hasMoreElements())
								break;
							cust = (Customer) customerStream.read();
							customerStream.releasePrevious();
						} else {
							if (!cacheStream.hasMoreElements())
								break;
							customerPropCache.setCurrentRow((DatabaseRow) cacheStream.read());
							cacheStream.releasePrevious();
							cacheId = customerPropCache.getKeyValue().toString();
						}
					}
				}
				log.debug("Profiling: " + cust.getCustomerId());
				profile.usePropertyCache(customerPropCache);
				assessCustomerProfile(cust, profile, uow);
				if (i % commitSize == 0) {
					uow.commit();
					uow.release();
					uow = session.acquireUnitOfWork();
				}

			} catch (Throwable t) {
				t.printStackTrace();
				log.debug(t);
			}
		}
		uow.commit();
		if (useCache)
			cacheStream.close();
		customerStream.close();
	}

	private RPMPropertyCache getCustomerPropertyCache(Vector propertiesUsed, BaseDescriptor[] bds) {
		RPMPropertyCache cache =
			new RPMPropertyCache("Select CUSTOMER_ID, 1 as ID FROM PTRCUSTOMER ", "VARCHAR(20)", "TMP_CUSTRPM_");
		Iterator i = propertiesUsed.iterator();
		while (i.hasNext()) {
			Object[] objs = (Object[]) i.next();
			String propName = objs[0].toString();
			String query = null;
			for (int j = 0; j < bds.length; j++) {
				try{
				query = bds[j].getQuery(propName, (Object[]) objs[1]);
				}catch(IllegalArgumentException e){}
				if (query != null) {
					cache.addProperty(propName, (Object[]) objs[1], query);
				}
			}
		}
		return cache;
	}

	private static String getValue(Object[] call, CustomerProfile cp) throws UnsupportedAttributeException {
		return cp.getValue((String) call[0], (Object[]) call[1]);
	}
	
	public static void assessCustomerProfile(Customer customer, CustomerProfile cp, UnitOfWork uow) throws UnsupportedAttributeException {
		cp.setCustomer(customer);
		double averageSpending =
			PrimitiveValue.doubleValue(getValue((Object[]) propertiesUsed.get(GET_TOTAL_SPENT_SINCE_CALL), cp), 0)
				/ CYCLES_ASSESSMENT;
		log.debug("Average Spending: " + averageSpending + "(total  " + averageSpending * CYCLES_ASSESSMENT + ")");
		double averageLineOfCredit =
			PrimitiveValue.doubleValue(getValue((Object[]) propertiesUsed.get(GET_AVERAGE_LOC_CALL), cp), 0);
		log.debug("Average LOC: " + averageLineOfCredit);
		double maxAccountProfileScore =
			PrimitiveValue.doubleValue(getValue((Object[]) propertiesUsed.get(GET_MAX_PROFILE_SCORE_CALL), cp), 0);
		log.debug("Max Prof. Score: " + maxAccountProfileScore);
		double maxAccountTransactionCount =
			PrimitiveValue.doubleValue(getValue((Object[]) propertiesUsed.get(GET_TRANSACTION_COUNT_CALL), cp), 0);
		log.debug("Transaction Count: " + maxAccountTransactionCount);
		double averageCarriedForward =
			PrimitiveValue.doubleValue(
				getValue((Object[]) propertiesUsed.get(GET_AVERAGE_BALANCE_CARRIED_CALL), cp),
				0);
		log.debug("Average Carried Fwd: " + averageCarriedForward);
		double maxYears =
			PrimitiveValue.doubleValue(getValue((Object[]) propertiesUsed.get(GET_OLDEST_ACCOUNT_CALL), cp), 0);
		log.debug("Oldest Account: " + maxYears);
		double totalDue =
			PrimitiveValue.doubleValue(getValue((Object[]) propertiesUsed.get(GET_DUE_AMOUNT_CALL), cp), 0);
		log.debug("Total Due: " + totalDue);
		double totalOS = PrimitiveValue.doubleValue(getValue((Object[]) propertiesUsed.get(GET_TOTAL_OS_CALL), cp), 0); 
		int incomeScore = (int) (averageSpending / averageLineOfCredit * INCOME_SCORE_MAX);
		int profileScore = (int) (maxAccountProfileScore / 1000 * PROFILE_SCORE_MAX);
		int transactionVolumeScore = 0;
		for (int i = 0; i < TRANSACTION_COUNT_STEPS.length; i++) {
			if (maxAccountTransactionCount > TRANSACTION_COUNT_STEPS[i]) {
				transactionVolumeScore = (int) (TRANSACTION_COUNT_WEIGHTS[i] * TRANSACTION_VOLUME_SCORE_MAX);
				break;
			}
		}
		int usageRiskScore = 
			(int) (averageSpending / (averageLineOfCredit - averageCarriedForward) * USAGE_RISK_SCORE_MAX);
		int membershipLengthScore = 5 * (int) maxYears;
		if (membershipLengthScore > MEMBERSHIP_LENGTH_SCORE_MAX)
			membershipLengthScore = MEMBERSHIP_LENGTH_SCORE_MAX;
		int score = incomeScore + profileScore + transactionVolumeScore + usageRiskScore + membershipLengthScore;
		log.debug("Income score: " + incomeScore);
		log.debug("Profile score: " + profileScore);
		log.debug("Trans score: " + transactionVolumeScore);
		log.debug("Usage Risk score: " + usageRiskScore);
		log.debug("Membership score: " + membershipLengthScore);
		log.debug("Final score: " + score);
		int rockId = getProfileSegmentId(score);

		ProfileSegmentRef profile = getProfileSegment(rockId);
		double weightedDue = 0;
		if (profile != null) {
            log.debug("Final profile: " + profile.getProfileDesc() + "(" + profile.getProfileId() + ")");
			weightedDue = totalOS * profile.getRecoveryPotential().doubleValue() / 100.00;
			log.debug("Final WRA: " + weightedDue);
		}
		Customer customerWCopy = (Customer) uow.registerExistingObject(customer);
		customerWCopy.setProfileSegmentRef((ProfileSegmentRef) uow.registerExistingObject(profile));
		if (weightedDue > 0)
			customerWCopy.setRecoveryPotentialAmount(new Double(weightedDue));
		else
			customerWCopy.setRecoveryPotentialAmount(new Double(0));
	}

	private static int getProfileSegmentId(int score) {
		ProfileSegmentRef[] profiles = ObjectPoolManager.getProfileSegments();
		for (int i = 0; i < profiles.length; i++)
			if (profiles[i].getMinimumScore() != null && profiles[i].getMinimumScore().intValue() < score)
				return profiles[i].getProfileId().intValue();
		return ObjectPoolManager.getDefaultProfileSegment().getProfileId().intValue();
	}

	private static ProfileSegmentRef getProfileSegment(int rockId) {
		ProfileSegmentRef[] profiles = ObjectPoolManager.getProfileSegments();
		for (int i = 0; i < profiles.length; i++)
			if (profiles[i].getProfileId().intValue() == rockId)
				return profiles[i];
		return null;
	}
}
