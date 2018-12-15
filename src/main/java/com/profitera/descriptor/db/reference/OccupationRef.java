// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class OccupationRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String OCCUPATION_CODE = "occupationCode";
	public static final String OCCUPATION_DESC = "occupationDesc";
	public static final String OCCUPATION_ID = "occupationId";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.String occupationCode;

	private java.lang.String occupationDesc;

	private java.lang.Double occupationId;

	private java.lang.Double sortPriority;

	public  OccupationRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.String getOccupationCode()
	{
		return occupationCode;
	}

	public java.lang.String getOccupationDesc()
	{
		return occupationDesc;
	}

	public java.lang.Double getOccupationId()
	{
		return occupationId;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setOccupationCode(java.lang.String occupationCode)
	{
		this.occupationCode = occupationCode;
	}

	public void setOccupationDesc(java.lang.String occupationDesc)
	{
		this.occupationDesc = occupationDesc;
	}

	public void setOccupationId(java.lang.Double occupationId)
	{
		this.occupationId = occupationId;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
