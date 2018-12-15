// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class CostingTypeRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String COST_TYPE_CODE = "costTypeCode";
	public static final String COST_TYPE_DESC = "costTypeDesc";
	public static final String COST_TYPE_ID = "costTypeId";
	public static final String COST_TYPE_PARENT = "costTypeParent";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String costTypeCode;

	private java.lang.String costTypeDesc;

	private java.lang.Double costTypeId;

	private java.lang.Double costTypeParent;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  CostingTypeRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getCostTypeCode()
	{
		return costTypeCode;
	}

	public java.lang.String getCostTypeDesc()
	{
		return costTypeDesc;
	}

	public java.lang.Double getCostTypeId()
	{
		return costTypeId;
	}

	public java.lang.Double getCostTypeParent()
	{
		return costTypeParent;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setCostTypeCode(java.lang.String costTypeCode)
	{
		this.costTypeCode = costTypeCode;
	}

	public void setCostTypeDesc(java.lang.String costTypeDesc)
	{
		this.costTypeDesc = costTypeDesc;
	}

	public void setCostTypeId(java.lang.Double costTypeId)
	{
		this.costTypeId = costTypeId;
	}

	public void setCostTypeParent(java.lang.Double costTypeParent)
	{
		this.costTypeParent = costTypeParent;
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
