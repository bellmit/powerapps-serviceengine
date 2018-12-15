// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.treatment;

public class ActionConversionMatrix
	implements java.io.Serializable
{
	// Generated constants
	public static final String ACTION_TYPE = "actionType";
	public static final String TREATMENT_PROCESS_CLASS = "treatmentProcessClass";
	// End of generated constants
	
	private java.lang.String actionType;

	private java.lang.String treatmentProcessClass;

	public  ActionConversionMatrix()
	{
		// Fill in method body here.
	}

	public java.lang.String getActionType()
	{
		return actionType;
	}

	public java.lang.String getTreatmentProcessClass()
	{
		return treatmentProcessClass;
	}

	public void setActionType(java.lang.String actionType)
	{
		this.actionType = actionType;
	}

	public void setTreatmentProcessClass(java.lang.String treatmentProcessClass)
	{
		this.treatmentProcessClass = treatmentProcessClass;
	}
}
