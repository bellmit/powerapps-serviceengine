// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.user;

public class BusinessUnit
	implements java.io.Serializable
{
	// Generated constants
	public static final String ADDRESS_DETAILS = "addressDetails";
	public static final String BRANCH_CREATE_BY = "branchCreateBy";
	public static final String BRANCH_CREATE_DATE = "branchCreateDate";
	public static final String BRANCH_ID = "branchId";
	public static final String BRANCH_NAME = "branchName";
	public static final String BRANCH_TYPE = "branchType";
	public static final String CHILD_BUSINESS_UNIT = "childBusinessUnit";
	public static final String COST_CENTER = "costCenter";
	public static final String DISABLE = "disable";
	public static final String HEAD_USER = "headUser";
	public static final String IS_COLLECTION = "isCollection";
	public static final String PARENT_BRANCH_ID = "parentBranchId";
	public static final String PARENT_BUSINESS_UNIT = "parentBusinessUnit";
	public static final String SORT_PRIORITY = "sortPriority";
	public static final String TEAMS = "teams";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface addressDetails= new oracle.toplink.indirection.ValueHolder();

	private java.lang.String branchCreateBy;

	private java.sql.Timestamp branchCreateDate;

	private java.lang.String branchId;

	private java.lang.String branchName;

	private java.lang.String branchType;

	private oracle.toplink.indirection.ValueHolderInterface childBusinessUnit= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.String costCenter;

	private java.lang.Double disable;

	private oracle.toplink.indirection.ValueHolderInterface headUser= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double isCollection;

	private java.lang.String parentBranchId;

	private oracle.toplink.indirection.ValueHolderInterface parentBusinessUnit= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double sortPriority;

	private oracle.toplink.indirection.ValueHolderInterface teams= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	public  BusinessUnit()
	{
		// Fill in method body here.
	}

	public com.profitera.descriptor.db.contact.AddressDetails getAddressDetails()
	{
		return (com.profitera.descriptor.db.contact.AddressDetails) addressDetails.getValue();
	}

	public java.lang.String getBranchCreateBy()
	{
		return branchCreateBy;
	}

	public java.sql.Timestamp getBranchCreateDate()
	{
		return branchCreateDate;
	}

	public java.lang.String getBranchId()
	{
		return branchId;
	}

	public java.lang.String getBranchName()
	{
		return branchName;
	}

	public java.lang.String getBranchType()
	{
		return branchType;
	}

	public java.util.Vector getChildBusinessUnit()
	{
		return (java.util.Vector) childBusinessUnit.getValue();
	}

	public java.lang.String getCostCenter()
	{
		return costCenter;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public com.profitera.descriptor.db.user.User getHeadUser()
	{
		return (com.profitera.descriptor.db.user.User) headUser.getValue();
	}

	public java.lang.Double getIsCollection()
	{
		return isCollection;
	}

	public java.lang.String getParentBranchId()
	{
		return parentBranchId;
	}

	public com.profitera.descriptor.db.user.BusinessUnit getParentBusinessUnit()
	{
		return (com.profitera.descriptor.db.user.BusinessUnit) parentBusinessUnit.getValue();
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public java.util.Vector getTeams()
	{
		return (java.util.Vector) teams.getValue();
	}

	public void setAddressDetails(com.profitera.descriptor.db.contact.AddressDetails addressDetails)
	{
		this.addressDetails.setValue(addressDetails);
	}

	public void setBranchCreateBy(java.lang.String branchCreateBy)
	{
		this.branchCreateBy = branchCreateBy;
	}

	public void setBranchCreateDate(java.sql.Timestamp branchCreateDate)
	{
		this.branchCreateDate = branchCreateDate;
	}

	public void setBranchId(java.lang.String branchId)
	{
		this.branchId = branchId;
	}

	public void setBranchName(java.lang.String branchName)
	{
		this.branchName = branchName;
	}

	public void setBranchType(java.lang.String branchType)
	{
		this.branchType = branchType;
	}

	public void setChildBusinessUnit(java.util.Vector childBusinessUnit)
	{
		this.childBusinessUnit.setValue(childBusinessUnit);
	}

	public void setCostCenter(java.lang.String costCenter)
	{
		this.costCenter = costCenter;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setHeadUser(com.profitera.descriptor.db.user.User headUser)
	{
		this.headUser.setValue(headUser);
	}

	public void setIsCollection(java.lang.Double isCollection)
	{
		this.isCollection = isCollection;
	}

	public void setParentBranchId(java.lang.String parentBranchId)
	{
		this.parentBranchId = parentBranchId;
	}

	public void setParentBusinessUnit(com.profitera.descriptor.db.user.BusinessUnit parentBusinessUnit)
	{
		this.parentBusinessUnit.setValue(parentBusinessUnit);
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}

	public void setTeams(java.util.Vector teams)
	{
		this.teams.setValue(teams);
	}
}
