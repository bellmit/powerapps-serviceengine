// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.contact;

public class ContactNumber
	implements java.io.Serializable
{
	// Generated constants
	public static final String NO = "no";
	public static final String PARENT_ADDRESS_DETAILS = "parentAddressDetails";
	public static final String PREFERRED_POSITION = "preferredPosition";
	public static final String TYPE_REF = "typeRef";
	// End of generated constants
	
	private java.lang.String no;

	private oracle.toplink.indirection.ValueHolderInterface parentAddressDetails= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double preferredPosition;

	private oracle.toplink.indirection.ValueHolderInterface typeRef= new oracle.toplink.indirection.ValueHolder();

	public  ContactNumber()
	{
		// Fill in method body here.
	}

	public java.lang.String getNo()
	{
		return no;
	}

	public com.profitera.descriptor.db.contact.AddressDetails getParentAddressDetails()
	{
		return (com.profitera.descriptor.db.contact.AddressDetails) parentAddressDetails.getValue();
	}

	public java.lang.Double getPreferredPosition()
	{
		return preferredPosition;
	}

	public com.profitera.descriptor.db.reference.ContactNumberTypeRef getTypeRef()
	{
		return (com.profitera.descriptor.db.reference.ContactNumberTypeRef) typeRef.getValue();
	}

	public void setNo(java.lang.String no)
	{
		this.no = no;
	}

	public void setParentAddressDetails(com.profitera.descriptor.db.contact.AddressDetails parentAddressDetails)
	{
		this.parentAddressDetails.setValue(parentAddressDetails);
	}

	public void setPreferredPosition(java.lang.Double preferredPosition)
	{
		this.preferredPosition = preferredPosition;
	}

	public void setTypeRef(com.profitera.descriptor.db.reference.ContactNumberTypeRef typeRef)
	{
		this.typeRef.setValue(typeRef);
	}
}
