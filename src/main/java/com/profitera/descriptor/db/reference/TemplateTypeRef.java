// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class TemplateTypeRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	public static final String TEMPLATE_TYPE_CODE = "templateTypeCode";
	public static final String TEMPLATE_TYPE_DESC = "templateTypeDesc";
	public static final String TEMPLATE_TYPE_ID = "templateTypeId";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	private java.lang.String templateTypeCode;

	private java.lang.String templateTypeDesc;

	private java.lang.Double templateTypeId;

	public  TemplateTypeRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public java.lang.String getTemplateTypeCode()
	{
		return templateTypeCode;
	}

	public java.lang.String getTemplateTypeDesc()
	{
		return templateTypeDesc;
	}

	public java.lang.Double getTemplateTypeId()
	{
		return templateTypeId;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}

	public void setTemplateTypeCode(java.lang.String templateTypeCode)
	{
		this.templateTypeCode = templateTypeCode;
	}

	public void setTemplateTypeDesc(java.lang.String templateTypeDesc)
	{
		this.templateTypeDesc = templateTypeDesc;
	}

	public void setTemplateTypeId(java.lang.Double templateTypeId)
	{
		this.templateTypeId = templateTypeId;
	}
	
	public String toString(){
		return getTemplateTypeId().toString();
	}
}
