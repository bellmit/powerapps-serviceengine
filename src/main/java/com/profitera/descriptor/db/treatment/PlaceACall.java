// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.treatment;

public class PlaceACall
	extends com.profitera.descriptor.db.treatment.TreatmentProcess
	implements java.io.Serializable
{
	// Generated constants
	public static final String CALL_DURATION = "callDuration";
	public static final String CALL_TIME = "callTime";
	public static final String CALL_UOM_REF = "callUomRef";
	public static final String CONTACT_NO = "contactNo";
	public static final String CONTACT_PERSON_NAME = "contactPersonName";
	public static final String SCHEDULED = "scheduled";
	// End of generated constants
	
	private java.lang.Double callDuration;

	private java.sql.Timestamp callTime;

	private oracle.toplink.indirection.ValueHolderInterface callUomRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.String contactNo;

	private java.lang.String contactPersonName;

	private java.lang.Character scheduled;

	public  PlaceACall()
	{
		// Fill in method body here.
	}

	public java.lang.Double getCallDuration()
	{
		return callDuration;
	}

	public java.sql.Timestamp getCallTime()
	{
		return callTime;
	}

	public com.profitera.descriptor.db.reference.UomMeasureRef getCallUomRef()
	{
		return (com.profitera.descriptor.db.reference.UomMeasureRef) callUomRef.getValue();
	}

	public java.lang.String getContactNo()
	{
		return contactNo;
	}

	public java.lang.String getContactPersonName()
	{
		return contactPersonName;
	}

	public java.lang.Character getScheduled()
	{
		return scheduled;
	}

	public void setCallDuration(java.lang.Double callDuration)
	{
		this.callDuration = callDuration;
	}

	public void setCallTime(java.sql.Timestamp callTime)
	{
		this.callTime = callTime;
	}

	public void setCallUomRef(com.profitera.descriptor.db.reference.UomMeasureRef callUomRef)
	{
		this.callUomRef.setValue(callUomRef);
	}

	public void setContactNo(java.lang.String contactNo)
	{
		this.contactNo = contactNo;
	}

	public void setContactPersonName(java.lang.String contactPersonName)
	{
		this.contactPersonName = contactPersonName;
	}

	public void setScheduled(java.lang.Character scheduled)
	{
		this.scheduled = scheduled;
	}
}
