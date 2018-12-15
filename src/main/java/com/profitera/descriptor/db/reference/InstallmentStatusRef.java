// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class InstallmentStatusRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String INST_STATUS_CODE = "instStatusCode";
	public static final String INST_STATUS_DESC = "instStatusDesc";
	public static final String INST_STATUS_ID = "instStatusId";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.String instStatusCode;

	private java.lang.String instStatusDesc;

	private java.lang.Double instStatusId;

	private java.lang.Double sortPriority;

	public  InstallmentStatusRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.String getInstStatusCode()
	{
		return instStatusCode;
	}

	public java.lang.String getInstStatusDesc()
	{
		return instStatusDesc;
	}

	public java.lang.Double getInstStatusId()
	{
		return instStatusId;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setInstStatusCode(java.lang.String instStatusCode)
	{
		this.instStatusCode = instStatusCode;
	}

	public void setInstStatusDesc(java.lang.String instStatusDesc)
	{
		this.instStatusDesc = instStatusDesc;
	}

	public void setInstStatusId(java.lang.Double instStatusId)
	{
		this.instStatusId = instStatusId;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
