// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class CitizenshipRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String CITIZENSHIP_CODE = "citizenshipCode";
	public static final String CITIZENSHIP_DESC = "citizenshipDesc";
	public static final String CITIZENSHIP_ID = "citizenshipId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String citizenshipCode;

	private java.lang.String citizenshipDesc;

	private java.lang.Double citizenshipId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  CitizenshipRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getCitizenshipCode()
	{
		return citizenshipCode;
	}

	public java.lang.String getCitizenshipDesc()
	{
		return citizenshipDesc;
	}

	public java.lang.Double getCitizenshipId()
	{
		return citizenshipId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setCitizenshipCode(java.lang.String citizenshipCode)
	{
		this.citizenshipCode = citizenshipCode;
	}

	public void setCitizenshipDesc(java.lang.String citizenshipDesc)
	{
		this.citizenshipDesc = citizenshipDesc;
	}

	public void setCitizenshipId(java.lang.Double citizenshipId)
	{
		this.citizenshipId = citizenshipId;
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
