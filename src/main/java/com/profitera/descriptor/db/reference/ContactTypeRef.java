// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class ContactTypeRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String CONTACT_TYPE_CODE = "contactTypeCode";
	public static final String CONTACT_TYPE_DESC = "contactTypeDesc";
	public static final String CONTACT_TYPE_ID = "contactTypeId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String contactTypeCode;

	private java.lang.String contactTypeDesc;

	private java.lang.Double contactTypeId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  ContactTypeRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getContactTypeCode()
	{
		return contactTypeCode;
	}

	public java.lang.String getContactTypeDesc()
	{
		return contactTypeDesc;
	}

	public java.lang.Double getContactTypeId()
	{
		return contactTypeId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setContactTypeCode(java.lang.String contactTypeCode)
	{
		this.contactTypeCode = contactTypeCode;
	}

	public void setContactTypeDesc(java.lang.String contactTypeDesc)
	{
		this.contactTypeDesc = contactTypeDesc;
	}

	public void setContactTypeId(java.lang.Double contactTypeId)
	{
		this.contactTypeId = contactTypeId;
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
