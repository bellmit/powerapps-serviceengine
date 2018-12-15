// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class CollectionStatusRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String COLL_STATUS_CODE = "collStatusCode";
	public static final String COLL_STATUS_DESC = "collStatusDesc";
	public static final String COLL_STATUS_ID = "collStatusId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String collStatusCode;

	private java.lang.String collStatusDesc;

	private java.lang.Double collStatusId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  CollectionStatusRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getCollStatusCode()
	{
		return collStatusCode;
	}

	public java.lang.String getCollStatusDesc()
	{
		return collStatusDesc;
	}

	public java.lang.Double getCollStatusId()
	{
		return collStatusId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setCollStatusCode(java.lang.String collStatusCode)
	{
		this.collStatusCode = collStatusCode;
	}

	public void setCollStatusDesc(java.lang.String collStatusDesc)
	{
		this.collStatusDesc = collStatusDesc;
	}

	public void setCollStatusId(java.lang.Double collStatusId)
	{
		this.collStatusId = collStatusId;
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
