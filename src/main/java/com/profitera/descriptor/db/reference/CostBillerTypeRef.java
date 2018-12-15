// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class CostBillerTypeRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String COST_BILLER_TYPE_CODE = "costBillerTypeCode";
	public static final String COST_BILLER_TYPE_DESC = "costBillerTypeDesc";
	public static final String COST_BILLER_TYPE_ID = "costBillerTypeId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String costBillerTypeCode;

	private java.lang.String costBillerTypeDesc;

	private java.lang.Double costBillerTypeId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  CostBillerTypeRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getCostBillerTypeCode()
	{
		return costBillerTypeCode;
	}

	public java.lang.String getCostBillerTypeDesc()
	{
		return costBillerTypeDesc;
	}

	public java.lang.Double getCostBillerTypeId()
	{
		return costBillerTypeId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setCostBillerTypeCode(java.lang.String costBillerTypeCode)
	{
		this.costBillerTypeCode = costBillerTypeCode;
	}

	public void setCostBillerTypeDesc(java.lang.String costBillerTypeDesc)
	{
		this.costBillerTypeDesc = costBillerTypeDesc;
	}

	public void setCostBillerTypeId(java.lang.Double costBillerTypeId)
	{
		this.costBillerTypeId = costBillerTypeId;
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
