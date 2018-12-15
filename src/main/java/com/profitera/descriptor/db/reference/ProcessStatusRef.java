// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class ProcessStatusRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String PROC_STATUS_CODE = "procStatusCode";
	public static final String PROC_STATUS_DESC = "procStatusDesc";
	public static final String PROC_STATUS_ID = "procStatusId";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.String procStatusCode;

	private java.lang.String procStatusDesc;

	private java.lang.Double procStatusId;

	private java.lang.Double sortPriority;

	public  ProcessStatusRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.String getProcStatusCode()
	{
		return procStatusCode;
	}

	public java.lang.String getProcStatusDesc()
	{
		return procStatusDesc;
	}

	public java.lang.Double getProcStatusId()
	{
		return procStatusId;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setProcStatusCode(java.lang.String procStatusCode)
	{
		this.procStatusCode = procStatusCode;
	}

	public void setProcStatusDesc(java.lang.String procStatusDesc)
	{
		this.procStatusDesc = procStatusDesc;
	}

	public void setProcStatusId(java.lang.Double procStatusId)
	{
		this.procStatusId = procStatusId;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
