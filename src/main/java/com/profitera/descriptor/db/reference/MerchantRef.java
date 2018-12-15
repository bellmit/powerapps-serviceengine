// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class MerchantRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String MERCHANT_CODE = "merchantCode";
	public static final String MERCHANT_DESC = "merchantDesc";
	public static final String MERCHANT_ID = "merchantId";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.String merchantCode;

	private java.lang.String merchantDesc;

	private java.lang.Double merchantId;

	private java.lang.Double sortPriority;

	public  MerchantRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.String getMerchantCode()
	{
		return merchantCode;
	}

	public java.lang.String getMerchantDesc()
	{
		return merchantDesc;
	}

	public java.lang.Double getMerchantId()
	{
		return merchantId;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setMerchantCode(java.lang.String merchantCode)
	{
		this.merchantCode = merchantCode;
	}

	public void setMerchantDesc(java.lang.String merchantDesc)
	{
		this.merchantDesc = merchantDesc;
	}

	public void setMerchantId(java.lang.Double merchantId)
	{
		this.merchantId = merchantId;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
