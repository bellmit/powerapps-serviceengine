package com.profitera.rpm.treatment;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.descriptor.business.treatment.admin.*;
import com.profitera.descriptor.rpm.Treatable;

/**
 * 
 *
 * @author jamison
 */
public abstract class AbstractTreatmentPlanner implements TreatmentPlanner {
  Log log = LogFactory.getLog(AbstractTreatmentPlanner.class);
	public void treat(Treatable treatable, TreatmentGraph actionGraph, TreatmentProducer producer, Date date){
	    if (treatable == null)
	      throw new IllegalArgumentException("Treatable to treat can not be null");
	    if (actionGraph == null)
	      throw new IllegalArgumentException("TreatmentGraph to treat wih can not be null");
	    if (producer == null)
	      throw new IllegalArgumentException("TreatmentProducer to treat wih can not be null");
		String nodeId = treatable.getCurrentLocation();
		ActionNode an = null;
		if (nodeId == null){
			if (treatable.getDesiredStreamId() == null){
				doTreatableHasNoStream(treatable);
				return;
			}
			an = actionGraph.getStreamStart(treatable.getDesiredStreamId());
			if (an == null){
				doDesiredStreamHasNoStart(treatable);
				return;
			}
		} else{
			an = actionGraph.getNode(nodeId);
			if (an == null){
				doTreatableCurrentNodeNotFound(treatable, nodeId, producer, date);
				return;
			}
		}
		if (shouldRetry(treatable, an)){
			producer.doActionRetry(treatable, an, date);
			return;
		}
		if (!canProceed(treatable, an)) return;
		boolean justCancelled = false;
		Transition trans = getTransitionToFollow(treatable, an, justCancelled);
		if (trans == null && shouldCancelCurrentAction(treatable, an)){
		    producer.doCancelCurrentAction(treatable, an, date);
		    justCancelled = true;
		    trans = getTransitionToFollow(treatable, an, justCancelled);
		}
		//!!!
		if (trans == null) return;
		Object key = trans.getDestinationKey(); 
		// Now we follow the transition:
		if (ActionNode.STAGE_KEY.equals(key)){
			producer.doStageTraversal(treatable, an, trans, date);
			return;
		}else{
			ActionNode nextAction = actionGraph.getNode(key);
			if (nextAction == null) throw new RuntimeException("Transition on '" + trans.getEntryStatus() + "' from '" + an.getId() + "' with key '" + key + "' leads nowhere");
			producer.doTraversal(treatable, an, nextAction, trans, date);
			return;
		}
	}

	protected abstract boolean shouldCancelCurrentAction(Treatable treatable, ActionNode an);

    /**
	 * We pass the node id in here as well because it could be the stream start, I suppose. But this should only
	 * happen when the treatable has an assigned position.
	 */
	private void doTreatableCurrentNodeNotFound(Treatable treatable, String nodeId, TreatmentProducer producer, Date date) {
    log.error("Unsupported state '" + nodeId + "' referenced by " + treatable.getId() + ", advancing stage to resolve path.");
    producer.doStageTraversal(treatable, null, null, date);
	}

	public void doDesiredStreamHasNoStart(Treatable treatable) {
    log.error("No stream start identified for '" + treatable.getDesiredStreamId() + "' assigned to " + treatable.getId());
	}

	public void doTreatableHasNoStream(Treatable treatable) {
    log.error("No destination stream identified for " + treatable.getId());
	}

	protected abstract Transition getTransitionToFollow(Treatable t, ActionNode an, boolean justCancelled);

	protected abstract boolean shouldRetry(Treatable t, ActionNode an);

	protected abstract boolean canProceed(Treatable t, ActionNode an);
}
