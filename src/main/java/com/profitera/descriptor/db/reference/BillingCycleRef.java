// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class BillingCycleRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String BILLING_CYCLE_CODE = "billingCycleCode";
	public static final String BILLING_CYCLE_DESC = "billingCycleDesc";
	public static final String BILLING_CYCLE_ID = "billingCycleId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String billingCycleCode;

	private java.lang.String billingCycleDesc;

	private java.lang.Double billingCycleId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  BillingCycleRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getBillingCycleCode()
	{
		return billingCycleCode;
	}

	public java.lang.String getBillingCycleDesc()
	{
		return billingCycleDesc;
	}

	public java.lang.Double getBillingCycleId()
	{
		return billingCycleId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setBillingCycleCode(java.lang.String billingCycleCode)
	{
		this.billingCycleCode = billingCycleCode;
	}

	public void setBillingCycleDesc(java.lang.String billingCycleDesc)
	{
		this.billingCycleDesc = billingCycleDesc;
	}

	public void setBillingCycleId(java.lang.Double billingCycleId)
	{
		this.billingCycleId = billingCycleId;
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
