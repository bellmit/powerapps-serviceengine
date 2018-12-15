package com.profitera.services.business.batch;

import java.util.HashMap;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.worklistmanager.WorkListService;

public class WorkListAssignmentBatch extends AbstractBatchProcess {
	public static final String ROOT = "root";
  
  public WorkListAssignmentBatch(){
    addRequiredProperty(ROOT, Long.class, "The work list root id", "Work list generation root for processing");
  }
	
	public TransferObject invoke() {
		Long root = (Long)getPropertyValue(ROOT);
		try {
			return new WorkListService().assignWorkLists(getEffectiveDate(), root, new HashMap());
    } catch (NumberFormatException e){
			return new TransferObject(TransferObject.ERROR, "INVALID_ROOT_PROVIDED");
		}
	}

	protected String getBatchDocumentation() {
		return "Batch program to assign work list to users";
	}

	protected String getBatchSummary() {
		return "This batch program goes through all the users and work lists in the system and assign them to each other based on the configuration in work list decision tree"; 
	}
}