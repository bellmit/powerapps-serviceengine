// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class AgencyTypeRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String AGY_TYPE_CODE = "agyTypeCode";
	public static final String AGY_TYPE_DESC = "agyTypeDesc";
	public static final String AGY_TYPE_ID = "agyTypeId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String agyTypeCode;

	private java.lang.String agyTypeDesc;

	private java.lang.Double agyTypeId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  AgencyTypeRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getAgyTypeCode()
	{
		return agyTypeCode;
	}

	public java.lang.String getAgyTypeDesc()
	{
		return agyTypeDesc;
	}

	public java.lang.Double getAgyTypeId()
	{
		return agyTypeId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setAgyTypeCode(java.lang.String agyTypeCode)
	{
		this.agyTypeCode = agyTypeCode;
	}

	public void setAgyTypeDesc(java.lang.String agyTypeDesc)
	{
		this.agyTypeDesc = agyTypeDesc;
	}

	public void setAgyTypeId(java.lang.Double agyTypeId)
	{
		this.agyTypeId = agyTypeId;
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
