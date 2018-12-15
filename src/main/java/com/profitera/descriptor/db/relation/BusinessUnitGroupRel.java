// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.relation;

public class BusinessUnitGroupRel
	implements java.io.Serializable
{
	// Generated constants
	public static final String BUSINESS_GROUP_REF = "businessGroupRef";
	public static final String BUSINESS_UNIT = "businessUnit";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface businessGroupRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface businessUnit= new oracle.toplink.indirection.ValueHolder();

	public  BusinessUnitGroupRel()
	{
		// Fill in method body here.
	}

	public com.profitera.descriptor.db.reference.BusinessGroupRef getBusinessGroupRef()
	{
		return (com.profitera.descriptor.db.reference.BusinessGroupRef) businessGroupRef.getValue();
	}

	public com.profitera.descriptor.db.user.BusinessUnit getBusinessUnit()
	{
		return (com.profitera.descriptor.db.user.BusinessUnit) businessUnit.getValue();
	}

	public void setBusinessGroupRef(com.profitera.descriptor.db.reference.BusinessGroupRef businessGroupRef)
	{
		this.businessGroupRef.setValue(businessGroupRef);
	}

	public void setBusinessUnit(com.profitera.descriptor.db.user.BusinessUnit businessUnit)
	{
		this.businessUnit.setValue(businessUnit);
	}
}
