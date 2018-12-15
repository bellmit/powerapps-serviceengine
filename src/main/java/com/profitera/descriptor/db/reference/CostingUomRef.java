// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class CostingUomRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String COST_UOM_CODE = "costUomCode";
	public static final String COST_UOM_DESC = "costUomDesc";
	public static final String COST_UOM_ID = "costUomId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String costUomCode;

	private java.lang.String costUomDesc;

	private java.lang.Double costUomId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  CostingUomRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getCostUomCode()
	{
		return costUomCode;
	}

	public java.lang.String getCostUomDesc()
	{
		return costUomDesc;
	}

	public java.lang.Double getCostUomId()
	{
		return costUomId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setCostUomCode(java.lang.String costUomCode)
	{
		this.costUomCode = costUomCode;
	}

	public void setCostUomDesc(java.lang.String costUomDesc)
	{
		this.costUomDesc = costUomDesc;
	}

	public void setCostUomId(java.lang.Double costUomId)
	{
		this.costUomId = costUomId;
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
