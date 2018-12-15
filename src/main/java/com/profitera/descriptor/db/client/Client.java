// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.client;

public class Client
	implements java.io.Serializable
{
	// Generated constants
	public static final String ACCOUNT = "account";
	public static final String ADDRESS_DETAILS = "addressDetails";
	public static final String AGENCY_ID = "agencyId";
	public static final String BATCH_DETAILS = "batchDetails";
	public static final String BATCH_PAYMENTS = "batchPayments";
	public static final String CLIENT_ID = "clientId";
	public static final String CLIENT_INVOICE_PAYMENTS = "clientInvoicePayments";
	public static final String CLIENT_INVOICES = "clientInvoices";
	public static final String CLIENT_NAME = "clientName";
	public static final String CLIENT_TYPE_REF = "clientTypeRef";
	public static final String CONTACTS = "contacts";
	public static final String DISABLE = "disable";
	public static final String REMARKS = "remarks";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface account= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private oracle.toplink.indirection.ValueHolderInterface addressDetails= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double agencyId;

	private oracle.toplink.indirection.ValueHolderInterface batchDetails= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private oracle.toplink.indirection.ValueHolderInterface batchPayments= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.Double clientId;

	private oracle.toplink.indirection.ValueHolderInterface clientInvoicePayments= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private oracle.toplink.indirection.ValueHolderInterface clientInvoices= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.String clientName;

	private oracle.toplink.indirection.ValueHolderInterface clientTypeRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface contacts= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.Double disable;

	private java.lang.String remarks;

	private java.lang.Double sortPriority;

	public  Client()
	{
		// Fill in method body here.
	}

	public java.util.Vector getAccount()
	{
		return (java.util.Vector) account.getValue();
	}

	public com.profitera.descriptor.db.contact.AddressDetails getAddressDetails()
	{
		return (com.profitera.descriptor.db.contact.AddressDetails) addressDetails.getValue();
	}

	public java.lang.Double getAgencyId()
	{
		return agencyId;
	}

	public java.util.Vector getBatchDetails()
	{
		return (java.util.Vector) batchDetails.getValue();
	}

	public java.util.Vector getBatchPayments()
	{
		return (java.util.Vector) batchPayments.getValue();
	}

	public java.lang.Double getClientId()
	{
		return clientId;
	}

	public java.util.Vector getClientInvoicePayments()
	{
		return (java.util.Vector) clientInvoicePayments.getValue();
	}

	public java.util.Vector getClientInvoices()
	{
		return (java.util.Vector) clientInvoices.getValue();
	}

	public java.lang.String getClientName()
	{
		return clientName;
	}

	public com.profitera.descriptor.db.reference.ClientTypeRef getClientTypeRef()
	{
		return (com.profitera.descriptor.db.reference.ClientTypeRef) clientTypeRef.getValue();
	}

	public java.util.Vector getContacts()
	{
		return (java.util.Vector) contacts.getValue();
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.String getRemarks()
	{
		return remarks;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setAccount(java.util.Vector account)
	{
		this.account.setValue(account);
	}

	public void setAddressDetails(com.profitera.descriptor.db.contact.AddressDetails addressDetails)
	{
		this.addressDetails.setValue(addressDetails);
	}

	public void setAgencyId(java.lang.Double agencyId)
	{
		this.agencyId = agencyId;
	}

	public void setBatchDetails(java.util.Vector batchDetails)
	{
		this.batchDetails.setValue(batchDetails);
	}

	public void setBatchPayments(java.util.Vector batchPayments)
	{
		this.batchPayments.setValue(batchPayments);
	}

	public void setClientId(java.lang.Double clientId)
	{
		this.clientId = clientId;
	}

	public void setClientInvoicePayments(java.util.Vector clientInvoicePayments)
	{
		this.clientInvoicePayments.setValue(clientInvoicePayments);
	}

	public void setClientInvoices(java.util.Vector clientInvoices)
	{
		this.clientInvoices.setValue(clientInvoices);
	}

	public void setClientName(java.lang.String clientName)
	{
		this.clientName = clientName;
	}

	public void setClientTypeRef(com.profitera.descriptor.db.reference.ClientTypeRef clientTypeRef)
	{
		this.clientTypeRef.setValue(clientTypeRef);
	}

	public void setContacts(java.util.Vector contacts)
	{
		this.contacts.setValue(contacts);
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setRemarks(java.lang.String remarks)
	{
		this.remarks = remarks;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
