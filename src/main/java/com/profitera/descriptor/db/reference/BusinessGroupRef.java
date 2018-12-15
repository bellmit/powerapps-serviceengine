// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class BusinessGroupRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String BUSINESS_GROUP_CODE = "businessGroupCode";
	public static final String BUSINESS_GROUP_DESC = "businessGroupDesc";
	public static final String BUSINESS_GROUP_ID = "businessGroupId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String businessGroupCode;

	private java.lang.String businessGroupDesc;

	private java.lang.Double businessGroupId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  BusinessGroupRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getBusinessGroupCode()
	{
		return businessGroupCode;
	}

	public java.lang.String getBusinessGroupDesc()
	{
		return businessGroupDesc;
	}

	public java.lang.Double getBusinessGroupId()
	{
		return businessGroupId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setBusinessGroupCode(java.lang.String businessGroupCode)
	{
		this.businessGroupCode = businessGroupCode;
	}

	public void setBusinessGroupDesc(java.lang.String businessGroupDesc)
	{
		this.businessGroupDesc = businessGroupDesc;
	}

	public void setBusinessGroupId(java.lang.Double businessGroupId)
	{
		this.businessGroupId = businessGroupId;
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
