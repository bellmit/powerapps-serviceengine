// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.account;

public class Account
	implements java.io.Serializable
{
	// Generated constants
	public static final String ACCOUNT_ID = "accountId";
	public static final String ACCOUNT_NUMBER = "accountNumber";
	public static final String ACCOUNT_OWNER_DET = "accountOwnerDet";
	public static final String ACCOUNT_PRODUCT_RELATIONSHIPS = "accountProductRelationships";
	public static final String ACCOUNT_STATUS_REF = "accountStatusRef";
	public static final String ACCOUNT_UNBILLED = "accountUnbilled";
	public static final String ACCOUNT_WORKLIST_HISTORY = "accountWorklistHistory";
	public static final String ACCOUNT_WORKLIST_STATUS_REF = "accountWorklistStatusRef";
	public static final String ACQUISITION_CHANNEL = "acquisitionChannel";
	public static final String AGREEMENT_DATE = "agreementDate";
	public static final String AGREEMENT_FILENO = "agreementFileno";
	public static final String AGREEMENT_LOCATION = "agreementLocation";
	public static final String AUTHORISED_BRANCH = "authorisedBranch";
	public static final String AUTHORISED_EMPLOYEE = "authorisedEmployee";
	public static final String AUTHORISED_NAME = "authorisedName";
	public static final String AUTO_PAY_REF = "autoPayRef";
	public static final String BEHAVIOUR_SCORE = "behaviourScore";
	public static final String BILLING_CYCLE_REF = "billingCycleRef";
	public static final String BLACKLISTED = "blacklisted";
	public static final String BLOCK_CODE_REF = "blockCodeRef";
	public static final String BUSINESS_UNIT = "businessUnit";
	public static final String CAMPAIGN_CODE_REF = "campaignCodeRef";
	public static final String CARD_EXPIRY = "cardExpiry";
	public static final String CHANNEL_CODE_REF = "channelCodeRef";
	public static final String CHARGEOFF_AMT = "chargeoffAmt";
	public static final String CHARGEOFF_DATE = "chargeoffDate";
	public static final String CHARGEOFF_REASON_REF = "chargeoffReasonRef";
	public static final String CHARGEOFF_STATUS_REF = "chargeoffStatusRef";
	public static final String CHILD_ACCOUNTS = "childAccounts";
	public static final String CLIENT = "client";
	public static final String COLLECTABILITY_STATUS_REF = "collectabilityStatusRef";
	public static final String COLLECTION_REASON_REF = "collectionReasonRef";
	public static final String CONTACTS = "contacts";
	public static final String CONTRACT_EXPIRY_DATE = "contractExpiryDate";
	public static final String CREDIT_LIMIT = "creditLimit";
	public static final String CREDIT_SCORE = "creditScore";
	public static final String CURR_DUE_AMT = "currDueAmt";
	public static final String CURRENT_RESPONSIBLE_USER = "currentResponsibleUser";
	public static final String CUSTOMER = "customer";
	public static final String CUSTOMER_SEGMENT = "customerSegment";
	public static final String CYC_DEL_ID = "cycDelId";
	public static final String DATE_IN_COLLECTION = "dateInCollection";
	public static final String DEBT_RECOVERY_STATUS_REF = "debtRecoveryStatusRef";
	public static final String DELINQUENCY_TYPE_REF = "delinquencyTypeRef";
	public static final String DEPOSIT_AMT = "depositAmt";
	public static final String EXTERNAL_PROFILING_SCORE = "externalProfilingScore";
	public static final String EXTERNAL_RATING = "externalRating";
	public static final String FIRST_CHARGEOFF_AMT = "firstChargeoffAmt";
	public static final String GUARANTORS = "guarantors";
	public static final String HI_BALANCE = "hiBalance";
	public static final String INDUSTRY_CATEGORY = "industryCategory";
	public static final String INTEREST_OVERDUE = "interestOverdue";
	public static final String INVOICES = "invoices";
	public static final String LAST_PAYMT_AMT = "lastPaymtAmt";
	public static final String LAST_PAYMT_DATE = "lastPaymtDate";
	public static final String LAST_TOTAL_OUT_AMT = "lastTotalOutAmt";
	public static final String LAST_TREATMENT_ACTION = "lastTreatmentAction";
	public static final String LAST_TREATMENT_RESULT = "lastTreatmentResult";
	public static final String MEMBERSHIP_SINCE = "membershipSince";
	public static final String OLDEST_OVERDUE_DATE = "oldestOverdueDate";
	public static final String OUTSTANDING_AMT = "outstandingAmt";
	public static final String OVERLIMIT_INDICATOR = "overlimitIndicator";
	public static final String PARENT_ACCOUNT = "parentAccount";
	public static final String PARENT_ACCOUNT_ID = "parentAccountId";
	public static final String PAYMENT_BEHAVIOUR_REF = "paymentBehaviourRef";
	public static final String PAYMENT_FREQUENCY_REF = "paymentFrequencyRef";
	public static final String PAYMENTS = "payments";
	public static final String PRINCIPAL_OVERDUE = "principalOverdue";
	public static final String PRODUCT_TYPE_REF = "productTypeRef";
	public static final String PROFILE_SEGMENT_REF = "profileSegmentRef";
	public static final String PROFILING_SCORE = "profilingScore";
	public static final String PROVISION_FOR_BAD_DEBT = "provisionForBadDebt";
	public static final String REMARKS = "remarks";
	public static final String RESCHEDULED = "rescheduled";
	public static final String RISK_BEHAVIOUR_SCORE = "riskBehaviourScore";
	public static final String RISK_EXTERNAL_SCORE = "riskExternalScore";
	public static final String RISK_LEVEL_REF = "riskLevelRef";
	public static final String RISK_PAYMENT_SCORE = "riskPaymentScore";
	public static final String RISK_PROFILE_SCORE = "riskProfileScore";
	public static final String RISK_SCORE = "riskScore";
	public static final String SENSITIVITY_STATUS_REF = "sensitivityStatusRef";
	public static final String STOP_STATEMENT = "stopStatement";
	public static final String TERMINATION_DATE = "terminationDate";
	public static final String TOTAL_DELQ_AMT = "totalDelqAmt";
	public static final String TREATMENT_PLANS = "treatmentPlans";
	public static final String TREATMENT_STAGE_REF = "treatmentStageRef";
	public static final String TREATMENT_STAGE_START_DATE = "treatmentStageStartDate";
	public static final String TREATMENT_STREAM_REF = "treatmentStreamRef";
	public static final String WEIGHTED_RECEIVABLE_AMOUNT = "weightedReceivableAmount";
	public static final String WORKLIST = "worklist";
	public static final String X_DAYS_INDICATOR = "xDaysIndicator";
	public static final String PROFILE_HISTORY = "profileHistory";
    public static final String TRANSFERRED_FROM_ACCOUNT = "transferredFromAccount";
    public static final String TRANSFER_DATE = "transferDate";   
    public static final String TRANSFER_ORGANIZATION_NUMBER = "transferOrganizationNumber";
    public static final String TRANSFER_TYPE_NUMBER = "transferTypeNumber";
    public static final String TRANSFER_ACCOUNT_NUMBER = "transferAccountNumber";
    public static final String TRANSFER_EFFECTIVE_DATE = "transferEffectiveDate";
    public static final String FD_PLEDGE = "fixedDepositPledgeFlag";
    public static final String ACCOUNT_RELATIONSHIP_REF = "accountRelationshipRef";    
    public static final String LEGAL_CODE = "legalCode";    
    public static final String CASH_CHARGE_OFF = "cashChargeOff";
    public static final String RETAIL_CHARGE_OFF = "retailChargeOff";
    public static final String DATE_CHARGE_OFF = "dateChargeOff";
    public static final String CARD_LINK_ID = "cardLinkID";
    public static final String AGENT_BANK_REF = "agentBankRef";    
    public static final String DATE_BLOCK_CODE = "dateBlockCode";
    public static final String DATE_ALTERNATE_BLOCK_CODE = "dateAlternateBlockCode";        
    public static final String DELINQUENCY_COUNT_XDAY = "delinquencyCountXDays";    
    public static final String DELINQUENCY_COUNT_30DAY = "delinquencyCount30Days";
    public static final String DELINQUENCY_COUNT_60DAY = "delinquencyCount60Days";
    public static final String DELINQUENCY_COUNT_90DAY = "delinquencyCount90Days";
    public static final String DELINQUENCY_COUNT_120DAY = "delinquencyCount120Days";
    public static final String DELINQUENCY_COUNT_150DAY = "delinquencyCount150Days";
    public static final String DELINQUENCY_COUNT_180DAY = "delinquencyCount180Days";
    public static final String DELINQUENCY_COUNT_210DAY = "delinquencyCount210Days";    
	// End of generated constants
	
	private java.lang.Double accountId;

	private java.lang.String accountNumber;

	private oracle.toplink.indirection.ValueHolderInterface accountOtherLoans= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private oracle.toplink.indirection.ValueHolderInterface accountOwnerDet= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface accountProductRelationships= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private oracle.toplink.indirection.ValueHolderInterface accountStatusRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface accountUnbilled= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private oracle.toplink.indirection.ValueHolderInterface accountWorklistHistory= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private oracle.toplink.indirection.ValueHolderInterface accountWorklistStatusRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.String acquisitionChannel;

	private java.sql.Timestamp agreementDate;

	private java.lang.String agreementFileno;

	private java.lang.String agreementLocation;

	private java.lang.String authorisedBranch;

	private java.lang.String authorisedEmployee;

	private java.lang.String authorisedName;

	private oracle.toplink.indirection.ValueHolderInterface autoPayRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double behaviourScore;

	private oracle.toplink.indirection.ValueHolderInterface billingCycleRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double blacklisted;

	private oracle.toplink.indirection.ValueHolderInterface blockCodeRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface businessUnit= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface campaignCodeRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.String cardExpiry;

	private oracle.toplink.indirection.ValueHolderInterface channelCodeRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double chargeoffAmt;

	private java.sql.Timestamp chargeoffDate;

	private oracle.toplink.indirection.ValueHolderInterface chargeoffReasonRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface chargeoffStatusRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface childAccounts= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private oracle.toplink.indirection.ValueHolderInterface client= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface collectabilityStatusRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface collectionReasonRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface contacts= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.sql.Timestamp contractExpiryDate;

	private java.lang.Double creditLimit;

	private java.lang.Double creditScore;

	private java.lang.Double currDueAmt;

	private oracle.toplink.indirection.ValueHolderInterface currentResponsibleUser= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface customer= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface customerSegment= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double cycDelId;

	private java.sql.Timestamp dateInCollection;

	private oracle.toplink.indirection.ValueHolderInterface debtRecoveryStatusRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface delinquencyTypeRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double depositAmt;

	private java.lang.Double externalProfilingScore;

	private oracle.toplink.indirection.ValueHolderInterface externalRating= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.Double firstChargeoffAmt;

	private oracle.toplink.indirection.ValueHolderInterface guarantors= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.Double hiBalance;

	private java.lang.String industryCategory;

	private java.lang.Double interestOverdue;

	private oracle.toplink.indirection.ValueHolderInterface invoices= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.Double lastPaymtAmt;

	private java.sql.Timestamp lastPaymtDate;

	private java.lang.Double lastTotalOutAmt;

	private oracle.toplink.indirection.ValueHolderInterface lastTreatmentAction= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface lastTreatmentResult= new oracle.toplink.indirection.ValueHolder();

	private java.sql.Timestamp membershipSince;

	private java.lang.Double outstandingAmt;

	private java.lang.Double overlimitIndicator;

	private oracle.toplink.indirection.ValueHolderInterface parentAccount= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double parentAccountId;

	private oracle.toplink.indirection.ValueHolderInterface paymentBehaviourRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface paymentFrequencyRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface payments= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.Double principalOverdue;

	private oracle.toplink.indirection.ValueHolderInterface productTypeRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface profileSegmentRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double profilingScore;

	private java.lang.Double provisionForBadDebt;

	private java.lang.String remarks;

	private java.lang.Double rescheduled;

	private java.lang.Double riskBehaviourScore;

	private java.lang.Double riskExternalScore;

	private oracle.toplink.indirection.ValueHolderInterface riskLevelRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double riskPaymentScore;

	private java.lang.Double riskProfileScore;

	private java.lang.Double riskScore;

	private oracle.toplink.indirection.ValueHolderInterface sensitivityStatusRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double stopStatement;

	private java.sql.Timestamp terminationDate;

	private java.lang.Double totalDelqAmt;

	private oracle.toplink.indirection.ValueHolderInterface treatmentPlans= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private oracle.toplink.indirection.ValueHolderInterface treatmentStageRef= new oracle.toplink.indirection.ValueHolder();

	private java.sql.Timestamp treatmentStageStartDate;

	private oracle.toplink.indirection.ValueHolderInterface treatmentStreamRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double weightedReceivableAmount;

	private oracle.toplink.indirection.ValueHolderInterface worklist= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double xDaysIndicator;

    private java.util.Date oldestOverdueDate;
  
	private oracle.toplink.indirection.ValueHolderInterface profileHistory = new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private oracle.toplink.indirection.ValueHolderInterface transferredFromAccount = new oracle.toplink.indirection.ValueHolder();
  
    private java.util.Date transferDate;
    
    private java.lang.Double delinquentAmountX;
    
    private java.lang.Double delinquentAmount30;
    
    private java.lang.Double delinquentAmount60;
    
    private java.lang.Double delinquentAmount90;
    
    private java.lang.Double delinquentAmount120;
    
    private java.lang.Double delinquentAmount150;
    
    private java.lang.Double delinquentAmount180;
    
    private java.lang.Double delinquentAmount210;

    private java.lang.Double transferOrganizationNumber;
    
    private java.lang.Double transferTypeNumber;
        
    private java.lang.Double transferAccountNumber;
    
    private java.util.Date transferEffectiveDate;
    
    private java.lang.Boolean fixedDepositPledgeFlag; 
    
    private java.lang.Double relationshipTypeID;
    
    private oracle.toplink.indirection.ValueHolderInterface accountRelationshipRef = new oracle.toplink.indirection.ValueHolder();
    
    private java.lang.String legalCode;    
    
    private java.lang.Double cashChargeOff;
    
    private java.lang.Double retailChargeOff;
    
    private java.util.Date dateChargeOff;  
    
    private java.lang.String cardLinkID;
    
    private java.lang.Double agentBank;
    
    private oracle.toplink.indirection.ValueHolderInterface agentBankRef = new oracle.toplink.indirection.ValueHolder();    
    
    private java.util.Date dateBlockCode;
        
    private java.util.Date dateAlternateBlockCode;        
    
    private java.lang.Long delinquencyCountXDays;    
    
    private java.lang.Long delinquencyCount30Days;
    
    private java.lang.Long delinquencyCount60Days;
    
    private java.lang.Long delinquencyCount90Days;
    
    private java.lang.Long delinquencyCount120Days;
    
    private java.lang.Long delinquencyCount150Days;
    
    private java.lang.Long delinquencyCount180Days;
    
    private java.lang.Long delinquencyCount210Days;
    private java.lang.Long userCode3Id;
        
	public  Account()
	{
		// Fill in method body here.
	}
	
	public java.lang.Double getAccountId()
	{
		return accountId;
	}

	public java.lang.String getAccountNumber()
	{
		return accountNumber;
	}

	public java.util.Vector getAccountOtherLoans()
	{
		return (java.util.Vector) accountOtherLoans.getValue();
	}

	public com.profitera.descriptor.db.account.AccountOwnerDetails getAccountOwnerDet()
	{
		return (com.profitera.descriptor.db.account.AccountOwnerDetails) accountOwnerDet.getValue();
	}

	public java.util.Vector getAccountProductRelationships()
	{
		return (java.util.Vector) accountProductRelationships.getValue();
	}

	public com.profitera.descriptor.db.reference.AccountStatusRef getAccountStatusRef()
	{
		return (com.profitera.descriptor.db.reference.AccountStatusRef) accountStatusRef.getValue();
	}

	public java.util.Vector getAccountUnbilled()
	{
		return (java.util.Vector) accountUnbilled.getValue();
	}

	public java.util.Vector getAccountWorklistHistory()
	{
		return (java.util.Vector) accountWorklistHistory.getValue();
	}

	public com.profitera.descriptor.db.reference.AccountWorkListStatusRef getAccountWorklistStatusRef()
	{
		return (com.profitera.descriptor.db.reference.AccountWorkListStatusRef) accountWorklistStatusRef.getValue();
	}

	public java.lang.String getAcquisitionChannel()
	{
		return acquisitionChannel;
	}

	public java.sql.Timestamp getAgreementDate()
	{
		return agreementDate;
	}

	public java.lang.String getAgreementFileno()
	{
		return agreementFileno;
	}

	public java.lang.String getAgreementLocation()
	{
		return agreementLocation;
	}

	public java.lang.String getAuthorisedBranch()
	{
		return authorisedBranch;
	}

	public java.lang.String getAuthorisedEmployee()
	{
		return authorisedEmployee;
	}

	public java.lang.String getAuthorisedName()
	{
		return authorisedName;
	}

	public com.profitera.descriptor.db.reference.AutoPayRef getAutoPayRef()
	{
		return (com.profitera.descriptor.db.reference.AutoPayRef) autoPayRef.getValue();
	}

    public com.profitera.descriptor.db.reference.AccountRelationshipRef getAccountRelationshipRef(){
        return (com.profitera.descriptor.db.reference.AccountRelationshipRef)accountRelationshipRef.getValue();
    }   	

    public com.profitera.descriptor.db.reference.AgentBankRef getAgentBankRef(){
        return (com.profitera.descriptor.db.reference.AgentBankRef)agentBankRef.getValue();
    }    
    
	public java.lang.Double getBehaviourScore()
	{
		return behaviourScore;
	}

	public com.profitera.descriptor.db.reference.BillingCycleRef getBillingCycleRef()
	{
		return (com.profitera.descriptor.db.reference.BillingCycleRef) billingCycleRef.getValue();
	}

	public java.lang.Double getBlacklisted()
	{
		return blacklisted;
	}

	public com.profitera.descriptor.db.reference.BlockCodeRef getBlockCodeRef()
	{
		return (com.profitera.descriptor.db.reference.BlockCodeRef) blockCodeRef.getValue();
	}

	public com.profitera.descriptor.db.user.BusinessUnit getBusinessUnit()
	{
		return (com.profitera.descriptor.db.user.BusinessUnit) businessUnit.getValue();
	}

	public com.profitera.descriptor.db.reference.CampaignCodeRef getCampaignCodeRef()
	{
		return (com.profitera.descriptor.db.reference.CampaignCodeRef) campaignCodeRef.getValue();
	}

	public java.lang.String getCardExpiry()
	{
		return cardExpiry;
	}

	public com.profitera.descriptor.db.reference.ChannelCodeRef getChannelCodeRef()
	{
		return (com.profitera.descriptor.db.reference.ChannelCodeRef) channelCodeRef.getValue();
	}

	public java.lang.Double getChargeoffAmt()
	{
		return chargeoffAmt;
	}

	public java.sql.Timestamp getChargeoffDate()
	{
		return chargeoffDate;
	}

	public com.profitera.descriptor.db.reference.ChargeOffReasonRef getChargeoffReasonRef()
	{
		return (com.profitera.descriptor.db.reference.ChargeOffReasonRef) chargeoffReasonRef.getValue();
	}

	public com.profitera.descriptor.db.reference.ChargeOffStatusRef getChargeoffStatusRef()
	{
		return (com.profitera.descriptor.db.reference.ChargeOffStatusRef) chargeoffStatusRef.getValue();
	}

	public java.util.Vector getChildAccounts()
	{
		return (java.util.Vector) childAccounts.getValue();
	}

	public com.profitera.descriptor.db.client.Client getClient()
	{
		return (com.profitera.descriptor.db.client.Client) client.getValue();
	}

	public com.profitera.descriptor.db.reference.CollectionStatusRef getCollectabilityStatusRef()
	{
		return (com.profitera.descriptor.db.reference.CollectionStatusRef) collectabilityStatusRef.getValue();
	}

	public com.profitera.descriptor.db.reference.CollectionReasonRef getCollectionReasonRef()
	{
		return (com.profitera.descriptor.db.reference.CollectionReasonRef) collectionReasonRef.getValue();
	}

	public java.util.Vector getContacts()
	{
		return (java.util.Vector) contacts.getValue();
	}

	public java.sql.Timestamp getContractExpiryDate()
	{
		return contractExpiryDate;
	}

	public java.lang.Double getCreditLimit()
	{
		return creditLimit;
	}

	public java.lang.Double getCreditScore()
	{
		return creditScore;
	}

	public java.lang.Double getCurrDueAmt()
	{
		return currDueAmt;
	}

	public com.profitera.descriptor.db.user.User getCurrentResponsibleUser()
	{
		return (com.profitera.descriptor.db.user.User) currentResponsibleUser.getValue();
	}

	public com.profitera.descriptor.db.account.Customer getCustomer()
	{
		return (com.profitera.descriptor.db.account.Customer) customer.getValue();
	}

	public com.profitera.descriptor.db.account.CustomerSegment getCustomerSegment()
	{
		return (com.profitera.descriptor.db.account.CustomerSegment) customerSegment.getValue();
	}

	public java.lang.Double getCycDelId()
	{
		return cycDelId;
	}

	public java.sql.Timestamp getDateInCollection()
	{
		return dateInCollection;
	}

	public com.profitera.descriptor.db.reference.DebtRecoveryStatusRef getDebtRecoveryStatusRef()
	{
		return (com.profitera.descriptor.db.reference.DebtRecoveryStatusRef) debtRecoveryStatusRef.getValue();
	}

	public com.profitera.descriptor.db.reference.DelinquencyTypeRef getDelinquencyTypeRef()
	{
		return (com.profitera.descriptor.db.reference.DelinquencyTypeRef) delinquencyTypeRef.getValue();
	}

	public java.lang.Double getDepositAmt()
	{
		return depositAmt;
	}

	public java.lang.Double getExternalProfilingScore()
	{
		return externalProfilingScore;
	}

	public java.util.Vector getExternalRating()
	{
		return (java.util.Vector) externalRating.getValue();
	}

	public java.lang.Double getFirstChargeoffAmt()
	{
		return firstChargeoffAmt;
	}

	public java.util.Vector getGuarantors()
	{
		return (java.util.Vector) guarantors.getValue();
	}

	public java.lang.Double getHiBalance()
	{
		return hiBalance;
	}

	public java.lang.String getIndustryCategory()
	{
		return industryCategory;
	}

	public java.lang.Double getInterestOverdue()
	{
		return interestOverdue;
	}

	public java.util.Vector getInvoices()
	{
		return (java.util.Vector) invoices.getValue();
	}

	public java.lang.Double getLastPaymtAmt()
	{
		return lastPaymtAmt;
	}

	public java.sql.Timestamp getLastPaymtDate()
	{
		return lastPaymtDate;
	}

	public java.lang.Double getLastTotalOutAmt()
	{
		return lastTotalOutAmt;
	}

	public com.profitera.descriptor.db.treatment.TreatmentProcess getLastTreatmentAction()
	{
		return (com.profitera.descriptor.db.treatment.TreatmentProcess) lastTreatmentAction.getValue();
	}

	public com.profitera.descriptor.db.treatment.TreatmentProcess getLastTreatmentResult()
	{
		return (com.profitera.descriptor.db.treatment.TreatmentProcess) lastTreatmentResult.getValue();
	}

	public java.sql.Timestamp getMembershipSince()
	{
		return membershipSince;
	}

	public java.lang.Double getOutstandingAmt()
	{
		return outstandingAmt;
	}

	public java.lang.Double getOverlimitIndicator()
	{
		return overlimitIndicator;
	}

	public com.profitera.descriptor.db.account.Account getParentAccount()
	{
		return (com.profitera.descriptor.db.account.Account) parentAccount.getValue();
	}

	public java.lang.Double getParentAccountId()
	{
		return parentAccountId;
	}

	public com.profitera.descriptor.db.reference.PaymentBehaviourRef getPaymentBehaviourRef()
	{
		return (com.profitera.descriptor.db.reference.PaymentBehaviourRef) paymentBehaviourRef.getValue();
	}

	public com.profitera.descriptor.db.reference.PaymentFrequencyRef getPaymentFrequencyRef()
	{
		return (com.profitera.descriptor.db.reference.PaymentFrequencyRef) paymentFrequencyRef.getValue();
	}

	public java.util.Vector getPayments()
	{
		return (java.util.Vector) payments.getValue();
	}

	public java.lang.Double getPrincipalOverdue()
	{
		return principalOverdue;
	}

	public com.profitera.descriptor.db.reference.ProductTypeRef getProductTypeRef()
	{
		return (com.profitera.descriptor.db.reference.ProductTypeRef) productTypeRef.getValue();
	}

	public com.profitera.descriptor.db.reference.ProfileSegmentRef getProfileSegmentRef()
	{
		return (com.profitera.descriptor.db.reference.ProfileSegmentRef) profileSegmentRef.getValue();
	}

	public java.lang.Double getProfilingScore()
	{
		return profilingScore;
	}

	public java.lang.Double getProvisionForBadDebt()
	{
		return provisionForBadDebt;
	}

	public java.lang.String getRemarks()
	{
		return remarks;
	}

	public java.lang.Double getRescheduled()
	{
		return rescheduled;
	}

	public java.lang.Double getRiskBehaviourScore()
	{
		return riskBehaviourScore;
	}

	public java.lang.Double getRiskExternalScore()
	{
		return riskExternalScore;
	}

	public com.profitera.descriptor.db.reference.RiskLevelRef getRiskLevelRef()
	{
		return (com.profitera.descriptor.db.reference.RiskLevelRef) riskLevelRef.getValue();
	}

	public java.lang.Double getRiskPaymentScore()
	{
		return riskPaymentScore;
	}

	public java.lang.Double getRiskProfileScore()
	{
		return riskProfileScore;
	}

	public java.lang.Double getRiskScore()
	{
		return riskScore;
	}

	public com.profitera.descriptor.db.reference.SensitiveStatusRef getSensitivityStatusRef()
	{
		return (com.profitera.descriptor.db.reference.SensitiveStatusRef) sensitivityStatusRef.getValue();
	}

	public java.lang.Double getStopStatement()
	{
		return stopStatement;
	}

	public java.sql.Timestamp getTerminationDate()
	{
		return terminationDate;
	}

	public java.lang.Double getTotalDelqAmt()
	{
		return totalDelqAmt;
	}

	public java.util.Vector getTreatmentPlans()
	{
		return (java.util.Vector) treatmentPlans.getValue();
	}

	public com.profitera.descriptor.db.reference.TreatmentStageRef getTreatmentStageRef()
	{
		return (com.profitera.descriptor.db.reference.TreatmentStageRef) treatmentStageRef.getValue();
	}

	public java.sql.Timestamp getTreatmentStageStartDate()
	{
		return treatmentStageStartDate;
	}

	public com.profitera.descriptor.db.reference.TreatmentStreamRef getTreatmentStreamRef()
	{
		return (com.profitera.descriptor.db.reference.TreatmentStreamRef) treatmentStreamRef.getValue();
	}

	public java.lang.Double getWeightedReceivableAmount()
	{
		return weightedReceivableAmount;
	}

	public com.profitera.descriptor.db.worklist.WorkList getWorklist()
	{
		return (com.profitera.descriptor.db.worklist.WorkList) worklist.getValue();
	}

	public java.lang.Double getXDaysIndicator()
	{
		return xDaysIndicator;
	}
	
	public java.lang.Double getRelationshipTypeID(){
	  return relationshipTypeID;	
	}
	
    public java.lang.Double getTransferOrganizationNumber(){	
	  return transferOrganizationNumber;	    	
    }

    public java.lang.Double getTransferTypeNumber(){
  	  return transferTypeNumber;	    	
    }    

    public java.lang.Double getTransferAccountNumber(){
  	  return transferAccountNumber;	    	
    }    

    public java.util.Date getTransferEffectiveDate(){
   	  return transferEffectiveDate;	    	
    }    

    public java.lang.Boolean getFixedDepositPledgeFlag(){	
   	  return fixedDepositPledgeFlag;	    	
    }      
          
	public String getLegalCode(){
		return legalCode;
	}
	
	public java.lang.Double getCashChargeOff(){
		return cashChargeOff;
	}
	
	public java.lang.Double getRetailChargeOff(){
		return retailChargeOff;
	}
	
	public java.util.Date getDateChargeOff(){
	    return dateChargeOff;	
	}
	
	public java.lang.String getCardLinkID(){
	    return cardLinkID;	
	}
	
	public java.lang.Double getAgentBank(){
		return agentBank;
	}
	
	public java.util.Date getDateBlockCode(){
		return dateBlockCode;
	}
	
	public java.util.Date getDateAlternateBlockCode(){
		return dateAlternateBlockCode;
	}
	
	public java.lang.Long getDelinquencyCountXDays(){
		return delinquencyCountXDays;
	}

	public java.lang.Long getDelinquencyCount30Days(){
		return delinquencyCount30Days;
	}
	
	public java.lang.Long getDelinquencyCount60Days(){
		return delinquencyCount60Days;
	}
	
	public java.lang.Long getDelinquencyCount90Days(){
		return delinquencyCount90Days;
	}	
	
	public java.lang.Long getDelinquencyCount120Days(){
		return delinquencyCount120Days;
	}
	
	public java.lang.Long getDelinquencyCount150Days(){
		return delinquencyCount150Days;
	}
	
	public java.lang.Long getDelinquencyCount180Days(){
		return delinquencyCount180Days;
	}
	
	public java.lang.Long getDelinquencyCount210Days(){
		return delinquencyCount210Days;
	}	
 	
	public void setAccountId(java.lang.Double accountId)
	{
		this.accountId = accountId;
	}

	public void setAccountNumber(java.lang.String accountNumber)
	{
		this.accountNumber = accountNumber;
	}

	public void setAccountOtherLoans(java.util.Vector accountOtherLoans)
	{
		this.accountOtherLoans.setValue(accountOtherLoans);
	}

	public void setAccountOwnerDet(com.profitera.descriptor.db.account.AccountOwnerDetails accountOwnerDet)
	{
		this.accountOwnerDet.setValue(accountOwnerDet);
	}

	public void setAccountProductRelationships(java.util.Vector accountProductRelationships)
	{
		this.accountProductRelationships.setValue(accountProductRelationships);
	}

	public void setAccountStatusRef(com.profitera.descriptor.db.reference.AccountStatusRef accountStatusRef)
	{
		this.accountStatusRef.setValue(accountStatusRef);
	}

	public void setAccountUnbilled(java.util.Vector accountUnbilled)
	{
		this.accountUnbilled.setValue(accountUnbilled);
	}

	public void setAccountWorklistHistory(java.util.Vector accountWorklistHistory)
	{
		this.accountWorklistHistory.setValue(accountWorklistHistory);
	}

	public void setAccountWorklistStatusRef(com.profitera.descriptor.db.reference.AccountWorkListStatusRef accountWorklistStatusRef)
	{
		this.accountWorklistStatusRef.setValue(accountWorklistStatusRef);
	}

	public void setAcquisitionChannel(java.lang.String acquisitionChannel)
	{
		this.acquisitionChannel = acquisitionChannel;
	}

	public void setAgreementDate(java.sql.Timestamp agreementDate)
	{
		this.agreementDate = agreementDate;
	}

	public void setAgreementFileno(java.lang.String agreementFileno)
	{
		this.agreementFileno = agreementFileno;
	}

	public void setAgreementLocation(java.lang.String agreementLocation)
	{
		this.agreementLocation = agreementLocation;
	}

	public void setAuthorisedBranch(java.lang.String authorisedBranch)
	{
		this.authorisedBranch = authorisedBranch;
	}

	public void setAuthorisedEmployee(java.lang.String authorisedEmployee)
	{
		this.authorisedEmployee = authorisedEmployee;
	}

	public void setAuthorisedName(java.lang.String authorisedName)
	{
		this.authorisedName = authorisedName;
	}

	public void setAutoPayRef(com.profitera.descriptor.db.reference.AutoPayRef autoPayRef)
	{
		this.autoPayRef.setValue(autoPayRef);
	}

	public void setBehaviourScore(java.lang.Double behaviourScore)
	{
		this.behaviourScore = behaviourScore;
	}

	public void setBillingCycleRef(com.profitera.descriptor.db.reference.BillingCycleRef billingCycleRef)
	{
		this.billingCycleRef.setValue(billingCycleRef);
	}

	public void setBlacklisted(java.lang.Double blacklisted)
	{
		this.blacklisted = blacklisted;
	}

	public void setBlockCodeRef(com.profitera.descriptor.db.reference.BlockCodeRef blockCodeRef)
	{
		this.blockCodeRef.setValue(blockCodeRef);
	}

	public void setBusinessUnit(com.profitera.descriptor.db.user.BusinessUnit businessUnit)
	{
		this.businessUnit.setValue(businessUnit);
	}

	public void setCampaignCodeRef(com.profitera.descriptor.db.reference.CampaignCodeRef campaignCodeRef)
	{
		this.campaignCodeRef.setValue(campaignCodeRef);
	}

	public void setCardExpiry(java.lang.String cardExpiry)
	{
		this.cardExpiry = cardExpiry;
	}

	public void setChannelCodeRef(com.profitera.descriptor.db.reference.ChannelCodeRef channelCodeRef)
	{
		this.channelCodeRef.setValue(channelCodeRef);
	}

	public void setChargeoffAmt(java.lang.Double chargeoffAmt)
	{
		this.chargeoffAmt = chargeoffAmt;
	}

	public void setChargeoffDate(java.sql.Timestamp chargeoffDate)
	{
		this.chargeoffDate = chargeoffDate;
	}

	public void setChargeoffReasonRef(com.profitera.descriptor.db.reference.ChargeOffReasonRef chargeoffReasonRef)
	{
		this.chargeoffReasonRef.setValue(chargeoffReasonRef);
	}

	public void setChargeoffStatusRef(com.profitera.descriptor.db.reference.ChargeOffStatusRef chargeoffStatusRef)
	{
		this.chargeoffStatusRef.setValue(chargeoffStatusRef);
	}

	public void setChildAccounts(java.util.Vector childAccounts)
	{
		this.childAccounts.setValue(childAccounts);
	}

	public void setClient(com.profitera.descriptor.db.client.Client client)
	{
		this.client.setValue(client);
	}

	public void setCollectabilityStatusRef(com.profitera.descriptor.db.reference.CollectionStatusRef collectabilityStatusRef)
	{
		this.collectabilityStatusRef.setValue(collectabilityStatusRef);
	}

	public void setCollectionReasonRef(com.profitera.descriptor.db.reference.CollectionReasonRef collectionReasonRef)
	{
		this.collectionReasonRef.setValue(collectionReasonRef);
	}

	public void setContacts(java.util.Vector contacts)
	{
		this.contacts.setValue(contacts);
	}

	public void setContractExpiryDate(java.sql.Timestamp contractExpiryDate)
	{
		this.contractExpiryDate = contractExpiryDate;
	}

	public void setCreditLimit(java.lang.Double creditLimit)
	{
		this.creditLimit = creditLimit;
	}

	public void setCreditScore(java.lang.Double creditScore)
	{
		this.creditScore = creditScore;
	}

	public void setCurrDueAmt(java.lang.Double currDueAmt)
	{
		this.currDueAmt = currDueAmt;
	}

	public void setCurrentResponsibleUser(com.profitera.descriptor.db.user.User currentResponsibleUser)
	{
		this.currentResponsibleUser.setValue(currentResponsibleUser);
	}

	public void setCustomer(com.profitera.descriptor.db.account.Customer customer)
	{
		this.customer.setValue(customer);
	}

	public void setCustomerSegment(com.profitera.descriptor.db.account.CustomerSegment customerSegment)
	{
		this.customerSegment.setValue(customerSegment);
	}

	public void setCycDelId(java.lang.Double cycDelId)
	{
		this.cycDelId = cycDelId;
	}

	public void setDateInCollection(java.sql.Timestamp dateInCollection)
	{
		this.dateInCollection = dateInCollection;
	}

	public void setDebtRecoveryStatusRef(com.profitera.descriptor.db.reference.DebtRecoveryStatusRef debtRecoveryStatusRef)
	{
		this.debtRecoveryStatusRef.setValue(debtRecoveryStatusRef);
	}

	public void setDelinquencyTypeRef(com.profitera.descriptor.db.reference.DelinquencyTypeRef delinquencyTypeRef)
	{
		this.delinquencyTypeRef.setValue(delinquencyTypeRef);
	}

	public void setDepositAmt(java.lang.Double depositAmt)
	{
		this.depositAmt = depositAmt;
	}

	public void setExternalProfilingScore(java.lang.Double externalProfilingScore)
	{
		this.externalProfilingScore = externalProfilingScore;
	}

	public void setExternalRating(java.util.Vector externalRating)
	{
		this.externalRating.setValue(externalRating);
	}

	public void setFirstChargeoffAmt(java.lang.Double firstChargeoffAmt)
	{
		this.firstChargeoffAmt = firstChargeoffAmt;
	}

	public void setGuarantors(java.util.Vector guarantors)
	{
		this.guarantors.setValue(guarantors);
	}

	public void setHiBalance(java.lang.Double hiBalance)
	{
		this.hiBalance = hiBalance;
	}

	public void setIndustryCategory(java.lang.String industryCategory)
	{
		this.industryCategory = industryCategory;
	}

	public void setInterestOverdue(java.lang.Double interestOverdue)
	{
		this.interestOverdue = interestOverdue;
	}

	public void setInvoices(java.util.Vector invoices)
	{
		this.invoices.setValue(invoices);
	}

	public void setLastPaymtAmt(java.lang.Double lastPaymtAmt)
	{
		this.lastPaymtAmt = lastPaymtAmt;
	}

	public void setLastPaymtDate(java.sql.Timestamp lastPaymtDate)
	{
		this.lastPaymtDate = lastPaymtDate;
	}

	public void setLastTotalOutAmt(java.lang.Double lastTotalOutAmt)
	{
		this.lastTotalOutAmt = lastTotalOutAmt;
	}

	public void setLastTreatmentAction(com.profitera.descriptor.db.treatment.TreatmentProcess lastTreatmentAction)
	{
		this.lastTreatmentAction.setValue(lastTreatmentAction);
	}

	public void setLastTreatmentResult(com.profitera.descriptor.db.treatment.TreatmentProcess lastTreatmentResult)
	{
		this.lastTreatmentResult.setValue(lastTreatmentResult);
	}

	public void setMembershipSince(java.sql.Timestamp membershipSince)
	{
		this.membershipSince = membershipSince;
	}

	public void setOutstandingAmt(java.lang.Double outstandingAmt)
	{
		this.outstandingAmt = outstandingAmt;
	}

	public void setOverlimitIndicator(java.lang.Double overlimitIndicator)
	{
		this.overlimitIndicator = overlimitIndicator;
	}

	public void setParentAccount(com.profitera.descriptor.db.account.Account parentAccount)
	{
		this.parentAccount.setValue(parentAccount);
	}

	public void setParentAccountId(java.lang.Double parentAccountId)
	{
		this.parentAccountId = parentAccountId;
	}

	public void setPaymentBehaviourRef(com.profitera.descriptor.db.reference.PaymentBehaviourRef paymentBehaviourRef)
	{
		this.paymentBehaviourRef.setValue(paymentBehaviourRef);
	}

	public void setPaymentFrequencyRef(com.profitera.descriptor.db.reference.PaymentFrequencyRef paymentFrequencyRef)
	{
		this.paymentFrequencyRef.setValue(paymentFrequencyRef);
	}

	public void setPayments(java.util.Vector payments)
	{
		this.payments.setValue(payments);
	}

	public void setPrincipalOverdue(java.lang.Double principalOverdue)
	{
		this.principalOverdue = principalOverdue;
	}

	public void setProductTypeRef(com.profitera.descriptor.db.reference.ProductTypeRef productTypeRef)
	{
		this.productTypeRef.setValue(productTypeRef);
	}

	public void setProfileSegmentRef(com.profitera.descriptor.db.reference.ProfileSegmentRef profileSegmentRef)
	{
		this.profileSegmentRef.setValue(profileSegmentRef);
	}

	public void setProfilingScore(java.lang.Double profilingScore)
	{
		this.profilingScore = profilingScore;
	}

	public void setProvisionForBadDebt(java.lang.Double provisionForBadDebt)
	{
		this.provisionForBadDebt = provisionForBadDebt;
	}

	public void setRemarks(java.lang.String remarks)
	{
		this.remarks = remarks;
	}

	public void setRescheduled(java.lang.Double rescheduled)
	{
		this.rescheduled = rescheduled;
	}

	public void setRiskBehaviourScore(java.lang.Double riskBehaviourScore)
	{
		this.riskBehaviourScore = riskBehaviourScore;
	}

	public void setRiskExternalScore(java.lang.Double riskExternalScore)
	{
		this.riskExternalScore = riskExternalScore;
	}

	public void setRiskLevelRef(com.profitera.descriptor.db.reference.RiskLevelRef riskLevelRef)
	{
		this.riskLevelRef.setValue(riskLevelRef);
	}

	public void setRiskPaymentScore(java.lang.Double riskPaymentScore)
	{
		this.riskPaymentScore = riskPaymentScore;
	}

	public void setRiskProfileScore(java.lang.Double riskProfileScore)
	{
		this.riskProfileScore = riskProfileScore;
	}

	public void setRiskScore(java.lang.Double riskScore)
	{
		this.riskScore = riskScore;
	}

	public void setSensitivityStatusRef(com.profitera.descriptor.db.reference.SensitiveStatusRef sensitivityStatusRef)
	{
		this.sensitivityStatusRef.setValue(sensitivityStatusRef);
	}

	public void setStopStatement(java.lang.Double stopStatement)
	{
		this.stopStatement = stopStatement;
	}

	public void setTerminationDate(java.sql.Timestamp terminationDate)
	{
		this.terminationDate = terminationDate;
	}

	public void setTotalDelqAmt(java.lang.Double totalDelqAmt)
	{
		this.totalDelqAmt = totalDelqAmt;
	}

	public void setTreatmentPlans(java.util.Vector treatmentPlans)
	{
		this.treatmentPlans.setValue(treatmentPlans);
	}

	public void setTreatmentStageRef(com.profitera.descriptor.db.reference.TreatmentStageRef treatmentStageRef)
	{
		this.treatmentStageRef.setValue(treatmentStageRef);
	}

	public void setTreatmentStageStartDate(java.sql.Timestamp treatmentStageStartDate)
	{
		this.treatmentStageStartDate = treatmentStageStartDate;
	}

	public void setTreatmentStreamRef(com.profitera.descriptor.db.reference.TreatmentStreamRef treatmentStreamRef)
	{
		this.treatmentStreamRef.setValue(treatmentStreamRef);
	}

	public void setWeightedReceivableAmount(java.lang.Double weightedReceivableAmount)
	{
		this.weightedReceivableAmount = weightedReceivableAmount;
	}

	public void setWorklist(com.profitera.descriptor.db.worklist.WorkList worklist)
	{
		this.worklist.setValue(worklist);
	}

	public void setXDaysIndicator(java.lang.Double xDaysIndicator)
	{
		this.xDaysIndicator = xDaysIndicator;
	}

    public void setOldestOverdueDate(java.util.Date oldestOverdueDate) {
      this.oldestOverdueDate = oldestOverdueDate;
    }

    public java.util.Date getOldestOverdueDate() {
      return oldestOverdueDate;
    }

    public java.util.Vector getProfileHistory()
	{
		return (java.util.Vector) profileHistory.getValue();
	}
	public void setProfileHistory(java.util.Vector profileHistory)
	{
		this.profileHistory.setValue(profileHistory);
	}

    public com.profitera.descriptor.db.account.Account getTransferredFromAccount(){
      return (com.profitera.descriptor.db.account.Account) transferredFromAccount.getValue();
    }

    public void setTransferredFromAccount(com.profitera.descriptor.db.account.Account transferredAccount){
      this.transferredFromAccount.setValue(transferredAccount);
    }
    
    public java.util.Date getTransferDate(){
      return transferDate;
    }
    
    public void setTransferDate(java.util.Date transferDate){
      this.transferDate = transferDate;
    }

    public java.lang.Double getDelinquentAmount120() {
      return delinquentAmount120;
    }

    public void setDelinquentAmount120(java.lang.Double delinquentAmount120) {
      this.delinquentAmount120 = delinquentAmount120;
    }

    public java.lang.Double getDelinquentAmount150() {
      return delinquentAmount150;
    }

    public void setDelinquentAmount150(java.lang.Double delinquentAmount150) {
      this.delinquentAmount150 = delinquentAmount150;
    }

    public java.lang.Double getDelinquentAmount180() {
      return delinquentAmount180;
    }

    public void setDelinquentAmount180(java.lang.Double delinquentAmount180) {
      this.delinquentAmount180 = delinquentAmount180;
    }

    public java.lang.Double getDelinquentAmount210() {
      return delinquentAmount210;
    }

    public void setDelinquentAmount210(java.lang.Double delinquentAmount210) {
      this.delinquentAmount210 = delinquentAmount210;
    }

    public java.lang.Double getDelinquentAmount30() {
      return delinquentAmount30;
    }

    public void setDelinquentAmount30(java.lang.Double delinquentAmount30) {
      this.delinquentAmount30 = delinquentAmount30;
    }

    public java.lang.Double getDelinquentAmount60() {
      return delinquentAmount60;
    }

    public void setDelinquentAmount60(java.lang.Double delinquentAmount60) {
      this.delinquentAmount60 = delinquentAmount60;
    }

    public java.lang.Double getDelinquentAmount90() {
      return delinquentAmount90;
    }

    public void setDelinquentAmount90(java.lang.Double delinquentAmount90) {
      this.delinquentAmount90 = delinquentAmount90;
    }

    public java.lang.Double getDelinquentAmountX() {
      return delinquentAmountX;
    }

    public void setDelinquentAmountX(java.lang.Double delinquentAmountX) {
      this.delinquentAmountX = delinquentAmountX;
    }
    
    public void setRelationshipTypeID(java.lang.Double relationshipTypeID){
      this.relationshipTypeID = relationshipTypeID;	
    }
    
    public void setTransferOrganizationNumber(java.lang.Double transferOrganizationNumber){
      this.transferOrganizationNumber = transferOrganizationNumber;    	
    }
    
    public void setTransferTypeNumber(java.lang.Double transferTypeNumber){
      this.transferTypeNumber = transferTypeNumber;    	
    }
    
    public void setTransferAccountNumber(java.lang.Double transferAccountNumber){
      this.transferAccountNumber = transferAccountNumber;
    }
    
    public void setTransferEffectiveDate(java.util.Date transferEffectiveDate){
      this.transferEffectiveDate = transferEffectiveDate;
    }
    
    public void setFixedDepositPledgeFlag(java.lang.Boolean fixedDepositPledgeFlag){
      this.fixedDepositPledgeFlag = fixedDepositPledgeFlag;
    }
    
    public void setAccountRelationshipRef(com.profitera.descriptor.db.reference.AccountRelationshipRef accountRelationshipRef){
      this.accountRelationshipRef.setValue(accountRelationshipRef);
    }     
    
    public void setAgentBankRef(com.profitera.descriptor.db.reference.AgentBankRef agentBankRef){
      this.agentBankRef.setValue(agentBankRef);	
    }
    
    public void setLegalCode(String legalCode){
      this.legalCode = legalCode;	
    }
    
    public void setCashChargeOff(java.lang.Double cashChargeOff){
      this.cashChargeOff = cashChargeOff;	
    }
    
    public void setRetailChargeOff(java.lang.Double retailChargeOff){
      this.retailChargeOff = retailChargeOff;	
    }
    
    public void setDateChargeOff(java.util.Date dateChargeOff){
      this.dateChargeOff = dateChargeOff;	
    }          
    
    public void setCardLinkID(java.lang.String cardLinkID){
      this.cardLinkID = cardLinkID;	
    }
    
    public void setAgentBank(java.lang.Double agentBank){
      this.agentBank = agentBank;	
    }
    
    public void setDateBlockCode(java.util.Date dateBlockCode){
      this.dateBlockCode = dateBlockCode;	
    }
    
    public void setDateAlternateBlockCode(java.util.Date dateAlternateBlockCode){
      this.dateAlternateBlockCode = dateAlternateBlockCode;    	    	
    }
    
    public void setDelinquencyCountXDays(java.lang.Long delinquencyCountXDays){
      this.delinquencyCountXDays = delinquencyCountXDays;	
    }
    
    public void setDelinquencyCount30Days(java.lang.Long delinquencyCount30Days){
      this.delinquencyCount30Days = delinquencyCount30Days;	
    }

    public void setDelinquencyCount60Days(java.lang.Long delinquencyCount60Days){
      this.delinquencyCount60Days = delinquencyCount60Days;	
    }
    
    public void setDelinquencyCount90Days(java.lang.Long delinquencyCount90Days){
      this.delinquencyCount90Days = delinquencyCount90Days;	
    }

    public void setDelinquencyCount120Days(java.lang.Long delinquencyCount120Days){
      this.delinquencyCount120Days = delinquencyCount120Days;	
    }
    
    public void setDelinquencyCount150Days(java.lang.Long delinquencyCount150Days){
      this.delinquencyCount150Days = delinquencyCount150Days;	
    }  
    
    public void setDelinquencyCount180Days(java.lang.Long delinquencyCount180Days){
      this.delinquencyCount180Days = delinquencyCount180Days;	
    }  
    
    public void setDelinquencyCount210Days(java.lang.Long delinquencyCount210Days){
      this.delinquencyCount210Days = delinquencyCount210Days;	
    }      
    
    public java.lang.Long getUserCode3Id(){
      return userCode3Id;
    }
    
    public void setUserCode3Id(Long i) {
      this.userCode3Id = i;
    }

}