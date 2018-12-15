// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class DelinquencyTypeRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DELINQUENCY_TYPE_CODE = "delinquencyTypeCode";
	public static final String DELINQUENCY_TYPE_DESC = "delinquencyTypeDesc";
	public static final String DELINQUENCY_TYPE_ID = "delinquencyTypeId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String delinquencyTypeCode;

	private java.lang.String delinquencyTypeDesc;

	private java.lang.Double delinquencyTypeId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  DelinquencyTypeRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getDelinquencyTypeCode()
	{
		return delinquencyTypeCode;
	}

	public java.lang.String getDelinquencyTypeDesc()
	{
		return delinquencyTypeDesc;
	}

	public java.lang.Double getDelinquencyTypeId()
	{
		return delinquencyTypeId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDelinquencyTypeCode(java.lang.String delinquencyTypeCode)
	{
		this.delinquencyTypeCode = delinquencyTypeCode;
	}

	public void setDelinquencyTypeDesc(java.lang.String delinquencyTypeDesc)
	{
		this.delinquencyTypeDesc = delinquencyTypeDesc;
	}

	public void setDelinquencyTypeId(java.lang.Double delinquencyTypeId)
	{
		this.delinquencyTypeId = delinquencyTypeId;
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
