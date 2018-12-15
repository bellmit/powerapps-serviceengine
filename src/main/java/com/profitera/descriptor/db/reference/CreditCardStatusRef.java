// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class CreditCardStatusRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String CREDIT_CARD_STATUS_CODE = "creditCardStatusCode";
	public static final String CREDIT_CARD_STATUS_DESC = "creditCardStatusDesc";
	public static final String CREDIT_CARD_STATUS_ID = "creditCardStatusId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String creditCardStatusCode;

	private java.lang.String creditCardStatusDesc;

	private java.lang.Double creditCardStatusId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  CreditCardStatusRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getCreditCardStatusCode()
	{
		return creditCardStatusCode;
	}

	public java.lang.String getCreditCardStatusDesc()
	{
		return creditCardStatusDesc;
	}

	public java.lang.Double getCreditCardStatusId()
	{
		return creditCardStatusId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setCreditCardStatusCode(java.lang.String creditCardStatusCode)
	{
		this.creditCardStatusCode = creditCardStatusCode;
	}

	public void setCreditCardStatusDesc(java.lang.String creditCardStatusDesc)
	{
		this.creditCardStatusDesc = creditCardStatusDesc;
	}

	public void setCreditCardStatusId(java.lang.Double creditCardStatusId)
	{
		this.creditCardStatusId = creditCardStatusId;
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
