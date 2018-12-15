// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class DirectDebitStatusRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DIRECT_DEBIT_STATUS_CODE = "directDebitStatusCode";
	public static final String DIRECT_DEBIT_STATUS_DESC = "directDebitStatusDesc";
	public static final String DIRECT_DEBIT_STATUS_ID = "directDebitStatusId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String directDebitStatusCode;

	private java.lang.String directDebitStatusDesc;

	private java.lang.Double directDebitStatusId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  DirectDebitStatusRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getDirectDebitStatusCode()
	{
		return directDebitStatusCode;
	}

	public java.lang.String getDirectDebitStatusDesc()
	{
		return directDebitStatusDesc;
	}

	public java.lang.Double getDirectDebitStatusId()
	{
		return directDebitStatusId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDirectDebitStatusCode(java.lang.String directDebitStatusCode)
	{
		this.directDebitStatusCode = directDebitStatusCode;
	}

	public void setDirectDebitStatusDesc(java.lang.String directDebitStatusDesc)
	{
		this.directDebitStatusDesc = directDebitStatusDesc;
	}

	public void setDirectDebitStatusId(java.lang.Double directDebitStatusId)
	{
		this.directDebitStatusId = directDebitStatusId;
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
