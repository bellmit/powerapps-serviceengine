package com.profitera.descriptor.rpm;

import java.util.Calendar;
import java.util.Date;

import oracle.toplink.sessions.Session;

import com.profitera.descriptor.db.account.Account;
import com.profitera.rpm.ObjectPoolManager;
import com.profitera.rpm.RPM;
import com.profitera.util.DateParser;
import com.profitera.util.PrimitiveValue;
import com.profitera.util.Stringifier;

public class AccountPreprocessor extends BaseDescriptor implements java.io.Serializable {

	private double totalOutstandingAmount;
	private double creditLimit;
	private double lastPaymentAmount;
	private double totalDelinquentAmount;
	private double currentMinimumPaymentAmount;

	private int risk;
	private int cardType;

	private int blockCode;
	private int currentCyclesDelinquent;

	private int score;
	private int paymentScore;
	private int profileScore;
	private int behaviourScore;
	private int externalScore;

	private boolean fixedLineIsPreferredContactNumber;
	private boolean homeIsPreferredMailingAddress;
	private boolean guarantor;

	private String accountNumber = "";

	private Double accountId;

	private Date registrationDate;
	private Date businessRegistrationDate;

	/* external scoring objects*/
	private boolean positiveCTOS;
	private boolean positiveBMC;
	private boolean positiveFIS;
	private boolean positiveCIF;
	/* end external scoring objects*/
	private int registrationYear;

	public AccountPreprocessor(Session tlSession) {
		this(null, tlSession);
	}

	public AccountPreprocessor(Account account, Session tlSession) {
		super(RPM.ACCOUNT_QUERY_FILE);
		session = tlSession;
		if (account!=null)
			setAccount(account);
	}
	public void setAccount(Account account) {
		Date dateNow = new Date(); //Use instead of calls to Date()
		this.accountId = account.getAccountId();
		setId(accountId);
		score = 0;
		paymentScore = 0;
		profileScore = 0;
		behaviourScore = 0;
		externalScore = 0;

		if (account == null) return;
		creditLimit = PrimitiveValue.doubleValue(account.getCreditLimit(), 0);
		totalDelinquentAmount = PrimitiveValue.doubleValue(account.getTotalDelqAmt(), 0);
		currentMinimumPaymentAmount = PrimitiveValue.doubleValue(account.getCurrDueAmt(), 0);
		totalOutstandingAmount = PrimitiveValue.doubleValue(account.getOutstandingAmt(),0);
		registrationDate = PrimitiveValue.dateValue(account.getAgreementDate(), dateNow);
		Calendar c = Calendar.getInstance();
		c.setTime(registrationDate);
		registrationYear = c.get(Calendar.YEAR);
		lastPaymentAmount = (PrimitiveValue.doubleValue(account.getLastPaymtAmt(), 0));
		currentCyclesDelinquent = PrimitiveValue.intValue(account.getCycDelId(), 0);
		accountNumber = PrimitiveValue.stringValue(account.getAccountNumber(), "");
		
		// TODO: Implement this for real, current data doesn't seem to support this anyway
		fixedLineIsPreferredContactNumber = true;
		homeIsPreferredMailingAddress = true;

		// TODO: there is no guarantor data in the db 
//		Vector guarantors = account.getGuarantors();
//		guarantor = guarantors.size() == 0 ? false : true;
		guarantor = false;
		risk = ObjectPoolManager.getDefaultRiskLevel().getRiskLevelId().intValue();
	}

	public int getBehaviourScore() {
		return behaviourScore;
	}

	public void setBehaviourScore(int behaviourScore) {
		this.behaviourScore = behaviourScore;
	}

	public int getPaymentScore() {
		return paymentScore;
	}

	public void setPaymentScore(int paymentScore) {
		this.paymentScore = paymentScore;
	}

	public int getProfileScore() {
		return profileScore;
	}

	public void setProfileScore(int profileScore) {
		this.profileScore = profileScore;
	}

	public int getExternalScore() {
		return externalScore;
	}

	public void setExternalScore(int externalScore) {
		this.externalScore = externalScore;
	}

	public double getTotalDelinquentAmount() {
		return totalDelinquentAmount;
	}

	public double getCurrentMinimumPaymentAmount() {
		return currentMinimumPaymentAmount;
	}

	public double getLastPaymentAmount() {
		return lastPaymentAmount;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	/* ==================================== RISK AND SCORE ========================*/
	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getRisk() {
		return risk;
	}

	public void setRisk(int risk) {
		this.risk = risk;
	}

	public double getTotalOutstandingAmount() {
		return totalOutstandingAmount;
	}

	public double getCreditLimit() {
		return creditLimit;
	}

	public int getCardType() {
		return cardType;
	}

	public int getDaysUntilRegistrationDate() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(registrationDate);
		cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
		Date d = new Date(cal.getTimeInMillis());
		long timeDiff = System.currentTimeMillis() - d.getTime();
		return Math.abs((int) (timeDiff / (1000 * 60 * 60 * 24)));
	}

	public int getBusinessDurationInMonths() {
		if (businessRegistrationDate == null)
			return 0;
		else
			return (DateParser.getDaysDifference(businessRegistrationDate, new Date(System.currentTimeMillis())) / 30);
	}

	public boolean getHasGuarantor() {
		return guarantor;
	}

	public int getBlockCode() {
		return blockCode;
	}

	public boolean getIsPositiveBMC() {
		return positiveBMC;
	}

	public boolean getIsPositiveCIF() {
		return positiveCIF;
	}

	public boolean getIsPositiveCTOS() {
		return positiveCTOS;
	}

	public boolean getIsPositiveFIS() {
		return positiveFIS;
	}

	public String getAccountId() {
		return accountId.toString();
	}

	public boolean getIsHomeIsPreferredMailingAddress() {
		return homeIsPreferredMailingAddress;
	}

	public boolean getIsFixedLineIsPreferredContactNumber() {
		return fixedLineIsPreferredContactNumber;
	}

	public int getCurrentCyclesDelinquent() {
		return currentCyclesDelinquent;
	}

	public String toString() {
		return Stringifier.stringify(this);
	}
	public int getRegistrationYear() {
		return registrationYear;
	}
}