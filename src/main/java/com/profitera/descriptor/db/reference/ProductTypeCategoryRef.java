// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class ProductTypeCategoryRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String PRODUCT_TYPE_CATEGORY_CODE = "productTypeCategoryCode";
	public static final String PRODUCT_TYPE_CATEGORY_DESC = "productTypeCategoryDesc";
	public static final String PRODUCT_TYPE_CATEGORY_ID = "productTypeCategoryId";
	public static final String PRODUCT_TYPE_CATEGORY_REF = "productTypeCategoryRef";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.String productTypeCategoryCode;

	private java.lang.String productTypeCategoryDesc;

	private java.lang.Double productTypeCategoryId;

	private oracle.toplink.indirection.ValueHolderInterface productTypeCategoryRef= new oracle.toplink.indirection.ValueHolder(new oracle.toplink.indirection.IndirectList());

	private java.lang.Double sortPriority;

	public  ProductTypeCategoryRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.String getProductTypeCategoryCode()
	{
		return productTypeCategoryCode;
	}

	public java.lang.String getProductTypeCategoryDesc()
	{
		return productTypeCategoryDesc;
	}

	public java.lang.Double getProductTypeCategoryId()
	{
		return productTypeCategoryId;
	}

	public com.profitera.descriptor.db.reference.ProductTypeRef getProductTypeCategoryRef()
	{
		return (com.profitera.descriptor.db.reference.ProductTypeRef) productTypeCategoryRef.getValue();
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setProductTypeCategoryCode(java.lang.String productTypeCategoryCode)
	{
		this.productTypeCategoryCode = productTypeCategoryCode;
	}

	public void setProductTypeCategoryDesc(java.lang.String productTypeCategoryDesc)
	{
		this.productTypeCategoryDesc = productTypeCategoryDesc;
	}

	public void setProductTypeCategoryId(java.lang.Double productTypeCategoryId)
	{
		this.productTypeCategoryId = productTypeCategoryId;
	}

	public void setProductTypeCategoryRef(com.profitera.descriptor.db.reference.ProductTypeRef productTypeCategoryRef)
	{
		this.productTypeCategoryRef.setValue(productTypeCategoryRef);
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
