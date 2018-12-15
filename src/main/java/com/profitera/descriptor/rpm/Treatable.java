package com.profitera.descriptor.rpm;

/**
 * 
 *
 * @author jamison
 */
public interface Treatable {

	public String getCurrentLocation();
	public Integer getCurrentActionType();
  public Long getStageId();
	public Object getId();
	public Integer getCurrentPlanActionStatus();
	public int getDaysAtCurrentPlanActionStatus();
	public boolean getCurrentPlanActionNotSuccessful();
	public int getCurrentPlanActionAttempts();
	public Object getDesiredStreamId();
	public Object getCurrentStreamId();
    public Number getCurrentPlanActionId();
}
