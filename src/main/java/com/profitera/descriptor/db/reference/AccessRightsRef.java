// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class AccessRightsRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String ACCESS_RIGHTS_DESC = "accessRightsDesc";
	public static final String ACCESS_RIGHTS_ID = "accessRightsId";
	public static final String DISABLE = "disable";
	public static final String PARENT_ACCESS_RIGHTS_ID = "parentAccessRightsId";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String accessRightsDesc;

	private java.lang.Double accessRightsId;

	private java.lang.Double disable;

	private java.lang.Double parentAccessRightsId;

	private java.lang.Double sortPriority;

	public  AccessRightsRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getAccessRightsDesc()
	{
		return accessRightsDesc;
	}

	public java.lang.Double getAccessRightsId()
	{
		return accessRightsId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getParentAccessRightsId()
	{
		return parentAccessRightsId;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setAccessRightsDesc(java.lang.String accessRightsDesc)
	{
		this.accessRightsDesc = accessRightsDesc;
	}

	public void setAccessRightsId(java.lang.Double accessRightsId)
	{
		this.accessRightsId = accessRightsId;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setParentAccessRightsId(java.lang.Double parentAccessRightsId)
	{
		this.parentAccessRightsId = parentAccessRightsId;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
