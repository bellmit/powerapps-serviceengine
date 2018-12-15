// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class PaymentTypeRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String PAY_TYPE_CODE = "payTypeCode";
	public static final String PAY_TYPE_DESC = "payTypeDesc";
	public static final String PAY_TYPE_ID = "payTypeId";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.String payTypeCode;

	private java.lang.String payTypeDesc;

	private java.lang.Double payTypeId;

	private java.lang.Double sortPriority;

	public  PaymentTypeRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.String getPayTypeCode()
	{
		return payTypeCode;
	}

	public java.lang.String getPayTypeDesc()
	{
		return payTypeDesc;
	}

	public java.lang.Double getPayTypeId()
	{
		return payTypeId;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setPayTypeCode(java.lang.String payTypeCode)
	{
		this.payTypeCode = payTypeCode;
	}

	public void setPayTypeDesc(java.lang.String payTypeDesc)
	{
		this.payTypeDesc = payTypeDesc;
	}

	public void setPayTypeId(java.lang.Double payTypeId)
	{
		this.payTypeId = payTypeId;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
