// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.history;

public class BlockCodeHistory
	implements java.io.Serializable
{
	// Generated constants
	public static final String ACCOUNT = "account";
	public static final String ACCOUNT_BLOCK_CODE_HISTORY_ID = "accountBlockCodeHistoryId";
	public static final String BLOCK_CODE_DATE = "blockCodeDate";
	public static final String BLOCK_CODE_REF = "blockCodeRef";
	public static final String BLOCK_CODE_REMARKS = "blockCodeRemarks";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface account= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double accountBlockCodeHistoryId;

	private java.sql.Timestamp blockCodeDate;

	private oracle.toplink.indirection.ValueHolderInterface blockCodeRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.String blockCodeRemarks;

	public  BlockCodeHistory()
	{
		// Fill in method body here.
	}

	public com.profitera.descriptor.db.account.Account getAccount()
	{
		return (com.profitera.descriptor.db.account.Account) account.getValue();
	}

	public java.lang.Double getAccountBlockCodeHistoryId()
	{
		return accountBlockCodeHistoryId;
	}

	public java.sql.Timestamp getBlockCodeDate()
	{
		return blockCodeDate;
	}

	public com.profitera.descriptor.db.reference.BlockCodeRef getBlockCodeRef()
	{
		return (com.profitera.descriptor.db.reference.BlockCodeRef) blockCodeRef.getValue();
	}

	public java.lang.String getBlockCodeRemarks()
	{
		return blockCodeRemarks;
	}

	public void setAccount(com.profitera.descriptor.db.account.Account account)
	{
		this.account.setValue(account);
	}

	public void setAccountBlockCodeHistoryId(java.lang.Double accountBlockCodeHistoryId)
	{
		this.accountBlockCodeHistoryId = accountBlockCodeHistoryId;
	}

	public void setBlockCodeDate(java.sql.Timestamp blockCodeDate)
	{
		this.blockCodeDate = blockCodeDate;
	}

	public void setBlockCodeRef(com.profitera.descriptor.db.reference.BlockCodeRef blockCodeRef)
	{
		this.blockCodeRef.setValue(blockCodeRef);
	}

	public void setBlockCodeRemarks(java.lang.String blockCodeRemarks)
	{
		this.blockCodeRemarks = blockCodeRemarks;
	}
}
