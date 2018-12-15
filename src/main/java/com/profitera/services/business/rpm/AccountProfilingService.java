package com.profitera.services.business.rpm;

import java.util.MissingResourceException;

import oracle.toplink.sessions.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.deployment.rmi.*;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.persistence.SessionManager;
import com.profitera.rpm.AccountProfiler;
import com.profitera.rpm.AgentFailureException;
import com.profitera.rpm.IAccountProfiler;
import com.profitera.rpm.NoSuchAgentException;
import com.profitera.rpm.RuleEngineException;
import com.profitera.rpm.expression.InvalidExpressionException;
import com.profitera.services.business.BusinessService;
import com.profitera.util.QueryBundle;
import com.profitera.util.TopLinkQuery;

/**
 * 
 * @author jamison
 */
public class AccountProfilingService extends BusinessService implements AccountProfilingServiceIntf {
	private static final String GET_MAX_ACCOUNT_ID_PROP = "getMaxAccountId";
	private static final String GET_MIN_ACCOUNT_ID_PROP = "getMinAccountId";
	public static Log log = LogFactory.getLog(AccountProfilingService.class);
	
	public TransferObject profileAccount(Double accountId, Boolean useCacheTables){
		return profileAccounts(accountId, accountId, useCacheTables);
	}
	
	public TransferObject profileAllAccounts(Boolean useCacheTables){
		Session session = SessionManager.getClientSession();
		try {
			QueryBundle qb = getQueryBundle();
			Double min = new Double(((Number)TopLinkQuery.queryOneValue(GET_MIN_ACCOUNT_ID_PROP, qb.getQuery(GET_MIN_ACCOUNT_ID_PROP), session)).doubleValue());
			Double max = new Double(((Number)TopLinkQuery.queryOneValue(GET_MAX_ACCOUNT_ID_PROP, qb.getQuery(GET_MAX_ACCOUNT_ID_PROP), session)).doubleValue());
			return profileAccounts(min, max, useCacheTables);
		} catch (MissingResourceException e) {
			return ProfilerRunner.fail(log, e.getMessage());
		}
	}
	
	public TransferObject createCache(){
		return new ProfilerRunner(){
			protected void runProfiler(Object o) throws NoSuchAgentException, InvalidExpressionException, RuleEngineException, AgentFailureException {
				IAccountProfiler ap = (IAccountProfiler) o;
				ap.dropCacheTables();
				ap.buildCacheTables();
			}}.execute(getNewAccountProfilerInstance(), log);
	}
	
	/**
	 * I reload it every time here on purpose so that any changes in the
	 * queries will be reflected without having to restart the server
	 */
	private QueryBundle getQueryBundle() {
		QueryBundle qb = new QueryBundle("accountquery");
		return qb;
	}

	private TransferObject profileAccounts(final Double accountId, final Double accountId2, final Boolean useCacheTables) {
		return new ProfilerRunner(){
			protected void runProfiler(Object o) throws NoSuchAgentException, InvalidExpressionException, RuleEngineException {
				IAccountProfiler ap = (IAccountProfiler) o;
				ap.profileAccounts(useCacheTables.booleanValue(), accountId.intValue(), accountId2.intValue());
			}}.execute(getNewAccountProfilerInstance(), log);
	}

  protected IAccountProfiler getNewAccountProfilerInstance() {
    return new AccountProfiler();
  }
}
