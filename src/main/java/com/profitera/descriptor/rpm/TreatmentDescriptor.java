package com.profitera.descriptor.rpm;

import com.profitera.descriptor.business.reference.TreatmentProcessTypeStatusRefBusinessBean;

/**
 * 
 *
 * @author jamison
 */
public class TreatmentDescriptor implements Treatable {
	private String currentLocation;
	private Integer currentActionType;
	private Integer currentPlanActionStatus;
	private int daysAtCurrentPlanActionStatus;
	private int currentPlanActionAttempts;
	private Object desiredStreamId;
	private Object currentStreamId;
	private Object id;
	private Number currentPlanActionId;
  private Long stageId;
	
	
	/**
	 * @see com.profitera.descriptor.rpm.Treatable#getCurrentPlanActionNotSuccessful()
	 */
	public boolean getCurrentPlanActionNotSuccessful() {
		return getCurrentPlanActionStatus().intValue() != TreatmentProcessTypeStatusRefBusinessBean.SUCCESSFUL_TREATMENT_PROCESS_STATUS.intValue()
			   && getCurrentPlanActionStatus().intValue() != TreatmentProcessTypeStatusRefBusinessBean.IN_PROGRESS_TREATMENT_PROCESS_STATUS.intValue();
	}
	
	public Integer getCurrentActionType() {
		return currentActionType;
	}
	public void setCurrentActionType(Integer currentActionType) {
		this.currentActionType = currentActionType;
	}
	public String getCurrentLocation() {
		return currentLocation;
	}
	public void setCurrentLocation(String currentLocation) {
		this.currentLocation = currentLocation;
	}
	public int getCurrentPlanActionAttempts() {
		return currentPlanActionAttempts;
	}
	public void setCurrentPlanActionAttempts(int currentPlanActionAttempts) {
		this.currentPlanActionAttempts = currentPlanActionAttempts;
	}
	public Integer getCurrentPlanActionStatus() {
		return currentPlanActionStatus;
	}
	public void setCurrentPlanActionStatus(Integer currentPlanActionStatus) {
		this.currentPlanActionStatus = currentPlanActionStatus;
	}
	public Object getCurrentStreamId() {
		return currentStreamId;
	}
	public void setCurrentStreamId(Object currentStreamId) {
		this.currentStreamId = currentStreamId;
	}
	public int getDaysAtCurrentPlanActionStatus() {
		return daysAtCurrentPlanActionStatus;
	}
	public void setDaysAtCurrentPlanActionStatus(
			int daysAtCurrentPlanActionStatus) {
		this.daysAtCurrentPlanActionStatus = daysAtCurrentPlanActionStatus;
	}
	public Object getDesiredStreamId() {
		return desiredStreamId;
	}
	public void setDesiredStreamId(Object desiredStreamId) {
		this.desiredStreamId = desiredStreamId;
	}
	public Object getId() {
		return id;
	}
	public void setId(Object id) {
		this.id = id;
	}
  public Number getCurrentPlanActionId() {
    return currentPlanActionId;
  }
  public void setCurrentPlanActionId(Number currentPlanActionId) {
    this.currentPlanActionId = currentPlanActionId;
  }

  public Long getStageId() {
    return stageId;
  }

  public void setStageId(Long stageId) {
    this.stageId = stageId;
  }
}
