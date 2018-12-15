// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class NotificationTypeRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String NOTIFICATION_TYPE_CODE = "notificationTypeCode";
	public static final String NOTIFICATION_TYPE_DESC = "notificationTypeDesc";
	public static final String NOTIFICATION_TYPE_ID = "notificationTypeId";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.String notificationTypeCode;

	private java.lang.String notificationTypeDesc;

	private java.lang.Double notificationTypeId;

	private java.lang.Double sortPriority;

	public  NotificationTypeRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.String getNotificationTypeCode()
	{
		return notificationTypeCode;
	}

	public java.lang.String getNotificationTypeDesc()
	{
		return notificationTypeDesc;
	}

	public java.lang.Double getNotificationTypeId()
	{
		return notificationTypeId;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setNotificationTypeCode(java.lang.String notificationTypeCode)
	{
		this.notificationTypeCode = notificationTypeCode;
	}

	public void setNotificationTypeDesc(java.lang.String notificationTypeDesc)
	{
		this.notificationTypeDesc = notificationTypeDesc;
	}

	public void setNotificationTypeId(java.lang.Double notificationTypeId)
	{
		this.notificationTypeId = notificationTypeId;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
