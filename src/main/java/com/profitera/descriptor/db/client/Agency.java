// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.client;

public class Agency
	implements java.io.Serializable
{
	// Generated constants
	public static final String ADDRESS_DETAILS = "addressDetails";
	public static final String AGENCY_CHGS_TO_DATE = "agencyChgsToDate";
	public static final String AGENCY_DESC = "agencyDesc";
	public static final String AGENCY_EFFECTIVE_DATE = "agencyEffectiveDate";
	public static final String AGENCY_END_DATE = "agencyEndDate";
	public static final String AGENCY_EXT_INT = "agencyExtInt";
	public static final String AGENCY_ID = "agencyId";
	public static final String AGENCY_NAME = "agencyName";
	public static final String AGENCY_PAID_TO_DATE = "agencyPaidToDate";
	public static final String AGENCY_REGION_REF = "agencyRegionRef";
	public static final String AGENCY_STATUS_REF = "agencyStatusRef";
	public static final String AGENCY_TOTAL_DEPOSIT = "agencyTotalDeposit";
	public static final String AGENCY_TYPE_REF = "agencyTypeRef";
	public static final String BATCH_PAYMENTS = "batchPayments";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface addressDetails= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double agencyChgsToDate;

	private java.lang.String agencyDesc;

	private java.sql.Timestamp agencyEffectiveDate;

	private java.sql.Timestamp agencyEndDate;

	private java.lang.Double agencyExtInt;

	private java.lang.Double agencyId;

	private java.lang.String agencyName;

	private java.lang.Double agencyPaidToDate;

	private java.lang.Double agencyTotalDeposit;

	private oracle.toplink.indirection.ValueHolderInterface agencyTypeRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface batchPayments= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  Agency()
	{
		// Fill in method body here.
	}

	public com.profitera.descriptor.db.contact.AddressDetails getAddressDetails()
	{
		return (com.profitera.descriptor.db.contact.AddressDetails) addressDetails.getValue();
	}

	public java.lang.Double getAgencyChgsToDate()
	{
		return agencyChgsToDate;
	}

	public java.lang.String getAgencyDesc()
	{
		return agencyDesc;
	}

	public java.sql.Timestamp getAgencyEffectiveDate()
	{
		return agencyEffectiveDate;
	}

	public java.sql.Timestamp getAgencyEndDate()
	{
		return agencyEndDate;
	}

	public java.lang.Double getAgencyExtInt()
	{
		return agencyExtInt;
	}

	public java.lang.Double getAgencyId()
	{
		return agencyId;
	}

	public java.lang.String getAgencyName()
	{
		return agencyName;
	}

	public java.lang.Double getAgencyPaidToDate()
	{
		return agencyPaidToDate;
	}

	public java.lang.Double getAgencyTotalDeposit()
	{
		return agencyTotalDeposit;
	}

	public com.profitera.descriptor.db.reference.AgencyTypeRef getAgencyTypeRef()
	{
		return (com.profitera.descriptor.db.reference.AgencyTypeRef) agencyTypeRef.getValue();
	}

	public java.util.Vector getBatchPayments()
	{
		return (java.util.Vector) batchPayments.getValue();
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setAddressDetails(com.profitera.descriptor.db.contact.AddressDetails addressDetails)
	{
		this.addressDetails.setValue(addressDetails);
	}

	public void setAgencyChgsToDate(java.lang.Double agencyChgsToDate)
	{
		this.agencyChgsToDate = agencyChgsToDate;
	}

	public void setAgencyDesc(java.lang.String agencyDesc)
	{
		this.agencyDesc = agencyDesc;
	}

	public void setAgencyEffectiveDate(java.sql.Timestamp agencyEffectiveDate)
	{
		this.agencyEffectiveDate = agencyEffectiveDate;
	}

	public void setAgencyEndDate(java.sql.Timestamp agencyEndDate)
	{
		this.agencyEndDate = agencyEndDate;
	}

	public void setAgencyExtInt(java.lang.Double agencyExtInt)
	{
		this.agencyExtInt = agencyExtInt;
	}

	public void setAgencyId(java.lang.Double agencyId)
	{
		this.agencyId = agencyId;
	}

	public void setAgencyName(java.lang.String agencyName)
	{
		this.agencyName = agencyName;
	}

	public void setAgencyPaidToDate(java.lang.Double agencyPaidToDate)
	{
		this.agencyPaidToDate = agencyPaidToDate;
	}

	public void setAgencyTotalDeposit(java.lang.Double agencyTotalDeposit)
	{
		this.agencyTotalDeposit = agencyTotalDeposit;
	}

	public void setAgencyTypeRef(com.profitera.descriptor.db.reference.AgencyTypeRef agencyTypeRef)
	{
		this.agencyTypeRef.setValue(agencyTypeRef);
	}

	public void setBatchPayments(java.util.Vector batchPayments)
	{
		this.batchPayments.setValue(batchPayments);
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
