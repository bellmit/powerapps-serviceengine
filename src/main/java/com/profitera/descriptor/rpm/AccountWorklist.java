package com.profitera.descriptor.rpm;

import java.util.Vector;

import com.profitera.descriptor.db.account.Account;
import com.profitera.rpm.RPM;
import com.profitera.services.system.dataaccess.RPMDataManager;
import com.profitera.util.PrimitiveValue;
import com.profitera.util.Stringifier;

public class AccountWorklist extends BaseDescriptor
    implements java.io.Serializable {

    private String customerId = "";
    private String accountId = "";
    private String accountNumber = "";
	private double totalOutstandingAmount;
    private int worklist = RPMDataManager.NONE_WORKLIST_ID;
    private int profile;

    public AccountWorklist(Account account) {
    	super(RPM.ACCOUNT_QUERY_FILE);
		customerId = account.getCustomer().getCustomerId();
		accountId = account.getAccountId().toString();
		setId(accountId);
		accountNumber = account.getAccountNumber().toString();
		if (account.getProfileSegmentRef()!=null)
			profile = account.getProfileSegmentRef().getProfileId().intValue();
		totalOutstandingAmount =
					PrimitiveValue.doubleValue(account.getCurrDueAmt(), 0)
						+ PrimitiveValue.doubleValue(account.getTotalDelqAmt(), 0);
    }
    
    public AccountWorklist(Vector accounts){
    	super("rpm");
    	AccountWorklist[] worklistDescriptors = new AccountWorklist[accounts.size()];
    	for(int i=0;i<worklistDescriptors.length;i++){
    		worklistDescriptors[i] = new AccountWorklist((Account)accounts.get(i)); 
    	}
    	customerId = worklistDescriptors[0].getCustomerId();
    	setId(customerId);
    	
    	// The customer cycles delinquent is based on max of accounts
		// The customer profile is based on the min of all the accounts
		int minProfile = worklistDescriptors[0].getProfile();
		double maxOutstanding = 0;
    	for(int i=1;i<worklistDescriptors.length;i++){
    		if (worklistDescriptors[i].getProfile()<minProfile)
    			minProfile = worklistDescriptors[i].getProfile();
    		if (worklistDescriptors[i].getTotalOutstandingAmount()>maxOutstanding)
    			maxOutstanding = worklistDescriptors[i].getTotalOutstandingAmount();
    	}
    	totalOutstandingAmount = maxOutstanding;
    	profile = minProfile;
    }
    
	/**
	 * @see com.profitera.descriptor.rpm.BaseDescriptor#getSupportedProperties()
	 */
	protected String[] getSupportedProperties() {
		return new String[0];
	}

    public String toString() {
    	return Stringifier.stringify(this);
    }


    public String getAccountId() {
        return accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public int getProfile() {
        return profile;
    }

    public int getWorklist() {
        return worklist;
    }

    public void setWorklist(int d) {
        worklist = d;
    }

    public String getCustomerId() {
        return customerId;
    }

    public double getTotalOutstandingAmount() {
        return totalOutstandingAmount;
    }

}