// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class SensitiveStatusRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String SENSE_STATUS_CODE = "senseStatusCode";
	public static final String SENSE_STATUS_DESC = "senseStatusDesc";
	public static final String SENSE_STATUS_ID = "senseStatusId";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.String senseStatusCode;

	private java.lang.String senseStatusDesc;

	private java.lang.Double senseStatusId;

	private java.lang.Double sortPriority;

	public  SensitiveStatusRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.String getSenseStatusCode()
	{
		return senseStatusCode;
	}

	public java.lang.String getSenseStatusDesc()
	{
		return senseStatusDesc;
	}

	public java.lang.Double getSenseStatusId()
	{
		return senseStatusId;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setSenseStatusCode(java.lang.String senseStatusCode)
	{
		this.senseStatusCode = senseStatusCode;
	}

	public void setSenseStatusDesc(java.lang.String senseStatusDesc)
	{
		this.senseStatusDesc = senseStatusDesc;
	}

	public void setSenseStatusId(java.lang.Double senseStatusId)
	{
		this.senseStatusId = senseStatusId;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
