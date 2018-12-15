// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.treatment;

public class Template
	implements java.io.Serializable
{
	// Generated constants
	public static final String CONTENT = "content";
	public static final String DESCRIPTION = "description";
	public static final String TEMPLATE_ID = "templateId";
	public static final String TEMPLATE_NAME = "templateName";
	public static final String TYPE_REF = "typeRef";
  public static final String DISABLE = "disable";
	// End of generated constants
	
	private java.lang.String content;

	private java.lang.String description;

	private java.lang.Double templateId;

	private java.lang.String templateName;

	private oracle.toplink.indirection.ValueHolderInterface typeRef= new oracle.toplink.indirection.ValueHolder();
  
  private java.lang.Boolean disable;

	public  Template()
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

	public java.lang.Double getTemplateId()
	{
		return templateId;
	}

	public java.lang.String getTemplateName()
	{
		return templateName;
	}

	public com.profitera.descriptor.db.reference.TemplateTypeRef getTypeRef()
	{
		return (com.profitera.descriptor.db.reference.TemplateTypeRef) typeRef.getValue();
	}

	public void setContent(java.lang.String content)
	{
		this.content = content;
	}

	public void setDescription(java.lang.String description)
	{
		this.description = description;
	}

	public void setTemplateId(java.lang.Double templateId)
	{
		this.templateId = templateId;
	}

	public void setTemplateName(java.lang.String templateName)
	{
		this.templateName = templateName;
	}

	public void setTypeRef(com.profitera.descriptor.db.reference.TemplateTypeRef typeRef)
	{
		this.typeRef.setValue(typeRef);
	}

  public java.lang.Boolean getDisable() {
    return disable;
  }

  public void setDisable(java.lang.Boolean disable) {
    this.disable = disable;
  }
}
