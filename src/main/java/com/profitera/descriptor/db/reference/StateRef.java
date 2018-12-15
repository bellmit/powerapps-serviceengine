// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class StateRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String ALT_WEEKEND_REF = "altWeekendRef";
	public static final String COUNTRY_REF = "countryRef";
	public static final String DISABLE = "disable";
	public static final String HOLIDAY_STATE_REL = "holidayStateRel";
	public static final String SORT_PRIORITY = "sortPriority";
	public static final String STATE_CODE = "stateCode";
	public static final String STATE_DESC = "stateDesc";
	public static final String STATE_ID = "stateId";
	public static final String WEEKEND_REF = "weekendRef";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface altWeekendRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface countryRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double disable;

	private oracle.toplink.indirection.ValueHolderInterface holidayStateRel= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.Double sortPriority;

	private java.lang.String stateCode;

	private java.lang.String stateDesc;

	private java.lang.Double stateId;

	private oracle.toplink.indirection.ValueHolderInterface weekendRef= new oracle.toplink.indirection.ValueHolder();

	public  StateRef()
	{
		// Fill in method body here.
	}

	public com.profitera.descriptor.db.reference.AlternateWeekendRef getAltWeekendRef()
	{
		return (com.profitera.descriptor.db.reference.AlternateWeekendRef) altWeekendRef.getValue();
	}

	public com.profitera.descriptor.db.reference.CountryRef getCountryRef()
	{
		return (com.profitera.descriptor.db.reference.CountryRef) countryRef.getValue();
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.util.Vector getHolidayStateRel()
	{
		return (java.util.Vector) holidayStateRel.getValue();
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public java.lang.String getStateCode()
	{
		return stateCode;
	}

	public java.lang.String getStateDesc()
	{
		return stateDesc;
	}

	public java.lang.Double getStateId()
	{
		return stateId;
	}

	public com.profitera.descriptor.db.reference.WeekendRef getWeekendRef()
	{
		return (com.profitera.descriptor.db.reference.WeekendRef) weekendRef.getValue();
	}

	public void setAltWeekendRef(com.profitera.descriptor.db.reference.AlternateWeekendRef altWeekendRef)
	{
		this.altWeekendRef.setValue(altWeekendRef);
	}

	public void setCountryRef(com.profitera.descriptor.db.reference.CountryRef countryRef)
	{
		this.countryRef.setValue(countryRef);
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setHolidayStateRel(java.util.Vector holidayStateRel)
	{
		this.holidayStateRel.setValue(holidayStateRel);
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}

	public void setStateCode(java.lang.String stateCode)
	{
		this.stateCode = stateCode;
	}

	public void setStateDesc(java.lang.String stateDesc)
	{
		this.stateDesc = stateDesc;
	}

	public void setStateId(java.lang.Double stateId)
	{
		this.stateId = stateId;
	}

	public void setWeekendRef(com.profitera.descriptor.db.reference.WeekendRef weekendRef)
	{
		this.weekendRef.setValue(weekendRef);
	}
}
