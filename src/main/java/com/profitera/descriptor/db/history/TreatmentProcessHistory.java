package com.profitera.descriptor.db.history;

public class TreatmentProcessHistory
	implements java.io.Serializable
{
	// Generated constants
	public static final String ACTUAL_END_DATE = "actualEndDate";
	public static final String ACTUAL_START_DATE = "actualStartDate";
  public static final String AGENCY = "agency";
	public static final String ATTEMPT_NUMBER = "attemptNumber";
	public static final String COST_BILLER_REF = "costBillerRef";
	public static final String EXPECTED_END_DATE = "expectedEndDate";
	public static final String EXPECTED_START_DATE = "expectedStartDate";
	public static final String HOST_UPDATED = "hostUpdated";
	public static final String MANUAL = "manual";
	public static final String OUTSTANDING_AMT = "outstandingAmt";
	public static final String TREATMENT_PROCESS = "treatmentProcess";
	public static final String PROCESS_COST = "processCost";
	public static final String PROCESS_REMARKS = "processRemarks";
	public static final String PROCESS_STATUS_REF = "processStatusRef";
	public static final String PROCESS_TYPE_ID_REF = "processTypeIdRef";
	public static final String PROFILE_SEGMENT_REF = "profileSegmentRef";
	public static final String TEMPLATE = "template";
	public static final String TREAT_PROCESS_TYPE_STATUS_REF = "treatProcessTypeStatusRef";
	public static final String ID = "id";
	public static final String TREATMENT_STAGE_REF = "treatmentStageRef";
	public static final String TREATMENT_STREAM_REF = "treatmentStreamRef";
	public static final String TREATPROC_SUBTYPE_REF = "treatprocSubtypeRef";
	public static final String USER = "user";
	public static final String CREATED_USER = "createdUser";
	public static final String CREATED_DATE = "createdDate";
	public static final String UPDATE_DATE = "updateDate";

  // End of generated constants
	
	private java.sql.Timestamp actualEndDate;

	private java.sql.Timestamp actualStartDate;
  
  private oracle.toplink.indirection.ValueHolderInterface agency= new oracle.toplink.indirection.ValueHolder();
  
  private java.sql.Timestamp updateDate;

	private java.lang.Integer attemptNumber;

	private oracle.toplink.indirection.ValueHolderInterface costBillerRef= new oracle.toplink.indirection.ValueHolder();

	private java.sql.Timestamp expectedEndDate;

	private java.sql.Timestamp expectedStartDate;

	private java.lang.Boolean hostUpdated;

	private java.lang.Boolean manual;

	private java.lang.Double outstandingAmt;

	private oracle.toplink.indirection.ValueHolderInterface treatmentProcess = new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double processCost;

	private oracle.toplink.indirection.ValueHolderInterface processRemarks = new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface processStatusRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface processTypeIdRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface profileSegmentRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface template= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface treatProcessTypeStatusRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double id;

	private oracle.toplink.indirection.ValueHolderInterface treatmentStageRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface treatmentStreamRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface treatprocSubtypeRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface user= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface createdUser= new oracle.toplink.indirection.ValueHolder();
	
	private java.sql.Timestamp createdDate;

	public  TreatmentProcessHistory()
	{
		// Fill in method body here.
	}

	public java.sql.Timestamp getActualEndDate()
	{
		return actualEndDate;
	}

	public java.sql.Timestamp getActualStartDate()
	{
		return actualStartDate;
	}
  
  public com.profitera.descriptor.db.client.Agency getAgency()
  {
    return (com.profitera.descriptor.db.client.Agency) agency.getValue();
  }

	public java.lang.Integer getAttemptNumber()
	{
		return attemptNumber;
	}

	public com.profitera.descriptor.db.reference.CostBillerRef getCostBillerRef()
	{
		return (com.profitera.descriptor.db.reference.CostBillerRef) costBillerRef.getValue();
	}

	public java.sql.Timestamp getExpectedEndDate()
	{
		return expectedEndDate;
	}

	public java.sql.Timestamp getExpectedStartDate()
	{
		return expectedStartDate;
	}

	public java.lang.Boolean getHostUpdated()
	{
		return hostUpdated;
	}

	public java.lang.Boolean getManual()
	{
		return manual;
	}

	public java.lang.Double getOutstandingAmt()
	{
		return outstandingAmt;
	}

	public com.profitera.descriptor.db.treatment.TreatmentProcess getParentTreatmentPlan()
	{
		return (com.profitera.descriptor.db.treatment.TreatmentProcess) treatmentProcess.getValue();
	}

	public java.lang.Double getProcessCost()
	{
		return processCost;
	}

	public com.profitera.descriptor.db.history.TreatmentProcessHistoryRemark getProcessRemarks()
	{
		return (TreatmentProcessHistoryRemark)processRemarks.getValue();
	}

	public com.profitera.descriptor.db.reference.TreatmentProcessStatusRef getProcessStatusRef()
	{
		return (com.profitera.descriptor.db.reference.TreatmentProcessStatusRef) processStatusRef.getValue();
	}

	public com.profitera.descriptor.db.reference.TreatmentProcessTypeRef getProcessTypeIdRef()
	{
		return (com.profitera.descriptor.db.reference.TreatmentProcessTypeRef) processTypeIdRef.getValue();
	}

	public com.profitera.descriptor.db.reference.ProfileSegmentRef getProfileSegmentRef()
	{
		return (com.profitera.descriptor.db.reference.ProfileSegmentRef) profileSegmentRef.getValue();
	}

	public com.profitera.descriptor.db.treatment.Template getTemplate()
	{
		return (com.profitera.descriptor.db.treatment.Template) template.getValue();
	}

	public com.profitera.descriptor.db.reference.TreatProcessTypeStatusRef getTreatProcessTypeStatusRef()
	{
		return (com.profitera.descriptor.db.reference.TreatProcessTypeStatusRef) treatProcessTypeStatusRef.getValue();
	}

	public java.lang.Double getId()
	{
		return id;
	}

	public com.profitera.descriptor.db.reference.TreatmentStageRef getTreatmentStageRef()
	{
		return (com.profitera.descriptor.db.reference.TreatmentStageRef) treatmentStageRef.getValue();
	}

	public com.profitera.descriptor.db.reference.TreatmentStreamRef getTreatmentStreamRef()
	{
		return (com.profitera.descriptor.db.reference.TreatmentStreamRef) treatmentStreamRef.getValue();
	}

	public com.profitera.descriptor.db.reference.TreatprocSubtypeRef getTreatprocSubtypeRef()
	{
		return (com.profitera.descriptor.db.reference.TreatprocSubtypeRef) treatprocSubtypeRef.getValue();
	}

	public com.profitera.descriptor.db.user.User getUser()
	{
		return (com.profitera.descriptor.db.user.User) user.getValue();
	}

	public com.profitera.descriptor.db.user.User getCreatedUser()
	{
		return (com.profitera.descriptor.db.user.User) createdUser.getValue();
	}

	public void setActualEndDate(java.sql.Timestamp actualEndDate)
	{
		this.actualEndDate = actualEndDate;
	}

	public void setActualStartDate(java.sql.Timestamp actualStartDate)
	{
		this.actualStartDate = actualStartDate;
	}
  
  public void setAgency(com.profitera.descriptor.db.client.Agency agency)
  {
    this.agency.setValue(agency);
  }


	public void setAttemptNumber(java.lang.Integer attemptNumber)
	{
		this.attemptNumber = attemptNumber;
	}

	public void setCostBillerRef(com.profitera.descriptor.db.reference.CostBillerRef costBillerRef)
	{
		this.costBillerRef.setValue(costBillerRef);
	}

	public void setExpectedEndDate(java.sql.Timestamp expectedEndDate)
	{
		this.expectedEndDate = expectedEndDate;
	}

	public void setExpectedStartDate(java.sql.Timestamp expectedStartDate)
	{
		this.expectedStartDate = expectedStartDate;
	}

	public void setHostUpdated(java.lang.Boolean hostUpdated)
	{
		this.hostUpdated = hostUpdated;
	}

	public void setManual(java.lang.Boolean manual)
	{
		this.manual = manual;
	}

	public void setOutstandingAmt(java.lang.Double outstandingAmt)
	{
		this.outstandingAmt = outstandingAmt;
	}

	public void setTreatmentProcess(com.profitera.descriptor.db.treatment.TreatmentProcess treatmentProcess)
	{
		this.treatmentProcess.setValue(treatmentProcess);
	}

	public void setProcessCost(java.lang.Double processCost)
	{
		this.processCost = processCost;
	}

	public void setProcessRemarks(com.profitera.descriptor.db.history.TreatmentProcessHistoryRemark processRemarks)
	{
		this.processRemarks.setValue(processRemarks);
	}

	public void setProcessStatusRef(com.profitera.descriptor.db.reference.TreatmentProcessStatusRef processStatusRef)
	{
		this.processStatusRef.setValue(processStatusRef);
	}

	public void setProcessTypeIdRef(com.profitera.descriptor.db.reference.TreatmentProcessTypeRef processTypeIdRef)
	{
		this.processTypeIdRef.setValue(processTypeIdRef);
	}

	public void setProfileSegmentRef(com.profitera.descriptor.db.reference.ProfileSegmentRef profileSegmentRef)
	{
		this.profileSegmentRef.setValue(profileSegmentRef);
	}

	public void setTemplate(com.profitera.descriptor.db.treatment.Template template)
	{
		this.template.setValue(template);
	}

	public void setTreatProcessTypeStatusRef(com.profitera.descriptor.db.reference.TreatProcessTypeStatusRef treatProcessTypeStatusRef)
	{
		this.treatProcessTypeStatusRef.setValue(treatProcessTypeStatusRef);
	}

	public void setId(java.lang.Double id)
	{
		this.id = id;
	}

	public void setTreatmentStageRef(com.profitera.descriptor.db.reference.TreatmentStageRef treatmentStageRef)
	{
		this.treatmentStageRef.setValue(treatmentStageRef);
	}

	public void setTreatmentStreamRef(com.profitera.descriptor.db.reference.TreatmentStreamRef treatmentStreamRef)
	{
		this.treatmentStreamRef.setValue(treatmentStreamRef);
	}

	public void setTreatprocSubtypeRef(com.profitera.descriptor.db.reference.TreatprocSubtypeRef treatprocSubtypeRef)
	{
		this.treatprocSubtypeRef.setValue(treatprocSubtypeRef);
	}

	public void setUser(com.profitera.descriptor.db.user.User user)
	{
		this.user.setValue(user);
	}

    public java.sql.Timestamp getCreatedDate() {
      return createdDate;
    }

    public void setCreatedDate(java.sql.Timestamp createdDate) {
      this.createdDate = createdDate;
    }

  public void setCreatedUser(com.profitera.descriptor.db.user.User createdUser)
	{
		this.createdUser.setValue(createdUser);
	}

  public java.sql.Timestamp getUpdateDate() {
    return updateDate;
  }

  public void setUpdateDate(java.sql.Timestamp updateDate) {
    this.updateDate = updateDate;
  }
}