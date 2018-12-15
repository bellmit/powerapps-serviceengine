package com.profitera.descriptor.rpm;

import oracle.toplink.sessions.Session;

import com.profitera.descriptor.db.account.Account;
import com.profitera.descriptor.db.reference.RiskLevelRef;
import com.profitera.rpm.RPM;
import com.profitera.services.system.dataaccess.RPMDataManager;
import com.profitera.util.PrimitiveValue;
import com.profitera.util.Stringifier;

public class AccountProfile extends BaseDescriptor implements java.io.Serializable {
	private Account account;
	private int profile = RPMDataManager.NONE_PROFILE;
	private int cyclesDelinquent;
	private int risk = 0;
	private String accountNumber = "";

	// Payment amount behaviour stuff
	private boolean isTransactor;
	private boolean isLightRevolver;
	private boolean isHardRevolver;
	private boolean isDelinquent;

	// Payment frequency behaviour stuff
	private boolean isFrequent;
	private boolean isRegular;
	private boolean isIrregular;
	private boolean isNoPayment;

	private double totalOutstandingBalance;
	private int score;
	
	public AccountProfile(Session tlSession){
		this(null, tlSession);
	}
	
	/**
	 * @param account
	 * @param session2
	 */
	public AccountProfile(Account account, Session session2) {
		super(RPM.ACCOUNT_QUERY_FILE);
		session = session2;
		if (account!=null) setAccount(account);
	}

	public void setAccount(Account account){
		this.account = account;
		isTransactor = false;
		isLightRevolver = false;
		isHardRevolver = false;
		isDelinquent = false;
		isFrequent = false;
		isRegular = false;
		isIrregular = false;
		isNoPayment = false;
		int cycles = 0;
		setId(account.getAccountId());
		if (account.getCycDelId() != null)
			cycles = account.getCycDelId().intValue();
		cyclesDelinquent = cycles;
		// Outstanding Balance
		totalOutstandingBalance = PrimitiveValue.doubleValue(account.getOutstandingAmt(), 0);
		// Account number
		accountNumber = account.getAccountNumber() == null ? "" : account.getAccountNumber();
		RiskLevelRef rlr = account.getRiskLevelRef();
		if (rlr != null)
			risk = rlr.getRiskLevelId().intValue();		
	}
	
	public String getAccountId() {
		return account.getAccountId().toString();
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public int getCyclesDelinquent() {
		return cyclesDelinquent;
	}

	public int getRisk() {
		return risk;
	}

	public double getTotalOutstandingBalance() {
		return totalOutstandingBalance;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getProfile() {
		return profile;
	}

	public void setProfile(int profile) {
		this.profile = profile;
	}

	public boolean getDelinquent() {
		return isDelinquent;
	}

	public boolean getFrequent() {
		return isFrequent;
	}

	public boolean getHardRevolver() {
		return isHardRevolver;
	}

	public boolean getIrregular() {
		return isIrregular;
	}

	public boolean getLightRevolver() {
		return isLightRevolver;
	}

	public boolean getNoPayment() {
		return isNoPayment;
	}

	public boolean getRegular() {
		return isRegular;
	}

	public boolean getTransactor() {
		return isTransactor;
	}

	public void setDelinquent(boolean b) {
		isDelinquent = b;
	}

	public void setFrequent(boolean b) {
		isFrequent = b;
	}

	public void setHardRevolver(boolean b) {
		isHardRevolver = b;
	}

	public void setIrregular(boolean b) {
		isIrregular = b;
	}

	public void setLightRevolver(boolean b) {
		isLightRevolver = b;
	}

	public void setNoPayment(boolean b) {
		isNoPayment = b;
	}

	public void setRegular(boolean b) {
		isRegular = b;
	}

	public void setTransactor(boolean b) {
		isTransactor = b;
	}

	public String toString() {
		return Stringifier.stringify(this);
	}
	/**
	 * Needed so that we don't have to commit the account 
	 * Object before running the profile, the preprocessor
	 * only generates this one attribute that we need for profiling
	 * @param i
	 */
	public void overrideRisk(int i) {
		risk = i;
	}
}