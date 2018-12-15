// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class ClassOfServiceRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String CLASS_OF_SERVICE_CODE = "classOfServiceCode";
	public static final String CLASS_OF_SERVICE_DESC = "classOfServiceDesc";
	public static final String CLASS_OF_SERVICE_ID = "classOfServiceId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String classOfServiceCode;

	private java.lang.String classOfServiceDesc;

	private java.lang.Double classOfServiceId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  ClassOfServiceRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getClassOfServiceCode()
	{
		return classOfServiceCode;
	}

	public java.lang.String getClassOfServiceDesc()
	{
		return classOfServiceDesc;
	}

	public java.lang.Double getClassOfServiceId()
	{
		return classOfServiceId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setClassOfServiceCode(java.lang.String classOfServiceCode)
	{
		this.classOfServiceCode = classOfServiceCode;
	}

	public void setClassOfServiceDesc(java.lang.String classOfServiceDesc)
	{
		this.classOfServiceDesc = classOfServiceDesc;
	}

	public void setClassOfServiceId(java.lang.Double classOfServiceId)
	{
		this.classOfServiceId = classOfServiceId;
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
