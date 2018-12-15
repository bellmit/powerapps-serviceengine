// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class PaymentFrequencyRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String PAYMENT_FREQUENCY_CODE = "paymentFrequencyCode";
	public static final String PAYMENT_FREQUENCY_DESC = "paymentFrequencyDesc";
	public static final String PAYMENT_FREQUENCY_ID = "paymentFrequencyId";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.String paymentFrequencyCode;

	private java.lang.String paymentFrequencyDesc;

	private java.lang.Double paymentFrequencyId;

	private java.lang.Double sortPriority;

	public  PaymentFrequencyRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.String getPaymentFrequencyCode()
	{
		return paymentFrequencyCode;
	}

	public java.lang.String getPaymentFrequencyDesc()
	{
		return paymentFrequencyDesc;
	}

	public java.lang.Double getPaymentFrequencyId()
	{
		return paymentFrequencyId;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setPaymentFrequencyCode(java.lang.String paymentFrequencyCode)
	{
		this.paymentFrequencyCode = paymentFrequencyCode;
	}

	public void setPaymentFrequencyDesc(java.lang.String paymentFrequencyDesc)
	{
		this.paymentFrequencyDesc = paymentFrequencyDesc;
	}

	public void setPaymentFrequencyId(java.lang.Double paymentFrequencyId)
	{
		this.paymentFrequencyId = paymentFrequencyId;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
