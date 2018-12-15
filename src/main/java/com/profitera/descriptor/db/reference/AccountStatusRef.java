// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class AccountStatusRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String ACCOUNT_STATUS_CODE = "accountStatusCode";
	public static final String ACCOUNT_STATUS_DESC = "accountStatusDesc";
	public static final String ACCOUNT_STATUS_ID = "accountStatusId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String accountStatusCode;

	private java.lang.String accountStatusDesc;

	private java.lang.Double accountStatusId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  AccountStatusRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getAccountStatusCode()
	{
		return accountStatusCode;
	}

	public java.lang.String getAccountStatusDesc()
	{
		return accountStatusDesc;
	}

	public java.lang.Double getAccountStatusId()
	{
		return accountStatusId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setAccountStatusCode(java.lang.String accountStatusCode)
	{
		this.accountStatusCode = accountStatusCode;
	}

	public void setAccountStatusDesc(java.lang.String accountStatusDesc)
	{
		this.accountStatusDesc = accountStatusDesc;
	}

	public void setAccountStatusId(java.lang.Double accountStatusId)
	{
		this.accountStatusId = accountStatusId;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
