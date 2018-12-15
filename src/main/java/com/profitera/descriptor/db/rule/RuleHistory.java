// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.rule;

public class RuleHistory
	implements java.io.Serializable
{
	// Generated constants
	public static final String CONTENT = "content";
	public static final String DESCRIPTION = "description";
	public static final String PARENT_RULE = "parentRule";
	public static final String REMARKS = "remarks";
	public static final String RULE_NAME = "ruleName";
	public static final String USER_ID = "userId";
	public static final String VERSION_DATE_TIME = "versionDateTime";
	// End of generated constants
	
	private java.lang.String content;

	private java.lang.String description;

	private oracle.toplink.indirection.ValueHolderInterface parentRule= new oracle.toplink.indirection.ValueHolder();

	private java.lang.String remarks;

	private java.lang.String ruleName;

	private java.lang.String userId;

	private java.sql.Timestamp versionDateTime;

	public  RuleHistory()
	{
		// Fill in method body here.
	}

	public java.lang.String getContent()
	{
		return content;
	}

	public java.lang.String getDescription()
	{
		return description;
	}

	public com.profitera.descriptor.db.rule.Rule getParentRule()
	{
		return (com.profitera.descriptor.db.rule.Rule) parentRule.getValue();
	}

	public java.lang.String getRemarks()
	{
		return remarks;
	}

	public java.lang.String getRuleName()
	{
		return ruleName;
	}

	public java.lang.String getUserId()
	{
		return userId;
	}

	public java.sql.Timestamp getVersionDateTime()
	{
		return versionDateTime;
	}

	public void setContent(java.lang.String content)
	{
		this.content = content;
	}

	public void setDescription(java.lang.String description)
	{
		this.description = description;
	}

	public void setParentRule(com.profitera.descriptor.db.rule.Rule parentRule)
	{
		this.parentRule.setValue(parentRule);
	}

	public void setRemarks(java.lang.String remarks)
	{
		this.remarks = remarks;
	}

	public void setRuleName(java.lang.String ruleName)
	{
		this.ruleName = ruleName;
	}

	public void setUserId(java.lang.String userId)
	{
		this.userId = userId;
	}

	public void setVersionDateTime(java.sql.Timestamp versionDateTime)
	{
		this.versionDateTime = versionDateTime;
	}
}
