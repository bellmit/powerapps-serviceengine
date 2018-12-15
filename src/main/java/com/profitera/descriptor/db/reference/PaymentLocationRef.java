// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class PaymentLocationRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String LOCATION_CATEGORY = "locationCategory";
	public static final String LOCATION_NAME = "locationName";
	public static final String PAYMENT_LOCATION_ID = "paymentLocationId";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.String locationCategory;

	private java.lang.String locationName;

	private java.lang.Double paymentLocationId;

	private java.lang.Double sortPriority;

	public  PaymentLocationRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.String getLocationCategory()
	{
		return locationCategory;
	}

	public java.lang.String getLocationName()
	{
		return locationName;
	}

	public java.lang.Double getPaymentLocationId()
	{
		return paymentLocationId;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setLocationCategory(java.lang.String locationCategory)
	{
		this.locationCategory = locationCategory;
	}

	public void setLocationName(java.lang.String locationName)
	{
		this.locationName = locationName;
	}

	public void setPaymentLocationId(java.lang.Double paymentLocationId)
	{
		this.paymentLocationId = paymentLocationId;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
