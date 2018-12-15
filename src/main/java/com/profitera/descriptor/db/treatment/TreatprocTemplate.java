// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.treatment;

public class TreatprocTemplate
	implements java.io.Serializable
{
	// Generated constants
	public static final String COST = "cost";
	public static final String DAYS_DURATION = "daysDuration";
	public static final String DOCUMENT_TEMPLATE = "documentTemplate";
	public static final String LEAD_TIME_HOURS = "leadTimeHours";
	public static final String NOTIFIER_CODE_REF = "notifierCodeRef";
	public static final String NOTIFIER_PROCESS = "notifierProcess";
	public static final String TREATPROC_SUBTYPE_ID = "treatprocSubtypeId";
	public static final String TREATPROC_TEMPLATE_ID = "treatprocTemplateId";
	public static final String TREATPROC_TYPE_ID = "treatprocTypeId";
	public static final String UPDATE_HOST = "updateHost";
	// End of generated constants
	
	private java.lang.Double cost;

	private java.lang.Integer daysDuration;

	private oracle.toplink.indirection.ValueHolderInterface documentTemplate= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Integer leadTimeHours;

	private oracle.toplink.indirection.ValueHolderInterface notifierCodeRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Boolean notifierProcess;

	private java.lang.Double treatprocSubtypeId;

	private java.lang.Double treatprocTemplateId;

	private java.lang.Double treatprocTypeId;

	private java.lang.Boolean updateHost;

	public  TreatprocTemplate()
	{
		// Fill in method body here.
	}

	public java.lang.Double getCost()
	{
		return cost;
	}

	public java.lang.Integer getDaysDuration()
	{
		return daysDuration;
	}

	public com.profitera.descriptor.db.treatment.Template getDocumentTemplate()
	{
		return (com.profitera.descriptor.db.treatment.Template) documentTemplate.getValue();
	}

	public java.lang.Integer getLeadTimeHours()
	{
		return leadTimeHours;
	}

	public com.profitera.descriptor.db.reference.NotifierCodeRef getNotifierCodeRef()
	{
		return (com.profitera.descriptor.db.reference.NotifierCodeRef) notifierCodeRef.getValue();
	}

	public java.lang.Boolean getNotifierProcess()
	{
		return notifierProcess;
	}

	public java.lang.Double getTreatprocSubtypeId()
	{
		return treatprocSubtypeId;
	}

	public java.lang.Double getTreatprocTemplateId()
	{
		return treatprocTemplateId;
	}

	public java.lang.Double getTreatprocTypeId()
	{
		return treatprocTypeId;
	}

	public java.lang.Boolean getUpdateHost()
	{
		return updateHost;
	}

	public void setCost(java.lang.Double cost)
	{
		this.cost = cost;
	}

	public void setDaysDuration(java.lang.Integer daysDuration)
	{
		this.daysDuration = daysDuration;
	}

	public void setDocumentTemplate(com.profitera.descriptor.db.treatment.Template documentTemplate)
	{
		this.documentTemplate.setValue(documentTemplate);
	}

	public void setLeadTimeHours(java.lang.Integer leadTimeHours)
	{
		this.leadTimeHours = leadTimeHours;
	}

	public void setNotifierCodeRef(com.profitera.descriptor.db.reference.NotifierCodeRef notifierCodeRef)
	{
		this.notifierCodeRef.setValue(notifierCodeRef);
	}

	public void setNotifierProcess(java.lang.Boolean notifierProcess)
	{
		this.notifierProcess = notifierProcess;
	}

	public void setTreatprocSubtypeId(java.lang.Double treatprocSubtypeId)
	{
		this.treatprocSubtypeId = treatprocSubtypeId;
	}

	public void setTreatprocTemplateId(java.lang.Double treatprocTemplateId)
	{
		this.treatprocTemplateId = treatprocTemplateId;
	}

	public void setTreatprocTypeId(java.lang.Double treatprocTypeId)
	{
		this.treatprocTypeId = treatprocTypeId;
	}

	public void setUpdateHost(java.lang.Boolean updateHost)
	{
		this.updateHost = updateHost;
	}
}
