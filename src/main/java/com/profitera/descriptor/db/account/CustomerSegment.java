// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.account;

public class CustomerSegment
	implements java.io.Serializable
{
	// Generated constants
	public static final String CHILD_CUSTOMER_SEGMENTS = "childCustomerSegments";
	public static final String CUST_SEGMENT_DESC = "custSegmentDesc";
	public static final String CUSTOMER_SEGMENT_CODE = "customerSegmentCode";
	public static final String CUSTOMER_SEGMENT_ID = "customerSegmentId";
	public static final String CUSTOMERS = "customers";
	public static final String DISABLE = "disable";
	public static final String PARENT_CUSTOMER_SEGMENT = "parentCustomerSegment";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface childCustomerSegments= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.String custSegmentDesc;

	private java.lang.String customerSegmentCode;

	private java.lang.Double customerSegmentId;

	private oracle.toplink.indirection.ValueHolderInterface customers= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.Double disable;

	private oracle.toplink.indirection.ValueHolderInterface parentCustomerSegment= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double sortPriority;

	public  CustomerSegment()
	{
		// Fill in method body here.
	}

	public java.util.Vector getChildCustomerSegments()
	{
		return (java.util.Vector) childCustomerSegments.getValue();
	}

	public java.lang.String getCustSegmentDesc()
	{
		return custSegmentDesc;
	}

	public java.lang.String getCustomerSegmentCode()
	{
		return customerSegmentCode;
	}

	public java.lang.Double getCustomerSegmentId()
	{
		return customerSegmentId;
	}

	public java.util.Vector getCustomers()
	{
		return (java.util.Vector) customers.getValue();
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public com.profitera.descriptor.db.account.CustomerSegment getParentCustomerSegment()
	{
		return (com.profitera.descriptor.db.account.CustomerSegment) parentCustomerSegment.getValue();
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setChildCustomerSegments(java.util.Vector childCustomerSegments)
	{
		this.childCustomerSegments.setValue(childCustomerSegments);
	}

	public void setCustSegmentDesc(java.lang.String custSegmentDesc)
	{
		this.custSegmentDesc = custSegmentDesc;
	}

	public void setCustomerSegmentCode(java.lang.String customerSegmentCode)
	{
		this.customerSegmentCode = customerSegmentCode;
	}

	public void setCustomerSegmentId(java.lang.Double customerSegmentId)
	{
		this.customerSegmentId = customerSegmentId;
	}

	public void setCustomers(java.util.Vector customers)
	{
		this.customers.setValue(customers);
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setParentCustomerSegment(com.profitera.descriptor.db.account.CustomerSegment parentCustomerSegment)
	{
		this.parentCustomerSegment.setValue(parentCustomerSegment);
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
