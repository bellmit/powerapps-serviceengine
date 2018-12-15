// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.payment;

public class Payment
	implements java.io.Serializable
{
	// Generated constants
	public static final String ACCOUNT = "account";
	public static final String FROM_HOST = "fromHost";
	public static final String INSTALLMENT_HISTORIES = "installmentHistories";
	public static final String PAYMENT_AMT = "paymentAmt";
	public static final String PAYMENT_DATE_TIME = "paymentDateTime";
	public static final String PAYMENT_ID = "paymentId";
	public static final String PAYMENT_PROCESS_DATE_TIME = "paymentProcessDateTime";
	public static final String PAYMENT_PROCESS_STATUS_REF = "paymentProcessStatusRef";
	public static final String PAYMENT_TYPE_REF = "paymentTypeRef";
	public static final String TRAN_CODE_REF = "tranCodeRef";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface account= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double fromHost;

	private java.lang.Double paymentAmt;

	private java.sql.Timestamp paymentDateTime;

	private java.lang.Double paymentId;

	private java.sql.Timestamp paymentProcessDateTime;

	private oracle.toplink.indirection.ValueHolderInterface paymentProcessStatusRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface paymentTypeRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface tranCodeRef= new oracle.toplink.indirection.ValueHolder();

	public  Payment()
	{
		// Fill in method body here.
	}

	public com.profitera.descriptor.db.account.Account getAccount()
	{
		return (com.profitera.descriptor.db.account.Account) account.getValue();
	}

	public java.lang.Double getFromHost()
	{
		return fromHost;
	}

	public java.lang.Double getPaymentAmt()
	{
		return paymentAmt;
	}

	public java.sql.Timestamp getPaymentDateTime()
	{
		return paymentDateTime;
	}

	public java.lang.Double getPaymentId()
	{
		return paymentId;
	}

	public java.sql.Timestamp getPaymentProcessDateTime()
	{
		return paymentProcessDateTime;
	}

	public com.profitera.descriptor.db.reference.ProcessStatusRef getPaymentProcessStatusRef()
	{
		return (com.profitera.descriptor.db.reference.ProcessStatusRef) paymentProcessStatusRef.getValue();
	}

	public com.profitera.descriptor.db.reference.PaymentTypeRef getPaymentTypeRef()
	{
		return (com.profitera.descriptor.db.reference.PaymentTypeRef) paymentTypeRef.getValue();
	}

	public com.profitera.descriptor.db.reference.TransactionCodeRef getTranCodeRef()
	{
		return (com.profitera.descriptor.db.reference.TransactionCodeRef) tranCodeRef.getValue();
	}

	public void setAccount(com.profitera.descriptor.db.account.Account account)
	{
		this.account.setValue(account);
	}

	public void setFromHost(java.lang.Double fromHost)
	{
		this.fromHost = fromHost;
	}

	public void setPaymentAmt(java.lang.Double paymentAmt)
	{
		this.paymentAmt = paymentAmt;
	}

	public void setPaymentDateTime(java.sql.Timestamp paymentDateTime)
	{
		this.paymentDateTime = paymentDateTime;
	}

	public void setPaymentId(java.lang.Double paymentId)
	{
		this.paymentId = paymentId;
	}

	public void setPaymentProcessDateTime(java.sql.Timestamp paymentProcessDateTime)
	{
		this.paymentProcessDateTime = paymentProcessDateTime;
	}

	public void setPaymentProcessStatusRef(com.profitera.descriptor.db.reference.ProcessStatusRef paymentProcessStatusRef)
	{
		this.paymentProcessStatusRef.setValue(paymentProcessStatusRef);
	}

	public void setPaymentTypeRef(com.profitera.descriptor.db.reference.PaymentTypeRef paymentTypeRef)
	{
		this.paymentTypeRef.setValue(paymentTypeRef);
	}

	public void setTranCodeRef(com.profitera.descriptor.db.reference.TransactionCodeRef tranCodeRef)
	{
		this.tranCodeRef.setValue(tranCodeRef);
	}
}
