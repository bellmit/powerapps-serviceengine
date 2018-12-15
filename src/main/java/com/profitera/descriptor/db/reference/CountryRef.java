// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class CountryRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String COUNTRY_CODE = "countryCode";
	public static final String COUNTRY_DESC = "countryDesc";
	public static final String COUNTRY_ID = "countryId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String countryCode;

	private java.lang.String countryDesc;

	private java.lang.Double countryId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  CountryRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getCountryCode()
	{
		return countryCode;
	}

	public java.lang.String getCountryDesc()
	{
		return countryDesc;
	}

	public java.lang.Double getCountryId()
	{
		return countryId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setCountryCode(java.lang.String countryCode)
	{
		this.countryCode = countryCode;
	}

	public void setCountryDesc(java.lang.String countryDesc)
	{
		this.countryDesc = countryDesc;
	}

	public void setCountryId(java.lang.Double countryId)
	{
		this.countryId = countryId;
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
