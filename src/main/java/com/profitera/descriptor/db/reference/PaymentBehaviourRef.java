// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class PaymentBehaviourRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String BEHAVIOUR_CODE = "behaviourCode";
	public static final String BEHAVIOUR_DESC = "behaviourDesc";
	public static final String BEHAVIOUR_ID = "behaviourId";
	public static final String DISABLE = "disable";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.String behaviourCode;

	private java.lang.String behaviourDesc;

	private java.lang.Double behaviourId;

	private java.lang.Double disable;

	private java.lang.Double sortPriority;

	public  PaymentBehaviourRef()
	{
		// Fill in method body here.
	}

	public java.lang.String getBehaviourCode()
	{
		return behaviourCode;
	}

	public java.lang.String getBehaviourDesc()
	{
		return behaviourDesc;
	}

	public java.lang.Double getBehaviourId()
	{
		return behaviourId;
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setBehaviourCode(java.lang.String behaviourCode)
	{
		this.behaviourCode = behaviourCode;
	}

	public void setBehaviourDesc(java.lang.String behaviourDesc)
	{
		this.behaviourDesc = behaviourDesc;
	}

	public void setBehaviourId(java.lang.Double behaviourId)
	{
		this.behaviourId = behaviourId;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
