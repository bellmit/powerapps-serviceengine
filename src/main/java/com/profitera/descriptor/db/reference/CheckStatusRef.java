// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class CheckStatusRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String CHECK_STATUS_CODE = "checkStatusCode";
	public static final String CHECK_STATUS_DESC = "checkStatusDesc";
	public static final String CHECK_STATUS_ID = "checkStatusId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String checkStatusCode;

	private java.lang.String checkStatusDesc;

	private java.lang.Double checkStatusId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  CheckStatusRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getCheckStatusCode()
	{
		return checkStatusCode;
	}

	public java.lang.String getCheckStatusDesc()
	{
		return checkStatusDesc;
	}

	public java.lang.Double getCheckStatusId()
	{
		return checkStatusId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setCheckStatusCode(java.lang.String checkStatusCode)
	{
		this.checkStatusCode = checkStatusCode;
	}

	public void setCheckStatusDesc(java.lang.String checkStatusDesc)
	{
		this.checkStatusDesc = checkStatusDesc;
	}

	public void setCheckStatusId(java.lang.Double checkStatusId)
	{
		this.checkStatusId = checkStatusId;
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
