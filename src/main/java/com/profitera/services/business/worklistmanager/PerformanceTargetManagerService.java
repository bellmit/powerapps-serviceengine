package com.profitera.services.business.worklistmanager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import oracle.toplink.expressions.Expression;
import oracle.toplink.expressions.ExpressionBuilder;
import oracle.toplink.queryframework.ReadAllQuery;
import oracle.toplink.queryframework.ReportQuery;
import oracle.toplink.sessions.Session;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.deployment.rmi.PerformanceManagerServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.admin.BusinessUnitBusinessBean;
import com.profitera.descriptor.business.admin.TeamBusinessBean;
import com.profitera.descriptor.business.admin.UserBusinessBean;
import com.profitera.descriptor.business.meta.IUser;
import com.profitera.descriptor.business.reference.TreatmentProcessTypeStatusRefBusinessBean;
import com.profitera.descriptor.db.account.Account;
import com.profitera.descriptor.db.account.AccountTreatmentPlan;
import com.profitera.descriptor.db.reference.TreatmentProcessStatusRef;
import com.profitera.descriptor.db.reference.TreatmentProcessTypeRef;
import com.profitera.descriptor.db.treatment.PaymentInstallment;
import com.profitera.descriptor.db.treatment.TreatmentProcess;
import com.profitera.descriptor.db.user.BusinessUnit;
import com.profitera.descriptor.db.user.User;
import com.profitera.descriptor.db.user.UserTeams;
import com.profitera.descriptor.db.worklist.WorkList;
import com.profitera.persistence.SessionManager;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.MapVerifyingMapCar;
import com.profitera.services.system.dataaccess.QueryManager;
import com.profitera.services.system.dataaccess.RPMDataManager;
import com.profitera.util.CollectionUtil;
import com.profitera.util.MapCar;
import com.profitera.util.QueryBundle;
import com.profitera.util.TopLinkQuery;

public class PerformanceTargetManagerService extends ProviderDrivenService implements
        PerformanceManagerServiceIntf {
    private static final Double[] IN_PROGRESS_STATUSES = new Double[] { TreatmentProcessTypeStatusRefBusinessBean.IN_PROGRESS_TREATMENT_PROCESS_STATUS };
    private static final String UNSUCCESSFUL_CALLS_CAT = "Unsuccessful Calls";
	private static final String SUCCESSFUL_CALLS_CAT = "Successful Calls";
	private static final String ALL_CALLS_CAT = "All Calls";
    private static final String ALL_PROMISES_TAKEN_CAT = "All Promises Taken";
    private static final String BROKEN_PROMISES_CAT = "Broken Promises";
    private static final String PENDING_PROMISES_CAT = "Pending Promises";
    private static final String ASSIGNED_ACCOUNTS_CAT = "Assigned Accounts";
    private static final String HOURS_WORKED_CAT = "Hours Worked";
    private static final String ACCOUNTS_WORKED_CAT = "Accounts Worked";
    
    /**
     * <code>DUMMY_NO_USER_USER</code> is required to generate valid SQL when
     * a BU or Team has no users
     */
    private static final String DUMMY_NO_USER_USER = "DUMMY *** NO USER";

    private static final Double[] UNSUCCESSFUL_STATUSES = new Double[] { TreatmentProcessTypeStatusRefBusinessBean.UNSUCCESSFUL_TREATMENT_PROCESS_STATUS };

    private static final Double[] SUCCESSFUL_STATUSES = new Double[] { TreatmentProcessTypeStatusRefBusinessBean.SUCCESSFUL_TREATMENT_PROCESS_STATUS };
    
    private static final Double[] CANCELLED_STATUSES = new Double[] { TreatmentProcessTypeStatusRefBusinessBean.CANCEL_TREATMENT_PROCESS_STATUS };

    private static final Double[] ALL_STATUSES = new Double[] {
            TreatmentProcessTypeStatusRefBusinessBean.UNSUCCESSFUL_TREATMENT_PROCESS_STATUS,
            TreatmentProcessTypeStatusRefBusinessBean.SUCCESSFUL_TREATMENT_PROCESS_STATUS,
            TreatmentProcessTypeStatusRefBusinessBean.IN_PROGRESS_TREATMENT_PROCESS_STATUS,
            TreatmentProcessTypeStatusRefBusinessBean.CANCEL_TREATMENT_PROCESS_STATUS};

	private interface PerformanceExtractor {
        // set these first
        public void setSession(Session s);

        public void setGrouping(String groupKey);

        public void setQueryLevelObject(Object o);

        // then use theses
        public String getCategory();

        public List getAllGroupingObjects();

        public List[] getPerformanceResults(Date startDate, Date endDate);

        public String[] getPerformanceResultKeys();
    }

    private abstract class BaseExtractor implements PerformanceExtractor {
        protected Session s;

        protected String seriesGrouping;

        protected Object levelObject;

        public void setSession(Session s) {
            this.s = s;
        }

        public void setGrouping(String groupKey) {
            seriesGrouping = groupKey;
        }

        public void setQueryLevelObject(Object o) {
            levelObject = o;
        }
    }

    private abstract class TreatmentActionExtractor extends BaseExtractor {
        public List getAllGroupingObjects() {
            if (seriesGrouping == null)
                return Collections.EMPTY_LIST;
            return getUserIdsForObject(levelObject, false, s);
        }

        public List[] getPerformanceResults(Date startDate, Date endDate) {
            ReportQuery query = getProcessQuery(levelObject, new Double[] { getProcessType() },
                    getStatuses(), startDate, endDate);
            query.addSum(PERF_OS_AMT_TABLE_KEY, getOSFromTreatmentProcess(query
                    .getExpressionBuilder()));
            query.addCount(PERF_COUNT_TABLE_KEY, getCountForProcessCount(query
                    .getExpressionBuilder()));
            if (PERF_USER_TABLE_KEY.equals(seriesGrouping)) {
                query.addGrouping(query.getExpressionBuilder().get(TreatmentProcess.CREATED_USER).get(
                        User.USER_ID));
                query.addAttribute(PERF_USER_TABLE_KEY, query.getExpressionBuilder().get(
                        TreatmentProcess.CREATED_USER).get(User.USER_ID));
            }
            query.setName("Get Process Performance");
            List results = TopLinkQuery.asList(query, s);
            return new List[] { results };
        }

        protected abstract Double getProcessType();

        public String[] getPerformanceResultKeys() {
            return new String[] { PERF_OS_AMT_TABLE_KEY, PERF_COUNT_TABLE_KEY };
        }

        protected abstract Double[] getStatuses();
    }

    private class TreatmentExtractor extends TreatmentActionExtractor {
        Double type;

        Double[] statuses;

        String category;

        public TreatmentExtractor(Double type, Double[] statuses, String category) {
            super();
            this.type = type;
            this.statuses = statuses;
            this.category = category;
        }

        protected Double getProcessType() {
            return type;
        }

        protected Double[] getStatuses() {
            return statuses;
        }

        public String getCategory() {
            return category;
        }
    }

    private class PromiseExtractor extends TreatmentExtractor {

        public PromiseExtractor(Double[] statuses, String category) {
            super(new Double(RPMDataManager.PAYMENT_PLAN_TREATMENT_PROCESS), statuses, category);
        }

        public List[] getPerformanceResults(Date startDate, Date endDate) {
            List[] base = super.getPerformanceResults(startDate, endDate);
            ReportQuery query = getPlanInstallmentQuery(levelObject, getStatuses(), startDate,
                    endDate);
            if (PERF_USER_TABLE_KEY.equals(seriesGrouping)) {
                query.addGrouping(query.getExpressionBuilder().get(
                        PaymentInstallment.PARENT_PAYMENT_PLAN).get(TreatmentProcess.CREATED_USER).get(
                        User.USER_ID));
                query.addAttribute(PERF_USER_TABLE_KEY, query.getExpressionBuilder().get(
                        PaymentInstallment.PARENT_PAYMENT_PLAN).get(TreatmentProcess.CREATED_USER).get(
                        User.USER_ID));
            }
            query.addSum(PERF_PROMISE_AMT_TABLE_KEY, query.getExpressionBuilder().get(
                    PaymentInstallment.INSTALLMENT_DUE_AMOUNT));
            query.setName("Get Promise Amount Taken");
            List results = TopLinkQuery.asList(query, s);
            return (List[]) CollectionUtil.extendArray(base, results);
        }

        public String[] getPerformanceResultKeys() {
            return new String[] { PERF_OS_AMT_TABLE_KEY, PERF_COUNT_TABLE_KEY,
                    PERF_PROMISE_AMT_TABLE_KEY };
        }
    }

    
    public class AccountsWorkedExtractor extends BaseExtractor {
		public String getCategory() {
            return ACCOUNTS_WORKED_CAT;
        }

        public List getAllGroupingObjects() {
            if (seriesGrouping == null)
                return Collections.EMPTY_LIST;
            return getUserIdsForObject(levelObject, false, s);
        }

        /**
         * @see com.profitera.services.business.worklistmanager.PerformanceTargetManagerService.PerformanceExtractor#getPerformanceResults(java.util.Date,
         *      java.util.Date)
         */
        public List[] getPerformanceResults(Date startDate, Date endDate) {
            ExpressionBuilder eb = new ExpressionBuilder();
            ReportQuery q = new ReportQuery(TreatmentProcess.class, eb);
            q.setSelectionCriteria(TopLinkQuery.between(eb.get(TreatmentProcess.ACTUAL_START_DATE),
                    startDate, endDate));
            q.addCount(PERF_COUNT_TABLE_KEY, eb.get(TreatmentProcess.PARENT_TREATMENT_PLAN).get(
                    AccountTreatmentPlan.ACCOUNT).get(Account.ACCOUNT_ID).distinct());
            if (seriesGrouping != null) {
                q.addGrouping(eb.get(TreatmentProcess.CREATED_USER).get(User.USER_ID));
                q.addAttribute(PERF_USER_TABLE_KEY, eb.get(TreatmentProcess.CREATED_USER).get(User.USER_ID));
            }
            List countResults = TopLinkQuery.asList(q, s);
            
            eb = new ExpressionBuilder();
            q = new ReportQuery(TreatmentProcess.class, eb);
            q.setSelectionCriteria(TopLinkQuery.between(eb.get(TreatmentProcess.ACTUAL_START_DATE),
                    startDate, endDate));
            q.addSum(PERF_OS_AMT_TABLE_KEY, eb.get(TreatmentProcess.OUTSTANDING_AMT).distinct());
            if (seriesGrouping != null) {
                q.addGrouping(eb.get(TreatmentProcess.CREATED_USER).get(User.USER_ID));
                q.addAttribute(PERF_USER_TABLE_KEY, eb.get(TreatmentProcess.CREATED_USER).get(User.USER_ID));
            }
            List sumResults = TopLinkQuery.asList(q, s);
            return new List[] { countResults, sumResults };
        }

        /**
         * @see com.profitera.services.business.worklistmanager.PerformanceTargetManagerService.PerformanceExtractor#getPerformanceResultKeys()
         */
        public String[] getPerformanceResultKeys() {
            return new String[] { PERF_OS_AMT_TABLE_KEY, PERF_COUNT_TABLE_KEY };
        }

    }

	public class AccountsAssignedExtractor extends BaseExtractor {

		public String getCategory() {
			return ASSIGNED_ACCOUNTS_CAT;
		}

		public List getAllGroupingObjects() {
			if (seriesGrouping == null)
                return Collections.EMPTY_LIST;
            return getUserIdsForObject(levelObject, false, s);
		}

		public List[] getPerformanceResults(Date startDate, Date endDate) {
			ExpressionBuilder eb = new ExpressionBuilder();
			Expression e = eb.get(Account.WORKLIST).notNull();
			List userIds = getUserIdsForObject(levelObject, true, SessionManager.getClientSession());
			e = e.and(eb.get(Account.WORKLIST).anyOf(WorkList.USERS).get(User.USER_ID).in(userIds.toArray()));
			ReportQuery query = new ReportQuery(Account.class, e);
            query.addSum(PERF_OS_AMT_TABLE_KEY, eb.get(Account.OUTSTANDING_AMT));
            query.addCount(PERF_COUNT_TABLE_KEY, eb.get(Account.ACCOUNT_ID));
            if (PERF_USER_TABLE_KEY.equals(seriesGrouping)) {
                query.addGrouping(eb.get(Account.WORKLIST).anyOf(WorkList.USERS).get(User.USER_ID));
                query.addAttribute(PERF_USER_TABLE_KEY, eb.get(Account.WORKLIST).anyOf(WorkList.USERS).get(User.USER_ID));
            }
            query.setName("Get Worklist Assignments");
            List results = TopLinkQuery.asList(query, s);
            return new List[] { results };
			
		}

		public String[] getPerformanceResultKeys() {
			return new String[] { PERF_OS_AMT_TABLE_KEY, PERF_COUNT_TABLE_KEY };
		}

	}
	
	public class HoursWorkedExtractor extends BaseExtractor {
		private static final String PERFORMANCE_QUERY_BUNDLE_NAME = "performancequery";
		private static final String HOURS_WORKED_QUERY_PROP = "hoursWorked";
		public String getCategory() {
			return HOURS_WORKED_CAT;
		}

		public List getAllGroupingObjects() {
			if (seriesGrouping == null)
                return Collections.EMPTY_LIST;
            return getUserIdsForObject(levelObject, false, s);
		}

		public List[] getPerformanceResults(Date startDate, Date endDate) {
			QueryBundle b = new QueryBundle(PERFORMANCE_QUERY_BUNDLE_NAME,new String[]{"MAIN_TABLE_REGEX"}, new String[]{"MAIN"});
            String sql = b.getQuery(HOURS_WORKED_QUERY_PROP, new Object[]{new Integer(300)}); 
            List results = TopLinkQuery.asList(HOURS_WORKED_QUERY_PROP, sql, s);
            return new List[] { results };
		}

		public String[] getPerformanceResultKeys() {
			return new String[] { PERF_COUNT_TABLE_KEY };
		}
	}
	
	
    private Expression getCountForProcessCount(ExpressionBuilder eb) {
        return eb.get(TreatmentProcess.TREATMENT_PROCESS_ID);
    }

    private Expression getOSFromTreatmentProcess(ExpressionBuilder eb) {
        return eb.get(TreatmentProcess.OUTSTANDING_AMT);
    }

    /**
     * @param evalObject
     * @param i
     * @param doubles
     * @return
     */
    private ReportQuery getProcessQuery(Object evalObject, Double[] processTypes,
            Double[] processStatuses, Date start, Date end) {
        List users = getUserIdsForObject(evalObject, true, SessionManager.getClientSession());
        // if userid list is empty the in clause will be empty -> bad for DB2
        // so add a useless user that cannot possible in the db
        if (users.size() == 0)
            users.add(DUMMY_NO_USER_USER);
        ExpressionBuilder eb = new ExpressionBuilder();
        Expression typesExpression = null;
        if (processTypes != null)
            typesExpression = eb.get(TreatmentProcess.PROCESS_TYPE_ID_REF).get(
                    TreatmentProcessTypeRef.TREATPROC_TYPE_ID).in(processTypes);
        Expression statiiExpression = null;
        if (processStatuses != null)
            statiiExpression = eb.get(TreatmentProcess.PROCESS_STATUS_REF).get(
                    TreatmentProcessStatusRef.TREATPROC_STATUS_ID).in(processStatuses);
        Expression dateExpression = TopLinkQuery.between(
                eb.get(TreatmentProcess.ACTUAL_START_DATE), start, end);
        Expression userExpression = eb.get(TreatmentProcess.CREATED_USER).get(User.USER_ID).in(
                users.toArray());
        return new ReportQuery(TreatmentProcess.class, TopLinkQuery.andExpressions(TopLinkQuery
                .andExpressions(TopLinkQuery.andExpressions(typesExpression, statiiExpression),
                        dateExpression), userExpression));
    }

    /**
     * This is really just a copy of getProcessQuery b/c I can't figure out a
     * way to reuse that logic relative to a different class
     */
    private ReportQuery getPlanInstallmentQuery(Object evalObject, Double[] processStatuses,
            Date start, Date end) {
        List users = getUserIdsForObject(evalObject, true, SessionManager.getClientSession());
        ExpressionBuilder eb = new ExpressionBuilder();
        Expression statiiExpression = null;
        if (processStatuses != null)
            statiiExpression = eb.get(PaymentInstallment.PARENT_PAYMENT_PLAN).get(
                    TreatmentProcess.PROCESS_STATUS_REF).get(
                    TreatmentProcessStatusRef.TREATPROC_STATUS_ID).in(processStatuses);
        Expression dateExpression = TopLinkQuery.between(eb.get(
                PaymentInstallment.PARENT_PAYMENT_PLAN).get(TreatmentProcess.ACTUAL_START_DATE),
                start, end);
        Expression userExpression = eb.get(PaymentInstallment.PARENT_PAYMENT_PLAN).get(
                TreatmentProcess.CREATED_USER).get(User.USER_ID).in(users.toArray());
        return new ReportQuery(PaymentInstallment.class, TopLinkQuery.andExpressions(TopLinkQuery
                .andExpressions(statiiExpression, dateExpression), userExpression));
    }

    /**
     *  if userid list is empty the in clause will be empty -> bad for DB2
     *  so add a useless user that cannot possible in the db
     * @param evalObject
     * @return
     */
    private List getUserIdsForObject(Object evalObject, boolean addDummy, Session s) {
    	List returnList = null;
        if (evalObject instanceof UserBusinessBean) {
            returnList = new ArrayList();
            returnList.add(((UserBusinessBean) evalObject).getUserId());
        }
        if (evalObject instanceof TeamBusinessBean) {
            TeamBusinessBean t = (TeamBusinessBean) evalObject;
            ReadAllQuery q = new ReadAllQuery(User.class);
            ExpressionBuilder eb = new ExpressionBuilder();
            q.setSelectionCriteria(eb.get(User.ACTIVE_STATUS).notEqual(QueryManager.DELETED).and(eb.anyOf(User.TEAMS).get(UserTeams.TEAM_ID).equal(t.getTeamId())));
            q.setName("Get Users For Team");
            returnList = MapCar.map(new MapCar() {
                public Object map(Object o) {
                    return ((User) o).getUserId();
                }
            }, TopLinkQuery.asList(q, s));
        }
        if (evalObject instanceof BusinessUnitBusinessBean) {
            BusinessUnitBusinessBean b = (BusinessUnitBusinessBean) evalObject;
            ReadAllQuery q = new ReadAllQuery(User.class);
            ExpressionBuilder eb = new ExpressionBuilder();
            q.setSelectionCriteria(eb.anyOf(User.TEAMS).get(UserTeams.BUSINESS_UNIT).get(
                    BusinessUnit.BRANCH_ID).equal(b.getBranchId()));
            q.setName("Get Users For Business Unit");
            returnList = MapCar.map(new MapCar() {
                public Object map(Object o) {
                    return ((User) o).getUserId();
                }
            }, TopLinkQuery.asList(q, s));
        }
        if (returnList != null){
        	if (returnList.size() == 0 && addDummy)
        		returnList.add(DUMMY_NO_USER_USER);
        	return returnList;
        }
        throw new RuntimeException("Unsupported eval object type "
                + evalObject.getClass().getName());
    }

    public TransferObject getPromisesSummary(Object queryObject, Date startDate, Date endDate,
            String seriesGrouping) {
        Session s = SessionManager.getClientSession();
        PerformanceExtractor[] extractors = {
                new PromiseExtractor(ALL_STATUSES, ALL_PROMISES_TAKEN_CAT),
                new PromiseExtractor(UNSUCCESSFUL_STATUSES, BROKEN_PROMISES_CAT),
                new PromiseExtractor(IN_PROGRESS_STATUSES, PENDING_PROMISES_CAT),
                new PromiseExtractor(CANCELLED_STATUSES, "Cancelled Promises")};
        List finalList = extractPerformance(extractors, queryObject, startDate, endDate,
                seriesGrouping, s);
        injectTargetInformation(queryObject, startDate, endDate, seriesGrouping, ALL_PROMISES_TAKEN_CAT, finalList);
        return new TransferObject(finalList);
    }

    private void injectTargetInformation(Object queryObject, Date startDate, Date endDate, String seriesGrouping, String cat, List finalList) {
      try {
        Long target = (Long) getTargetTypesByDescription().get(cat);
        if (target == null)
          return;
        List users = getUserIdsForObject(queryObject, false, SessionManager.getClientSession());
        Map args = new HashMap();
        args.put("TARGET_TYPE_ID", target);
        args.put("START_DATE", startDate);
        args.put("END_DATE", endDate);
        args.put("USERS", users);
        List results = Collections.EMPTY_LIST;
        if (seriesGrouping != null){
            results = executeListQuery(IReadOnlyDataProvider.LIST_RESULTS, "getGroupedCategoryTarget", args, new MapVerifyingMapCar(new String[]{"USER_ID", "TARGET"}), getReadOnlyProvider());
            for (Iterator i = results.iterator(); i.hasNext();) {
              Map r = (Map) i.next();
              String user = (String) r.get("USER_ID");
              for (Iterator j = finalList.iterator(); j.hasNext();) {
                Map f = (Map) j.next();
                if (f.get(PERF_CATEGORY_TABLE_KEY).equals(cat) && f.get("USER").equals(user)){
                  f.put("TARGET", r.get("TARGET"));
                  Number num = (Number) f.get(PERF_COUNT_TABLE_KEY);
                  Number goal = (Number) f.get("TARGET");
                  if (goal != null && num != null && goal.longValue() > 0){
                    long percent =  (long) (num.doubleValue()/goal.doubleValue() * 100);
                    f.put("PERCENT", new Long(percent));
                  }
                }
              }
            }
            
        } else {
          // No grouping I expect 1 row:
          results = executeListQuery(IReadOnlyDataProvider.LIST_RESULTS, "getCategoryTarget", args, new MapVerifyingMapCar(new String[0]), getReadOnlyProvider());
          if (results.size() > 0){
            for (Iterator i = finalList.iterator(); i.hasNext();) {
              Map element = (Map) i.next();
              if (element.get(PERF_CATEGORY_TABLE_KEY).equals(cat)){
                Map m = (Map) results.get(0);
                element.putAll((Map) m);
                Number num = (Number) element.get(PERF_COUNT_TABLE_KEY);
                Number goal = (Number) element.get("TARGET");
                if (goal != null && num != null && goal.longValue() > 0){
                  long percent =  (long) (num.doubleValue()/goal.doubleValue() * 100);
                  element.put("PERCENT", new Long(percent));
                }
              }
            }
          }
        }
      } catch (TransferObjectException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
    }

    public TransferObject getCallSummary(Object queryObject, Date startDate, Date endDate,
            String seriesGrouping) {
        Session s = SessionManager.getClientSession();
        PerformanceExtractor[] extractors = {
                new TreatmentExtractor(new Double(RPMDataManager.PLACE_A_CALL_TREATMENT_PROCESS),
                        ALL_STATUSES, ALL_CALLS_CAT),
                new TreatmentExtractor(new Double(RPMDataManager.PLACE_A_CALL_TREATMENT_PROCESS),
                        SUCCESSFUL_STATUSES, SUCCESSFUL_CALLS_CAT),
                new TreatmentExtractor(new Double(RPMDataManager.PLACE_A_CALL_TREATMENT_PROCESS),
                        UNSUCCESSFUL_STATUSES, UNSUCCESSFUL_CALLS_CAT) };
        List finalList = extractPerformance(extractors, queryObject, startDate, endDate,
                seriesGrouping, s);
        injectTargetInformation(queryObject, startDate, endDate, seriesGrouping, ALL_CALLS_CAT, finalList);
        return new TransferObject(finalList);
    }

    private List extractPerformance(PerformanceExtractor[] extractors, Object queryObject,
            Date startDate, Date endDate, String seriesGrouping, Session session) {
        Map finalResults = new HashMap();
        for (int k = 0; k < extractors.length; k++) {
            PerformanceExtractor ape = extractors[k];
            ape.setSession(session);
            ape.setGrouping(seriesGrouping);
            ape.setQueryLevelObject(queryObject);
            List allGroupObjects = ape.getAllGroupingObjects();
            if (allGroupObjects.size() > 0) {
                finalResults.put(ape.getCategory(), getAllGroupingsMap(allGroupObjects,
                        seriesGrouping, session));
            } else {
                finalResults.put(ape.getCategory(), new HashMap());
            }
            List[] resultsArray = ape.getPerformanceResults(startDate, endDate);
            String[] keys = ape.getPerformanceResultKeys();
            for (int i = 0; i < resultsArray.length; i++) {
                for (int j = 0; j < keys.length; j++) {
                    addResults(resultsArray[i], keys[j], seriesGrouping, (Map) finalResults.get(ape
                            .getCategory()));
                }
            }
        }

        List finalList = new ArrayList();
        if (seriesGrouping != null) {
            for (Iterator i = finalResults.entrySet().iterator(); i.hasNext();) {
                Map.Entry e = (Entry) i.next();
                Map m = (Map) e.getValue();
                Object parentKey = e.getKey();
                for (Iterator i2 = m.entrySet().iterator(); i2.hasNext();) {
                    Map.Entry e2 = (Entry) i2.next();
                    Map m2 = (Map) e2.getValue();
                    m2.put(PERF_CATEGORY_TABLE_KEY, parentKey);
                    m2.put(seriesGrouping, e2.getKey());
                    finalList.add(m2);
                }
            }
        } else {

            for (Iterator i = finalResults.entrySet().iterator(); i.hasNext();) {
                Map.Entry e = (Entry) i.next();
                Map m = (Map) e.getValue();
                m.put(PERF_CATEGORY_TABLE_KEY, e.getKey());
                finalList.add(m);
            }
        }
        return finalList;
    }

    private void addResults(List queryResultsMaps, String resultValueKey, String seriesGrouping,
            Map categoryResultMap) {
        for (Iterator i = queryResultsMaps.iterator(); i.hasNext();) {
            Map result = (Map) i.next();
            Map groupMap = null;
            if (seriesGrouping != null) {
                groupMap = (Map) categoryResultMap.get(result.get(seriesGrouping));
                if (groupMap == null) {
                    log.warn("Series grouping item " + result.get(seriesGrouping)
                            + " for " + seriesGrouping + " not found!");
                    continue;
                }
            } else {
                groupMap = categoryResultMap;
            }
            Object catResultValue = result.get(resultValueKey);
            if (catResultValue != null)
                groupMap.put(resultValueKey, catResultValue);
        }
    }

    private Map getAllGroupingsMap(List l, String seriesGrouping, Session s) {
        if (PERF_USER_TABLE_KEY.equals(seriesGrouping)) {
            HashMap m = new HashMap();
            for (Iterator i = l.iterator(); i.hasNext();) {
                m.put(i.next(), new HashMap());
            }
            return m;
        }
        throw new RuntimeException("Unsupported grouping: " + seriesGrouping);
    }

    /**
     * @see com.profitera.deployment.rmi.PerformanceManagerServiceIntf#getAccountsWorkedSummary(java.lang.Object,
     *      java.util.Date, java.util.Date, java.lang.String)
     */
    public TransferObject getAccountsWorkedSummary(Object queryObject, Date startDate,
            Date endDate, String seriesGrouping) {
        Session s = SessionManager.getClientSession();
        PerformanceExtractor[] extractors = { new AccountsWorkedExtractor() };
        List finalList = extractPerformance(extractors, queryObject, startDate, endDate,
                seriesGrouping, s);
        injectTargetInformation(queryObject, startDate, endDate, seriesGrouping,  ACCOUNTS_WORKED_CAT, finalList);
        return new TransferObject(finalList);
    }

	/**
	 * @see com.profitera.deployment.rmi.PerformanceManagerServiceIntf#getAccountsAssignedSummary(java.lang.Object, java.lang.String)
	 */
	public TransferObject getAccountsAssignedSummary(Object queryObject, String seriesGrouping) {
		Session s = SessionManager.getClientSession();
        PerformanceExtractor[] extractors = { new AccountsAssignedExtractor() };
        List finalList = extractPerformance(extractors, queryObject, null, null, seriesGrouping, s);
        return new TransferObject(finalList);
	}

	/**
	 * @see com.profitera.deployment.rmi.PerformanceManagerServiceIntf#getHoursWorkedSummary(java.lang.Object, java.lang.String)
	 */
	public TransferObject getHoursWorkedSummary(Object queryObject, String seriesGrouping) {
		Session s = SessionManager.getClientSession();
        PerformanceExtractor[] extractors = { new HoursWorkedExtractor() };
        List finalList = extractPerformance(extractors, queryObject, null, null, seriesGrouping, s);
        return new TransferObject(finalList);
	}

  public TransferObject setUserTargets(List targets) {
    try {
      List trans = new ArrayList();
      Map targetTypes = getTargetTypesByCode();
      Set baseKeys = new HashSet();
      baseKeys.add(IUser.USER_ID);
      baseKeys.add("START_DATE");
      baseKeys.add("END_DATE");
      for (Iterator i = targets.iterator(); i.hasNext();) {
        Map userTarget = (Map) i.next();
        String user = (String) userTarget.get(IUser.USER_ID);
        Date targetDate = (Date) userTarget.get("TARGET_DATE");
        for (Iterator tIter = userTarget.entrySet().iterator(); tIter.hasNext();) {
          Map.Entry element = (Map.Entry) tIter.next();
          String targetkey = (String) element.getKey();
          Long targetId = (Long) targetTypes.get(targetkey);
          if (targetId != null){
            trans.add(getUserTargetTransaction(user, targetId, targetDate, (Long) element.getValue()));
          }
        }
      }
      getReadWriteProvider().execute(new RunnableTransactionSet((IRunnableTransaction[]) trans.toArray(new IRunnableTransaction[0])));
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    } catch (AbortTransactionException e) {
      return sqlFailure("update/insert", "target-related statements", null, e);
    } catch (SQLException e) {
      return sqlFailure("update/insert", "target-related statements", null, e);
    }
    return new TransferObject(Boolean.TRUE);
  }

  private Map getTargetTypesByCode() throws TransferObjectException {
    Iterator i = executeIteratorQuery(IReadOnlyDataProvider.LIST_RESULTS, "getPerformanceTargetTypes", null, getReadOnlyProvider());
    Map m = new HashMap();
    while(i.hasNext()){
      Map tt = (Map) i.next();
      m.put(tt.get("CODE"), tt.get("ID"));
    }
    return m;
  }
  
  private Map getTargetTypesByDescription() throws TransferObjectException {
    Iterator i = executeIteratorQuery(IReadOnlyDataProvider.LIST_RESULTS, "getPerformanceTargetTypes", null, getReadOnlyProvider());
    Map m = new HashMap();
    while(i.hasNext()){
      Map tt = (Map) i.next();
      m.put(tt.get("DESCRIPTION"), tt.get("ID"));
    }
    return m;
  }

  private IRunnableTransaction getUserTargetTransaction(final String user, final Long targetId, Date date, final Long target) throws TransferObjectException {
    final IReadWriteDataProvider p = getReadWriteProvider();
    final Map currentTarget = getUserTarget(user, targetId, date);
    final Date targetDate = date;
    return new IRunnableTransaction(){
      public void execute(ITransaction t) throws SQLException, AbortTransactionException {
        Map m = getTargetArgs(user, targetId, target, targetDate);
        if (currentTarget == null){
          p.insert("insertUserTarget", m, t);
        } else {
          p.update("updateUserTarget", m, t);
        }
      }};
  }

  private Map getUserTarget(String user, Long targetId, Date start) throws TransferObjectException {
    List l = executeListQuery(IReadOnlyDataProvider.LIST_RESULTS, "getUserTarget", getTargetArgs(user, targetId, null, start), new MapVerifyingMapCar(new String[0]), getReadWriteProvider());
    if (l.size() > 0)
      return (Map) l.get(0);
    return null;
    
  }

  private Map getTargetArgs(final String user, final Long targetId, final Long target, final Date targetDate) {
    Map m = new HashMap();
    m.put(IUser.USER_ID, user);
    m.put("TARGET_DATE", targetDate);
    m.put("TARGET_TYPE_ID", targetId);
    if (target == null){
      m.put("TARGET", new Long(0));
    } else {
      m.put("TARGET", target);
    }
    return m;
  }
  
  public TransferObject getUserTargets(String userId, Date start, Date end) {
    Date[] dates = getDatesInRange(start, end);
    try {
      List result = new ArrayList();
      Map args = new HashMap();
      args.put("USER_ID", userId);
      getTargetsForEachDay(dates, result, args);
      return new TransferObject(result);
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
  }

  private void getTargetsForEachDay(Date[] dates, List result, Map args) throws TransferObjectException {
    for (int i = 0; i < dates.length; i++) {
      final Date d = dates[i];
      args.put("DATE", d);
      List l = executeListQuery(IReadOnlyDataProvider.LIST_RESULTS, "getPerformanceTargets", args, new MapVerifyingMapCar(new String[0]), getReadWriteProvider());
      result.addAll(MapCar.map(new MapCar(){
        public Object map(Object o) {
          Map m = (Map) o;
          m.put("TARGET_DATE", d);
          return o;
        }},l));
    }
  }

  public TransferObject getTeamTargets(String teamId, Date start, Date end) {
    Date[] dates = getDatesInRange(start, end);
    try {
      List result = new ArrayList();
      Map args = new HashMap();
      args.put("TEAM_ID", teamId);
      getTargetsForEachDay(dates, result, args);
      return new TransferObject(result);
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
  }
  
  private Date[] getDatesInRange(Date start, Date end) {
    List l = new ArrayList();
    Calendar cal = Calendar.getInstance();
    cal.setTime(start);
    while(!cal.getTime().after(end)){
      l.add(cal.getTime());
      cal.add(Calendar.DATE, 1);
    }
    return (Date[]) l.toArray(new Date[0]);
  }
}