// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.relation;

public class HolidayStateRel
	implements java.io.Serializable
{
	// Generated constants
	public static final String HOLIDAY = "holiday";
	public static final String STATE_REF = "stateRef";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface holiday= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface stateRef= new oracle.toplink.indirection.ValueHolder();

	public  HolidayStateRel()
	{
		// Fill in method body here.
	}

	public com.profitera.descriptor.db.config.Holiday getHoliday()
	{
		return (com.profitera.descriptor.db.config.Holiday) holiday.getValue();
	}

	public com.profitera.descriptor.db.reference.StateRef getStateRef()
	{
		return (com.profitera.descriptor.db.reference.StateRef) stateRef.getValue();
	}

	public void setHoliday(com.profitera.descriptor.db.config.Holiday holiday)
	{
		this.holiday.setValue(holiday);
	}

	public void setStateRef(com.profitera.descriptor.db.reference.StateRef stateRef)
	{
		this.stateRef.setValue(stateRef);
	}
}
