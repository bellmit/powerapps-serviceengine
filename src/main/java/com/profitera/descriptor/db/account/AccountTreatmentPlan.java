// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.account;

public class AccountTreatmentPlan
	implements java.io.Serializable
{
	// Generated constants
	public static final String ACCOUNT = "account";
	public static final String LAST_TREATMENT_ACTION_REF = "lastTreatmentActionRef";
	public static final String LAST_TREATMENT_RESULT_REF = "lastTreatmentResultRef";
	public static final String NODE_LOCATION = "nodeLocation";
	public static final String TREATMENT_END_DATE = "treatmentEndDate";
	public static final String TREATMENT_PLAN_DESC = "treatmentPlanDesc";
	public static final String TREATMENT_PLAN_ID = "treatmentPlanId";
	public static final String TREATMENT_PROCESSES = "treatmentProcesses";
	public static final String TREATMENT_START_DATE = "treatmentStartDate";
	public static final String TREATMENT_STREAM_REF = "treatmentStreamRef";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface account= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface lastTreatmentActionRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface lastTreatmentResultRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.String nodeLocation;

	private java.sql.Timestamp treatmentEndDate;

	private java.lang.String treatmentPlanDesc;

	private java.lang.Double treatmentPlanId;

	private oracle.toplink.indirection.ValueHolderInterface treatmentProcesses= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.sql.Timestamp treatmentStartDate;

	private oracle.toplink.indirection.ValueHolderInterface treatmentStreamRef= new oracle.toplink.indirection.ValueHolder();

	public  AccountTreatmentPlan()
	{
		// Fill in method body here.
	}

	public com.profitera.descriptor.db.account.Account getAccount()
	{
		return (com.profitera.descriptor.db.account.Account) account.getValue();
	}

	public com.profitera.descriptor.db.treatment.TreatmentProcess getLastTreatmentActionRef()
	{
		return (com.profitera.descriptor.db.treatment.TreatmentProcess) lastTreatmentActionRef.getValue();
	}

	public com.profitera.descriptor.db.treatment.TreatmentProcess getLastTreatmentResultRef()
	{
		return (com.profitera.descriptor.db.treatment.TreatmentProcess) lastTreatmentResultRef.getValue();
	}

	public java.lang.String getNodeLocation()
	{
		return nodeLocation;
	}

	public java.sql.Timestamp getTreatmentEndDate()
	{
		return treatmentEndDate;
	}

	public java.lang.String getTreatmentPlanDesc()
	{
		return treatmentPlanDesc;
	}

	public java.lang.Double getTreatmentPlanId()
	{
		return treatmentPlanId;
	}

	public java.util.Vector getTreatmentProcesses()
	{
		return (java.util.Vector) treatmentProcesses.getValue();
	}

	public java.sql.Timestamp getTreatmentStartDate()
	{
		return treatmentStartDate;
	}

	public com.profitera.descriptor.db.reference.TreatmentStreamRef getTreatmentStreamRef()
	{
		return (com.profitera.descriptor.db.reference.TreatmentStreamRef) treatmentStreamRef.getValue();
	}

	public void setAccount(com.profitera.descriptor.db.account.Account account)
	{
		this.account.setValue(account);
	}

	public void setLastTreatmentActionRef(com.profitera.descriptor.db.treatment.TreatmentProcess lastTreatmentActionRef)
	{
		this.lastTreatmentActionRef.setValue(lastTreatmentActionRef);
	}

	public void setLastTreatmentResultRef(com.profitera.descriptor.db.treatment.TreatmentProcess lastTreatmentResultRef)
	{
		this.lastTreatmentResultRef.setValue(lastTreatmentResultRef);
	}

	public void setNodeLocation(java.lang.String nodeLocation)
	{
		this.nodeLocation = nodeLocation;
	}

	public void setTreatmentEndDate(java.sql.Timestamp treatmentEndDate)
	{
		this.treatmentEndDate = treatmentEndDate;
	}

	public void setTreatmentPlanDesc(java.lang.String treatmentPlanDesc)
	{
		this.treatmentPlanDesc = treatmentPlanDesc;
	}

	public void setTreatmentPlanId(java.lang.Double treatmentPlanId)
	{
		this.treatmentPlanId = treatmentPlanId;
	}

	public void setTreatmentProcesses(java.util.Vector treatmentProcesses)
	{
		this.treatmentProcesses.setValue(treatmentProcesses);
	}

	public void setTreatmentStartDate(java.sql.Timestamp treatmentStartDate)
	{
		this.treatmentStartDate = treatmentStartDate;
	}

	public void setTreatmentStreamRef(com.profitera.descriptor.db.reference.TreatmentStreamRef treatmentStreamRef)
	{
		this.treatmentStreamRef.setValue(treatmentStreamRef);
	}
}
