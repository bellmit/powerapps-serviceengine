// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class ClientTypeRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String CLIENT_TYPE_CODE = "clientTypeCode";
	public static final String CLIENT_TYPE_DESC = "clientTypeDesc";
	public static final String CLIENT_TYPE_ID = "clientTypeId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String clientTypeCode;

	private java.lang.String clientTypeDesc;

	private java.lang.Double clientTypeId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  ClientTypeRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getClientTypeCode()
	{
		return clientTypeCode;
	}

	public java.lang.String getClientTypeDesc()
	{
		return clientTypeDesc;
	}

	public java.lang.Double getClientTypeId()
	{
		return clientTypeId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setClientTypeCode(java.lang.String clientTypeCode)
	{
		this.clientTypeCode = clientTypeCode;
	}

	public void setClientTypeDesc(java.lang.String clientTypeDesc)
	{
		this.clientTypeDesc = clientTypeDesc;
	}

	public void setClientTypeId(java.lang.Double clientTypeId)
	{
		this.clientTypeId = clientTypeId;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
