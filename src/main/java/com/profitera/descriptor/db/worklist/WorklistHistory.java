// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.worklist;

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
	
	private java.sql.Date assignedDateTime;

	private java.lang.String customerId;

	private java.lang.String delinquentAmt;

	private java.sql.Date endDateTime;

	private java.sql.Date movedDateTime;

	private java.sql.Date startDateTime;

	private java.lang.String userId;

	private java.lang.String workListId;

	private java.lang.String worklistHistoryId;

	private java.lang.String worklistStatus;

	public java.sql.Date getAssignedDateTime()
	{
		return assignedDateTime;
	}

	public java.lang.String getCustomerId()
	{
		return customerId;
	}

	public java.lang.String getDelinquentAmt()
	{
		return delinquentAmt;
	}

	public java.sql.Date getEndDateTime()
	{
		return endDateTime;
	}

	public java.sql.Date getMovedDateTime()
	{
		return movedDateTime;
	}

	public java.sql.Date getStartDateTime()
	{
		return startDateTime;
	}

	public java.lang.String getUserId()
	{
		return userId;
	}

	public java.lang.String getWorkListId()
	{
		return workListId;
	}

	public java.lang.String getWorklistHistoryId()
	{
		return worklistHistoryId;
	}

	public java.lang.String getWorklistStatus()
	{
		return worklistStatus;
	}

	public void setAssignedDateTime(java.sql.Date assignedDateTime)
	{
		this.assignedDateTime = assignedDateTime;
	}

	public void setCustomerId(java.lang.String customerId)
	{
		this.customerId = customerId;
	}

	public void setDelinquentAmt(java.lang.String delinquentAmt)
	{
		this.delinquentAmt = delinquentAmt;
	}

	public void setEndDateTime(java.sql.Date endDateTime)
	{
		this.endDateTime = endDateTime;
	}

	public void setMovedDateTime(java.sql.Date movedDateTime)
	{
		this.movedDateTime = movedDateTime;
	}

	public void setStartDateTime(java.sql.Date startDateTime)
	{
		this.startDateTime = startDateTime;
	}

	public void setUserId(java.lang.String userId)
	{
		this.userId = userId;
	}

	public void setWorkListId(java.lang.String workListId)
	{
		this.workListId = workListId;
	}

	public void setWorklistHistoryId(java.lang.String worklistHistoryId)
	{
		this.worklistHistoryId = worklistHistoryId;
	}

	public void setWorklistStatus(java.lang.String worklistStatus)
	{
		this.worklistStatus = worklistStatus;
	}
}
