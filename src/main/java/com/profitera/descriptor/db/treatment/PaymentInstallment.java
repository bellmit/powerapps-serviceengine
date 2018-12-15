// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.treatment;

public class PaymentInstallment
	implements java.io.Serializable
{
	// Generated constants
	public static final String COMMISSION_CHARGE_AMT = "commissionChargeAmt";
	public static final String INSTALLMENT_DUE_AMOUNT = "installmentDueAmount";
	public static final String INSTALLMENT_DUE_DATE = "installmentDueDate";
	public static final String INSTALLMENT_PAID_AMOUNT = "installmentPaidAmount";
	public static final String INSTALLMENT_PAID_STATUS_REF = "installmentPaidStatusRef";
	public static final String INSTALLMENT_STATUS_DATE = "installmentStatusDate";
	public static final String INSTALLMENT_STATUS_REF = "installmentStatusRef";
	public static final String INTEREST_CHARGE_AMT = "interestChargeAmt";
	public static final String LATE_CHARGE_AMT = "lateChargeAmt";
	public static final String OPENING_BAL_AMT = "openingBalAmt";
	public static final String PARENT_PAYMENT_PLAN = "parentPaymentPlan";
	public static final String REMAINING_AMT = "remainingAmt";
	// End of generated constants
	
	private java.lang.Double commissionChargeAmt;

	private java.lang.Double installmentDueAmount;

	private java.sql.Timestamp installmentDueDate;

	private java.lang.Double installmentPaidAmount;

	private oracle.toplink.indirection.ValueHolderInterface installmentPaidStatusRef= new oracle.toplink.indirection.ValueHolder();

	private java.sql.Timestamp installmentStatusDate;

	private oracle.toplink.indirection.ValueHolderInterface installmentStatusRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double interestChargeAmt;

	private java.lang.Double lateChargeAmt;

	private java.lang.Double openingBalAmt;

	private oracle.toplink.indirection.ValueHolderInterface parentPaymentPlan= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double remainingAmt;

	public  PaymentInstallment()
	{
		// Fill in method body here.
	}

	public java.lang.Double getCommissionChargeAmt()
	{
		return commissionChargeAmt;
	}

	public java.lang.Double getInstallmentDueAmount()
	{
		return installmentDueAmount;
	}

	public java.sql.Timestamp getInstallmentDueDate()
	{
		return installmentDueDate;
	}

	public java.lang.Double getInstallmentPaidAmount()
	{
		return installmentPaidAmount;
	}

	public com.profitera.descriptor.db.reference.InstallmentPaidStatusRef getInstallmentPaidStatusRef()
	{
		return (com.profitera.descriptor.db.reference.InstallmentPaidStatusRef) installmentPaidStatusRef.getValue();
	}

	public java.sql.Timestamp getInstallmentStatusDate()
	{
		return installmentStatusDate;
	}

	public com.profitera.descriptor.db.reference.InstallmentStatusRef getInstallmentStatusRef()
	{
		return (com.profitera.descriptor.db.reference.InstallmentStatusRef) installmentStatusRef.getValue();
	}

	public java.lang.Double getInterestChargeAmt()
	{
		return interestChargeAmt;
	}

	public java.lang.Double getLateChargeAmt()
	{
		return lateChargeAmt;
	}

	public java.lang.Double getOpeningBalAmt()
	{
		return openingBalAmt;
	}

	public com.profitera.descriptor.db.treatment.PaymentPlan getParentPaymentPlan()
	{
		return (com.profitera.descriptor.db.treatment.PaymentPlan) parentPaymentPlan.getValue();
	}

	public java.lang.Double getRemainingAmt()
	{
		return remainingAmt;
	}

	public void setCommissionChargeAmt(java.lang.Double commissionChargeAmt)
	{
		this.commissionChargeAmt = commissionChargeAmt;
	}

	public void setInstallmentDueAmount(java.lang.Double installmentDueAmount)
	{
		this.installmentDueAmount = installmentDueAmount;
	}

	public void setInstallmentDueDate(java.sql.Timestamp installmentDueDate)
	{
		this.installmentDueDate = installmentDueDate;
	}

	public void setInstallmentPaidAmount(java.lang.Double installmentPaidAmount)
	{
		this.installmentPaidAmount = installmentPaidAmount;
	}

	public void setInstallmentPaidStatusRef(com.profitera.descriptor.db.reference.InstallmentPaidStatusRef installmentPaidStatusRef)
	{
		this.installmentPaidStatusRef.setValue(installmentPaidStatusRef);
	}

	public void setInstallmentStatusDate(java.sql.Timestamp installmentStatusDate)
	{
		this.installmentStatusDate = installmentStatusDate;
	}

	public void setInstallmentStatusRef(com.profitera.descriptor.db.reference.InstallmentStatusRef installmentStatusRef)
	{
		this.installmentStatusRef.setValue(installmentStatusRef);
	}

	public void setInterestChargeAmt(java.lang.Double interestChargeAmt)
	{
		this.interestChargeAmt = interestChargeAmt;
	}

	public void setLateChargeAmt(java.lang.Double lateChargeAmt)
	{
		this.lateChargeAmt = lateChargeAmt;
	}

	public void setOpeningBalAmt(java.lang.Double openingBalAmt)
	{
		this.openingBalAmt = openingBalAmt;
	}

	public void setParentPaymentPlan(com.profitera.descriptor.db.treatment.PaymentPlan parentPaymentPlan)
	{
		this.parentPaymentPlan.setValue(parentPaymentPlan);
	}

	public void setRemainingAmt(java.lang.Double remainingAmt)
	{
		this.remainingAmt = remainingAmt;
	}
}
