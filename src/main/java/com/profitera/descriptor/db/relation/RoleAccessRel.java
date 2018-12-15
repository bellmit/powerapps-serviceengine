// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.relation;

public class RoleAccessRel
	implements java.io.Serializable
{
	// Generated constants
	public static final String ACCESS_RIGHTS_REF = "accessRightsRef";
	public static final String USER_ROLE_REF = "userRoleRef";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface accessRightsRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface userRoleRef= new oracle.toplink.indirection.ValueHolder();

	public  RoleAccessRel()
	{
		// Fill in method body here.
	}

	public com.profitera.descriptor.db.reference.AccessRightsRef getAccessRightsRef()
	{
		return (com.profitera.descriptor.db.reference.AccessRightsRef) accessRightsRef.getValue();
	}

	public com.profitera.descriptor.db.reference.UserRoleRef getUserRoleRef()
	{
		return (com.profitera.descriptor.db.reference.UserRoleRef) userRoleRef.getValue();
	}

	public void setAccessRightsRef(com.profitera.descriptor.db.reference.AccessRightsRef accessRightsRef)
	{
		this.accessRightsRef.setValue(accessRightsRef);
	}

	public void setUserRoleRef(com.profitera.descriptor.db.reference.UserRoleRef userRoleRef)
	{
		this.userRoleRef.setValue(userRoleRef);
	}
}
