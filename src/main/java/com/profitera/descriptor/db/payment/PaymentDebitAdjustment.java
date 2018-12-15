// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.payment;

public class PaymentDebitAdjustment
	extends com.profitera.descriptor.db.payment.Payment
	implements java.io.Serializable
{
	// Generated constants
	public static final String PAYMENT_ID = "paymentId";
	// End of generated constants

	private java.lang.Double paymentId;

	public  PaymentDebitAdjustment()
	{
		// Fill in method body here.
	}
	public java.lang.Double getPaymentId()
	{
		return paymentId;
	}

	public void setPaymentId(java.lang.Double paymentId)
	{
		this.paymentId = paymentId;
	}
}
