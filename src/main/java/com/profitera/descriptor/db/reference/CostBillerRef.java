// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class CostBillerRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String COST_BILLER_ID = "costBillerId";
	public static final String COST_BILLER_TYPE_REF = "costBillerTypeRef";
	public static final String COST_PER_UNIT = "costPerUnit";
	public static final String COST_REMARKS = "costRemarks";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.Double costBillerId;

	private oracle.toplink.indirection.ValueHolderInterface costBillerTypeRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double costPerUnit;

	private java.lang.String costRemarks;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  CostBillerRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getCostBillerId()
	{
		return costBillerId;
	}

	public com.profitera.descriptor.db.reference.CostBillerTypeRef getCostBillerTypeRef()
	{
		return (com.profitera.descriptor.db.reference.CostBillerTypeRef) costBillerTypeRef.getValue();
	}

	public java.lang.Double getCostPerUnit()
	{
		return costPerUnit;
	}

	public java.lang.String getCostRemarks()
	{
		return costRemarks;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setCostBillerId(java.lang.Double costBillerId)
	{
		this.costBillerId = costBillerId;
	}

	public void setCostBillerTypeRef(com.profitera.descriptor.db.reference.CostBillerTypeRef costBillerTypeRef)
	{
		this.costBillerTypeRef.setValue(costBillerTypeRef);
	}

	public void setCostPerUnit(java.lang.Double costPerUnit)
	{
		this.costPerUnit = costPerUnit;
	}

	public void setCostRemarks(java.lang.String costRemarks)
	{
		this.costRemarks = costRemarks;
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
