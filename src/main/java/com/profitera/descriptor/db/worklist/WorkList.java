// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.worklist;

public class WorkList
	implements java.io.Serializable
{
	// Generated constants
	public static final String ACCOUNTS = "accounts";
	public static final String AGENCY_TYPE_REF = "agencyTypeRef";
	public static final String BLOCK_CODE = "blockCode";
	public static final String DISABLE = "disable";
	public static final String FOR_UNASSIGNED = "forUnassigned";
	public static final String SORT_PRIORITY = "sortPriority";
	public static final String TREATMENT_STAGE_REF = "treatmentStageRef";
	public static final String USER_WORK_LIST_ASSIGN = "userWorkListAssign";
	public static final String USERS = "users";
	public static final String WORK_LIST_CREATE_DATE = "workListCreateDate";
	public static final String WORK_LIST_CREATOR = "workListCreator";
	public static final String WORK_LIST_DESC = "workListDesc";
	public static final String WORK_LIST_EFFECTIVE_DATE = "workListEffectiveDate";
	public static final String WORK_LIST_EXPIRY_DATE = "workListExpiryDate";
	public static final String WORK_LIST_GEN_FREQUENCY_REF = "workListGenFrequencyRef";
	public static final String WORK_LIST_GENERATION_CONDITIONS = "workListGenerationConditions";
	public static final String WORK_LIST_ID = "workListId";
	public static final String WORK_LIST_NAME = "workListName";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface accounts= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private oracle.toplink.indirection.ValueHolderInterface agencyTypeRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface blockCode= new oracle.toplink.indirection.ValueHolder(new oracle.toplink.indirection.IndirectList());

	private java.lang.Double disable;

	private java.lang.Double forUnassigned;

	private java.lang.Double sortPriority;

	private oracle.toplink.indirection.ValueHolderInterface treatmentStageRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface userWorkListAssign= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private oracle.toplink.indirection.ValueHolderInterface users= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.sql.Timestamp workListCreateDate;

	private oracle.toplink.indirection.ValueHolderInterface workListCreator= new oracle.toplink.indirection.ValueHolder();

	private java.lang.String workListDesc;

	private java.sql.Timestamp workListEffectiveDate;

	private java.sql.Timestamp workListExpiryDate;

	private java.lang.Double workListId;

	private java.lang.String workListName;

	public  WorkList()
	{
		// Fill in method body here.
	}

	public java.util.Vector getAccounts()
	{
		return (java.util.Vector) accounts.getValue();
	}

	public com.profitera.descriptor.db.reference.AgencyTypeRef getAgencyTypeRef()
	{
		return (com.profitera.descriptor.db.reference.AgencyTypeRef) agencyTypeRef.getValue();
	}

	public java.util.Vector getBlockCode()
	{
		return (java.util.Vector) blockCode.getValue();
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getForUnassigned()
	{
		return forUnassigned;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public com.profitera.descriptor.db.reference.TreatmentStageRef getTreatmentStageRef()
	{
		return (com.profitera.descriptor.db.reference.TreatmentStageRef) treatmentStageRef.getValue();
	}

	public java.util.Vector getUserWorkListAssign()
	{
		return (java.util.Vector) userWorkListAssign.getValue();
	}

	public java.util.Vector getUsers()
	{
		return (java.util.Vector) users.getValue();
	}


	public java.sql.Timestamp getWorkListCreateDate()
	{
		return workListCreateDate;
	}

	public com.profitera.descriptor.db.user.User getWorkListCreator()
	{
		return (com.profitera.descriptor.db.user.User) workListCreator.getValue();
	}

	public java.lang.String getWorkListDesc()
	{
		return workListDesc;
	}

	public java.sql.Timestamp getWorkListEffectiveDate()
	{
		return workListEffectiveDate;
	}

	public java.sql.Timestamp getWorkListExpiryDate()
	{
		return workListExpiryDate;
	}

	public java.lang.Double getWorkListId()
	{
		return workListId;
	}

	public java.lang.String getWorkListName()
	{
		return workListName;
	}

	public void setAccounts(java.util.Vector accounts)
	{
		this.accounts.setValue(accounts);
	}

	public void setAgencyTypeRef(com.profitera.descriptor.db.reference.AgencyTypeRef agencyTypeRef)
	{
		this.agencyTypeRef.setValue(agencyTypeRef);
	}

	public void setBlockCode(java.util.Vector blockCode)
	{
		this.blockCode.setValue(blockCode);
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setForUnassigned(java.lang.Double forUnassigned)
	{
		this.forUnassigned = forUnassigned;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}

	public void setTreatmentStageRef(com.profitera.descriptor.db.reference.TreatmentStageRef treatmentStageRef)
	{
		this.treatmentStageRef.setValue(treatmentStageRef);
	}

	public void setUserWorkListAssign(java.util.Vector userWorkListAssign)
	{
		this.userWorkListAssign.setValue(userWorkListAssign);
	}

	public void setUsers(java.util.Vector users)
	{
		this.users.setValue(users);
	}

	public void setWorkListCreateDate(java.sql.Timestamp workListCreateDate)
	{
		this.workListCreateDate = workListCreateDate;
	}

	public void setWorkListCreator(com.profitera.descriptor.db.user.User workListCreator)
	{
		this.workListCreator.setValue(workListCreator);
	}

	public void setWorkListDesc(java.lang.String workListDesc)
	{
		this.workListDesc = workListDesc;
	}

	public void setWorkListEffectiveDate(java.sql.Timestamp workListEffectiveDate)
	{
		this.workListEffectiveDate = workListEffectiveDate;
	}

	public void setWorkListExpiryDate(java.sql.Timestamp workListExpiryDate)
	{
		this.workListExpiryDate = workListExpiryDate;
	}

	public void setWorkListId(java.lang.Double workListId)
	{
		this.workListId = workListId;
	}

	public void setWorkListName(java.lang.String workListName)
	{
		this.workListName = workListName;
	}
}
