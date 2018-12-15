// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class UomMeasureRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	public static final String UOM_CATEGORY = "uomCategory";
	public static final String UOM_ID = "uomId";
	public static final String UOM_TYPE = "uomType";
	public static final String UOM_VALUE = "uomValue";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	private java.lang.String uomCategory;

	private java.lang.Double uomId;

	private java.lang.String uomType;

	private java.lang.Double uomValue;

	public  UomMeasureRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public java.lang.String getUomCategory()
	{
		return uomCategory;
	}

	public java.lang.Double getUomId()
	{
		return uomId;
	}

	public java.lang.String getUomType()
	{
		return uomType;
	}

	public java.lang.Double getUomValue()
	{
		return uomValue;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}

	public void setUomCategory(java.lang.String uomCategory)
	{
		this.uomCategory = uomCategory;
	}

	public void setUomId(java.lang.Double uomId)
	{
		this.uomId = uomId;
	}

	public void setUomType(java.lang.String uomType)
	{
		this.uomType = uomType;
	}

	public void setUomValue(java.lang.Double uomValue)
	{
		this.uomValue = uomValue;
	}
}
