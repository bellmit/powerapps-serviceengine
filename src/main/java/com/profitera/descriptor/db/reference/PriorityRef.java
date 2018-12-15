// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class PriorityRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String PRIORITY_CODE = "priorityCode";
	public static final String PRIORITY_DESC = "priorityDesc";
	public static final String PRIORITY_ID = "priorityId";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.String priorityCode;

	private java.lang.String priorityDesc;

	private java.lang.Double priorityId;

	private java.lang.Double sortPriority;

	public  PriorityRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.String getPriorityCode()
	{
		return priorityCode;
	}

	public java.lang.String getPriorityDesc()
	{
		return priorityDesc;
	}

	public java.lang.Double getPriorityId()
	{
		return priorityId;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setPriorityCode(java.lang.String priorityCode)
	{
		this.priorityCode = priorityCode;
	}

	public void setPriorityDesc(java.lang.String priorityDesc)
	{
		this.priorityDesc = priorityDesc;
	}

	public void setPriorityId(java.lang.Double priorityId)
	{
		this.priorityId = priorityId;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
