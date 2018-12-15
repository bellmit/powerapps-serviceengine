// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class ImageTypeRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String IMAGE_TYPE_CODE = "imageTypeCode";
	public static final String IMAGE_TYPE_DESC = "imageTypeDesc";
	public static final String IMAGE_TYPE_ID = "imageTypeId";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.String imageTypeCode;

	private java.lang.String imageTypeDesc;

	private java.lang.Double imageTypeId;

	private java.lang.Double sortPriority;

	public  ImageTypeRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.String getImageTypeCode()
	{
		return imageTypeCode;
	}

	public java.lang.String getImageTypeDesc()
	{
		return imageTypeDesc;
	}

	public java.lang.Double getImageTypeId()
	{
		return imageTypeId;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setImageTypeCode(java.lang.String imageTypeCode)
	{
		this.imageTypeCode = imageTypeCode;
	}

	public void setImageTypeDesc(java.lang.String imageTypeDesc)
	{
		this.imageTypeDesc = imageTypeDesc;
	}

	public void setImageTypeId(java.lang.Double imageTypeId)
	{
		this.imageTypeId = imageTypeId;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
