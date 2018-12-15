// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.account;

public class AccountOwnerDetails
	implements java.io.Serializable
{
	// Generated constants
	public static final String ACCOUNT = "account";
	public static final String ADDRESS_DETAILS = "addressDetails";
	public static final String ANNUAL_INCOME = "annualIncome";
	public static final String BUSINESS_REG_DATE = "businessRegDate";
	public static final String BUSINESS_REG_NUMBER = "businessRegNumber";
	public static final String CITIZENSHIP_REF = "citizenshipRef";
	public static final String DATE_OF_BIRTH = "dateOfBirth";
	public static final String DESIGNATION = "designation";
	public static final String EMBOSSED_NAME = "embossedName";
	public static final String EMPLOYEMENT_DATE = "employementDate";
	public static final String IDENTITY_NUMBER = "identityNumber";
	public static final String IMAGES = "images";
	public static final String MARITAL_STATUS_REF = "maritalStatusRef";
	public static final String MILITARY_NUMBER = "militaryNumber";
	public static final String NO_OF_DEPENDENTS = "noOfDependents";
	public static final String OCCUPATION_REF = "occupationRef";
	public static final String OLD_IDENTITY_NUMBER = "oldIdentityNumber";
	public static final String PASSPORT_NUMBER = "passportNumber";
	public static final String PREMISES_REF = "premisesRef";
	public static final String RACE_TYPE_REF = "raceTypeRef";
	public static final String SEX_REF = "sexRef";
	public static final String SHORT_NAME = "shortName";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface account= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface addressDetails= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double annualIncome;

	private java.sql.Timestamp businessRegDate;

	private java.lang.String businessRegNumber;

	private oracle.toplink.indirection.ValueHolderInterface citizenshipRef= new oracle.toplink.indirection.ValueHolder();

	private java.sql.Timestamp dateOfBirth;

	private java.lang.String designation;

	private java.lang.String embossedName;

	private java.sql.Timestamp employementDate;

	private java.lang.String identityNumber;

	private oracle.toplink.indirection.ValueHolderInterface maritalStatusRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.String militaryNumber;

	private java.lang.Double noOfDependents;

	private oracle.toplink.indirection.ValueHolderInterface occupationRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.String oldIdentityNumber;

	private java.lang.String passportNumber;

	private oracle.toplink.indirection.ValueHolderInterface premisesRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface raceTypeRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface sexRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.String shortName;

	public  AccountOwnerDetails()
	{
		// Fill in method body here.
	}

	public com.profitera.descriptor.db.account.Account getAccount()
	{
		return (com.profitera.descriptor.db.account.Account) account.getValue();
	}

	public com.profitera.descriptor.db.contact.AddressDetails getAddressDetails()
	{
		return (com.profitera.descriptor.db.contact.AddressDetails) addressDetails.getValue();
	}

	public java.lang.Double getAnnualIncome()
	{
		return annualIncome;
	}

	public java.sql.Timestamp getBusinessRegDate()
	{
		return businessRegDate;
	}

	public java.lang.String getBusinessRegNumber()
	{
		return businessRegNumber;
	}

	public com.profitera.descriptor.db.reference.CitizenshipRef getCitizenshipRef()
	{
		return (com.profitera.descriptor.db.reference.CitizenshipRef) citizenshipRef.getValue();
	}

	public java.sql.Timestamp getDateOfBirth()
	{
		return dateOfBirth;
	}

	public java.lang.String getDesignation()
	{
		return designation;
	}

	public java.lang.String getEmbossedName()
	{
		return embossedName;
	}

	public java.sql.Timestamp getEmployementDate()
	{
		return employementDate;
	}

	public java.lang.String getIdentityNumber()
	{
		return identityNumber;
	}

	public com.profitera.descriptor.db.reference.MaritalStatusRef getMaritalStatusRef()
	{
		return (com.profitera.descriptor.db.reference.MaritalStatusRef) maritalStatusRef.getValue();
	}

	public java.lang.String getMilitaryNumber()
	{
		return militaryNumber;
	}

	public java.lang.Double getNoOfDependents()
	{
		return noOfDependents;
	}

	public com.profitera.descriptor.db.reference.OccupationRef getOccupationRef()
	{
		return (com.profitera.descriptor.db.reference.OccupationRef) occupationRef.getValue();
	}

	public java.lang.String getOldIdentityNumber()
	{
		return oldIdentityNumber;
	}

	public java.lang.String getPassportNumber()
	{
		return passportNumber;
	}

	public com.profitera.descriptor.db.reference.PremisesRef getPremisesRef()
	{
		return (com.profitera.descriptor.db.reference.PremisesRef) premisesRef.getValue();
	}

	public com.profitera.descriptor.db.reference.RaceTypeRef getRaceTypeRef()
	{
		return (com.profitera.descriptor.db.reference.RaceTypeRef) raceTypeRef.getValue();
	}

	public com.profitera.descriptor.db.reference.AccountSexRef getSexRef()
	{
		return (com.profitera.descriptor.db.reference.AccountSexRef) sexRef.getValue();
	}

	public java.lang.String getShortName()
	{
		return shortName;
	}

	public void setAccount(com.profitera.descriptor.db.account.Account account)
	{
		this.account.setValue(account);
	}

	public void setAddressDetails(com.profitera.descriptor.db.contact.AddressDetails addressDetails)
	{
		this.addressDetails.setValue(addressDetails);
	}

	public void setAnnualIncome(java.lang.Double annualIncome)
	{
		this.annualIncome = annualIncome;
	}

	public void setBusinessRegDate(java.sql.Timestamp businessRegDate)
	{
		this.businessRegDate = businessRegDate;
	}

	public void setBusinessRegNumber(java.lang.String businessRegNumber)
	{
		this.businessRegNumber = businessRegNumber;
	}

	public void setCitizenshipRef(com.profitera.descriptor.db.reference.CitizenshipRef citizenshipRef)
	{
		this.citizenshipRef.setValue(citizenshipRef);
	}

	public void setDateOfBirth(java.sql.Timestamp dateOfBirth)
	{
		this.dateOfBirth = dateOfBirth;
	}

	public void setDesignation(java.lang.String designation)
	{
		this.designation = designation;
	}

	public void setEmbossedName(java.lang.String embossedName)
	{
		this.embossedName = embossedName;
	}

	public void setEmployementDate(java.sql.Timestamp employementDate)
	{
		this.employementDate = employementDate;
	}

	public void setIdentityNumber(java.lang.String identityNumber)
	{
		this.identityNumber = identityNumber;
	}

	public void setMaritalStatusRef(com.profitera.descriptor.db.reference.MaritalStatusRef maritalStatusRef)
	{
		this.maritalStatusRef.setValue(maritalStatusRef);
	}

	public void setMilitaryNumber(java.lang.String militaryNumber)
	{
		this.militaryNumber = militaryNumber;
	}

	public void setNoOfDependents(java.lang.Double noOfDependents)
	{
		this.noOfDependents = noOfDependents;
	}

	public void setOccupationRef(com.profitera.descriptor.db.reference.OccupationRef occupationRef)
	{
		this.occupationRef.setValue(occupationRef);
	}

	public void setOldIdentityNumber(java.lang.String oldIdentityNumber)
	{
		this.oldIdentityNumber = oldIdentityNumber;
	}

	public void setPassportNumber(java.lang.String passportNumber)
	{
		this.passportNumber = passportNumber;
	}

	public void setPremisesRef(com.profitera.descriptor.db.reference.PremisesRef premisesRef)
	{
		this.premisesRef.setValue(premisesRef);
	}

	public void setRaceTypeRef(com.profitera.descriptor.db.reference.RaceTypeRef raceTypeRef)
	{
		this.raceTypeRef.setValue(raceTypeRef);
	}

	public void setSexRef(com.profitera.descriptor.db.reference.AccountSexRef sexRef)
	{
		this.sexRef.setValue(sexRef);
	}

	public void setShortName(java.lang.String shortName)
	{
		this.shortName = shortName;
	}
}
