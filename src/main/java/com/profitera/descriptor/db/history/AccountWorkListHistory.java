// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.history;

public class AccountWorkListHistory
	implements java.io.Serializable
{
	// Generated constants
	public static final String ACC_WORKLIST_HISTORY_ID = "accWorklistHistoryId";
	public static final String ACC_WORKLIST_STATUS_REF = "accWorklistStatusRef";
	public static final String ACCOUNT = "account";
	public static final String ASSIGNED_DATE_TIME = "assignedDateTime";
	public static final String DELINQENT_AMT = "delinqentAmt";
	public static final String END_DATE_TIME = "endDateTime";
	public static final String MOVED_DATE_TIME = "movedDateTime";
	public static final String START_DATE_TIME = "startDateTime";
	public static final String USER = "user";
	public static final String WORK_LIST = "workList";
	// End of generated constants
	
	private java.lang.Double accWorklistHistoryId;

	private oracle.toplink.indirection.ValueHolderInterface accWorklistStatusRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface account= new oracle.toplink.indirection.ValueHolder();

	private java.sql.Timestamp assignedDateTime;

	private java.lang.Double delinqentAmt;

	private java.sql.Timestamp endDateTime;

	private java.sql.Timestamp movedDateTime;

	private java.sql.Timestamp startDateTime;

	private oracle.toplink.indirection.ValueHolderInterface user= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface workList= new oracle.toplink.indirection.ValueHolder();

	public  AccountWorkListHistory()
	{
		// Fill in method body here.
	}

	public java.lang.Double getAccWorklistHistoryId()
	{
		return accWorklistHistoryId;
	}

	public com.profitera.descriptor.db.reference.AccountWorkListStatusRef getAccWorklistStatusRef()
	{
		return (com.profitera.descriptor.db.reference.AccountWorkListStatusRef) accWorklistStatusRef.getValue();
	}

	public com.profitera.descriptor.db.account.Account getAccount()
	{
		return (com.profitera.descriptor.db.account.Account) account.getValue();
	}

	public java.sql.Timestamp getAssignedDateTime()
	{
		return assignedDateTime;
	}

	public java.lang.Double getDelinqentAmt()
	{
		return delinqentAmt;
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

	public com.profitera.descriptor.db.user.User getUser()
	{
		return (com.profitera.descriptor.db.user.User) user.getValue();
	}

	public com.profitera.descriptor.db.worklist.WorkList getWorkList()
	{
		return (com.profitera.descriptor.db.worklist.WorkList) workList.getValue();
	}

	public void setAccWorklistHistoryId(java.lang.Double accWorklistHistoryId)
	{
		this.accWorklistHistoryId = accWorklistHistoryId;
	}

	public void setAccWorklistStatusRef(com.profitera.descriptor.db.reference.AccountWorkListStatusRef accWorklistStatusRef)
	{
		this.accWorklistStatusRef.setValue(accWorklistStatusRef);
	}

	public void setAccount(com.profitera.descriptor.db.account.Account account)
	{
		this.account.setValue(account);
	}

	public void setAssignedDateTime(java.sql.Timestamp assignedDateTime)
	{
		this.assignedDateTime = assignedDateTime;
	}

	public void setDelinqentAmt(java.lang.Double delinqentAmt)
	{
		this.delinqentAmt = delinqentAmt;
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

	public void setUser(com.profitera.descriptor.db.user.User user)
	{
		this.user.setValue(user);
	}

	public void setWorkList(com.profitera.descriptor.db.worklist.WorkList workList)
	{
		this.workList.setValue(workList);
	}
}
