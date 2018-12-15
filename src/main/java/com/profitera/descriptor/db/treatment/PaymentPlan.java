// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.treatment;

public class PaymentPlan
	extends com.profitera.descriptor.db.treatment.TreatmentProcess
	implements java.io.Serializable
{
	// Generated constants
	public static final String COMMISSION_CHARGE_RATE = "commissionChargeRate";
	public static final String INSTALLMENT_UOM = "installmentUom";
	public static final String INTEREST_CHARGE_RATE = "interestChargeRate";
	public static final String LATE_CHARGE_RATE = "lateChargeRate";
	public static final String NO_OF_INSTALLMENTS = "noOfInstallments";
	public static final String PAYMENT_INSTALLMENTS = "paymentInstallments";
	// End of generated constants
	
	private java.lang.Double commissionChargeRate;

	private oracle.toplink.indirection.ValueHolderInterface installmentUom= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double interestChargeRate;

	private java.lang.Double lateChargeRate;

	private java.lang.Double noOfInstallments;

	private oracle.toplink.indirection.ValueHolderInterface paymentInstallments= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	public  PaymentPlan()
	{
		// Fill in method body here.
	}

	public java.lang.Double getCommissionChargeRate()
	{
		return commissionChargeRate;
	}

	public com.profitera.descriptor.db.reference.UomMeasureRef getInstallmentUom()
	{
		return (com.profitera.descriptor.db.reference.UomMeasureRef) installmentUom.getValue();
	}

	public java.lang.Double getInterestChargeRate()
	{
		return interestChargeRate;
	}

	public java.lang.Double getLateChargeRate()
	{
		return lateChargeRate;
	}

	public java.lang.Double getNoOfInstallments()
	{
		return noOfInstallments;
	}

	public java.util.Vector getPaymentInstallments()
	{
		return (java.util.Vector) paymentInstallments.getValue();
	}

	public void setCommissionChargeRate(java.lang.Double commissionChargeRate)
	{
		this.commissionChargeRate = commissionChargeRate;
	}

	public void setInstallmentUom(com.profitera.descriptor.db.reference.UomMeasureRef installmentUom)
	{
		this.installmentUom.setValue(installmentUom);
	}

	public void setInterestChargeRate(java.lang.Double interestChargeRate)
	{
		this.interestChargeRate = interestChargeRate;
	}

	public void setLateChargeRate(java.lang.Double lateChargeRate)
	{
		this.lateChargeRate = lateChargeRate;
	}

	public void setNoOfInstallments(java.lang.Double noOfInstallments)
	{
		this.noOfInstallments = noOfInstallments;
	}

	public void setPaymentInstallments(java.util.Vector paymentInstallments)
	{
		this.paymentInstallments.setValue(paymentInstallments);
	}
}
