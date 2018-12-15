package com.profitera.descriptor.rpm;

import oracle.toplink.sessions.Session;

import com.profitera.descriptor.db.account.Account;
import com.profitera.descriptor.db.reference.BillingCycleRef;
import com.profitera.rpm.RPM;
import com.profitera.services.system.dataaccess.RPMDataManager;
import com.profitera.util.PrimitiveValue;
import com.profitera.util.Stringifier;

public class AccountDelinquency extends BaseDescriptor implements java.io.Serializable {

	private int currentCyclesDelinquent;
	private boolean potentiallyDelinquent = false;
	private boolean delinquent = false;
	private int risk;
	private String accountId = "";
	private String accountNumber = "";
	private int cycleDate;
	private int daysRemainingInCycle;
	private double overlimitAmount = 0;
	private double totalDelinquentAmount = 0;

    public AccountDelinquency(Account account, Session tlSession) {
    	super(RPM.ACCOUNT_QUERY_FILE);
		this.accountId = account.getAccountId().toString();
		setId(accountId);
		// If the account is not over the result will be negative, 
		// in which case the overlimit amount is 0
		// TODO: this is wrong, overlimit amt is customer level
		overlimitAmount = Math.max(0,PrimitiveValue.doubleValue(account.getOutstandingAmt(),0)- PrimitiveValue.doubleValue(account.getCreditLimit(),0));
		totalDelinquentAmount = PrimitiveValue.doubleValue(account.getTotalDelqAmt(),0);
		currentCyclesDelinquent = PrimitiveValue.intValue(account.getCycDelId(), 0);
		setAccountNumber(PrimitiveValue.stringValue(account.getAccountNumber(), ""));
		BillingCycleRef billingCycle = account.getBillingCycleRef();
		if (billingCycle != null) {
			cycleDate = PrimitiveValue.intValue(account.getBillingCycleRef().getBillingCycleDesc(), 0);
			daysRemainingInCycle = RPMDataManager.daysUntil(cycleDate); 
		} else{
			cycleDate = 0;
			daysRemainingInCycle = 30;
		}
		risk = PrimitiveValue.intValue(account.getRiskScore(),0);
	}
	
	public String getAccountId() {
		return accountId;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public int getCurrentCyclesDelinquent() {
		return currentCyclesDelinquent;
	}
	

	public boolean getPotentiallyDelinquent() {
		return potentiallyDelinquent;
	}

	public void setPotentiallyDelinquent(boolean b) {
		potentiallyDelinquent = b;
	}
	
	public int getRisk() {
		return risk;
	}
	public int getDaysRemainingInCycle() {
		return daysRemainingInCycle;
	}

	public String toString() {
		return Stringifier.stringify(this);
	}

	/**
	 * @return Returns the overlimitAmount.
	 */
	public double getOverlimitAmount() {
		return overlimitAmount;
	}

	/**
	 * @return Returns the totalDelinquentAmount.
	 */
	public double getTotalDelinquentAmount() {
		return totalDelinquentAmount;
	}
	
	/**
	 * @return Returns the delinquent.
	 */
	public boolean getDelinquent() {
		return delinquent;
	}

	/**
	 * @param delinquent The delinquent to set.
	 */
	public void setDelinquent(boolean delinquent) {
		this.delinquent = delinquent;
	}
	

}
