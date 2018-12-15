// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.user;

public class UserTeams
	implements java.io.Serializable
{
	// Generated constants
	public static final String AUTO_ASSIGN = "autoAssign";
	public static final String BUSINESS_UNIT = "businessUnit";
	public static final String DEPARTMENT = "department";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	public static final String TEAM_CAPACITY = "teamCapacity";
	public static final String TEAM_CREATE_BY = "teamCreateBy";
	public static final String TEAM_CREATE_DATE = "teamCreateDate";
	public static final String TEAM_DESC = "teamDesc";
	public static final String TEAM_ID = "teamId";
	public static final String TEAM_LEADER = "teamLeader";
	public static final String TREATMENT_STAGE_REF = "treatmentStageRef";
	public static final String USERS = "users";
	// End of generated constants
	
	private java.lang.Boolean autoAssign;

	private oracle.toplink.indirection.ValueHolderInterface businessUnit= new oracle.toplink.indirection.ValueHolder();

	private java.lang.String department;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	private java.lang.Double teamCapacity;

	private java.lang.String teamCreateBy;

	private java.sql.Timestamp teamCreateDate;

	private java.lang.String teamDesc;

	private java.lang.String teamId;

	private oracle.toplink.indirection.ValueHolderInterface teamLeader= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface treatmentStageRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface users= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	public  UserTeams()
	{
		// Fill in method body here.
	}

	public java.lang.Boolean getAutoAssign()
	{
		return autoAssign;
	}

	public com.profitera.descriptor.db.user.BusinessUnit getBusinessUnit()
	{
		return (com.profitera.descriptor.db.user.BusinessUnit) businessUnit.getValue();
	}

	public java.lang.String getDepartment()
	{
		return department;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public java.lang.Double getTeamCapacity()
	{
		return teamCapacity;
	}

	public java.lang.String getTeamCreateBy()
	{
		return teamCreateBy;
	}

	public java.sql.Timestamp getTeamCreateDate()
	{
		return teamCreateDate;
	}

	public java.lang.String getTeamDesc()
	{
		return teamDesc;
	}

	public java.lang.String getTeamId()
	{
		return teamId;
	}

	public com.profitera.descriptor.db.user.User getTeamLeader()
	{
		return (com.profitera.descriptor.db.user.User) teamLeader.getValue();
	}

	public com.profitera.descriptor.db.reference.TreatmentStageRef getTreatmentStageRef()
	{
		return (com.profitera.descriptor.db.reference.TreatmentStageRef) treatmentStageRef.getValue();
	}

	public java.util.Vector getUsers()
	{
		return (java.util.Vector) users.getValue();
	}

	public void setAutoAssign(java.lang.Boolean autoAssign)
	{
		this.autoAssign = autoAssign;
	}

	public void setBusinessUnit(com.profitera.descriptor.db.user.BusinessUnit businessUnit)
	{
		this.businessUnit.setValue(businessUnit);
	}

	public void setDepartment(java.lang.String department)
	{
		this.department = department;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}

	public void setTeamCapacity(java.lang.Double teamCapacity)
	{
		this.teamCapacity = teamCapacity;
	}

	public void setTeamCreateBy(java.lang.String teamCreateBy)
	{
		this.teamCreateBy = teamCreateBy;
	}

	public void setTeamCreateDate(java.sql.Timestamp teamCreateDate)
	{
		this.teamCreateDate = teamCreateDate;
	}

	public void setTeamDesc(java.lang.String teamDesc)
	{
		this.teamDesc = teamDesc;
	}

	public void setTeamId(java.lang.String teamId)
	{
		this.teamId = teamId;
	}

	public void setTeamLeader(com.profitera.descriptor.db.user.User teamLeader)
	{
		this.teamLeader.setValue(teamLeader);
	}

	public void setTreatmentStageRef(com.profitera.descriptor.db.reference.TreatmentStageRef treatmentStageRef)
	{
		this.treatmentStageRef.setValue(treatmentStageRef);
	}

	public void setUsers(java.util.Vector users)
	{
		this.users.setValue(users);
	}
}
