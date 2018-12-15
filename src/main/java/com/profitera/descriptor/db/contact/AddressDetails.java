// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.contact;

public class AddressDetails
	implements java.io.Serializable
{
	// Generated constants
	public static final String ACCOUNT_CONTACTS = "accountContacts";
	public static final String ADDRESS1 = "address1";
	public static final String ADDRESS2 = "address2";
	public static final String CITY = "city";
	public static final String CONTACT_ALIAS = "contactAlias";
	public static final String CONTACT_CATEGORY = "contactCategory";
	public static final String CONTACT_FIRST_NAME = "contactFirstName";
	public static final String CONTACT_ID = "contactId";
	public static final String CONTACT_LAST_NAME = "contactLastName";
	public static final String CONTACT_MIDDLE_NAME = "contactMiddleName";
	public static final String CONTACT_NUMBERS = "contactNumbers";
	public static final String CONTACT_OWNER_ID = "contactOwnerId";
	public static final String CONTACT_TIME_AFTER = "contactTimeAfter";
	public static final String CONTACT_TIME_BEFORE = "contactTimeBefore";
	public static final String CONTACT_TYPE_REF = "contactTypeRef";
	public static final String COUNTRY_REF = "countryRef";
	public static final String CUSTOMER_CONTACTS = "customerContacts";
	public static final String EMPLOYEE_CONTACTS = "employeeContacts";
	public static final String GUARANTOR_CONTACTS = "guarantorContacts";
	public static final String OWNER_ACCOUNT_DETAILS = "ownerAccountDetails";
	public static final String OWNER_AGENCY = "ownerAgency";
	public static final String OWNER_BUSINESS_UNIT = "ownerBusinessUnit";
	public static final String OWNER_CUSTOMER = "ownerCustomer";
	public static final String OWNER_EMPLOYEE = "ownerEmployee";
	public static final String PREFERRED_POSITION = "preferredPosition";
	public static final String REMARKS = "remarks";
	public static final String SALUTATION_TYPE_REF = "salutationTypeRef";
	public static final String SECTION = "section";
	public static final String STATE_REF = "stateRef";
	public static final String STREET_NAME = "streetName";
	public static final String ZIP_CODE = "zipCode";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface accountContacts= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.String address1;

	private java.lang.String address2;

	private java.lang.String city;

	private java.lang.String contactAlias;

	private java.lang.String contactCategory;

	private java.lang.String contactFirstName;

	private java.lang.Double contactId;

	private java.lang.String contactLastName;

	private java.lang.String contactMiddleName;

	private oracle.toplink.indirection.ValueHolderInterface contactNumbers= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.String contactOwnerId;

	private java.sql.Timestamp contactTimeAfter;

	private java.sql.Timestamp contactTimeBefore;

	private oracle.toplink.indirection.ValueHolderInterface contactTypeRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface countryRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface customerContacts= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private oracle.toplink.indirection.ValueHolderInterface employeeContacts= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private oracle.toplink.indirection.ValueHolderInterface guarantorContacts= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private oracle.toplink.indirection.ValueHolderInterface ownerAccountDetails= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface ownerAgency= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface ownerBusinessUnit= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface ownerCustomer= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface ownerEmployee= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double preferredPosition;

	private java.lang.String remarks;

	private oracle.toplink.indirection.ValueHolderInterface salutationTypeRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.String section;

	private oracle.toplink.indirection.ValueHolderInterface stateRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.String streetName;

	private java.lang.String zipCode;

	public  AddressDetails()
	{
		// Fill in method body here.
	}

	public java.util.Vector getAccountContacts()
	{
		return (java.util.Vector) accountContacts.getValue();
	}

	public java.lang.String getAddress1()
	{
		return address1;
	}

	public java.lang.String getAddress2()
	{
		return address2;
	}

	public java.lang.String getCity()
	{
		return city;
	}

	public java.lang.String getContactAlias()
	{
		return contactAlias;
	}

	public java.lang.String getContactCategory()
	{
		return contactCategory;
	}

	public java.lang.String getContactFirstName()
	{
		return contactFirstName;
	}

	public java.lang.Double getContactId()
	{
		return contactId;
	}

	public java.lang.String getContactLastName()
	{
		return contactLastName;
	}

	public java.lang.String getContactMiddleName()
	{
		return contactMiddleName;
	}

	public java.util.Vector getContactNumbers()
	{
		return (java.util.Vector) contactNumbers.getValue();
	}

	public java.lang.String getContactOwnerId()
	{
		return contactOwnerId;
	}

	public java.sql.Timestamp getContactTimeAfter()
	{
		return contactTimeAfter;
	}

	public java.sql.Timestamp getContactTimeBefore()
	{
		return contactTimeBefore;
	}

	public com.profitera.descriptor.db.reference.ContactTypeRef getContactTypeRef()
	{
		return (com.profitera.descriptor.db.reference.ContactTypeRef) contactTypeRef.getValue();
	}

	public com.profitera.descriptor.db.reference.CountryRef getCountryRef()
	{
		return (com.profitera.descriptor.db.reference.CountryRef) countryRef.getValue();
	}

	public java.util.Vector getCustomerContacts()
	{
		return (java.util.Vector) customerContacts.getValue();
	}

	public java.util.Vector getEmployeeContacts()
	{
		return (java.util.Vector) employeeContacts.getValue();
	}

	public java.util.Vector getGuarantorContacts()
	{
		return (java.util.Vector) guarantorContacts.getValue();
	}

	public com.profitera.descriptor.db.account.AccountOwnerDetails getOwnerAccountDetails()
	{
		return (com.profitera.descriptor.db.account.AccountOwnerDetails) ownerAccountDetails.getValue();
	}

	public com.profitera.descriptor.db.client.Agency getOwnerAgency()
	{
		return (com.profitera.descriptor.db.client.Agency) ownerAgency.getValue();
	}

	public com.profitera.descriptor.db.user.BusinessUnit getOwnerBusinessUnit()
	{
		return (com.profitera.descriptor.db.user.BusinessUnit) ownerBusinessUnit.getValue();
	}

	public com.profitera.descriptor.db.account.Customer getOwnerCustomer()
	{
		return (com.profitera.descriptor.db.account.Customer) ownerCustomer.getValue();
	}

	public com.profitera.descriptor.db.user.Employee getOwnerEmployee()
	{
		return (com.profitera.descriptor.db.user.Employee) ownerEmployee.getValue();
	}

	public java.lang.Double getPreferredPosition()
	{
		return preferredPosition;
	}

	public java.lang.String getRemarks()
	{
		return remarks;
	}

	public com.profitera.descriptor.db.reference.SalutationTypeRef getSalutationTypeRef()
	{
		return (com.profitera.descriptor.db.reference.SalutationTypeRef) salutationTypeRef.getValue();
	}

	public java.lang.String getSection()
	{
		return section;
	}

	public com.profitera.descriptor.db.reference.StateRef getStateRef()
	{
		return (com.profitera.descriptor.db.reference.StateRef) stateRef.getValue();
	}

	public java.lang.String getStreetName()
	{
		return streetName;
	}

	public java.lang.String getZipCode()
	{
		return zipCode;
	}

	public void setAccountContacts(java.util.Vector accountContacts)
	{
		this.accountContacts.setValue(accountContacts);
	}

	public void setAddress1(java.lang.String address1)
	{
		this.address1 = address1;
	}

	public void setAddress2(java.lang.String address2)
	{
		this.address2 = address2;
	}

	public void setCity(java.lang.String city)
	{
		this.city = city;
	}

	public void setContactAlias(java.lang.String contactAlias)
	{
		this.contactAlias = contactAlias;
	}

	public void setContactCategory(java.lang.String contactCategory)
	{
		this.contactCategory = contactCategory;
	}

	public void setContactFirstName(java.lang.String contactFirstName)
	{
		this.contactFirstName = contactFirstName;
	}

	public void setContactId(java.lang.Double contactId)
	{
		this.contactId = contactId;
	}

	public void setContactLastName(java.lang.String contactLastName)
	{
		this.contactLastName = contactLastName;
	}

	public void setContactMiddleName(java.lang.String contactMiddleName)
	{
		this.contactMiddleName = contactMiddleName;
	}

	public void setContactNumbers(java.util.Vector contactNumbers)
	{
		this.contactNumbers.setValue(contactNumbers);
	}

	public void setContactOwnerId(java.lang.String contactOwnerId)
	{
		this.contactOwnerId = contactOwnerId;
	}

	public void setContactTimeAfter(java.sql.Timestamp contactTimeAfter)
	{
		this.contactTimeAfter = contactTimeAfter;
	}

	public void setContactTimeBefore(java.sql.Timestamp contactTimeBefore)
	{
		this.contactTimeBefore = contactTimeBefore;
	}

	public void setContactTypeRef(com.profitera.descriptor.db.reference.ContactTypeRef contactTypeRef)
	{
		this.contactTypeRef.setValue(contactTypeRef);
	}

	public void setCountryRef(com.profitera.descriptor.db.reference.CountryRef countryRef)
	{
		this.countryRef.setValue(countryRef);
	}

	public void setCustomerContacts(java.util.Vector customerContacts)
	{
		this.customerContacts.setValue(customerContacts);
	}

	public void setEmployeeContacts(java.util.Vector employeeContacts)
	{
		this.employeeContacts.setValue(employeeContacts);
	}

	public void setGuarantorContacts(java.util.Vector guarantorContacts)
	{
		this.guarantorContacts.setValue(guarantorContacts);
	}

	public void setOwnerAccountDetails(com.profitera.descriptor.db.account.AccountOwnerDetails ownerAccountDetails)
	{
		this.ownerAccountDetails.setValue(ownerAccountDetails);
	}

	public void setOwnerAgency(com.profitera.descriptor.db.client.Agency ownerAgency)
	{
		this.ownerAgency.setValue(ownerAgency);
	}

	public void setOwnerBusinessUnit(com.profitera.descriptor.db.user.BusinessUnit ownerBusinessUnit)
	{
		this.ownerBusinessUnit.setValue(ownerBusinessUnit);
	}

	public void setOwnerCustomer(com.profitera.descriptor.db.account.Customer ownerCustomer)
	{
		this.ownerCustomer.setValue(ownerCustomer);
	}

	public void setOwnerEmployee(com.profitera.descriptor.db.user.Employee ownerEmployee)
	{
		this.ownerEmployee.setValue(ownerEmployee);
	}

	public void setPreferredPosition(java.lang.Double preferredPosition)
	{
		this.preferredPosition = preferredPosition;
	}

	public void setRemarks(java.lang.String remarks)
	{
		this.remarks = remarks;
	}

	public void setSalutationTypeRef(com.profitera.descriptor.db.reference.SalutationTypeRef salutationTypeRef)
	{
		this.salutationTypeRef.setValue(salutationTypeRef);
	}

	public void setSection(java.lang.String section)
	{
		this.section = section;
	}

	public void setStateRef(com.profitera.descriptor.db.reference.StateRef stateRef)
	{
		this.stateRef.setValue(stateRef);
	}

	public void setStreetName(java.lang.String streetName)
	{
		this.streetName = streetName;
	}

	public void setZipCode(java.lang.String zipCode)
	{
		this.zipCode = zipCode;
	}
}
