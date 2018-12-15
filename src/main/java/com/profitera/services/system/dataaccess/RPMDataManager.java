/*
 * Created on Sep 2, 2003
 */
package com.profitera.services.system.dataaccess;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import oracle.toplink.expressions.Expression;
import oracle.toplink.expressions.ExpressionBuilder;
import oracle.toplink.queryframework.CursoredStream;
import oracle.toplink.queryframework.ObjectLevelReadQuery;
import oracle.toplink.queryframework.ReadAllQuery;
import oracle.toplink.queryframework.ReadObjectQuery;
import oracle.toplink.queryframework.ReportQuery;
import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.UnitOfWork;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.descriptor.business.reference.TreatmentProcessTypeStatusRefBusinessBean;
import com.profitera.descriptor.db.account.Account;
import com.profitera.descriptor.db.account.AccountTreatmentPlan;
import com.profitera.descriptor.db.account.Customer;
import com.profitera.descriptor.db.reference.AgencyTypeRef;
import com.profitera.descriptor.db.reference.BlockCodeRef;
import com.profitera.descriptor.db.reference.ProfileSegmentRef;
import com.profitera.descriptor.db.reference.RiskLevelRef;
import com.profitera.descriptor.db.reference.TreatProcessTypeStatusRef;
import com.profitera.descriptor.db.reference.TreatmentProcessStatusRef;
import com.profitera.descriptor.db.reference.TreatmentProcessTypeRef;
import com.profitera.descriptor.db.reference.TreatmentStageRef;
import com.profitera.descriptor.db.reference.TreatmentStreamRef;
import com.profitera.descriptor.db.reference.TreatprocSubtypeRef;
import com.profitera.descriptor.db.treatment.PaymentPlan;
import com.profitera.descriptor.db.treatment.PlaceACall;
import com.profitera.descriptor.db.treatment.SmsMessage;
import com.profitera.descriptor.db.treatment.Template;
import com.profitera.descriptor.db.treatment.TreatmentProcess;
import com.profitera.descriptor.db.treatment.TreatprocTemplate;
import com.profitera.descriptor.db.user.User;
import com.profitera.descriptor.db.user.UserTeams;
import com.profitera.descriptor.db.user.UserWorkListAssign;
import com.profitera.descriptor.db.worklist.WorkList;
import com.profitera.descriptor.db.worklist.WorkListBlockCode;
import com.profitera.descriptor.rpm.AccountDelinquency;
import com.profitera.descriptor.rpm.AccountPreprocessor;
import com.profitera.descriptor.rpm.AccountProfile;
import com.profitera.descriptor.rpm.AccountWorklist;
import com.profitera.persistence.SessionManager;
import com.profitera.rpm.IncompleteAccountDataException;
import com.profitera.util.TopLinkQuery;

/**
 * @author jamison
 *
 */
public class RPMDataManager {
    public static final String SYSTEM_USER_ID = "Admin";
	private static final String INCONSISTENT_ACCOUNT_EMSG = "The account is not consistent in the database: ";
	public static int NONE_TREATMENT_STAGE = 0;

    //TODO: Retail purchase TC code 40, HLBB specific
    public static String RETAIL_TC_CODE = "40";

    public static int NONE_PROFILE = 0;

    public final static int NONE_WORKLIST_ID = 0;

    private final static Log log = LogFactory.getLog(RPMDataManager.class);

    // Note: Some constanct below commented for bug 1015.
    
    //public static final int FAX_TREATMENT_PROCESS = 1;
    public static final int PAYMENT_PLAN_TREATMENT_PROCESS = 2;
    //public static final int REACTIVATION_TREATMENT_PROCESS = 3;
    //public static final int SUSPENSION_TREATMENT_PROCESS = 4;
    public static final int NOTES_TREATMENT_PROCESS = 5;
    public static final int DISPUTE_TREATMENT_PROCESS = 6;
    public static final int LEGAL_ACTION_TREATMENT_PROCESS = 7;
    public static final int PLACE_A_CALL_TREATMENT_PROCESS = 8;
    //public static final int ESCALATE_TREATMENT_PROCESS = 9;
    //public static final int FRAUD_TREATMENT_PROCESS = 10;
    //
    //public static final int EMAIL_TREATMENT_PROCESS = 12;
    public static final int SMS_TREATMENT_PROCESS = 13;
    public static final int LETTER_TREATMENT_PROCESS = 14;
    public static final int APPOINTMENT_TREATMENT_PROCESS = 15;
    public static final int WAIVE_TREATMENT_PROCESS = 16;
    //public static final int CLAIMED_PAYMENT_TREATMENT_PROCESS = 17;
    //public static final int SKIP_TRACE_TREATMENT_PROCESS = 18;
    public static final int OUTSOURCE_AGENCY_TREATMENT_PROCESS = 19;
    public static final int ACCOUNT_TERMINATION_TREATMENT_PROCESS = 20;
    public static final int BLOCK_CARD_TREATMENT_PROCESS = 21;
    public static final int NONE_TREATMENT_PROCESS = 22;
    public static final int PAYOFF_INQUIRY_TREATMENT_PROCESS = 24;
    public static final int TPS_TREATMENT_PROCESS = 25;
    
    public static final int TREATMENT_PROCESS_TYPE_NONE = 38501;
    public static final int ACCOUNT_PROFILE_NONE = 10;

    public static final Double NORMAL_LETTER_HOUSEKEEP_STATUS = new Double(61601);

    // Constants for Installment Status Refs
    public static final Double INPROGRESS_INST_STATUS = new Double(60601);
    public static final Double CANCELLED_INST_STATUS = new Double(60602);
    public static final Double COMPLETED_INST_STATUS = new Double(60603);
    public static final Double BROKEN_INST_STATUS = new Double(60604);

    public static final int OCA_AGENCY_TYPE = 1;
    public static final int LEGAL_AGENCY_TYPE = 2;

    public static final Map PROCESS_TYPEID_MAP;

    static
    {
      Map map = new HashMap(16);
      map.put(new Integer(PAYMENT_PLAN_TREATMENT_PROCESS), PaymentPlan.class);
      map.put(new Integer(PLACE_A_CALL_TREATMENT_PROCESS), PlaceACall.class);
      map.put(new Integer(SMS_TREATMENT_PROCESS), SmsMessage.class);
      PROCESS_TYPEID_MAP = Collections.unmodifiableMap(map);
    }

    public RPMDataManager() {
        super();
    }

    public static Session getSession() {
        return SessionManager.getClientSession();
    }

    public static Account getAccount(Double accountId, Session session) throws IncompleteAccountDataException {
        return getAccount(accountId.doubleValue(), session);
    }

    /**
     * This method tries to pre-join as much of the delayed eval stuff as possible
     * since the RPM is going to use almost everything anyway to avoid multiple
     * round-trips
     * @param accountId
     * @param session
     * @return
     * @throws IncompleteAccountDataException
     */
    public static Account getAccount(double accountId, Session session) throws IncompleteAccountDataException {
        ExpressionBuilder eb = new ExpressionBuilder();
        Expression exp1 = eb.get(Account.ACCOUNT_ID).equal(accountId);
        // TODO: CCAccount only here...
        ReadObjectQuery roq = new ReadObjectQuery(Account.class, exp1);
        Account account = (Account) session.executeQuery(roq);
        // If the joins fail, the account will be null, we can't process it anyway b/c we need all these
        // attributes, so we'll just throw an exception
        if (account == null)
            throw new IncompleteAccountDataException(INCONSISTENT_ACCOUNT_EMSG + accountId );
        return account;
    }

    public static void addAccountCustomerJoins(ObjectLevelReadQuery q, ExpressionBuilder eb) {
		q.addJoinedAttribute(eb.get(Account.CUSTOMER));
		q.addJoinedAttribute(eb.get(Account.CUSTOMER).getAllowingNull(Customer.CUSTOMER_SEGMENT));
		q.addJoinedAttribute(eb.get(Account.CUSTOMER).getAllowingNull(Customer.EMPLOYMENT_TYPE_REF));
	}

	public static void addAccountRefJoins(ObjectLevelReadQuery q, ExpressionBuilder eb) {
//		 Billing cycle is a non-null column
		q.addJoinedAttribute(Account.BILLING_CYCLE_REF);
		q.addJoinedAttribute(eb.getAllowingNull(Account.DELINQUENCY_TYPE_REF));
		q.addJoinedAttribute(eb.getAllowingNull(Account.AUTO_PAY_REF));
		q.addJoinedAttribute(eb.getAllowingNull(Account.BLOCK_CODE_REF));
		q.addJoinedAttribute(eb.getAllowingNull(Account.CHANNEL_CODE_REF));
		q.addJoinedAttribute(eb.getAllowingNull(Account.CAMPAIGN_CODE_REF));
		q.addJoinedAttribute(eb.getAllowingNull(Account.CHARGEOFF_REASON_REF));
		q.addJoinedAttribute(eb.getAllowingNull(Account.CHARGEOFF_STATUS_REF));
		q.addJoinedAttribute(eb.getAllowingNull(Account.COLLECTABILITY_STATUS_REF));
		q.addJoinedAttribute(eb.getAllowingNull(Account.COLLECTION_REASON_REF));
		q.addJoinedAttribute(eb.getAllowingNull(Account.DEBT_RECOVERY_STATUS_REF));
		q.addJoinedAttribute(eb.getAllowingNull(Account.PRODUCT_TYPE_REF));
		q.addJoinedAttribute(eb.getAllowingNull(Account.PROFILE_SEGMENT_REF));
		q.addJoinedAttribute(eb.getAllowingNull(Account.SENSITIVITY_STATUS_REF));
	}

	/**
     * If you pass in the original object it is updated to
     * reflect the changes made here so there is no need to
     * make another round trip to the DB.
     * @param account
     * @param ap
     * @param session
     */
    public static void updateAccountRiskScores(Account account, AccountPreprocessor ap, Session session) {
        log.debug("About to commit changes for account " + ap.getAccountId());
        UnitOfWork handle = session.acquireUnitOfWork();
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
        handle.commit();
    }

    public static void updateAccountProfile(Account account, AccountProfile ap, Session session) {
        UnitOfWork handle = session.acquireUnitOfWork();
		Account ac = (Account) handle.registerObject(account);
        ProfileSegmentRef profileSegment = null;
        if (ap.getProfile() != NONE_PROFILE) {
            profileSegment = new ProfileSegmentRef();
            profileSegment.setProfileId(new Double(ap.getProfile()));
            profileSegment = (ProfileSegmentRef) handle.registerExistingObject(profileSegment);
        }
        ac.setProfilingScore(new Double(ap.getScore()));
        ac.setProfileSegmentRef(profileSegment);
        handle.commit();
    }

    public static AccountDelinquency createAccountDelinquency(Account account, Session session) {
        return new AccountDelinquency(account, session);
    }

    public static AccountWorklist createAccountWorklist(Account account) {
        return new AccountWorklist(account);
    }

    public static AccountWorklist createAccountWorklist(Vector accounts) {
        return new AccountWorklist(accounts);
    }

    public static AccountProfile createAccountProfile(Account account, Session session) {
        return new AccountProfile(account, session);
    }

    public static AccountPreprocessor createAccountPreprocessor(Account account, Session session) {
        AccountPreprocessor ap = new AccountPreprocessor(account, session);
        return ap;
    }

    /**
     * Returns the current open plan if there is one, otherwise it return null,
     * prefetches treatmentProcesses since the RPM usually needs this anyway.
     * @param account
     * @param session
     * @return
     */
    public static AccountTreatmentPlan getCurrentTreatmentPlan(Account account, Session session) {
        ExpressionBuilder eb = new ExpressionBuilder();
        // A plan with a null end date is an unfinished plan.
        Expression exp = eb.get(AccountTreatmentPlan.ACCOUNT).get(Account.ACCOUNT_ID).equal(account.getAccountId()).and(eb.get(AccountTreatmentPlan.TREATMENT_END_DATE).isNull());
        ReadObjectQuery roq = new ReadObjectQuery(AccountTreatmentPlan.class, exp);
        roq.addJoinedAttribute(eb.getAllowingNull(AccountTreatmentPlan.TREATMENT_STREAM_REF));
        // This query should always return 1 or 0, otherwise something is wrong!
        AccountTreatmentPlan plan = (AccountTreatmentPlan) session.executeQuery(roq);
        return plan;
    }

    /**
     * Returns the <b>newest</b> process that is <b>in progress</b> in the
     * current treatment plan.
     * @param account
     * @param session
     * @return
     */
    public static TreatmentProcess getCurrentTreatmentProcess(Account account, Session session) {
        Double[] notComplete = { TreatmentProcessTypeStatusRefBusinessBean.IN_PROGRESS_TREATMENT_PROCESS_STATUS };
        ReadAllQuery q = getCurrentPlanProcessByStatusQuery(account, notComplete);
        q.setName("Get Current Treatment Process");
        // Order by expected start, newest in progress will have max expected start
        q.addDescendingOrdering(TreatmentProcess.EXPECTED_START_DATE);
        return (TreatmentProcess) TopLinkQuery.getSingleRowQueryResult(q, session);
    }
    
    public static TreatmentProcess getCurrentAutomaticTreatmentProcess(Account account, Session session) {
      Double[] notComplete = { TreatmentProcessTypeStatusRefBusinessBean.IN_PROGRESS_TREATMENT_PROCESS_STATUS };
      ReadAllQuery q = getCurrentPlanProcessByStatusQuery(account, notComplete);
      q.setSelectionCriteria(q.getSelectionCriteria().and(q.getExpressionBuilder().get(TreatmentProcess.MANUAL).equal(false)));
      q.setName("Get Current Auto Treatment Process");
      // Order by expected start, newest in progress will have max expected start
      q.addDescendingOrdering(TreatmentProcess.EXPECTED_START_DATE);
      return (TreatmentProcess) TopLinkQuery.getSingleRowQueryResult(q, session);
    }

    public static TreatmentProcess getLastTreatmentProcess(Account a, Session session) {
    	AccountTreatmentPlan p = getCurrentTreatmentPlan(a, session);
    	if (p==null) return null;
    	return getLastTreatmentProcess(p, session);
    }

    /**
     * Returns the <b>newest</b> process that is <b>sucessful or unsuccessful</b> in the
     * current treatment plan.
     * @param plan
     * @param session
     * @return
     */
    public static TreatmentProcess getLastTreatmentProcess(AccountTreatmentPlan plan, Session session) {
        Double[] complete = { TreatmentProcessTypeStatusRefBusinessBean.SUCCESSFUL_TREATMENT_PROCESS_STATUS, TreatmentProcessTypeStatusRefBusinessBean.UNSUCCESSFUL_TREATMENT_PROCESS_STATUS };
        ExpressionBuilder eb = new ExpressionBuilder();
        ReadObjectQuery mainQuery = new ReadObjectQuery(TreatmentProcess.class, eb);
        ExpressionBuilder maxActualDateEb = new ExpressionBuilder();
        ReportQuery maxActualEnd = new ReportQuery(TreatmentProcess.class, maxActualDateEb);
        maxActualEnd.addMaximum(TreatmentProcess.ACTUAL_END_DATE);
        maxActualEnd.setSelectionCriteria(maxActualDateEb.get(TreatmentProcess.PARENT_TREATMENT_PLAN).equal(plan).and(maxActualDateEb.get(TreatmentProcess.PROCESS_STATUS_REF).get(TreatmentProcessStatusRef.TREATPROC_STATUS_ID).in(complete)));
        mainQuery.setSelectionCriteria(eb.get(TreatmentProcess.ACTUAL_END_DATE).equal(maxActualEnd).and(eb.get(TreatmentProcess.PARENT_TREATMENT_PLAN).equal(plan)));
        addTreatmentProcessJoins(mainQuery);
        return (TreatmentProcess) session.executeQuery(mainQuery);
    }

    /**
     * Returns a query that gets all the treatment processes that have a status in the list of status ids provided.
     * @param account
     * @param statusIds
     * @return
     */
    private static ReadAllQuery getCurrentPlanProcessByStatusQuery(Account account, Double[] statusIds) {
    	ExpressionBuilder eb = new ExpressionBuilder();
    	// A plan with a null end date is an unfinished plan.
    	Expression currentPlan =
    		eb.get(TreatmentProcess.PARENT_TREATMENT_PLAN).get(AccountTreatmentPlan.ACCOUNT).get(Account.ACCOUNT_ID).equal(account.getAccountId()).and(
    				eb.get(TreatmentProcess.PARENT_TREATMENT_PLAN).get(AccountTreatmentPlan.TREATMENT_END_DATE).isNull());
    	Expression process = eb.get(TreatmentProcess.PROCESS_STATUS_REF).get(TreatmentProcessStatusRef.TREATPROC_STATUS_ID).in(statusIds);
    	ReadAllQuery roq = new ReadAllQuery(TreatmentProcess.class, currentPlan.and(process));
    	addTreatmentProcessJoins(roq);
    	return roq;
    }

    /**
	 * @param roq
	 */
	private static void addTreatmentProcessJoins(ObjectLevelReadQuery roq) {
		roq.addJoinedAttribute(TreatmentProcess.PROCESS_STATUS_REF);
    	roq.addJoinedAttribute(TreatmentProcess.PROCESS_TYPE_ID_REF);
    	roq.addJoinedAttribute(TreatmentProcess.TREATPROC_SUBTYPE_REF);
    	roq.addJoinedAttribute(TreatmentProcess.TREAT_PROCESS_TYPE_STATUS_REF);
	}

	/**
     * Closes this treatment plan (right now it just sets the end date to today's date).
     * Returns the working copy of the treatment plan in case you need it.
     * @param plan
     * @param handle
     * @return
     */
    private static AccountTreatmentPlan closeTreatmentPlan(AccountTreatmentPlan plan, UnitOfWork handle) {
        AccountTreatmentPlan planWCopy = (AccountTreatmentPlan) handle.registerObject(plan);
        planWCopy.setTreatmentEndDate(new Timestamp(new Date().getTime()));
        return planWCopy;
    }

    /**
     * Cancels a plan by first closing it and then cancelling all of the
     * ongoing treatment processes. If there is no current plan we return null
     * having done nothing.
     * @param account
     * @param handle
     * @return
     */
    public static AccountTreatmentPlan cancelCurrentTreatmentPlan(Account account, UnitOfWork handle) {
        throw new UnsupportedOperationException();
    }
    
    public static AccountTreatmentPlan createNewAccountTreatmentPlan(Account account, UnitOfWork handle) {
      return createNewAccountTreatmentPlan(account, new Date(), handle);
    }

    public static AccountTreatmentPlan createNewAccountTreatmentPlan(Account account, Date openDate, UnitOfWork handle) {
      throw new UnsupportedOperationException();
    }

    /**
     * This method used to always advance the stage when the stream changed,
     * that is no longer the case, it now really listens to the advance stage
     * param
     *
     * param passed in,
     * @param account
     * @param newStreamId
     * @param advanceStage
     * @param handle
     */
    public static void updateTreatmentStreamProgress(Account account, AccountTreatmentPlan currentPlan, Double newStreamId, boolean advanceStage, UnitOfWork handle) {
        log.debug(account.getAccountId() + ": Progressing account.");
        TreatmentStageRef newStageRef = null;
        // we also need to cover the case where there is no current stage, we advance from nothing
        if (advanceStage || account.getTreatmentStageRef() == null) {
            newStageRef = getNextTreatmentStage(account.getTreatmentStageRef(), handle);
            if (newStageRef == null){
                log.error(account.getAccountId() + ": Next stage was not found, attempt to advance beyond last stage");
				log.error(account.getAccountId() + ": Refusing to advance");
                newStageRef = account.getTreatmentStageRef();
            }else
                log.debug(account.getAccountId() + ": Advancing to " + newStageRef.getTreatmentStageCode());
        }
        Account accountWCopy = (Account) handle.registerExistingObject(account);
        // if we just created this plan its not existing yet.
        AccountTreatmentPlan planWCopy = (AccountTreatmentPlan) handle.registerObject(currentPlan);

        TreatmentStreamRef wcTreamentStreamRef = new TreatmentStreamRef();
        wcTreamentStreamRef.setTreatmentStreamId(newStreamId);
        wcTreamentStreamRef = (TreatmentStreamRef) handle.registerExistingObject(wcTreamentStreamRef);
        accountWCopy.setTreatmentStreamRef(wcTreamentStreamRef);
        planWCopy.setTreatmentStreamRef(wcTreamentStreamRef);
        if (newStageRef != null) {
            newStageRef = (TreatmentStageRef) handle.registerExistingObject(newStageRef);
            account.setTreatmentStageRef(newStageRef);
            accountWCopy.setTreatmentStageStartDate(new Timestamp(new Date().getTime()));
        }
        log.debug(account.getAccountId() + ": Progressed (stream " + newStreamId + ")");
    }

    /**
     * TODO: Assumes ordering by ID is ok for treatment stages!
     * @param session
     * @return
     */
    public static List getTreatmentStagesInOrder(Session session) {
    	ReadAllQuery roq;
   		roq = new ReadAllQuery(TreatmentStageRef.class);
    	roq.addAscendingOrdering(TreatmentStageRef.TREATMENT_STAGE_ID);
    	roq.setName("Get All Treatment Stages");
    	return TopLinkQuery.asList(roq, session);
    }

    private static TreatmentStageRef getNextTreatmentStage(TreatmentStageRef ref, Session session) {
        ExpressionBuilder eb = new ExpressionBuilder();
        ReadAllQuery roq;
        if (ref != null) {
            Expression exp1 = eb.get(TreatmentStageRef.TREATMENT_STAGE_ID).greaterThan(ref.getTreatmentStageId());
            roq = new ReadAllQuery(TreatmentStageRef.class, exp1);
        } else
            roq = new ReadAllQuery(TreatmentStageRef.class);
        roq.addAscendingOrdering(TreatmentStageRef.TREATMENT_STAGE_ID);
        roq.setName("Get Next Treatment Stage");
        return (TreatmentStageRef) TopLinkQuery.getSingleRowQueryResult(roq, session);
    }
    
    /**
     * @deprecated You should pass in an effective date!
     */
    public static TreatmentProcess addTreatmentProcess(Account account, AccountTreatmentPlan currentPlan, TreatprocSubtypeRef subType, TreatprocTemplate template, User user, int attempt, UnitOfWork handle) {
      return addTreatmentProcess(account, currentPlan, subType, template, user, attempt, new Date(), handle);
    }

    public static TreatmentProcess addTreatmentProcess(Account account, AccountTreatmentPlan currentPlan, TreatprocSubtypeRef subType, TreatprocTemplate template, User user, int attempt, Date effectiveDate, UnitOfWork handle) {
        TreatmentProcessStatusRef processStatus = new TreatmentProcessStatusRef();
        processStatus.setTreatprocStatusId(TreatmentProcessTypeStatusRefBusinessBean.IN_PROGRESS_TREATMENT_PROCESS_STATUS);
        TreatProcessTypeStatusRef processTypeStatus = getTypeStatus(TreatmentProcessTypeStatusRefBusinessBean.IN_PROGRESS_TREATMENT_PROCESS_STATUS, subType.getTreatprocTypeId(), handle);
        TreatmentProcessTypeRef processType = new TreatmentProcessTypeRef();
        processType.setTreatprocTypeId(subType.getTreatprocTypeId());
        return addNewTreatmentProcess(account, currentPlan, subType, template, user, attempt, processStatus, processTypeStatus, processType, effectiveDate, handle);

    }

    public static TreatmentProcess addNewTreatmentProcess(
            Account account,
            AccountTreatmentPlan plan,
            TreatprocSubtypeRef processSubtype,
            TreatprocTemplate template,
    		User creatingUser,
    		int attempt,
    		TreatmentProcessStatusRef processStatus, 
    		TreatProcessTypeStatusRef processTypeStatus,
    		TreatmentProcessTypeRef processType,
    		Date effectiveDate, 
            UnitOfWork handle) {
        //
      log.debug(account.getAccountId() + ": Adding process " + processSubtype.getTreatprocTypeDesc() + "(ID: " + processSubtype.getTreatprocSubtypeId() + ")");
      log.debug(account.getAccountId() + ": Process base-type is " + processSubtype.getTreatprocTypeId());
      if (processSubtype.getTreatprocTypeId().intValue() == NONE_TREATMENT_PROCESS) {
          log.error(account.getAccountId() + ": Process to be created is the NONE type, aborting");
          return null;
      }
      if (plan == null){
          log.error(account.getAccountId() + ": Account has no open treatment plan, refusing to create treatment!");
          return null;
      } else
          log.debug(account.getAccountId() + ": Current plan is " + plan.getTreatmentPlanId());
        if (processTypeStatus == null)
            throw new RuntimeException(account.getAccountId() + ": was assigned a treatment type with no in-progress status! (" + processSubtype.getTreatprocTypeId() + ")");
        long effectiveDateMillis = effectiveDate.getTime();
        Timestamp expectedStart = new java.sql.Timestamp(effectiveDateMillis);
        Timestamp expectedEnd = new java.sql.Timestamp(effectiveDateMillis);
        Template documentTemplate = null;
        if (template != null) {
            int days = template.getDaysDuration().intValue();
            Calendar cal = Calendar.getInstance();
            cal.setTime(expectedStart);
            cal.add(Calendar.DATE, days);
            expectedEnd = new Timestamp(cal.getTimeInMillis());
            if (template.getDocumentTemplate() != null)
                documentTemplate = template.getDocumentTemplate();
        } else
            log.error(account.getAccountId() + ": no template for subtype (" + processSubtype.getTreatprocSubtypeId() + ")");
        TreatmentProcess newProcess = (TreatmentProcess) handle.registerNewObject(getNewProcess(processSubtype.getTreatprocTypeId())); // This is just hiding a big switch statement
        newProcess.setExpectedStartDate(expectedStart);
        newProcess.setExpectedEndDate(expectedEnd);
        newProcess.setProcessStatusRef((TreatmentProcessStatusRef) handle.registerExistingObject(processStatus));
        newProcess.setTreatProcessTypeStatusRef((TreatProcessTypeStatusRef) handle.registerExistingObject(processTypeStatus));
        newProcess.setProcessTypeIdRef((TreatmentProcessTypeRef) handle.registerExistingObject(processType));
        newProcess.setTreatprocSubtypeRef((TreatprocSubtypeRef) handle.registerExistingObject(processSubtype));
        newProcess.setCreatedUser((User) handle.registerExistingObject(creatingUser));
        newProcess.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        newProcess.setParentTreatmentPlan((AccountTreatmentPlan) handle.registerExistingObject(plan));
        newProcess.setManual(Boolean.FALSE);
        newProcess.setAttemptNumber(new Integer(attempt));
        if (documentTemplate != null)
            newProcess.setTemplate((Template) handle.registerExistingObject(documentTemplate));
        // Plan's stream should never be null. PERIOD
        if (plan.getTreatmentStreamRef() == null) {
            log.error(account.getAccountId() + ": has a open plan with a null stream! Will fix in this transaction.");
            TreatmentStreamRef streamRef = account.getTreatmentStreamRef();
            if (streamRef == null)
                log.error(account.getAccountId() + ": Account ID Link also has null stream! New process will not have an associated stream!.");
            else
                newProcess.setTreatmentStreamRef((TreatmentStreamRef) handle.registerExistingObject(streamRef));
        } else
            newProcess.setTreatmentStreamRef((TreatmentStreamRef) handle.registerExistingObject(plan.getTreatmentStreamRef()));
        // An account's stage should never be null, if has an open plan -- we update it to the 1st stage in that case.
        if (account.getTreatmentStageRef() == null) {
            log.error(account.getAccountId() + ": has a null stage! Will fix in this transaction.");
            TreatmentStageRef firstStageWCopy = (TreatmentStageRef) handle.registerExistingObject(getNextTreatmentStage(null, handle));
            Account accountWCopy = (Account) handle.registerExistingObject(account);
            accountWCopy.setTreatmentStageRef(firstStageWCopy);
            log.debug(account.getAccountId() + ": stage will be updated in this transaction (ID: " + firstStageWCopy.getTreatmentStageId() + ")");
            newProcess.setTreatmentStageRef(firstStageWCopy);
        } else {
            newProcess.setTreatmentStageRef((TreatmentStageRef) handle.registerExistingObject(account.getTreatmentStageRef()));
        }
        log.debug(account.getAccountId() + ": treatment process added sucessfully");
        return newProcess;
    }

    private static TreatmentProcess getNewProcess(Double type) {
        int processType = type.intValue();
        TreatmentProcess newProcess;
        switch (processType) {
            case PAYMENT_PLAN_TREATMENT_PROCESS : //TODO: Should never happen, right?
                newProcess = new PaymentPlan();
                break;
            case PLACE_A_CALL_TREATMENT_PROCESS :
                newProcess = new PlaceACall();
                break;
            case SMS_TREATMENT_PROCESS :
                newProcess = new SmsMessage();
                break;
            default :
            	// When fixing bug [ #590 ] Treatment Assignment - BAU 1 Rule 07 - \
            	// Block D the Card 7 days -batch file shows exception.
            	// for no block card creation in RPM added this fail-fast behviour (which is better)
                log.error("New Treatment process creation failed with ID: " + type);
                throw new RuntimeException("New Treatment process creation failed with ID: " + type + ", unrecognized type");
        }
        return newProcess;
    }

    /**
     * @param session
     * @return
     */
    public static User getSystemUser(Session session) {
        ReadAllQuery q = new ReadAllQuery(User.class);
        q.setName("Get System User");
        q.setSelectionCriteria(new ExpressionBuilder().get(User.USER_ID).equal(SYSTEM_USER_ID));
        return (User) TopLinkQuery.getSingleRowQueryResult(q, session);
    }

    /**
     * Gets the status for a given type of process by mapping it back from the base
     * process status passed in and the type id. Although this can return mulitple values
     * this method will only return 1, it should be used only for Statii were we know that
     * there <b>should</b> be a 1 to 1 relationship (in-progress is an example) or if we
     * don't really care.
     * @param processStatus
     * @param processType
     * @param session
     * @return
     */
    public static TreatProcessTypeStatusRef getTypeStatus(Double processStatus, Double processType, Session session) {
        ReadAllQuery q = new ReadAllQuery(TreatProcessTypeStatusRef.class);
        ExpressionBuilder eb = new ExpressionBuilder();
        Expression expression = eb.get(TreatProcessTypeStatusRef.TREATPROC_TYPE_ID).equal(processType);
        expression = expression.and(eb.get(TreatProcessTypeStatusRef.TREATPROC_STATUS_ID).equal(processStatus));
        q.setSelectionCriteria(expression);
        q.setName("Get Treatment Type Status");
        return (TreatProcessTypeStatusRef) TopLinkQuery.getSingleRowQueryResult(q, session);
    }

    public static Vector getAllCustomerPrimaryAccounts(Customer customer, Session session) {
    	ExpressionBuilder eb = new ExpressionBuilder();
    	ReadAllQuery rq = new ReadAllQuery(Account.class, eb.get(Account.CUSTOMER).equal(customer).and(eb.get(Account.PARENT_ACCOUNT).isNull()));
    	rq.addJoinedAttribute(eb.getAllowingNull(Account.PROFILE_SEGMENT_REF));
    	rq.addJoinedAttribute(eb.getAllowingNull(Account.TREATMENT_STAGE_REF));
    	rq.addJoinedAttribute(eb.getAllowingNull(Account.TREATMENT_STREAM_REF));
    	Vector accounts = (Vector) session.executeQuery(rq);
//    	ReportQuery rq = new ReportQuery(Account.class, eb.get(Account.CUSTOMER).equal(customer).and(eb.get(Account.PARENT_ACCOUNT).isNull()));
//    	rq.addAttribute(Account.ACCOUNT_ID);
//    	Vector result = (Vector) session.executeQuery(rq);
//        Vector accounts = new Vector();
//        for(int i=0;i<result.size();i++){
//        	ReportQueryResult r = (ReportQueryResult) result.get(i);
//        	accounts.add(getAccount((Double) r.getByIndex(0),session));
//        }
        return accounts;
    }

    public static Customer getCustomer(String customerId, Session session) {
        log.debug(customerId + ": getting customer data.");
        ExpressionBuilder eb = new ExpressionBuilder();
        Expression exp1 = eb.get(Customer.CUSTOMER_ID).equal(customerId);
        ReadObjectQuery roq = new ReadObjectQuery(Customer.class, exp1);
        // This might speed things up if he has only 1 account.
        roq.addJoinedAttribute(eb.anyOfAllowingNone(Customer.ACCOUNT));
        return (Customer) session.executeQuery(roq);
    }

    /**
     * Assigns all of the accounts in the vector to the worklist by ID.
     * If the id is null the accounts are removed from any existing
     * worklist they might be in.
     * @param accounts
     * @param workListId
     * @param session
     */
    public static void assignToWorklist(Vector accounts, Double workListId, Session session) {
        Iterator iter = accounts.iterator();
        UnitOfWork handle = session.acquireUnitOfWork();
        WorkList workListWCopy = null;
        if (workListId != null) {
            WorkList workList = new WorkList();
            workList.setWorkListId(workListId);
            workListWCopy = (WorkList) handle.registerExistingObject(workList);
        }
        while (iter.hasNext()) {
            Account account = (Account) iter.next();
            WorkList currentWorkList = account.getWorklist();
            if (currentWorkList == null || workListWCopy == null)
                continue;
            if (currentWorkList.getWorkListId().equals(workListId))
                continue;
            if (getCurrentTreatmentProcess(account, session) != null) {
                // TODO: Do we need to do WorkList History?
                Account accountWCopy = (Account) handle.registerExistingObject(account);
                accountWCopy.setWorklist(workListWCopy);
            }
        }
        handle.commit();
    }


    //	TODO: Cycle start for a given cycle, only works for HLBB
    public static Date getCycleStart(int cyclesAgo) {
        Calendar c = Calendar.getInstance();
        c.setLenient(true);
        c.add(Calendar.MONTH, -cyclesAgo);
        return new Date(c.getTimeInMillis());
    }

    //	TODO: Cycle end for a given cycle, only works for HLBB
    public static Date getCycleEnd(int cyclesAgo) {
        Calendar c = Calendar.getInstance();
        c.setLenient(true);
        c.add(Calendar.MONTH, - (cyclesAgo - 1));
        return new Date(c.getTimeInMillis());
    }

    /**
     * An example: Cycle date 12, current date 06/08/03
     * Create a cal object to represent 12/08/03, once we see that it is already
     * past today subtract a month to get 12/07/03, then take the difference in days:
     * days in month - cycledate + current date <br/>
     * (31 - 12) + 6 <br/>
     * Cycle date 6, current date 12/08/03, just subtract the days: currentdate - cycledate <br/>
     * 12 -6
     *
     * @param cycleDate
     * @return
     */
    public static int daysSince(int cycleDate) {
        Calendar cyc = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        cyc.set(Calendar.DATE, cycleDate);
        if (cyc.after(now)) {
            // We need lenient true so that we can wrap year with the -1 on month
            cyc.setLenient(true);
            cyc.set(Calendar.MONTH, cyc.get(Calendar.MONTH) - 1);
            return cyc.getActualMaximum(Calendar.MONTH) - cycleDate + now.get(Calendar.DATE);
        } else
            return now.get(Calendar.DATE) - cycleDate;
    }

    /**
     * Basically the same as daysSince...
     *
     * @param cycleDate
     * @return
     */
    public static int daysUntil(int cycleDate) {
        Calendar cyc = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        cyc.set(Calendar.DATE, cycleDate);
        if (cyc.before(now)) {
            return now.getActualMaximum(Calendar.MONTH) - now.get(Calendar.DATE) + cycleDate;
        } else
            return cycleDate - now.get(Calendar.DATE);
    }

    public static void updateCustomerProfile(Customer c, int rockId, double weightedDue, Session session) {
        UnitOfWork handle = session.acquireUnitOfWork();
        Customer customerWCopy = (Customer) handle.registerObject(c);
        ProfileSegmentRef profileSegment = null;
        if (rockId != NONE_PROFILE) {
            profileSegment = new ProfileSegmentRef();
            profileSegment.setProfileId(new Double(rockId));
            profileSegment = (ProfileSegmentRef) handle.registerExistingObject(profileSegment);
        }
        customerWCopy.setProfileSegmentRef(profileSegment);
        customerWCopy.setRecoveryPotentialAmount(new Double(weightedDue));
        handle.commit();
    }

    public static void updateCustomerTreatmentStage(Customer c, int stageId, UnitOfWork handle) {
        Customer customerWCopy = (Customer) handle.registerExistingObject(c);
        TreatmentStageRef treatmentStage = null;
        if (stageId != NONE_TREATMENT_STAGE) {
            treatmentStage = new TreatmentStageRef();
            treatmentStage.setTreatmentStageId(new Double(stageId));
            treatmentStage = (TreatmentStageRef) handle.registerExistingObject(treatmentStage);
        }
        customerWCopy.setTreatmentStageRef(treatmentStage);
    }

    public static Vector getProfileSegments(Session session) {
        ReadAllQuery q = new ReadAllQuery(ProfileSegmentRef.class);
        q.addDescendingOrdering(ProfileSegmentRef.MINIMUM_SCORE);
        return (Vector) session.executeQuery(q);
    }

    public static Vector getRiskLevels(Session session) {
        ReadAllQuery q = new ReadAllQuery(RiskLevelRef.class);
        q.addDescendingOrdering(RiskLevelRef.MINIMUM_SCORE);
        return (Vector) session.executeQuery(q);
    }

    public static double getAverageCustomerCollectability(WorkList wl, Session session) {
        ExpressionBuilder eb = new ExpressionBuilder();
        Expression e = eb.get(Customer.WORKLIST).equal(wl);
        ReportQuery q = new ReportQuery(Customer.class, e);
        q.addAverage(ProfileSegmentRef.RECOVERY_POTENTIAL, eb.get(Customer.PROFILE_SEGMENT_REF).get(ProfileSegmentRef.RECOVERY_POTENTIAL));
        double results[] = { 0 };
        q.setName("Get Worklist Average Customer Collectability");
        return TopLinkQuery.getSingleRowQueryResult(results, q, session)[0];
    }

    public static double getAverageDueAmount(WorkList wl, Session session) {
        ExpressionBuilder eb = new ExpressionBuilder();
        Expression e = eb.get(Account.CUSTOMER).get(Customer.WORKLIST).equal(wl);
        e = e.and(eb.get(Account.PARENT_ACCOUNT).isNull());
        ReportQuery q = new ReportQuery(Account.class, e);
        q.addAverage(Account.CURR_DUE_AMT);
        double results[] = { 0 };
        q.setName("Get Worklist Average Due Amount");
        return TopLinkQuery.getSingleRowQueryResult(results, q, session)[0];
    }

    public static List getTeams(TreatmentStageRef stage, Session session) {
        ExpressionBuilder eb = new ExpressionBuilder();
        ReadAllQuery q = new ReadAllQuery(UserTeams.class);
        q.setSelectionCriteria(eb.get(UserTeams.TREATMENT_STAGE_REF).get(TreatmentStageRef.TREATMENT_STAGE_ID).equal(stage.getTreatmentStageId()));
        q.setName("Get Stage Teams");
        return TopLinkQuery.asList(q, session);
    }

    public static List getAllTreatmentStages(Session session) {
        ReadAllQuery roq = new ReadAllQuery(TreatmentStageRef.class);
        roq.addAscendingOrdering(TreatmentStageRef.TREATMENT_STAGE_ID);
        roq.setName("Get All Treatment Stages");
        return TopLinkQuery.asList(roq, session);
    }

    public static List getNormalWorkLists(TreatmentStageRef stage, Session session) {
		ExpressionBuilder workListBuilder = new ExpressionBuilder(WorkList.class);
		ExpressionBuilder blockCodeBuilder = new ExpressionBuilder(WorkListBlockCode.class);
		ReportQuery subQuery = new ReportQuery(WorkListBlockCode.class,blockCodeBuilder);
		subQuery.addAttribute(WorkList.WORK_LIST_ID,blockCodeBuilder.get(WorkListBlockCode.WORKLIST).get(WorkList.WORK_LIST_ID));
		Expression exp = workListBuilder.get(WorkList.WORK_LIST_ID).in(subQuery).not();
		exp = exp.and(workListBuilder.get(WorkList.TREATMENT_STAGE_REF).equal(stage));
		exp = exp.and(workListBuilder.get(WorkList.AGENCY_TYPE_REF).isNull());
		exp = exp.and(workListBuilder.get(WorkList.DISABLE).equal(1).not());
		ReadAllQuery query = new ReadAllQuery(WorkList.class,exp);
		query.setName("Get Normal Worklists");
        return TopLinkQuery.asList(query, session);
    }

    public static void removeUserWorklistAssignments(WorkList wl, Session session) {
    	Expression e = new ExpressionBuilder().get(UserWorkListAssign.WORK_LIST).equal(wl);
        Vector list = (Vector) session.executeQuery(new ReadAllQuery(UserWorkListAssign.class,e));
        UnitOfWork handle = session.acquireUnitOfWork();
        handle.deleteAllObjects(list);
        handle.commit();
    }

    public static void setWorklists(UserTeams team, List worklists, Session session) {
        Iterator i = worklists.iterator();
        while (i.hasNext()) {
            WorkList wl = (WorkList) i.next();
            UnitOfWork handle = session.acquireUnitOfWork();
			Iterator i1 = team.getUsers().iterator();
			while (i1.hasNext()) {
				User u = (User) i1.next();
				if (getUserWorkListAssignment(u, wl, session) != null) continue;
			    UserWorkListAssign assignment = (UserWorkListAssign) handle.registerNewObject(new UserWorkListAssign());
			    assignment.setAssigner((User) handle.registerExistingObject(getSystemUser(session)));
			    assignment.setUser((User) handle.registerExistingObject(u));
			    assignment.setWorkList((WorkList) handle.registerExistingObject(wl));
			}
			handle.commit();
        }
    }

    private static UserWorkListAssign getUserWorkListAssignment(User u, WorkList wl, Session session) {
    	ExpressionBuilder eb = new ExpressionBuilder();
    	ReadObjectQuery q = new ReadObjectQuery(UserWorkListAssign.class);
    	q.setSelectionCriteria(eb.get(UserWorkListAssign.USER).equal(u).and(eb.get(UserWorkListAssign.WORK_LIST).get(WorkList.WORK_LIST_ID).equal(wl.getWorkListId())));
    	q.setName("Get User WorkList Assignment");
		return (UserWorkListAssign) TopLinkQuery.asObject(q, session);
	}

	public static CursoredStream getCustomerStream(String customerId, String endId, int batchSize, Session session) {
        ExpressionBuilder eb = new ExpressionBuilder(Customer.class);
        ReadAllQuery raq = new ReadAllQuery(Customer.class, eb);
        Expression e = null;
        Expression e2 = null;
        if (customerId != null)
            e = eb.get(Customer.CUSTOMER_ID).greaterThanEqual(customerId);
        if (endId!=null)
        	e2 = eb.get(Customer.CUSTOMER_ID).lessThanEqual(endId);
        // This might speed things up if he has only 1 account.
        //raq.addBatchReadAttribute(eb.anyOfAllowingNone("account"));
        if (e==null && e2 != null) e = e2;
        if (e !=null && e2!=null) e = e.and(e2);
        if (e!=null)
        	raq.setSelectionCriteria(e);
        raq.addAscendingOrdering(Customer.CUSTOMER_ID);
        raq.useCursoredStream(batchSize, batchSize);
        return (CursoredStream) session.executeQuery(raq);
    }

    /**
     * Selects all customers who have 1 or more delq accounts OR have
     * 1 or more accounts with treatment plans open (if the account
     * has an open plan but is not delq I assume the ensuing program
     * will close the plan)
     * <b>NOTE:</b>For pref sensitive stuff ensure that this doesn not
     * join on account, and if you plan to get the accounts don't access
     * the account attrib since it will pull down an ineffient Account
     * (no pre-joins)
     * @param startId
     * @param endId
     * @param session
     * @return
     */
    public static CursoredStream getTreatableCustomerStream(String startId, String endId, Session session){
    	ExpressionBuilder eb = new ExpressionBuilder(Customer.class);
    	ReadAllQuery raq = new ReadAllQuery(Customer.class, eb);
    	Expression e = null;
    	Expression e2 = null;
    	if (startId != null)
    		e = eb.get(Customer.CUSTOMER_ID).greaterThanEqual(startId);
    	if (endId!=null)
    		e2 = eb.get(Customer.CUSTOMER_ID).lessThanEqual(endId);
    	e = TopLinkQuery.andExpressions(e, e2);
    	ExpressionBuilder accountEb = new ExpressionBuilder(Account.class);
    	ReportQuery accountReport = new ReportQuery(Account.class, accountEb);
    	accountReport.addAttribute(Account.ACCOUNT_ID);
    	Expression accountCriteria = accountEb.get(Account.CUSTOMER).equal(eb);
    	ExpressionBuilder planEb = new ExpressionBuilder(AccountTreatmentPlan.class);
    	ReportQuery planQuery = new ReportQuery(AccountTreatmentPlan.class, planEb);
    	planQuery.addAttribute(AccountTreatmentPlan.TREATMENT_PLAN_ID);
    	planQuery.setSelectionCriteria(planEb.get(AccountTreatmentPlan.ACCOUNT).equal(accountEb).and(planEb.get(AccountTreatmentPlan.TREATMENT_END_DATE).isNull()));
    	// The OR condition is 'inside' the AND so there is no precedence problems here.
    	accountCriteria = accountCriteria.and(accountEb.get(Account.CYC_DEL_ID).greaterThan(0).or(accountEb.exists(planQuery)));
    	accountReport.setSelectionCriteria(accountCriteria);
    	e = TopLinkQuery.andExpressions(e, eb.exists(accountReport));
    	raq.setSelectionCriteria(e);
    	raq.addAscendingOrdering(Customer.CUSTOMER_ID);
    	return TopLinkQuery.asCursoredStream(raq,1000,1000,session);
    }

    public static Iterator getDelinquentAccountStreamWithoutPlansByCustomer(String strategy, String startId, String endId, Session session){
    	ExpressionBuilder eb = new ExpressionBuilder(Account.class);
    	ReadAllQuery raq = new ReadAllQuery(Account.class, eb);
    	Expression e = null;
    	Expression e2 = null;
    	if (startId != null)
    		e = eb.get(Account.CUSTOMER).get(Customer.CUSTOMER_ID).greaterThanEqual(startId);
    	if (endId!=null)
    		e2 = eb.get(Account.CUSTOMER).get(Customer.CUSTOMER_ID).lessThanEqual(endId);
    	e = TopLinkQuery.andExpressions(e, e2);
    	e = TopLinkQuery.andExpressions(e, eb.get(Account.CYC_DEL_ID).greaterThan(0));
    	e = TopLinkQuery.andExpressions(e, eb.get(Account.PARENT_ACCOUNT).isNull());
    	ExpressionBuilder planBuilder = new ExpressionBuilder(AccountTreatmentPlan.class);
    	ReportQuery rq = new ReportQuery(AccountTreatmentPlan.class, planBuilder);
    	rq.setSelectionCriteria(planBuilder.get(AccountTreatmentPlan.TREATMENT_END_DATE).isNull().and(planBuilder.get(AccountTreatmentPlan.ACCOUNT).equal(eb)));
    	rq.addAttribute(AccountTreatmentPlan.TREATMENT_PLAN_ID);
    	e = TopLinkQuery.andExpressions(e, eb.notExists(rq));
    	raq.setSelectionCriteria(e);
    	raq.addJoinedAttribute(eb.getAllowingNull(Account.PROFILE_SEGMENT_REF));
    	raq.addAscendingOrdering(Account.ACCOUNT_ID);
    	raq.setName("Get Delinquent Account Stream Without Plans By Customer");
    	return TopLinkQuery.asIterator(strategy, raq, 1000, session);
    }

    public static Iterator getDelinquentAccountPlansByCustomer(String strategy, String startId, String endId, int batchSize, Session session){
    	ExpressionBuilder eb = new ExpressionBuilder(AccountTreatmentPlan.class);
    	ReadAllQuery raq = new ReadAllQuery(AccountTreatmentPlan.class, eb);
    	Expression e = null;
    	Expression e2 = null;
    	if (startId != null)
    		e = eb.get(AccountTreatmentPlan.ACCOUNT).get(Account.CUSTOMER).get(Customer.CUSTOMER_ID).greaterThanEqual(startId);
    	if (endId!=null)
    		e2 = eb.get(AccountTreatmentPlan.ACCOUNT).get(Account.CUSTOMER).get(Customer.CUSTOMER_ID).lessThanEqual(endId);
    	e = TopLinkQuery.andExpressions(e, e2);
		e = TopLinkQuery.andExpressions(e, eb.get(AccountTreatmentPlan.TREATMENT_END_DATE).isNull());
    	e = TopLinkQuery.andExpressions(e, eb.get(AccountTreatmentPlan.ACCOUNT).get(Account.CYC_DEL_ID).greaterThan(0));
    	e = TopLinkQuery.andExpressions(e, eb.get(AccountTreatmentPlan.ACCOUNT).get(Account.PARENT_ACCOUNT).isNull());
    	raq.setSelectionCriteria(e);
    	raq.addJoinedAttribute(eb.get(AccountTreatmentPlan.ACCOUNT));
    	raq.addJoinedAttribute(eb.get(AccountTreatmentPlan.ACCOUNT).getAllowingNull(Account.PROFILE_SEGMENT_REF));
    	raq.addAscendingOrdering(AccountTreatmentPlan.TREATMENT_PLAN_ID);
    	raq.setName("Get Delinquent Account Plan Stream By Customer");
    	return TopLinkQuery.asIterator(strategy, raq, batchSize, session);
    }


    public static TreatprocSubtypeRef getNoneTreatmentSubtype(Session session) {
		ExpressionBuilder eb = new ExpressionBuilder();
		TreatprocSubtypeRef st = (TreatprocSubtypeRef) session.executeQuery(new ReadObjectQuery(TreatprocSubtypeRef.class,eb.get(TreatprocSubtypeRef.TREATPROC_TYPE_ID).equal(NONE_TREATMENT_PROCESS)));
		return st;
    }

	/**
	 * @param session
	 * @return
	 */
	public static CursoredStream getPrimaryAccountStream(double startingid, double endingId, int batchSize,  boolean doJoins, Session session) {
		ExpressionBuilder eb = new ExpressionBuilder();
		Expression e = eb.get(Account.PARENT_ACCOUNT).isNull().and(eb.get(Account.ACCOUNT_ID).greaterThanEqual(startingid));
		if (endingId >= startingid)
			e = e.and(eb.get(Account.ACCOUNT_ID).lessThanEqual(endingId));
		ReadAllQuery query = new ReadAllQuery(Account.class, e);
		query.useCursoredStream(batchSize, batchSize);
		query.addAscendingOrdering(Account.ACCOUNT_ID);
		return (CursoredStream) session.executeQuery(query);
	}
	/**
	 * @param customerId
	 * @param batchSize
	 * @param session
	 * @return
	 */
	public static Vector getCustomerBatch(String customerId, int batchSize, Session session) {
		ExpressionBuilder eb = new ExpressionBuilder(Customer.class);
		ReadAllQuery raq = new ReadAllQuery(Customer.class, eb);
		if (customerId != null)
			raq.setSelectionCriteria(eb.get(Customer.CUSTOMER_ID).greaterThanEqual(customerId));
		raq.addAscendingOrdering(Customer.CUSTOMER_ID);
		raq.setMaxRows(batchSize);
		return (Vector) session.executeQuery(raq);
	}

	/**
	 * @param typeId
	 * @param session
	 * @return
	 */
	public static List getTreamentSubtypes(Double typeId, Session session) {
		ExpressionBuilder eb = new ExpressionBuilder();
		ReadAllQuery raq = new ReadAllQuery(TreatprocSubtypeRef.class,eb.get(TreatprocSubtypeRef.TREATPROC_TYPE_ID).equal(typeId));
		return (Vector) session.executeQuery(raq);
	}
	public static TreatmentProcess getLastTreatmentProcessNotInStream(AccountTreatmentPlan plan, Double streamId, Session tlSession) {
		ReadAllQuery q = new ReadAllQuery(TreatmentProcess.class);
		ExpressionBuilder eb = new ExpressionBuilder();
		Expression e = eb.get(TreatmentProcess.PARENT_TREATMENT_PLAN).equal(plan).and(eb.get(TreatmentProcess.TREATMENT_STREAM_REF).get(TreatmentStreamRef.TREATMENT_STREAM_ID).notEqual(streamId));
		q.setSelectionCriteria(e);
		q.addDescendingOrdering(TreatmentProcess.ACTUAL_END_DATE);
		List l = TopLinkQuery.asList(q, tlSession);
		if (l ==null || l.size() == 0) return null;
		return (TreatmentProcess) l.get(0);
	}

	/**
	 * @param session
	 * @return a query result, each row being {String, Double} the String is the Customer IDand the Double the block code ID
	 */
	public static List getAccountsForBlockCodeWorkLists(Session session) {
		ExpressionBuilder eb = new ExpressionBuilder();
		ReportQuery q = new ReportQuery(Customer.class, eb);
		ReportQuery subQ = new ReportQuery(WorkListBlockCode.class, new ExpressionBuilder());
		subQ.addAttribute(BlockCodeRef.BLOCK_CODE_ID,subQ.getExpressionBuilder().get(WorkListBlockCode.BLOCK_CODE_REF).get(BlockCodeRef.BLOCK_CODE_ID));
		Expression  e = eb.anyOf(Customer.ACCOUNT).get(Account.BLOCK_CODE_REF).get(BlockCodeRef.BLOCK_CODE_ID).in(subQ);
		q.setSelectionCriteria(e);
		q.addAttribute(Customer.CUSTOMER_ID);
		q.addAttribute(Account.ACCOUNT_ID, eb.anyOf(Customer.ACCOUNT).get(Account.ACCOUNT_ID));
		q.addAttribute(BlockCodeRef.BLOCK_CODE_ID, eb.anyOf(Customer.ACCOUNT).get(Account.BLOCK_CODE_REF).get(BlockCodeRef.BLOCK_CODE_ID));
		q.addAscendingOrdering(Customer.CUSTOMER_ID);
		return TopLinkQuery.asList(q, session);
	}

	/**
	 * @param session
	 * @return
	 */
	public static List getTreatmentStreams(Session session) {
		ReadAllQuery raq = new ReadAllQuery(TreatmentStreamRef.class);
		raq.setName("Get All Treatment Streams");
		return TopLinkQuery.asList(raq, session);
	}

	/**
	 * @param s
	 * @return an array of 2 List objects, one containing the subtyperefs and the
	 * other containing the templates, or null in the case of no template.
	 */
	public static List[] getTreatmentSubtypeTemplates(Session s) {
		ReadAllQuery q = new ReadAllQuery(TreatprocSubtypeRef.class);
		q.setName("Get All Treatment Subtype Templates");
		List subTypes = new ArrayList();
		List templates = new ArrayList();
		for (Iterator i = TopLinkQuery.asList(q, s).iterator(); i.hasNext();) {
			TreatprocSubtypeRef str = (TreatprocSubtypeRef) i.next();
			subTypes.add(str);
			templates.add(s.executeQuery(new ReadObjectQuery(TreatprocTemplate.class, new ExpressionBuilder().get(TreatprocTemplate.TREATPROC_SUBTYPE_ID).equal(str.getTreatprocSubtypeId()))));
		}
		return new List[]{subTypes, templates};
	}

	/**
	 * @param session
	 * @return
	 */
	public static List getPendingAgencyWorklists(int typeId, Session session) {
		ReadAllQuery q = new ReadAllQuery(WorkList.class);
		q.setName("Get Pending Agency Worklists");
		ExpressionBuilder eb = new ExpressionBuilder();
		q.setSelectionCriteria(eb.get(WorkList.AGENCY_TYPE_REF).get(AgencyTypeRef.AGY_TYPE_ID).equal(typeId).and(eb.get(WorkList.FOR_UNASSIGNED).equal(1).and(eb.get(WorkList.DISABLE).equal(1).not())));
		return TopLinkQuery.asList(q,session);
	}

	/**
	 * @param session
	 * @return
	 */
	public static List getAssignedAgencyWorklists(int typeId, Session session) {
		ReadAllQuery q = new ReadAllQuery(WorkList.class);
		q.setName("Get Assigned Agency Worklists");
		ExpressionBuilder eb = new ExpressionBuilder();
		q.setSelectionCriteria(eb.get(WorkList.AGENCY_TYPE_REF).get(AgencyTypeRef.AGY_TYPE_ID).equal(typeId).and(eb.get(WorkList.FOR_UNASSIGNED).equal(0).and(eb.get(WorkList.DISABLE).equal(1).not())));
		return TopLinkQuery.asList(q,session);
	}

	/**
	 * @param list
	 * @return
	 */
	public static int getAccountCount(WorkList list, Session s) {
		ExpressionBuilder eb = new ExpressionBuilder();
		ReportQuery rq = new ReportQuery(Account.class, eb);
		rq.setSelectionCriteria(eb.get(Account.WORKLIST).equal(list));
		rq.addCount();
		rq.setName("Get Worklist Account Count");
		return (int)TopLinkQuery.getSingleRowQueryResult(new double[]{0}, rq, s)[0];
	}
}