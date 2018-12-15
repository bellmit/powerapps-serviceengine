// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class DebtRecoveryStatusRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DEBTREC_STATUS_CODE = "debtrecStatusCode";
	public static final String DEBTREC_STATUS_DESC = "debtrecStatusDesc";
	public static final String DEBTREC_STATUS_ID = "debtrecStatusId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String debtrecStatusCode;

	private java.lang.String debtrecStatusDesc;

	private java.lang.Double debtrecStatusId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  DebtRecoveryStatusRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getDebtrecStatusCode()
	{
		return debtrecStatusCode;
	}

	public java.lang.String getDebtrecStatusDesc()
	{
		return debtrecStatusDesc;
	}

	public java.lang.Double getDebtrecStatusId()
	{
		return debtrecStatusId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDebtrecStatusCode(java.lang.String debtrecStatusCode)
	{
		this.debtrecStatusCode = debtrecStatusCode;
	}

	public void setDebtrecStatusDesc(java.lang.String debtrecStatusDesc)
	{
		this.debtrecStatusDesc = debtrecStatusDesc;
	}

	public void setDebtrecStatusId(java.lang.Double debtrecStatusId)
	{
		this.debtrecStatusId = debtrecStatusId;
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
