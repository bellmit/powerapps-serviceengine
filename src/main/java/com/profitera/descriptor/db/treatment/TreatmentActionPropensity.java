// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.treatment;

public class TreatmentActionPropensity
	implements java.io.Serializable
{
	// Generated constants
	public static final String ACCOUNT_ID = "accountId";
	public static final String PROPENSITY = "propensity";
	public static final String RESULT_TYPE_ID = "resultTypeId";
	public static final String TREATMENT_ACTION_ID = "treatmentActionId";
	public static final String TREATMENT_ACTION_PROPENSITY_ID = "treatmentActionPropensityId";
	// End of generated constants
	
	private java.lang.Double accountId;

	private java.lang.Double propensity;

	private java.lang.Double resultTypeId;

	private java.lang.Double treatmentActionId;

	private java.lang.Double treatmentActionPropensityId;

	public  TreatmentActionPropensity()
	{
		// Fill in method body here.
	}

	public java.lang.Double getAccountId()
	{
		return accountId;
	}

	public java.lang.Double getPropensity()
	{
		return propensity;
	}

	public java.lang.Double getResultTypeId()
	{
		return resultTypeId;
	}

	public java.lang.Double getTreatmentActionId()
	{
		return treatmentActionId;
	}

	public java.lang.Double getTreatmentActionPropensityId()
	{
		return treatmentActionPropensityId;
	}

	public void setAccountId(java.lang.Double accountId)
	{
		this.accountId = accountId;
	}

	public void setPropensity(java.lang.Double propensity)
	{
		this.propensity = propensity;
	}

	public void setResultTypeId(java.lang.Double resultTypeId)
	{
		this.resultTypeId = resultTypeId;
	}

	public void setTreatmentActionId(java.lang.Double treatmentActionId)
	{
		this.treatmentActionId = treatmentActionId;
	}

	public void setTreatmentActionPropensityId(java.lang.Double treatmentActionPropensityId)
	{
		this.treatmentActionPropensityId = treatmentActionPropensityId;
	}
}
