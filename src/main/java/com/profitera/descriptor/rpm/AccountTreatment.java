package com.profitera.descriptor.rpm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import oracle.toplink.sessions.Session;

import com.profitera.descriptor.business.reference.TreatmentStreamReferenceBusinessBean;
import com.profitera.descriptor.db.account.Account;
import com.profitera.descriptor.db.account.AccountTreatmentPlan;
import com.profitera.descriptor.db.reference.ProfileSegmentRef;
import com.profitera.rpm.RPM;
import com.profitera.services.system.dataaccess.RPMDataManager;
import com.profitera.util.DateParser;
import com.profitera.util.PrimitiveValue;
import com.profitera.util.Stringifier;

public class AccountTreatment extends BaseDescriptor implements java.io.Serializable {
	private static final Map DEFAULT_VALUES = new HashMap();
	static{
		DEFAULT_VALUES.put("getLastTreatmentActionType",new Double(RPMDataManager.TREATMENT_PROCESS_TYPE_NONE));
        DEFAULT_VALUES.put("getCurrentTreatmentActionType",new Double(RPMDataManager.TREATMENT_PROCESS_TYPE_NONE));
        DEFAULT_VALUES.put("getDaysUntilCurrentTreatmentAction",new Double(0));
        DEFAULT_VALUES.put("getDaysSinceLastTreatmentAction",new Double(0));
	}
	
    private String accountNumber = "";
    private int treatmentStream = TreatmentStreamReferenceBusinessBean.TREATMENT_STREAM_NONE; //read/write
    private double totalOutstandingAmount; //read-only
    private int currentCyclesDelinquent; //read-only
	private int nextTreatmentActionType; // write-only
	private int daysSinceStartOfStream; // read-only
    private int treatmentStreamStage; // read-only
    private boolean advanceToNextStage; //write-only
    private int profile = RPMDataManager.ACCOUNT_PROFILE_NONE;
	private final Map overrideValues = new HashMap();
	private static final String ZERO_VALUE = "0.0";

    
    public AccountTreatment(Account account, Session tlSession) {
    	this(account, null, tlSession);
    }
    public AccountTreatment(Account account, AccountTreatmentPlan plan, Session tlSession) {
    	this(account, plan, new Date(), tlSession);
    }
    	
	public AccountTreatment(Account account, AccountTreatmentPlan plan, Date d, Session tlSession) {
    	super(RPM.TREATMENT_QUERY_FILE);
        session = tlSession;
        
		nextTreatmentActionType = RPMDataManager.TREATMENT_PROCESS_TYPE_NONE;
        setId(account.getAccountId());
        // Fix: [ #580 ] Treatment Assignment - Total Outstanding Amount Differs from database
        totalOutstandingAmount = PrimitiveValue.doubleValue(account.getOutstandingAmt(), 0);
        currentCyclesDelinquent = PrimitiveValue.intValue(account.getCycDelId(), 0);
        accountNumber = PrimitiveValue.stringValue(account.getAccountNumber(), "");
        if (plan == null) // this should almost never be called:
        	plan = RPMDataManager.getCurrentTreatmentPlan(account, tlSession);
        if (account.getTreatmentStageStartDate() == null)
        	daysSinceStartOfStream = 0;
        else
        	daysSinceStartOfStream = DateParser.getDaysDifference(account.getTreatmentStageStartDate(), d);
        
        treatmentStream =
        	account.getTreatmentStreamRef() == null
			? TreatmentStreamReferenceBusinessBean.TREATMENT_STREAM_NONE
			: account.getTreatmentStreamRef().getTreatmentStreamId().intValue();
        ProfileSegmentRef psr = account.getProfileSegmentRef();
        if (psr != null)
            profile = PrimitiveValue.intValue(psr.getProfileId(), RPMDataManager.ACCOUNT_PROFILE_NONE);
        if (account.getTreatmentStageRef()!=null)
        	treatmentStreamStage = account.getTreatmentStageRef().getTreatmentStageId().intValue();
    }
	
	/**
	 * @see com.profitera.descriptor.rpm.Descriptor#getValue(java.lang.String, java.lang.Object[])
	 */
	public String getValue(String propertyName, Object[] arguments) throws UnsupportedAttributeException {
		Object overrideValue = overrideValues.get(propertyName);
		if (overrideValue != null) return overrideValue.toString();
		String val = super.getValue(propertyName, arguments);
		Object defaultValue = DEFAULT_VALUES.get(propertyName);
		if (defaultValue == null) return val;
		if (val.equals(ZERO_VALUE)) return defaultValue.toString();
		return val;
	}
	
	public void overrideValue(String propertyName, Object value){
		overrideValues.put(propertyName, value);
	}
    
    public String getAccountNumber() {
        return accountNumber;
    }

    public int getTreatmentStream() {
        return treatmentStream;
    }

    public void setTreatmentStream(int i) {
        treatmentStream = i;
    }

    public double getTotalOutstandingAmount() {
        return totalOutstandingAmount;
    }

    public int getCurrentCyclesDelinquent() {
        return currentCyclesDelinquent;
    }

    public int getTreatmentStreamStage() {
        return treatmentStreamStage;
    }

    public int getProfile() {
        return profile;
    }
    
	public int getDaysSinceStartOfStream() {
		 return daysSinceStartOfStream;
	 }

	 public boolean getAdvanceToNextStage() {
		 return advanceToNextStage;
	 }

	 public void setAdvanceToNextStage(boolean b) {
		 advanceToNextStage = b;
	 }

	 public int getNextTreatmentActionType() {
		 return nextTreatmentActionType;
	 }

	 public void setNextTreatmentActionType(int i) {
		 nextTreatmentActionType = i;
	 }
	 
	 public int getLastTreatmentActionType(){
	 	return getValueByQuery("getLastTreatmentActionType", new Object[0], (Number)getId(), (Number)DEFAULT_VALUES.get("getLastTreatmentActionType") ,session).intValue();
	 }
	 
	 public int getDaysSinceLastTreatmentAction() {
	 	return getValueByQuery("getDaysSinceLastTreatmentAction", new Object[0], (Number)getId(), (Number)DEFAULT_VALUES.get("getDaysSinceLastTreatmentAction") ,session).intValue();
	 }
	 
	 public int getCurrentTreatmentActionType() {
	 	return getValueByQuery("getCurrentTreatmentActionType", new Object[0], (Number)getId(), (Number)DEFAULT_VALUES.get("getCurrentTreatmentActionType") ,session).intValue();
	 }
	 
	 public int getDaysUntilCurrentTreatmentAction() {
	 	return getValueByQuery("getDaysUntilCurrentTreatmentAction", new Object[0], (Number)getId(), (Number)DEFAULT_VALUES.get("getDaysUntilCurrentTreatmentAction") ,session).intValue();
	 }
	 
	 
    public String toString() {
        return Stringifier.stringify(this,new String[]{"getQBundle"});
    }
 

}