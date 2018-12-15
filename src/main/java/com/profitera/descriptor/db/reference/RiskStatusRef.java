// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class RiskStatusRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String RISK_CODE = "riskCode";
	public static final String RISK_DESC = "riskDesc";
	public static final String RISK_ID = "riskId";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.String riskCode;

	private java.lang.String riskDesc;

	private java.lang.Double riskId;

	private java.lang.Double sortPriority;

	public  RiskStatusRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.String getRiskCode()
	{
		return riskCode;
	}

	public java.lang.String getRiskDesc()
	{
		return riskDesc;
	}

	public java.lang.Double getRiskId()
	{
		return riskId;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setRiskCode(java.lang.String riskCode)
	{
		this.riskCode = riskCode;
	}

	public void setRiskDesc(java.lang.String riskDesc)
	{
		this.riskDesc = riskDesc;
	}

	public void setRiskId(java.lang.Double riskId)
	{
		this.riskId = riskId;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
