// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class InvoiceTypeRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String INVOICE_TYPE_CODE = "invoiceTypeCode";
	public static final String INVOICE_TYPE_DESC = "invoiceTypeDesc";
	public static final String INVOICE_TYPE_ID = "invoiceTypeId";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.String invoiceTypeCode;

	private java.lang.String invoiceTypeDesc;

	private java.lang.Double invoiceTypeId;

	private java.lang.Double sortPriority;

	public  InvoiceTypeRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.String getInvoiceTypeCode()
	{
		return invoiceTypeCode;
	}

	public java.lang.String getInvoiceTypeDesc()
	{
		return invoiceTypeDesc;
	}

	public java.lang.Double getInvoiceTypeId()
	{
		return invoiceTypeId;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setInvoiceTypeCode(java.lang.String invoiceTypeCode)
	{
		this.invoiceTypeCode = invoiceTypeCode;
	}

	public void setInvoiceTypeDesc(java.lang.String invoiceTypeDesc)
	{
		this.invoiceTypeDesc = invoiceTypeDesc;
	}

	public void setInvoiceTypeId(java.lang.Double invoiceTypeId)
	{
		this.invoiceTypeId = invoiceTypeId;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
