// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.treatment;

public class TreatmentActionTransition
	implements java.io.Serializable
{
	// Generated constants
	public static final String DESTINATION_ACTION_NODE = "destinationActionNode";
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String SOURCE_ACTION_NODE = "sourceActionNode";
	public static final String TREATPROC_STATUS_REF = "treatprocStatusRef";
	public static final String WEIGHT = "weight";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface destinationActionNode= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Double id;

	private java.lang.String name;

	private oracle.toplink.indirection.ValueHolderInterface sourceActionNode= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface treatprocStatusRef= new oracle.toplink.indirection.ValueHolder();

	private java.lang.Integer weight;

	public  TreatmentActionTransition()
	{
		// Fill in method body here.
	}

	public com.profitera.descriptor.db.treatment.TreatmentActionNode getDestinationActionNode()
	{
		return (com.profitera.descriptor.db.treatment.TreatmentActionNode) destinationActionNode.getValue();
	}

	public java.lang.Double getId()
	{
		return id;
	}

	public java.lang.String getName()
	{
		return name;
	}

	public com.profitera.descriptor.db.treatment.TreatmentActionNode getSourceActionNode()
	{
		return (com.profitera.descriptor.db.treatment.TreatmentActionNode) sourceActionNode.getValue();
	}

	public com.profitera.descriptor.db.reference.TreatmentProcessStatusRef getTreatprocStatusRef()
	{
		return (com.profitera.descriptor.db.reference.TreatmentProcessStatusRef) treatprocStatusRef.getValue();
	}

	public java.lang.Integer getWeight()
	{
		return weight;
	}

	public void setDestinationActionNode(com.profitera.descriptor.db.treatment.TreatmentActionNode destinationActionNode)
	{
		this.destinationActionNode.setValue(destinationActionNode);
	}

	public void setId(java.lang.Double id)
	{
		this.id = id;
	}

	public void setName(java.lang.String name)
	{
		this.name = name;
	}

	public void setSourceActionNode(com.profitera.descriptor.db.treatment.TreatmentActionNode sourceActionNode)
	{
		this.sourceActionNode.setValue(sourceActionNode);
	}

	public void setTreatprocStatusRef(com.profitera.descriptor.db.reference.TreatmentProcessStatusRef treatprocStatusRef)
	{
		this.treatprocStatusRef.setValue(treatprocStatusRef);
	}

	public void setWeight(java.lang.Integer weight)
	{
		this.weight = weight;
	}
}
