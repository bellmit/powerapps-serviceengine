// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.relation;

public class ProductBillingCycleRel
	implements java.io.Serializable
{
	// Generated constants
	public static final String BILLING_CYCLE_REF = "billingCycleRef";
	public static final String PRODUCT_TYPE_REF = "productTypeRef";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface billingCycleRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface productTypeRef= new oracle.toplink.indirection.ValueHolder();

	public  ProductBillingCycleRel()
	{
		// Fill in method body here.
	}

	public com.profitera.descriptor.db.reference.BillingCycleRef getBillingCycleRef()
	{
		return (com.profitera.descriptor.db.reference.BillingCycleRef) billingCycleRef.getValue();
	}

	public com.profitera.descriptor.db.reference.ProductTypeRef getProductTypeRef()
	{
		return (com.profitera.descriptor.db.reference.ProductTypeRef) productTypeRef.getValue();
	}

	public void setBillingCycleRef(com.profitera.descriptor.db.reference.BillingCycleRef billingCycleRef)
	{
		this.billingCycleRef.setValue(billingCycleRef);
	}

	public void setProductTypeRef(com.profitera.descriptor.db.reference.ProductTypeRef productTypeRef)
	{
		this.productTypeRef.setValue(productTypeRef);
	}
}
