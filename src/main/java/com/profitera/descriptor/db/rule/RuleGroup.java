// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.rule;

public class RuleGroup
	implements java.io.Serializable
{
	// Generated constants
	public static final String CHILD_GROUPS = "childGroups";
	public static final String CREATED_BY = "createdBy";
	public static final String CREATED_DATE = "createdDate";
	public static final String DEPLOY_DATE = "deployDate";
	public static final String DESCRIPTION = "description";
	public static final String GROUP_ID = "groupId";
	public static final String GROUP_NAME = "groupName";
	public static final String PARENT_GROUP = "parentGroup";
	public static final String RULES = "rules";
	public static final String UPDATED_BY = "updatedBy";
	public static final String UPDATED_DATE = "updatedDate";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface childGroups= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.String createdBy;

	private java.sql.Timestamp createdDate;

	private java.sql.Timestamp deployDate;

	private java.lang.String description;

	private java.lang.Long groupId;

	private java.lang.String groupName;

	private oracle.toplink.indirection.ValueHolderInterface parentGroup= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface rules= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.String updatedBy;

	private java.sql.Timestamp updatedDate;

	public  RuleGroup()
	{
		// Fill in method body here.
	}

	public java.util.Vector getChildGroups()
	{
		return (java.util.Vector) childGroups.getValue();
	}

	public java.lang.String getCreatedBy()
	{
		return createdBy;
	}

	public java.sql.Timestamp getCreatedDate()
	{
		return createdDate;
	}

	public java.sql.Timestamp getDeployDate()
	{
		return deployDate;
	}

	public java.lang.String getDescription()
	{
		return description;
	}

	public java.lang.Long getGroupId()
	{
		return groupId;
	}

	public java.lang.String getGroupName()
	{
		return groupName;
	}

	public com.profitera.descriptor.db.rule.RuleGroup getParentGroup()
	{
		return (com.profitera.descriptor.db.rule.RuleGroup) parentGroup.getValue();
	}

	public java.util.Vector getRules()
	{
		return (java.util.Vector) rules.getValue();
	}

	public java.lang.String getUpdatedBy()
	{
		return updatedBy;
	}

	public java.sql.Timestamp getUpdatedDate()
	{
		return updatedDate;
	}

	public void setChildGroups(java.util.Vector childGroups)
	{
		this.childGroups.setValue(childGroups);
	}

	public void setCreatedBy(java.lang.String createdBy)
	{
		this.createdBy = createdBy;
	}

	public void setCreatedDate(java.sql.Timestamp createdDate)
	{
		this.createdDate = createdDate;
	}

	public void setDeployDate(java.sql.Timestamp deployDate)
	{
		this.deployDate = deployDate;
	}

	public void setDescription(java.lang.String description)
	{
		this.description = description;
	}

	public void setGroupId(java.lang.Long groupId)
	{
		this.groupId = groupId;
	}

	public void setGroupName(java.lang.String groupName)
	{
		this.groupName = groupName;
	}

	public void setParentGroup(com.profitera.descriptor.db.rule.RuleGroup parentGroup)
	{
		this.parentGroup.setValue(parentGroup);
	}

	public void setRules(java.util.Vector rules)
	{
		this.rules.setValue(rules);
	}

	public void setUpdatedBy(java.lang.String updatedBy)
	{
		this.updatedBy = updatedBy;
	}

	public void setUpdatedDate(java.sql.Timestamp updatedDate)
	{
		this.updatedDate = updatedDate;
	}
}
