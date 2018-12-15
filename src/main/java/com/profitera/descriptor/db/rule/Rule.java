// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.rule;

public class Rule
	implements java.io.Serializable
{
	// Generated constants
	public static final String CONTENT = "content";
	public static final String CREATED_BY = "createdBy";
	public static final String CREATED_DATE = "createdDate";
	public static final String DELETED = "deleted";
	public static final String DEPLOY_DATE = "deployDate";
	public static final String DESCRIPTION = "description";
	public static final String HISTORY = "history";
	public static final String RULE_BOM = "ruleBom";
	public static final String RULE_GROUP = "ruleGroup";
	public static final String RULE_ID = "ruleId";
	public static final String RULE_NAME = "ruleName";
	public static final String STATUS = "status";
	public static final String UPDATED_BY = "updatedBy";
	public static final String UPDATED_DATE = "updatedDate";
	// End of generated constants
	
	private java.lang.String content;

	private java.lang.String createdBy;

	private java.sql.Timestamp createdDate;

	private java.lang.Boolean deleted;

	private java.sql.Timestamp deployDate;

	private java.lang.String description;

	private oracle.toplink.indirection.ValueHolderInterface history= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private oracle.toplink.indirection.ValueHolderInterface ruleBom= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface ruleGroup= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Long ruleId;

	private java.lang.String ruleName;

	private java.lang.String status;

	private java.lang.String updatedBy;

	private java.sql.Timestamp updatedDate;

	public  Rule()
	{
		// Fill in method body here.
	}

	public java.lang.String getContent()
	{
		return content;
	}

	public java.lang.String getCreatedBy()
	{
		return createdBy;
	}

	public java.sql.Timestamp getCreatedDate()
	{
		return createdDate;
	}

	public java.lang.Boolean getDeleted()
	{
		return deleted;
	}

	public java.sql.Timestamp getDeployDate()
	{
		return deployDate;
	}

	public java.lang.String getDescription()
	{
		return description;
	}

	public java.util.Vector getHistory()
	{
		return (java.util.Vector) history.getValue();
	}

	public com.profitera.descriptor.db.rule.RuleBom getRuleBom()
	{
		return (com.profitera.descriptor.db.rule.RuleBom) ruleBom.getValue();
	}

	public com.profitera.descriptor.db.rule.RuleGroup getRuleGroup()
	{
		return (com.profitera.descriptor.db.rule.RuleGroup) ruleGroup.getValue();
	}

	public java.lang.Long getRuleId()
	{
		return ruleId;
	}

	public java.lang.String getRuleName()
	{
		return ruleName;
	}

	public java.lang.String getStatus()
	{
		return status;
	}

	public java.lang.String getUpdatedBy()
	{
		return updatedBy;
	}

	public java.sql.Timestamp getUpdatedDate()
	{
		return updatedDate;
	}

	public void setContent(java.lang.String content)
	{
		this.content = content;
	}

	public void setCreatedBy(java.lang.String createdBy)
	{
		this.createdBy = createdBy;
	}

	public void setCreatedDate(java.sql.Timestamp createdDate)
	{
		this.createdDate = createdDate;
	}

	public void setDeleted(java.lang.Boolean deleted)
	{
		this.deleted = deleted;
	}

	public void setDeployDate(java.sql.Timestamp deployDate)
	{
		this.deployDate = deployDate;
	}

	public void setDescription(java.lang.String description)
	{
		this.description = description;
	}

	public void setHistory(java.util.Vector history)
	{
		this.history.setValue(history);
	}

	public void setRuleBom(com.profitera.descriptor.db.rule.RuleBom ruleBom)
	{
		this.ruleBom.setValue(ruleBom);
	}

	public void setRuleGroup(com.profitera.descriptor.db.rule.RuleGroup ruleGroup)
	{
		this.ruleGroup.setValue(ruleGroup);
	}

	public void setRuleId(java.lang.Long ruleId)
	{
		this.ruleId = ruleId;
	}

	public void setRuleName(java.lang.String ruleName)
	{
		this.ruleName = ruleName;
	}

	public void setStatus(java.lang.String status)
	{
		this.status = status;
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
