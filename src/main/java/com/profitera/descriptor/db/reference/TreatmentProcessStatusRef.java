// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class TreatmentProcessStatusRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	public static final String TREATPROC_STATUS_CODE = "treatprocStatusCode";
	public static final String TREATPROC_STATUS_DESC = "treatprocStatusDesc";
	public static final String TREATPROC_STATUS_ID = "treatprocStatusId";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	private java.lang.String treatprocStatusCode;

	private java.lang.String treatprocStatusDesc;

	private java.lang.Double treatprocStatusId;

	public  TreatmentProcessStatusRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public java.lang.String getTreatprocStatusCode()
	{
		return treatprocStatusCode;
	}

	public java.lang.String getTreatprocStatusDesc()
	{
		return treatprocStatusDesc;
	}

	public java.lang.Double getTreatprocStatusId()
	{
		return treatprocStatusId;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}

	public void setTreatprocStatusCode(java.lang.String treatprocStatusCode)
	{
		this.treatprocStatusCode = treatprocStatusCode;
	}

	public void setTreatprocStatusDesc(java.lang.String treatprocStatusDesc)
	{
		this.treatprocStatusDesc = treatprocStatusDesc;
	}

	public void setTreatprocStatusId(java.lang.Double treatprocStatusId)
	{
		this.treatprocStatusId = treatprocStatusId;
	}
}
