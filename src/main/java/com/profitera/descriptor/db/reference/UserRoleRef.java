// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class UserRoleRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String ACCESS_RIGHTS = "accessRights";
	public static final String DISABLE = "disable";
	public static final String PROFILE = "profile";
	public static final String ROLE_CREATED_BY = "roleCreatedBy";
	public static final String ROLE_CREATED_DATE = "roleCreatedDate";
	public static final String ROLE_DESC = "roleDesc";
	public static final String ROLE_EXPIRY_DATE = "roleExpiryDate";
	public static final String ROLE_ID = "roleId";
	public static final String ROLE_NAME = "roleName";
	public static final String SORT_PRIORITY = "sortPriority";
	public static final String USERS = "users";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface accessRights= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.Double disable;

	private java.lang.String profile;

	private java.lang.String roleCreatedBy;

	private java.sql.Timestamp roleCreatedDate;

	private java.lang.String roleDesc;

	private java.sql.Timestamp roleExpiryDate;

	private java.lang.Double roleId;

	private java.lang.String roleName;

	private java.lang.Double sortPriority;

	private oracle.toplink.indirection.ValueHolderInterface users= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	public  UserRoleRef()
	{
		// Fill in method body here.
	}

	public java.util.Vector getAccessRights()
	{
		return (java.util.Vector) accessRights.getValue();
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.String getProfile()
	{
		return profile;
	}

	public java.lang.String getRoleCreatedBy()
	{
		return roleCreatedBy;
	}

	public java.sql.Timestamp getRoleCreatedDate()
	{
		return roleCreatedDate;
	}

	public java.lang.String getRoleDesc()
	{
		return roleDesc;
	}

	public java.sql.Timestamp getRoleExpiryDate()
	{
		return roleExpiryDate;
	}

	public java.lang.Double getRoleId()
	{
		return roleId;
	}

	public java.lang.String getRoleName()
	{
		return roleName;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public java.util.Vector getUsers()
	{
		return (java.util.Vector) users.getValue();
	}

	public void setAccessRights(java.util.Vector accessRights)
	{
		this.accessRights.setValue(accessRights);
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setProfile(java.lang.String profile)
	{
		this.profile = profile;
	}

	public void setRoleCreatedBy(java.lang.String roleCreatedBy)
	{
		this.roleCreatedBy = roleCreatedBy;
	}

	public void setRoleCreatedDate(java.sql.Timestamp roleCreatedDate)
	{
		this.roleCreatedDate = roleCreatedDate;
	}

	public void setRoleDesc(java.lang.String roleDesc)
	{
		this.roleDesc = roleDesc;
	}

	public void setRoleExpiryDate(java.sql.Timestamp roleExpiryDate)
	{
		this.roleExpiryDate = roleExpiryDate;
	}

	public void setRoleId(java.lang.Double roleId)
	{
		this.roleId = roleId;
	}

	public void setRoleName(java.lang.String roleName)
	{
		this.roleName = roleName;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}

	public void setUsers(java.util.Vector users)
	{
		this.users.setValue(users);
	}
}
