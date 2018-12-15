// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class ServiceStatusRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String SERVICE_STATUS_CODE = "serviceStatusCode";
	public static final String SERVICE_STATUS_DESC = "serviceStatusDesc";
	public static final String SERVICE_STATUS_ID = "serviceStatusId";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.String serviceStatusCode;

	private java.lang.String serviceStatusDesc;

	private java.lang.Double serviceStatusId;

	private java.lang.Double sortPriority;

	public  ServiceStatusRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.String getServiceStatusCode()
	{
		return serviceStatusCode;
	}

	public java.lang.String getServiceStatusDesc()
	{
		return serviceStatusDesc;
	}

	public java.lang.Double getServiceStatusId()
	{
		return serviceStatusId;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setServiceStatusCode(java.lang.String serviceStatusCode)
	{
		this.serviceStatusCode = serviceStatusCode;
	}

	public void setServiceStatusDesc(java.lang.String serviceStatusDesc)
	{
		this.serviceStatusDesc = serviceStatusDesc;
	}

	public void setServiceStatusId(java.lang.Double serviceStatusId)
	{
		this.serviceStatusId = serviceStatusId;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
