package com.profitera.rpm.treatment;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.descriptor.business.reference.TreatmentProcessTypeStatusRefBusinessBean;
import com.profitera.descriptor.business.treatment.admin.*;
import com.profitera.descriptor.rpm.Treatable;
import com.profitera.server.ServiceEngine;
import com.profitera.services.system.dataaccess.RPMDataManager;

/**
 * 
 *
 * @author jamison
 */
public class LinearTreatmentPlanner extends AbstractTreatmentPlanner {
	private static final String TREATMENTASSIGNMENT_IGNOREINPROGRESSPROMISES = "treatmentassignment.ignoreinprogresspromises";
    private Log log = LogFactory.getLog(LinearTreatmentPlanner.class);
	private static final Integer PAYMENT_PLAN_CODE = new Integer(RPMDataManager.PAYMENT_PLAN_TREATMENT_PROCESS);
	private static final Integer CANCELLED_STATUS = new Integer(TreatmentProcessTypeStatusRefBusinessBean.CANCEL_TREATMENT_PROCESS_STATUS.intValue());
	private boolean proceedOnInProgressPromise = false;

	public LinearTreatmentPlanner(){
	  Properties config = ServiceEngine.getConfig(true);
	  String val = null;
	  if (config != null){
	    val = (String) config.get(TREATMENTASSIGNMENT_IGNOREINPROGRESSPROMISES);
	  }
	  if (val == null || val.equals("") || !val.toUpperCase().startsWith("T")){
	    proceedOnInProgressPromise = false;
	    log.info("Treatment will pause for treatable objects with promises to pay (" + TREATMENTASSIGNMENT_IGNOREINPROGRESSPROMISES + "=" + proceedOnInProgressPromise + ")");
	  } else {
	    proceedOnInProgressPromise = true;
	    log.info("Treatment will continue for treatable objects with promises to pay (" + TREATMENTASSIGNMENT_IGNOREINPROGRESSPROMISES + "=" + proceedOnInProgressPromise + ")");
	  }
	}
	
	protected Transition getTransitionToFollow(Treatable t, ActionNode an, boolean justCancelled) {
		Transition trans = null;
		Integer evalStatus = null;
		if (justCancelled){
		  log.debug(t.getId() + " just had current action cancelled, trying cancelled transition");
		  evalStatus = CANCELLED_STATUS;
		} else
		  evalStatus = t.getCurrentPlanActionStatus();
		trans = an.getTransition(evalStatus);
		if (trans == null){
			trans = an.getDefaultTransition();
			if (trans == null){
			    log.warn("Treatable " + t.getId() + " has no default transition and no transition for status " + evalStatus);
			    return null;
			}
			log.debug("Treatable " + t.getId() + " trying default transition with weight " + trans.getWeight() + " and destination " + trans.getDestinationKey());
		} else {
			log.debug("Treatable " + t.getId() + " trying transition (" + evalStatus + ") with weight " + trans.getWeight() + " and destination " + trans.getDestinationKey());
		}
		Object key = trans.getDestinationKey();
		// If we don't have the days to traverse or this is the wait-trans, do nothing
		if (key == null){
			log.debug("Treatable " + t.getId() + " will not follow transition, no destination");
			return null;
		}
		// If you have just cancelled then it should be because you exceeded the weight on your chosen path,
		// for an optional process, but I suppose it is possible that shouldCancel() might change, in which
		// case we would need to reflect the policy change here as well which would be redundant, so this will
		// always proceed for a cancelled action
		if (t.getDaysAtCurrentPlanActionStatus() < trans.getWeight() && !justCancelled){
			log.debug("Treatable " + t.getId() + " will not follow transition, current weight only " + t.getDaysAtCurrentPlanActionStatus());
			return null;
		}
		log.debug("Treatable " + t.getId() + " will follow transition to " + trans.getDestinationKey() + " , current weight " + t.getDaysAtCurrentPlanActionStatus());
		return trans;
	}

	public void doTreatableHasNoStream(Treatable treatable) {
		log.debug("Treatable " + treatable.getId() + " was not assigned a stream, no transitions will be evaluated");
	}
	protected boolean shouldRetry(Treatable t, ActionNode an) {
		return t.getCurrentPlanActionNotSuccessful() 
				&& (an.getRetries() == -1 
						|| t.getCurrentPlanActionAttempts() <= an.getRetries());
	}

	protected boolean canProceed(Treatable t, ActionNode an) {
		if (t.getCurrentActionType().equals(PAYMENT_PLAN_CODE)){
		  if (proceedOnInProgressPromise){
		    log.debug("Treatable " + t.getId() + " is currently in the midst of a payment plan, treatment continues anyway");
			return true; 
		  } else {
			log.debug("Treatable " + t.getId() + " is currently in the midst of a payment plan, will not treat");
			return false; // Nothing to do, we're in the midst of a P2P
		  }
		}
		return true;
	}

  /**
   * @see com.profitera.rpm.treatment.AbstractTreatmentPlanner#shouldCancelCurrentAction(com.profitera.descriptor.rpm.Treatable, com.profitera.descriptor.business.treatment.admin.ActionNode)
   */
  protected boolean shouldCancelCurrentAction(Treatable treatable, ActionNode an) {
    if (an.isManditory()){
      return false;
    }
    int weight = treatable.getDaysAtCurrentPlanActionStatus();
    Transition defaultTrans = an.getDefaultTransition();
    if (defaultTrans != null){
      if (defaultTrans.getWeight() > weight) {
        return false;
      } else {
        log.debug(treatable.getId() + " will cancel current action and follow default transition.");
        return true;
      }
    }
    // If we have no default transition we will expire the action after the cancelled transition weight:
    Transition cancelledTrans = an.getTransition(CANCELLED_STATUS);
    if (cancelledTrans != null){
      if (cancelledTrans.getWeight() < weight) {
        return false;
      } else {
        log.debug(treatable.getId() + " will cancel current action and follow cancellation transition.");
        return true;
      }
    }
    log.warn(treatable.getId() + " : Optional node " + an.getId() + " has no default or cancel transitions.");
    return false;
  }
}
