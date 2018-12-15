// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class ReportTypeRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String REPORT_TYPE_CODE = "reportTypeCode";
	public static final String REPORT_TYPE_DESC = "reportTypeDesc";
	public static final String REPORT_TYPE_ID = "reportTypeId";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.String reportTypeCode;

	private java.lang.String reportTypeDesc;

	private java.lang.Double reportTypeId;

	private java.lang.Double sortPriority;

	public  ReportTypeRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.String getReportTypeCode()
	{
		return reportTypeCode;
	}

	public java.lang.String getReportTypeDesc()
	{
		return reportTypeDesc;
	}

	public java.lang.Double getReportTypeId()
	{
		return reportTypeId;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setReportTypeCode(java.lang.String reportTypeCode)
	{
		this.reportTypeCode = reportTypeCode;
	}

	public void setReportTypeDesc(java.lang.String reportTypeDesc)
	{
		this.reportTypeDesc = reportTypeDesc;
	}

	public void setReportTypeId(java.lang.Double reportTypeId)
	{
		this.reportTypeId = reportTypeId;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
