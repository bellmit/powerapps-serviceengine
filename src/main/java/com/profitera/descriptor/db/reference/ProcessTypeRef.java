// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class ProcessTypeRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String PROCESS_TYPE_CODE = "processTypeCode";
	public static final String PROCESS_TYPE_DESC = "processTypeDesc";
	public static final String PROCESS_TYPE_ID = "processTypeId";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.String processTypeCode;

	private java.lang.String processTypeDesc;

	private java.lang.Double processTypeId;

	private java.lang.Double sortPriority;

	public  ProcessTypeRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.String getProcessTypeCode()
	{
		return processTypeCode;
	}

	public java.lang.String getProcessTypeDesc()
	{
		return processTypeDesc;
	}

	public java.lang.Double getProcessTypeId()
	{
		return processTypeId;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setProcessTypeCode(java.lang.String processTypeCode)
	{
		this.processTypeCode = processTypeCode;
	}

	public void setProcessTypeDesc(java.lang.String processTypeDesc)
	{
		this.processTypeDesc = processTypeDesc;
	}

	public void setProcessTypeId(java.lang.Double processTypeId)
	{
		this.processTypeId = processTypeId;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
