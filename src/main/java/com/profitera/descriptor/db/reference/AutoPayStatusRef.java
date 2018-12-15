// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class AutoPayStatusRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String AUTO_PAY_STATUS_CODE = "autoPayStatusCode";
	public static final String AUTO_PAY_STATUS_DESC = "autoPayStatusDesc";
	public static final String AUTO_PAY_STATUS_ID = "autoPayStatusId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String autoPayStatusCode;

	private java.lang.String autoPayStatusDesc;

	private java.lang.Double autoPayStatusId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  AutoPayStatusRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getAutoPayStatusCode()
	{
		return autoPayStatusCode;
	}

	public java.lang.String getAutoPayStatusDesc()
	{
		return autoPayStatusDesc;
	}

	public java.lang.Double getAutoPayStatusId()
	{
		return autoPayStatusId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setAutoPayStatusCode(java.lang.String autoPayStatusCode)
	{
		this.autoPayStatusCode = autoPayStatusCode;
	}

	public void setAutoPayStatusDesc(java.lang.String autoPayStatusDesc)
	{
		this.autoPayStatusDesc = autoPayStatusDesc;
	}

	public void setAutoPayStatusId(java.lang.Double autoPayStatusId)
	{
		this.autoPayStatusId = autoPayStatusId;
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
