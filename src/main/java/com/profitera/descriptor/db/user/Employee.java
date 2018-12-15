// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.user;

public class Employee
	implements java.io.Serializable
{
	// Generated constants
	public static final String ADDRESS_DETAILS = "addressDetails";
	public static final String AVERAGE_COLLECTION = "averageCollection";
	public static final String CITIZENSHIP = "citizenship";
	public static final String COLLECTABILITY_REF = "collectabilityRef";
	public static final String CONTACTS = "contacts";
	public static final String DATE_JOINED_IN_CCC = "dateJoinedInCcc";
	public static final String DATE_OF_BIRTH = "dateOfBirth";
	public static final String DEPARTMENT = "department";
	public static final String DESIGNATION = "designation";
	public static final String EDUCATION_ATTAINMENT = "educationAttainment";
	public static final String EMPLOYEE_ID = "employeeId";
	public static final String EMPLOYEE_PROFILE_REF = "employeeProfileRef";
	public static final String EMPLOYEE_TYPE_REF = "employeeTypeRef";
	public static final String GRADE = "grade";
	public static final String HIRED_DATE = "hiredDate";
	public static final String IDENTITY_NUMBER = "identityNumber";
	public static final String LANGUAGE_PROFICIENCY = "languageProficiency";
	public static final String OLD_IDENTITY_NUMBER = "oldIdentityNumber";
	public static final String PASSPORT_NUMBER = "passportNumber";
	public static final String PREVIOUS_DEPARTMENT = "previousDepartment";
	public static final String REMARKS = "remarks";
	public static final String REPORTS_TO = "reportsTo";
	public static final String RETIRED_DATE = "retiredDate";
	public static final String SEX_REF = "sexRef";
	public static final String SKILL_LEVEL = "skillLevel";
	public static final String TRANSFER_DATE = "transferDate";
	public static final String USERS = "users";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface addressDetails= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double averageCollection;

	private java.lang.String citizenship;

	private oracle.toplink.indirection.ValueHolderInterface collectabilityRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface contacts= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.sql.Timestamp dateJoinedInCcc;

	private java.sql.Timestamp dateOfBirth;

	private java.lang.String department;

	private java.lang.String designation;

	private java.lang.String educationAttainment;

	private java.lang.String employeeId;

	private oracle.toplink.indirection.ValueHolderInterface employeeProfileRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface employeeTypeRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.String grade;

	private java.sql.Timestamp hiredDate;

	private java.lang.String identityNumber;

	private java.lang.String languageProficiency;

	private java.lang.String oldIdentityNumber;

	private java.lang.String passportNumber;

	private java.lang.String previousDepartment;

	private java.lang.String remarks;

	private oracle.toplink.indirection.ValueHolderInterface reportsTo= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.sql.Timestamp retiredDate;

	private oracle.toplink.indirection.ValueHolderInterface sexRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.String skillLevel;

	private java.sql.Timestamp transferDate;

	private oracle.toplink.indirection.ValueHolderInterface users= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	public  Employee()
	{
		// Fill in method body here.
	}

	public com.profitera.descriptor.db.contact.AddressDetails getAddressDetails()
	{
		return (com.profitera.descriptor.db.contact.AddressDetails) addressDetails.getValue();
	}

	public java.lang.Double getAverageCollection()
	{
		return averageCollection;
	}

	public java.lang.String getCitizenship()
	{
		return citizenship;
	}

	public com.profitera.descriptor.db.reference.CollectabilityRef getCollectabilityRef()
	{
		return (com.profitera.descriptor.db.reference.CollectabilityRef) collectabilityRef.getValue();
	}

	public java.util.Vector getContacts()
	{
		return (java.util.Vector) contacts.getValue();
	}

	public java.sql.Timestamp getDateJoinedInCcc()
	{
		return dateJoinedInCcc;
	}

	public java.sql.Timestamp getDateOfBirth()
	{
		return dateOfBirth;
	}

	public java.lang.String getDepartment()
	{
		return department;
	}

	public java.lang.String getDesignation()
	{
		return designation;
	}

	public java.lang.String getEducationAttainment()
	{
		return educationAttainment;
	}

	public java.lang.String getEmployeeId()
	{
		return employeeId;
	}

	public com.profitera.descriptor.db.reference.EmployeeProfileRef getEmployeeProfileRef()
	{
		return (com.profitera.descriptor.db.reference.EmployeeProfileRef) employeeProfileRef.getValue();
	}

	public com.profitera.descriptor.db.reference.EmployeeTypeRef getEmployeeTypeRef()
	{
		return (com.profitera.descriptor.db.reference.EmployeeTypeRef) employeeTypeRef.getValue();
	}

	public java.lang.String getGrade()
	{
		return grade;
	}

	public java.sql.Timestamp getHiredDate()
	{
		return hiredDate;
	}

	public java.lang.String getIdentityNumber()
	{
		return identityNumber;
	}

	public java.lang.String getLanguageProficiency()
	{
		return languageProficiency;
	}

	public java.lang.String getOldIdentityNumber()
	{
		return oldIdentityNumber;
	}

	public java.lang.String getPassportNumber()
	{
		return passportNumber;
	}

	public java.lang.String getPreviousDepartment()
	{
		return previousDepartment;
	}

	public java.lang.String getRemarks()
	{
		return remarks;
	}

	public java.util.Vector getReportsTo()
	{
		return (java.util.Vector) reportsTo.getValue();
	}

	public java.sql.Timestamp getRetiredDate()
	{
		return retiredDate;
	}

	public com.profitera.descriptor.db.reference.AccountSexRef getSexRef()
	{
		return (com.profitera.descriptor.db.reference.AccountSexRef) sexRef.getValue();
	}

	public java.lang.String getSkillLevel()
	{
		return skillLevel;
	}

	public java.sql.Timestamp getTransferDate()
	{
		return transferDate;
	}

	public java.util.Vector getUsers()
	{
		return (java.util.Vector) users.getValue();
	}

	public void setAddressDetails(com.profitera.descriptor.db.contact.AddressDetails addressDetails)
	{
		this.addressDetails.setValue(addressDetails);
	}

	public void setAverageCollection(java.lang.Double averageCollection)
	{
		this.averageCollection = averageCollection;
	}

	public void setCitizenship(java.lang.String citizenship)
	{
		this.citizenship = citizenship;
	}

	public void setCollectabilityRef(com.profitera.descriptor.db.reference.CollectabilityRef collectabilityRef)
	{
		this.collectabilityRef.setValue(collectabilityRef);
	}

	public void setContacts(java.util.Vector contacts)
	{
		this.contacts.setValue(contacts);
	}

	public void setDateJoinedInCcc(java.sql.Timestamp dateJoinedInCcc)
	{
		this.dateJoinedInCcc = dateJoinedInCcc;
	}

	public void setDateOfBirth(java.sql.Timestamp dateOfBirth)
	{
		this.dateOfBirth = dateOfBirth;
	}

	public void setDepartment(java.lang.String department)
	{
		this.department = department;
	}

	public void setDesignation(java.lang.String designation)
	{
		this.designation = designation;
	}

	public void setEducationAttainment(java.lang.String educationAttainment)
	{
		this.educationAttainment = educationAttainment;
	}

	public void setEmployeeId(java.lang.String employeeId)
	{
		this.employeeId = employeeId;
	}

	public void setEmployeeProfileRef(com.profitera.descriptor.db.reference.EmployeeProfileRef employeeProfileRef)
	{
		this.employeeProfileRef.setValue(employeeProfileRef);
	}

	public void setEmployeeTypeRef(com.profitera.descriptor.db.reference.EmployeeTypeRef employeeTypeRef)
	{
		this.employeeTypeRef.setValue(employeeTypeRef);
	}

	public void setGrade(java.lang.String grade)
	{
		this.grade = grade;
	}

	public void setHiredDate(java.sql.Timestamp hiredDate)
	{
		this.hiredDate = hiredDate;
	}

	public void setIdentityNumber(java.lang.String identityNumber)
	{
		this.identityNumber = identityNumber;
	}

	public void setLanguageProficiency(java.lang.String languageProficiency)
	{
		this.languageProficiency = languageProficiency;
	}

	public void setOldIdentityNumber(java.lang.String oldIdentityNumber)
	{
		this.oldIdentityNumber = oldIdentityNumber;
	}

	public void setPassportNumber(java.lang.String passportNumber)
	{
		this.passportNumber = passportNumber;
	}

	public void setPreviousDepartment(java.lang.String previousDepartment)
	{
		this.previousDepartment = previousDepartment;
	}

	public void setRemarks(java.lang.String remarks)
	{
		this.remarks = remarks;
	}

	public void setReportsTo(java.util.Vector reportsTo)
	{
		this.reportsTo.setValue(reportsTo);
	}

	public void setRetiredDate(java.sql.Timestamp retiredDate)
	{
		this.retiredDate = retiredDate;
	}

	public void setSexRef(com.profitera.descriptor.db.reference.AccountSexRef sexRef)
	{
		this.sexRef.setValue(sexRef);
	}

	public void setSkillLevel(java.lang.String skillLevel)
	{
		this.skillLevel = skillLevel;
	}

	public void setTransferDate(java.sql.Timestamp transferDate)
	{
		this.transferDate = transferDate;
	}

	public void setUsers(java.util.Vector users)
	{
		this.users.setValue(users);
	}
}
