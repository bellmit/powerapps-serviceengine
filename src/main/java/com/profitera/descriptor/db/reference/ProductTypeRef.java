// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class ProductTypeRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String PRODUCT_TYPE_ABBREVIATION = "productTypeAbbreviation";
	public static final String PRODUCT_TYPE_CATEGORY = "productTypeCategory";
	public static final String PRODUCT_TYPE_CODE = "productTypeCode";
	public static final String PRODUCT_TYPE_DESC = "productTypeDesc";
	public static final String PRODUCT_TYPE_GROUP = "productTypeGroup";
	public static final String PRODUCT_TYPE_ID = "productTypeId";
	public static final String PRODUCT_TYPE_LEVEL_REF = "productTypeLevelRef";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.String productTypeAbbreviation;

	private oracle.toplink.indirection.ValueHolderInterface productTypeCategory= new oracle.toplink.indirection.ValueHolder();

	private java.lang.String productTypeCode;

	private java.lang.String productTypeDesc;

	private java.lang.String productTypeGroup;

	private java.lang.Double productTypeId;

	private oracle.toplink.indirection.ValueHolderInterface productTypeLevelRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double sortPriority;

	public  ProductTypeRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.String getProductTypeAbbreviation()
	{
		return productTypeAbbreviation;
	}

	public com.profitera.descriptor.db.reference.ProductTypeCategoryRef getProductTypeCategory()
	{
		return (com.profitera.descriptor.db.reference.ProductTypeCategoryRef) productTypeCategory.getValue();
	}

	public java.lang.String getProductTypeCode()
	{
		return productTypeCode;
	}

	public java.lang.String getProductTypeDesc()
	{
		return productTypeDesc;
	}

	public java.lang.String getProductTypeGroup()
	{
		return productTypeGroup;
	}

	public java.lang.Double getProductTypeId()
	{
		return productTypeId;
	}

	public com.profitera.descriptor.db.reference.ProductTypeLevelRef getProductTypeLevelRef()
	{
		return (com.profitera.descriptor.db.reference.ProductTypeLevelRef) productTypeLevelRef.getValue();
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setProductTypeAbbreviation(java.lang.String productTypeAbbreviation)
	{
		this.productTypeAbbreviation = productTypeAbbreviation;
	}

	public void setProductTypeCategory(com.profitera.descriptor.db.reference.ProductTypeCategoryRef productTypeCategory)
	{
		this.productTypeCategory.setValue(productTypeCategory);
	}

	public void setProductTypeCode(java.lang.String productTypeCode)
	{
		this.productTypeCode = productTypeCode;
	}

	public void setProductTypeDesc(java.lang.String productTypeDesc)
	{
		this.productTypeDesc = productTypeDesc;
	}

	public void setProductTypeGroup(java.lang.String productTypeGroup)
	{
		this.productTypeGroup = productTypeGroup;
	}

	public void setProductTypeId(java.lang.Double productTypeId)
	{
		this.productTypeId = productTypeId;
	}

	public void setProductTypeLevelRef(com.profitera.descriptor.db.reference.ProductTypeLevelRef productTypeLevelRef)
	{
		this.productTypeLevelRef.setValue(productTypeLevelRef);
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
