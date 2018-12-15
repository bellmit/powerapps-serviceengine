// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.account;

public class Customer
	implements java.io.Serializable
{
	// Generated constants
	public static final String ACCOUNT = "account";
	public static final String ADDRESS_DETAILS = "addressDetails";
	public static final String BUSINESS_REG_NUMBER = "businessRegNumber";
	public static final String CHILD_CUSTOMERS = "childCustomers";
	public static final String CLIENT = "client";
	public static final String CONTACTS = "contacts";
	public static final String CUST_CATEGORY = "custCategory";
	public static final String CUST_SUB_SEGMENT_ID = "custSubSegmentId";
	public static final String CUSTOMER_ID = "customerId";
	public static final String CUSTOMER_LOCKED_TIME = "customerLockedTime";
	public static final String CUSTOMER_SEGMENT = "customerSegment";
	public static final String EMPLOYER_BUSINESS_REF = "employerBusinessRef";
	public static final String EMPLOYER_NAME = "employerName";
	public static final String EMPLOYMENT_TYPE_REF = "employmentTypeRef";
	public static final String INDUSTRY = "industry";
	public static final String NO_TIMES_FRAUD = "noTimesFraud";
	public static final String NO_TIMES_LOST = "noTimesLost";
	public static final String NO_TIMES_STOLEN = "noTimesStolen";
	public static final String PARENT_CUSTOMER = "parentCustomer";
	public static final String PRIORITY_CUSTOMER_IND = "priorityCustomerInd";
	public static final String PROFILE_SEGMENT_REF = "profileSegmentRef";
	public static final String RECOVERY_POTENTIAL_AMOUNT = "recoveryPotentialAmount";
	public static final String TREATMENT_STAGE_REF = "treatmentStageRef";
	public static final String USER = "user";
	public static final String WORK_LIST_STATUS_REF = "workListStatusRef";
	public static final String WORKLIST = "worklist";
	public static final String AVAILABLE_CREDIT = "availableCredit";
	public static final String AVAILABLE_CASH = "availableCash";
	public static final String PRIORITY_BANK_FLAG = "priorityBankFlag";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface account= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private oracle.toplink.indirection.ValueHolderInterface addressDetails= new oracle.toplink.indirection.ValueHolder();

	private java.lang.String businessRegNumber;

	private oracle.toplink.indirection.ValueHolderInterface childCustomers= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private oracle.toplink.indirection.ValueHolderInterface client= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface contacts= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.String custCategory;

	private java.lang.String custSubSegmentId;

	private java.lang.String customerId;

	private java.sql.Timestamp customerLockedTime;

	private oracle.toplink.indirection.ValueHolderInterface customerSegment= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface employerBusinessRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.String employerName;

	private oracle.toplink.indirection.ValueHolderInterface employmentTypeRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.String industry;

	private java.lang.Double noTimesFraud;

	private java.lang.Double noTimesLost;

	private java.lang.Double noTimesStolen;

	private oracle.toplink.indirection.ValueHolderInterface parentCustomer= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double priorityCustomerInd;

	private oracle.toplink.indirection.ValueHolderInterface profileSegmentRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double recoveryPotentialAmount;

	private oracle.toplink.indirection.ValueHolderInterface treatmentStageRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface user= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface workListStatusRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface worklist= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double availableCredit;
	
	private java.lang.Double availableCash;
	
	private java.lang.Boolean priorityBankFlag;			
	
	public  Customer()
	{
		// Fill in method body here.
	}

	public java.util.Vector getAccount()
	{
		return (java.util.Vector) account.getValue();
	}

	public com.profitera.descriptor.db.contact.AddressDetails getAddressDetails()
	{
		return (com.profitera.descriptor.db.contact.AddressDetails) addressDetails.getValue();
	}

	public java.lang.String getBusinessRegNumber()
	{
		return businessRegNumber;
	}

	public java.util.Vector getChildCustomers()
	{
		return (java.util.Vector) childCustomers.getValue();
	}

	public com.profitera.descriptor.db.client.Client getClient()
	{
		return (com.profitera.descriptor.db.client.Client) client.getValue();
	}

	public java.util.Vector getContacts()
	{
		return (java.util.Vector) contacts.getValue();
	}

	public java.lang.String getCustCategory()
	{
		return custCategory;
	}

	public java.lang.String getCustSubSegmentId()
	{
		return custSubSegmentId;
	}

	public java.lang.String getCustomerId()
	{
		return customerId;
	}

	public java.sql.Timestamp getCustomerLockedTime()
	{
		return customerLockedTime;
	}

	public com.profitera.descriptor.db.account.CustomerSegment getCustomerSegment()
	{
		return (com.profitera.descriptor.db.account.CustomerSegment) customerSegment.getValue();
	}

	public com.profitera.descriptor.db.reference.EmployerBusinessRef getEmployerBusinessRef()
	{
		return (com.profitera.descriptor.db.reference.EmployerBusinessRef) employerBusinessRef.getValue();
	}

	public java.lang.String getEmployerName()
	{
		return employerName;
	}

	public com.profitera.descriptor.db.reference.EmploymentTypeRef getEmploymentTypeRef()
	{
		return (com.profitera.descriptor.db.reference.EmploymentTypeRef) employmentTypeRef.getValue();
	}

	public java.lang.String getIndustry()
	{
		return industry;
	}

	public java.lang.Double getNoTimesFraud()
	{
		return noTimesFraud;
	}

	public java.lang.Double getNoTimesLost()
	{
		return noTimesLost;
	}

	public java.lang.Double getNoTimesStolen()
	{
		return noTimesStolen;
	}

	public com.profitera.descriptor.db.account.Customer getParentCustomer()
	{
		return (com.profitera.descriptor.db.account.Customer) parentCustomer.getValue();
	}

	public java.lang.Double getPriorityCustomerInd()
	{
		return priorityCustomerInd;
	}

	public com.profitera.descriptor.db.reference.ProfileSegmentRef getProfileSegmentRef()
	{
		return (com.profitera.descriptor.db.reference.ProfileSegmentRef) profileSegmentRef.getValue();
	}

	public java.lang.Double getRecoveryPotentialAmount()
	{
		return recoveryPotentialAmount;
	}

	public com.profitera.descriptor.db.reference.TreatmentStageRef getTreatmentStageRef()
	{
		return (com.profitera.descriptor.db.reference.TreatmentStageRef) treatmentStageRef.getValue();
	}

	public com.profitera.descriptor.db.user.User getUser()
	{
		return (com.profitera.descriptor.db.user.User) user.getValue();
	}

	public com.profitera.descriptor.db.reference.AccountWorkListStatusRef getWorkListStatusRef()
	{
		return (com.profitera.descriptor.db.reference.AccountWorkListStatusRef) workListStatusRef.getValue();
	}

	public com.profitera.descriptor.db.worklist.WorkList getWorklist()
	{
		return (com.profitera.descriptor.db.worklist.WorkList) worklist.getValue();
	}

	public java.lang.Double getAvailableCredit(){
	    return availableCredit;	
	}

	public java.lang.Double getAvailableCash(){
	    return availableCash;	
	}	

	public java.lang.Boolean getPriorityBankFlag(){
	    return priorityBankFlag;	
	}	
	
	public void setAccount(java.util.Vector account)
	{
		this.account.setValue(account);
	}

	public void setAddressDetails(com.profitera.descriptor.db.contact.AddressDetails addressDetails)
	{
		this.addressDetails.setValue(addressDetails);
	}

	public void setBusinessRegNumber(java.lang.String businessRegNumber)
	{
		this.businessRegNumber = businessRegNumber;
	}

	public void setChildCustomers(java.util.Vector childCustomers)
	{
		this.childCustomers.setValue(childCustomers);
	}

	public void setClient(com.profitera.descriptor.db.client.Client client)
	{
		this.client.setValue(client);
	}

	public void setContacts(java.util.Vector contacts)
	{
		this.contacts.setValue(contacts);
	}

	public void setCustCategory(java.lang.String custCategory)
	{
		this.custCategory = custCategory;
	}

	public void setCustSubSegmentId(java.lang.String custSubSegmentId)
	{
		this.custSubSegmentId = custSubSegmentId;
	}

	public void setCustomerId(java.lang.String customerId)
	{
		this.customerId = customerId;
	}

	public void setCustomerLockedTime(java.sql.Timestamp customerLockedTime)
	{
		this.customerLockedTime = customerLockedTime;
	}

	public void setCustomerSegment(com.profitera.descriptor.db.account.CustomerSegment customerSegment)
	{
		this.customerSegment.setValue(customerSegment);
	}

	public void setEmployerBusinessRef(com.profitera.descriptor.db.reference.EmployerBusinessRef employerBusinessRef)
	{
		this.employerBusinessRef.setValue(employerBusinessRef);
	}

	public void setEmployerName(java.lang.String employerName)
	{
		this.employerName = employerName;
	}

	public void setEmploymentTypeRef(com.profitera.descriptor.db.reference.EmploymentTypeRef employmentTypeRef)
	{
		this.employmentTypeRef.setValue(employmentTypeRef);
	}

	public void setIndustry(java.lang.String industry)
	{
		this.industry = industry;
	}

	public void setNoTimesFraud(java.lang.Double noTimesFraud)
	{
		this.noTimesFraud = noTimesFraud;
	}

	public void setNoTimesLost(java.lang.Double noTimesLost)
	{
		this.noTimesLost = noTimesLost;
	}

	public void setNoTimesStolen(java.lang.Double noTimesStolen)
	{
		this.noTimesStolen = noTimesStolen;
	}

	public void setParentCustomer(com.profitera.descriptor.db.account.Customer parentCustomer)
	{
		this.parentCustomer.setValue(parentCustomer);
	}

	public void setPriorityCustomerInd(java.lang.Double priorityCustomerInd)
	{
		this.priorityCustomerInd = priorityCustomerInd;
	}

	public void setProfileSegmentRef(com.profitera.descriptor.db.reference.ProfileSegmentRef profileSegmentRef)
	{
		this.profileSegmentRef.setValue(profileSegmentRef);
	}

	public void setRecoveryPotentialAmount(java.lang.Double recoveryPotentialAmount)
	{
		this.recoveryPotentialAmount = recoveryPotentialAmount;
	}

	public void setTreatmentStageRef(com.profitera.descriptor.db.reference.TreatmentStageRef treatmentStageRef)
	{
		this.treatmentStageRef.setValue(treatmentStageRef);
	}

	public void setUser(com.profitera.descriptor.db.user.User user)
	{
		this.user.setValue(user);
	}

	public void setWorkListStatusRef(com.profitera.descriptor.db.reference.AccountWorkListStatusRef workListStatusRef)
	{
		this.workListStatusRef.setValue(workListStatusRef);
	}

	public void setWorklist(com.profitera.descriptor.db.worklist.WorkList worklist)
	{
		this.worklist.setValue(worklist);
	}
	
	public void setAvailableCredit(java.lang.Double availableCredit){
	    this.availableCredit = availableCredit;	
	}

	public void setAvailableCash(java.lang.Double availableCash){
	    this.availableCash = availableCash;	
	}	

	public void setPriorityBankFlag(java.lang.Boolean priorityBankFlag){
	    this.priorityBankFlag = priorityBankFlag;	    
	}	
}
