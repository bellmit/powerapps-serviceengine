package com.profitera.rpm.treatment;

import java.util.Date;
import java.util.Map;

import com.profitera.descriptor.business.treatment.admin.ActionNode;
import com.profitera.descriptor.business.treatment.admin.Transition;
import com.profitera.descriptor.rpm.Treatable;

/**
 * 
 *
 * @author jamison
 */
public interface TreatmentProducer {
	public abstract void doActionRetry(Treatable t, ActionNode an, Date date);

	public abstract void doTraversal(Treatable t, ActionNode an, ActionNode nextAction, Transition trans, Date date);
	
	public abstract long doStageTraversal(Treatable t, ActionNode an, Transition trans, Date date);
	public abstract void doStageTraversal(final Map transactionPlan);
  public abstract void doCancelCurrentAction(Treatable treatable, ActionNode an, Date date);
}
