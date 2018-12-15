// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.user;

public class User
	implements java.io.Serializable
{
	// Generated constants
	public static final String ACTIVE_STATUS = "activeStatus";
	public static final String AGENCY = "agency";
	public static final String AUDIT_LOG = "auditLog";
	public static final String BATCHES = "batches";
	public static final String CREATE_DATE = "createDate";
	public static final String EMPLOYEE = "employee";
	public static final String IS_COLLECTOR = "isCollector";
	public static final String LOGON_STATUS = "logonStatus";
	public static final String PASSWD_EXP_DATE = "passwdExpDate";
	public static final String PASSWORD = "password";
	public static final String PASSWORD_HISTORY = "passwordHistory";
	public static final String ROLES = "roles";
	public static final String TEAMS = "teams";
	public static final String USER_EMAIL_ADDRESS = "userEmailAddress";
	public static final String USER_EXP_DATE = "userExpDate";
	public static final String USER_ID = "userId";
	public static final String WORKLISTS = "worklists";
	// End of generated constants
	
	private java.lang.Character activeStatus;

	private oracle.toplink.indirection.ValueHolderInterface agency= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface auditLog= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private oracle.toplink.indirection.ValueHolderInterface batches= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.sql.Timestamp createDate;

	private oracle.toplink.indirection.ValueHolderInterface employee= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double isCollector;

	private java.lang.Character logonStatus;

	private java.sql.Timestamp passwdExpDate;

	private java.lang.String password;

	private oracle.toplink.indirection.ValueHolderInterface passwordHistory= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private oracle.toplink.indirection.ValueHolderInterface roles= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private oracle.toplink.indirection.ValueHolderInterface teams= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.String userEmailAddress;

	private java.sql.Timestamp userExpDate;

	private java.lang.String userId;

	private oracle.toplink.indirection.ValueHolderInterface worklists= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	public  User()
	{
		// Fill in method body here.
	}

	public java.lang.Character getActiveStatus()
	{
		return activeStatus;
	}

	public com.profitera.descriptor.db.client.Agency getAgency()
	{
		return (com.profitera.descriptor.db.client.Agency) agency.getValue();
	}

	public java.util.Vector getAuditLog()
	{
		return (java.util.Vector) auditLog.getValue();
	}

	public java.util.Vector getBatches()
	{
		return (java.util.Vector) batches.getValue();
	}

	public java.sql.Timestamp getCreateDate()
	{
		return createDate;
	}

	public com.profitera.descriptor.db.user.Employee getEmployee()
	{
		return (com.profitera.descriptor.db.user.Employee) employee.getValue();
	}

	public java.lang.Double getIsCollector()
	{
		return isCollector;
	}

	public java.lang.Character getLogonStatus()
	{
		return logonStatus;
	}

	public java.sql.Timestamp getPasswdExpDate()
	{
		return passwdExpDate;
	}

	public java.lang.String getPassword()
	{
		return password;
	}

	public java.util.Vector getPasswordHistory()
	{
		return (java.util.Vector) passwordHistory.getValue();
	}

	public java.util.Vector getRoles()
	{
		return (java.util.Vector) roles.getValue();
	}

	public java.util.Vector getTeams()
	{
		return (java.util.Vector) teams.getValue();
	}

	public java.lang.String getUserEmailAddress()
	{
		return userEmailAddress;
	}

	public java.sql.Timestamp getUserExpDate()
	{
		return userExpDate;
	}

	public java.lang.String getUserId()
	{
		return userId;
	}

	public java.util.Vector getWorklists()
	{
		return (java.util.Vector) worklists.getValue();
	}

	public void setActiveStatus(java.lang.Character activeStatus)
	{
		this.activeStatus = activeStatus;
	}

	public void setAgency(com.profitera.descriptor.db.client.Agency agency)
	{
		this.agency.setValue(agency);
	}

	public void setAuditLog(java.util.Vector auditLog)
	{
		this.auditLog.setValue(auditLog);
	}

	public void setBatches(java.util.Vector batches)
	{
		this.batches.setValue(batches);
	}

	public void setCreateDate(java.sql.Timestamp createDate)
	{
		this.createDate = createDate;
	}

	public void setEmployee(com.profitera.descriptor.db.user.Employee employee)
	{
		this.employee.setValue(employee);
	}

	public void setIsCollector(java.lang.Double isCollector)
	{
		this.isCollector = isCollector;
	}

	public void setLogonStatus(java.lang.Character logonStatus)
	{
		this.logonStatus = logonStatus;
	}

	public void setPasswdExpDate(java.sql.Timestamp passwdExpDate)
	{
		this.passwdExpDate = passwdExpDate;
	}

	public void setPassword(java.lang.String password)
	{
		this.password = password;
	}

	public void setPasswordHistory(java.util.Vector passwordHistory)
	{
		this.passwordHistory.setValue(passwordHistory);
	}

	public void setRoles(java.util.Vector roles)
	{
		this.roles.setValue(roles);
	}

	public void setTeams(java.util.Vector teams)
	{
		this.teams.setValue(teams);
	}

	public void setUserEmailAddress(java.lang.String userEmailAddress)
	{
		this.userEmailAddress = userEmailAddress;
	}

	public void setUserExpDate(java.sql.Timestamp userExpDate)
	{
		this.userExpDate = userExpDate;
	}

	public void setUserId(java.lang.String userId)
	{
		this.userId = userId;
	}

	public void setWorklists(java.util.Vector worklists)
	{
		this.worklists.setValue(worklists);
	}
}
