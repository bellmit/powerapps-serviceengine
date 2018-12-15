// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.account;

public class AccountUnbilled
	implements java.io.Serializable
{
	// Generated constants
	public static final String ACCOUNT = "account";
	public static final String ACCOUNT_UNBILLED_ID = "accountUnbilledId";
	public static final String CHARGE_AMT = "chargeAmt";
	public static final String CHARGE_DATE = "chargeDate";
	public static final String CHARGE_REFERENCE_NO = "chargeReferenceNo";
	public static final String CHARGE_REMARKS = "chargeRemarks";
	public static final String CHARGE_TYPE_REF = "chargeTypeRef";
	public static final String FOREIGN_CHARGE_AMT = "foreignChargeAmt";
	public static final String TRAN_CODE_REF = "tranCodeRef";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface account= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double accountUnbilledId;

	private java.lang.Double chargeAmt;

	private java.sql.Timestamp chargeDate;

	private java.lang.String chargeReferenceNo;

	private java.lang.String chargeRemarks;

	private oracle.toplink.indirection.ValueHolderInterface chargeTypeRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double foreignChargeAmt;

	private oracle.toplink.indirection.ValueHolderInterface tranCodeRef= new oracle.toplink.indirection.ValueHolder();

	public com.profitera.descriptor.db.account.Account getAccount()
	{
		return (com.profitera.descriptor.db.account.Account) account.getValue();
	}

	public java.lang.Double getAccountUnbilledId()
	{
		return accountUnbilledId;
	}

	public java.lang.Double getChargeAmt()
	{
		return chargeAmt;
	}

	public java.sql.Timestamp getChargeDate()
	{
		return chargeDate;
	}

	public java.lang.String getChargeReferenceNo()
	{
		return chargeReferenceNo;
	}

	public java.lang.String getChargeRemarks()
	{
		return chargeRemarks;
	}

	public com.profitera.descriptor.db.reference.UnbilledTypeRef getChargeTypeRef()
	{
		return (com.profitera.descriptor.db.reference.UnbilledTypeRef) chargeTypeRef.getValue();
	}

	public java.lang.Double getForeignChargeAmt()
	{
		return foreignChargeAmt;
	}

	public com.profitera.descriptor.db.reference.TransactionCodeRef getTranCodeRef()
	{
		return (com.profitera.descriptor.db.reference.TransactionCodeRef) tranCodeRef.getValue();
	}

	public void setAccount(com.profitera.descriptor.db.account.Account account)
	{
		this.account.setValue(account);
	}

	public void setAccountUnbilledId(java.lang.Double accountUnbilledId)
	{
		this.accountUnbilledId = accountUnbilledId;
	}

	public void setChargeAmt(java.lang.Double chargeAmt)
	{
		this.chargeAmt = chargeAmt;
	}

	public void setChargeDate(java.sql.Timestamp chargeDate)
	{
		this.chargeDate = chargeDate;
	}

	public void setChargeReferenceNo(java.lang.String chargeReferenceNo)
	{
		this.chargeReferenceNo = chargeReferenceNo;
	}

	public void setChargeRemarks(java.lang.String chargeRemarks)
	{
		this.chargeRemarks = chargeRemarks;
	}

	public void setChargeTypeRef(com.profitera.descriptor.db.reference.UnbilledTypeRef chargeTypeRef)
	{
		this.chargeTypeRef.setValue(chargeTypeRef);
	}

	public void setForeignChargeAmt(java.lang.Double foreignChargeAmt)
	{
		this.foreignChargeAmt = foreignChargeAmt;
	}

	public void setTranCodeRef(com.profitera.descriptor.db.reference.TransactionCodeRef tranCodeRef)
	{
		this.tranCodeRef.setValue(tranCodeRef);
	}
}
