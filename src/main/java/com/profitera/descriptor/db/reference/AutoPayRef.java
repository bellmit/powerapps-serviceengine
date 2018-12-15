// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class AutoPayRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String AUTO_PAY_CODE = "autoPayCode";
	public static final String AUTO_PAY_DESC = "autoPayDesc";
	public static final String AUTO_PAY_ID = "autoPayId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String autoPayCode;

	private java.lang.String autoPayDesc;

	private java.lang.Double autoPayId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  AutoPayRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getAutoPayCode()
	{
		return autoPayCode;
	}

	public java.lang.String getAutoPayDesc()
	{
		return autoPayDesc;
	}

	public java.lang.Double getAutoPayId()
	{
		return autoPayId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setAutoPayCode(java.lang.String autoPayCode)
	{
		this.autoPayCode = autoPayCode;
	}

	public void setAutoPayDesc(java.lang.String autoPayDesc)
	{
		this.autoPayDesc = autoPayDesc;
	}

	public void setAutoPayId(java.lang.Double autoPayId)
	{
		this.autoPayId = autoPayId;
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
