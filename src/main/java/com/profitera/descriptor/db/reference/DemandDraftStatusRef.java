// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class DemandDraftStatusRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DEMAND_DRAFT_STATUS_CODE = "demandDraftStatusCode";
	public static final String DEMAND_DRAFT_STATUS_DESC = "demandDraftStatusDesc";
	public static final String DEMAND_DRAFT_STATUS_ID = "demandDraftStatusId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String demandDraftStatusCode;

	private java.lang.String demandDraftStatusDesc;

	private java.lang.Double demandDraftStatusId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  DemandDraftStatusRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getDemandDraftStatusCode()
	{
		return demandDraftStatusCode;
	}

	public java.lang.String getDemandDraftStatusDesc()
	{
		return demandDraftStatusDesc;
	}

	public java.lang.Double getDemandDraftStatusId()
	{
		return demandDraftStatusId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDemandDraftStatusCode(java.lang.String demandDraftStatusCode)
	{
		this.demandDraftStatusCode = demandDraftStatusCode;
	}

	public void setDemandDraftStatusDesc(java.lang.String demandDraftStatusDesc)
	{
		this.demandDraftStatusDesc = demandDraftStatusDesc;
	}

	public void setDemandDraftStatusId(java.lang.Double demandDraftStatusId)
	{
		this.demandDraftStatusId = demandDraftStatusId;
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
