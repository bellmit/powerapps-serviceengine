// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.user;

public class UserWorkListAssign
	implements java.io.Serializable
{
	// Generated constants
	public static final String ASSIGNER = "assigner";
	public static final String COMPLETED_BY_DATE = "completedByDate";
	public static final String EFFECTIVE_DATE = "effectiveDate";
	public static final String EXPIRY_DATE = "expiryDate";
	public static final String PRIORITY = "priority";
	public static final String USER = "user";
	public static final String WORK_LIST = "workList";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface assigner= new oracle.toplink.indirection.ValueHolder();

	private java.sql.Timestamp completedByDate;

	private java.sql.Timestamp effectiveDate;

	private java.sql.Timestamp expiryDate;

	private java.lang.Double priority;

	private oracle.toplink.indirection.ValueHolderInterface user= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface workList= new oracle.toplink.indirection.ValueHolder();

	public  UserWorkListAssign()
	{
		// Fill in method body here.
	}

	public com.profitera.descriptor.db.user.User getAssigner()
	{
		return (com.profitera.descriptor.db.user.User) assigner.getValue();
	}

	public java.sql.Timestamp getCompletedByDate()
	{
		return completedByDate;
	}

	public java.sql.Timestamp getEffectiveDate()
	{
		return effectiveDate;
	}

	public java.sql.Timestamp getExpiryDate()
	{
		return expiryDate;
	}

	public java.lang.Double getPriority()
	{
		return priority;
	}

	public com.profitera.descriptor.db.user.User getUser()
	{
		return (com.profitera.descriptor.db.user.User) user.getValue();
	}

	public com.profitera.descriptor.db.worklist.WorkList getWorkList()
	{
		return (com.profitera.descriptor.db.worklist.WorkList) workList.getValue();
	}

	public void setAssigner(com.profitera.descriptor.db.user.User assigner)
	{
		this.assigner.setValue(assigner);
	}

	public void setCompletedByDate(java.sql.Timestamp completedByDate)
	{
		this.completedByDate = completedByDate;
	}

	public void setEffectiveDate(java.sql.Timestamp effectiveDate)
	{
		this.effectiveDate = effectiveDate;
	}

	public void setExpiryDate(java.sql.Timestamp expiryDate)
	{
		this.expiryDate = expiryDate;
	}

	public void setPriority(java.lang.Double priority)
	{
		this.priority = priority;
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
