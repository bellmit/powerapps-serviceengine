// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.history;

public class WorklistHistory
	implements java.io.Serializable
{
	// Generated constants
	public static final String ASSIGNED_DATE_TIME = "assignedDateTime";
	public static final String CUSTOMER_ID = "customerId";
	public static final String DELINQUENT_AMT = "delinquentAmt";
	public static final String END_DATE_TIME = "endDateTime";
	public static final String MOVED_DATE_TIME = "movedDateTime";
	public static final String START_DATE_TIME = "startDateTime";
	public static final String USER_ID = "userId";
	public static final String WORK_LIST_ID = "workListId";
	public static final String WORKLIST_HISTORY_ID = "worklistHistoryId";
	public static final String WORKLIST_STATUS = "worklistStatus";
	// End of generated constants
	
	private java.sql.Timestamp assignedDateTime;

	private java.lang.String customerId;

	private java.lang.Double delinquentAmt;

	private java.sql.Timestamp endDateTime;

	private java.sql.Timestamp movedDateTime;

	private java.sql.Timestamp startDateTime;

	private java.lang.String userId;

	private java.lang.Double workListId;

	private java.lang.Double worklistHistoryId;

	private java.lang.Double worklistStatus;

	public  WorklistHistory()
	{
		// Fill in method body here.
	}

	public java.sql.Timestamp getAssignedDateTime()
	{
		return assignedDateTime;
	}

	public java.lang.String getCustomerId()
	{
		return customerId;
	}

	public java.lang.Double getDelinquentAmt()
	{
		return delinquentAmt;
	}

	public java.sql.Timestamp getEndDateTime()
	{
		return endDateTime;
	}

	public java.sql.Timestamp getMovedDateTime()
	{
		return movedDateTime;
	}

	public java.sql.Timestamp getStartDateTime()
	{
		return startDateTime;
	}

	public java.lang.String getUserId()
	{
		return userId;
	}

	public java.lang.Double getWorkListId()
	{
		return workListId;
	}

	public java.lang.Double getWorklistHistoryId()
	{
		return worklistHistoryId;
	}

	public java.lang.Double getWorklistStatus()
	{
		return worklistStatus;
	}

	public void setAssignedDateTime(java.sql.Timestamp assignedDateTime)
	{
		this.assignedDateTime = assignedDateTime;
	}

	public void setCustomerId(java.lang.String customerId)
	{
		this.customerId = customerId;
	}

	public void setDelinquentAmt(java.lang.Double delinquentAmt)
	{
		this.delinquentAmt = delinquentAmt;
	}

	public void setEndDateTime(java.sql.Timestamp endDateTime)
	{
		this.endDateTime = endDateTime;
	}

	public void setMovedDateTime(java.sql.Timestamp movedDateTime)
	{
		this.movedDateTime = movedDateTime;
	}

	public void setStartDateTime(java.sql.Timestamp startDateTime)
	{
		this.startDateTime = startDateTime;
	}

	public void setUserId(java.lang.String userId)
	{
		this.userId = userId;
	}

	public void setWorkListId(java.lang.Double workListId)
	{
		this.workListId = workListId;
	}

	public void setWorklistHistoryId(java.lang.Double worklistHistoryId)
	{
		this.worklistHistoryId = worklistHistoryId;
	}

	public void setWorklistStatus(java.lang.Double worklistStatus)
	{
		this.worklistStatus = worklistStatus;
	}
}
