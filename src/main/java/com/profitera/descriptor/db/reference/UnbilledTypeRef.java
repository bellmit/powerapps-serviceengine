// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class UnbilledTypeRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	public static final String TRANSCATION_CODE_REF = "transcationCodeRef";
	public static final String UNBILLED_TYPE_CODE = "unbilledTypeCode";
	public static final String UNBILLED_TYPE_DESC = "unbilledTypeDesc";
	public static final String UNBILLED_TYPE_ID = "unbilledTypeId";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	private oracle.toplink.indirection.ValueHolderInterface transcationCodeRef= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.String unbilledTypeCode;

	private java.lang.String unbilledTypeDesc;

	private java.lang.Double unbilledTypeId;

	public  UnbilledTypeRef()
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

	public java.util.Vector getTranscationCodeRef()
	{
		return (java.util.Vector) transcationCodeRef.getValue();
	}

	public java.lang.String getUnbilledTypeCode()
	{
		return unbilledTypeCode;
	}

	public java.lang.String getUnbilledTypeDesc()
	{
		return unbilledTypeDesc;
	}

	public java.lang.Double getUnbilledTypeId()
	{
		return unbilledTypeId;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}

	public void setTranscationCodeRef(java.util.Vector transcationCodeRef)
	{
		this.transcationCodeRef.setValue(transcationCodeRef);
	}

	public void setUnbilledTypeCode(java.lang.String unbilledTypeCode)
	{
		this.unbilledTypeCode = unbilledTypeCode;
	}

	public void setUnbilledTypeDesc(java.lang.String unbilledTypeDesc)
	{
		this.unbilledTypeDesc = unbilledTypeDesc;
	}

	public void setUnbilledTypeId(java.lang.Double unbilledTypeId)
	{
		this.unbilledTypeId = unbilledTypeId;
	}
}
