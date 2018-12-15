// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.worklist;

public class WorkListBlockCode
	implements java.io.Serializable
{
	// Generated constants
	public static final String BLOCK_CODE_REF = "blockCodeRef";
	public static final String WORKLIST = "worklist";
	// End of generated constants
	
	private oracle.toplink.indirection.ValueHolderInterface blockCodeRef= new oracle.toplink.indirection.ValueHolder();

	private oracle.toplink.indirection.ValueHolderInterface worklist= new oracle.toplink.indirection.ValueHolder();

	public  WorkListBlockCode()
	{
		// Fill in method body here.
	}

	public com.profitera.descriptor.db.reference.BlockCodeRef getBlockCodeRef()
	{
		return (com.profitera.descriptor.db.reference.BlockCodeRef) blockCodeRef.getValue();
	}

	public com.profitera.descriptor.db.worklist.WorkList getWorklist()
	{
		return (com.profitera.descriptor.db.worklist.WorkList) worklist.getValue();
	}

	public void setBlockCodeRef(com.profitera.descriptor.db.reference.BlockCodeRef blockCodeRef)
	{
		this.blockCodeRef.setValue(blockCodeRef);
	}

	public void setWorklist(com.profitera.descriptor.db.worklist.WorkList worklist)
	{
		this.worklist.setValue(worklist);
	}
}
