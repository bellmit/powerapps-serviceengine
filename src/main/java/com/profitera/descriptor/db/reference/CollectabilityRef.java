// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class CollectabilityRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String COLLECTABILITY_CODE = "collectabilityCode";
	public static final String COLLECTABILITY_DESC = "collectabilityDesc";
	public static final String COLLECTABILITY_ID = "collectabilityId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String collectabilityCode;

	private java.lang.String collectabilityDesc;

	private java.lang.Double collectabilityId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  CollectabilityRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getCollectabilityCode()
	{
		return collectabilityCode;
	}

	public java.lang.String getCollectabilityDesc()
	{
		return collectabilityDesc;
	}

	public java.lang.Double getCollectabilityId()
	{
		return collectabilityId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setCollectabilityCode(java.lang.String collectabilityCode)
	{
		this.collectabilityCode = collectabilityCode;
	}

	public void setCollectabilityDesc(java.lang.String collectabilityDesc)
	{
		this.collectabilityDesc = collectabilityDesc;
	}

	public void setCollectabilityId(java.lang.Double collectabilityId)
	{
		this.collectabilityId = collectabilityId;
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
