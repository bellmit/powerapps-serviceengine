// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class TreatmentStreamRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	public static final String TREATMENT_STAGE_REF = "treatmentStageRef";
	public static final String TREATMENT_STREAM_CODE = "treatmentStreamCode";
	public static final String TREATMENT_STREAM_DESC = "treatmentStreamDesc";
	public static final String TREATMENT_STREAM_ID = "treatmentStreamId";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	private oracle.toplink.indirection.ValueHolderInterface treatmentStageRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.String treatmentStreamCode;

	private java.lang.String treatmentStreamDesc;

	private java.lang.Double treatmentStreamId;

	public  TreatmentStreamRef()
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

	public com.profitera.descriptor.db.reference.TreatmentStageRef getTreatmentStageRef()
	{
		return (com.profitera.descriptor.db.reference.TreatmentStageRef) treatmentStageRef.getValue();
	}

	public java.lang.String getTreatmentStreamCode()
	{
		return treatmentStreamCode;
	}

	public java.lang.String getTreatmentStreamDesc()
	{
		return treatmentStreamDesc;
	}

	public java.lang.Double getTreatmentStreamId()
	{
		return treatmentStreamId;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}

	public void setTreatmentStageRef(com.profitera.descriptor.db.reference.TreatmentStageRef treatmentStageRef)
	{
		this.treatmentStageRef.setValue(treatmentStageRef);
	}

	public void setTreatmentStreamCode(java.lang.String treatmentStreamCode)
	{
		this.treatmentStreamCode = treatmentStreamCode;
	}

	public void setTreatmentStreamDesc(java.lang.String treatmentStreamDesc)
	{
		this.treatmentStreamDesc = treatmentStreamDesc;
	}

	public void setTreatmentStreamId(java.lang.Double treatmentStreamId)
	{
		this.treatmentStreamId = treatmentStreamId;
	}
}
