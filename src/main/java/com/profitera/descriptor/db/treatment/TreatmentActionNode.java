// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.treatment;

public class TreatmentActionNode
	implements java.io.Serializable
{
	// Generated constants
	public static final String ACTION_ID = "actionId";
	public static final String NAME = "name";
	public static final String RETRIES = "retries";
	public static final String TREATMENT_STAGE_REF = "treatmentStageRef";
	public static final String TREATMENT_STREAM_REF = "treatmentStreamRef";
	public static final String TREATPROC_SUBTYPE_REF = "treatprocSubtypeRef";
	public static final String X_POSITION = "xPosition";
	public static final String Y_POSITION = "yPosition";
	public static final String MANDATORY = "mandatory";
	public static final String DELETED = "deleted";
	// End of generated constants
	
	private java.lang.String actionId;

	private java.lang.String name;

	private java.lang.Integer retries;
	
	private java.lang.Boolean mandatory;

	private oracle.toplink.indirection.ValueHolderInterface treatmentStageRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface treatmentStreamRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface treatprocSubtypeRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Long xPosition;

	private java.lang.Long yPosition;
	
	private java.lang.Boolean deleted;

	public  TreatmentActionNode()
	{
		// Fill in method body here.
	}

	public java.lang.String getActionId()
	{
		return actionId;
	}

	public java.lang.String getName()
	{
		return name;
	}

	public java.lang.Integer getRetries()
	{
		return retries;
	}

	public com.profitera.descriptor.db.reference.TreatmentStageRef getTreatmentStageRef()
	{
		return (com.profitera.descriptor.db.reference.TreatmentStageRef) treatmentStageRef.getValue();
	}

	public com.profitera.descriptor.db.reference.TreatmentStreamRef getTreatmentStreamRef()
	{
		return (com.profitera.descriptor.db.reference.TreatmentStreamRef) treatmentStreamRef.getValue();
	}

	public com.profitera.descriptor.db.reference.TreatprocSubtypeRef getTreatprocSubtypeRef()
	{
		return (com.profitera.descriptor.db.reference.TreatprocSubtypeRef) treatprocSubtypeRef.getValue();
	}

	public java.lang.Long getXPosition()
	{
		return xPosition;
	}

	public java.lang.Long getYPosition()
	{
		return yPosition;
	}

	public void setActionId(java.lang.String actionId)
	{
		this.actionId = actionId;
	}

	public void setName(java.lang.String name)
	{
		this.name = name;
	}

	public void setRetries(java.lang.Integer retries)
	{
		this.retries = retries;
	}

	public void setTreatmentStageRef(com.profitera.descriptor.db.reference.TreatmentStageRef treatmentStageRef)
	{
		this.treatmentStageRef.setValue(treatmentStageRef);
	}

	public void setTreatmentStreamRef(com.profitera.descriptor.db.reference.TreatmentStreamRef treatmentStreamRef)
	{
		this.treatmentStreamRef.setValue(treatmentStreamRef);
	}

	public void setTreatprocSubtypeRef(com.profitera.descriptor.db.reference.TreatprocSubtypeRef treatprocSubtypeRef)
	{
		this.treatprocSubtypeRef.setValue(treatprocSubtypeRef);
	}

	public void setXPosition(java.lang.Long xPosition)
	{
		this.xPosition = xPosition;
	}

	public void setYPosition(java.lang.Long yPosition)
	{
		this.yPosition = yPosition;
	}
    public java.lang.Boolean getMandatory() {
        return mandatory;
    }
    public void setMandatory(java.lang.Boolean mandatory) {
        this.mandatory = mandatory;
    }
    public java.lang.Boolean getDeleted() {
        return deleted;
    }
    public void setDeleted(java.lang.Boolean deleted) {
       this.deleted = deleted;
    }
}
