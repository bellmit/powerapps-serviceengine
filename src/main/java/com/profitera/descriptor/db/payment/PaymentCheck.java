// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.payment;

public class PaymentCheck
	extends com.profitera.descriptor.db.payment.Payment
	implements java.io.Serializable
{
	// Generated constants
	public static final String CHECK_STATUS_REF = "checkStatusRef";
	public static final String PAYMENT_ID = "paymentId";
	public static final String PAYMENT_REALIZATION_DAYS = "paymentRealizationDays";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface checkStatusRef= new oracle.toplink.indirection.ValueHolder();
	private java.lang.Double paymentId;
	private java.lang.Double paymentRealizationDays;

	public  PaymentCheck()
	{
		// Fill in method body here.
	}

	public com.profitera.descriptor.db.reference.CheckStatusRef getCheckStatusRef()
	{
		return (com.profitera.descriptor.db.reference.CheckStatusRef) checkStatusRef.getValue();
	}

	public java.lang.Double getPaymentId()
	{
		return paymentId;
	}

	public java.lang.Double getPaymentRealizationDays()
	{
		return paymentRealizationDays;
	}

	public void setCheckStatusRef(com.profitera.descriptor.db.reference.CheckStatusRef checkStatusRef)
	{
		this.checkStatusRef.setValue(checkStatusRef);
	}

	public void setPaymentId(java.lang.Double paymentId)
	{
		this.paymentId = paymentId;
	}

	public void setPaymentRealizationDays(java.lang.Double paymentRealizationDays)
	{
		this.paymentRealizationDays = paymentRealizationDays;
	}
}
