// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.rule;

public class RuleBom
	implements java.io.Serializable
{
	// Generated constants
	public static final String AGENT_CODE = "agentCode";
	public static final String BOM_ID = "bomId";
	public static final String BOM_NAME = "bomName";
	public static final String CONTENT = "content";
	public static final String CREATED_BY = "createdBy";
	public static final String CREATED_DATE = "createdDate";
	public static final String DESCRIPTION = "description";
	public static final String RULES = "rules";
	public static final String UPDATED_BY = "updatedBy";
	public static final String UPDATED_DATE = "updatedDate";
	// End of generated constants
	
	private java.lang.String agentCode;

	private java.lang.Long bomId;

	private java.lang.String bomName;

	private java.lang.String content;

	private java.lang.String createdBy;

	private java.sql.Timestamp createdDate;

	private java.lang.String description;

	private oracle.toplink.indirection.ValueHolderInterface rules= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.String updatedBy;

	private java.sql.Timestamp updatedDate;

	public  RuleBom()
	{
		// Fill in method body here.
	}

	public java.lang.String getAgentCode()
	{
		return agentCode;
	}

	public java.lang.Long getBomId()
	{
		return bomId;
	}

	public java.lang.String getBomName()
	{
		return bomName;
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

	public java.lang.String getDescription()
	{
		return description;
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

	public void setAgentCode(java.lang.String agentCode)
	{
		this.agentCode = agentCode;
	}

	public void setBomId(java.lang.Long bomId)
	{
		this.bomId = bomId;
	}

	public void setBomName(java.lang.String bomName)
	{
		this.bomName = bomName;
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

	public void setDescription(java.lang.String description)
	{
		this.description = description;
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


	public String toString(){
		return bomId.toString();
	}

}
