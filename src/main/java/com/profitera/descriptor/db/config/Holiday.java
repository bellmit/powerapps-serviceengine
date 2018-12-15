// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.config;

public class Holiday
	implements java.io.Serializable
{
	// Generated constants
	public static final String HOLIDAY_DATE = "holidayDate";
	public static final String HOLIDAY_DESC = "holidayDesc";
	public static final String HOLIDAY_ID = "holidayId";
	public static final String HOLIDAY_STATE_REL = "holidayStateRel";
	public static final String NO_OF_DAYS = "noOfDays";
	// End of generated constants
	
	private java.sql.Timestamp holidayDate;

	private java.lang.String holidayDesc;

	private java.lang.Double holidayId;

	private oracle.toplink.indirection.ValueHolderInterface holidayStateRel= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.Integer noOfDays;

	public  Holiday()
	{
		// Fill in method body here.
	}

	public java.sql.Timestamp getHolidayDate()
	{
		return holidayDate;
	}

	public java.lang.String getHolidayDesc()
	{
		return holidayDesc;
	}

	public java.lang.Double getHolidayId()
	{
		return holidayId;
	}

	public java.util.Vector getHolidayStateRel()
	{
		return (java.util.Vector) holidayStateRel.getValue();
	}

	public java.lang.Integer getNoOfDays()
	{
		return noOfDays;
	}

	public void setHolidayDate(java.sql.Timestamp holidayDate)
	{
		this.holidayDate = holidayDate;
	}

	public void setHolidayDesc(java.lang.String holidayDesc)
	{
		this.holidayDesc = holidayDesc;
	}

	public void setHolidayId(java.lang.Double holidayId)
	{
		this.holidayId = holidayId;
	}

	public void setHolidayStateRel(java.util.Vector holidayStateRel)
	{
		this.holidayStateRel.setValue(holidayStateRel);
	}

	public void setNoOfDays(java.lang.Integer noOfDays)
	{
		this.noOfDays = noOfDays;
	}
}
