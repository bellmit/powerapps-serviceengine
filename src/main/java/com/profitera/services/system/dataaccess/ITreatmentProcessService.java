package com.profitera.services.system.dataaccess;

import java.util.Date;
import java.util.Map;

import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.descriptor.business.meta.ITreatmentProcess;

public interface ITreatmentProcessService {

  public static final String ACCOUNT_ID = "ACCOUNT_ID";
  public static final String[] TREATMENT_PROCESS_FIELDS = {
    ITreatmentProcess.TREATMENT_PLAN_ID,
     ITreatmentProcess.PROCESS_TYPE_ID,
     ITreatmentProcess.PROCESS_SUBTYPE_ID,
     ITreatmentProcess.PROCESS_TYPE_STATUS_ID,
     ITreatmentProcess.PROCESS_STATUS_ID,
     ITreatmentProcess.TREATMENT_PROCESS_ID,
     ITreatmentProcess.CREATED_DATE,
     ITreatmentProcess.EXPECTED_START_DATE,
     ITreatmentProcess.EXPECTED_END_DATE,
     ITreatmentProcess.MANUAL,
     ITreatmentProcess.ACTUAL_START_DATE,
     ITreatmentProcess.ACTUAL_END_DATE,
     ITreatmentProcess.TEMPLATE_ID,
     ITreatmentProcess.PROCESS_REMARKS,
     ITreatmentProcess.COST_BILLER_ID,
     ITreatmentProcess.HOST_UPDATED,
     ITreatmentProcess.ATTEMPT_NUMBER,
     ITreatmentProcess.OUTSTANDING_AMT,
     ITreatmentProcess.PROFILE_SEGMENT_ID,
     ITreatmentProcess.TREATMENT_STAGE_ID,
     ITreatmentProcess.TREATMENT_STREAM_ID,
     ITreatmentProcess.USER_ID,
     ITreatmentProcess.PROCESS_COST
  };

  /**
   * The only strictly required info to create a treatment process is the
   * accountId, type and user creating, everything else can be defaulted to
   * reasonable values. The processor that is doing the creation has the option
   * to reject the process creation request as well if values that it requires
   * are missing and any client would be expected to handle such a scenario
   * gracefully.
   * 
   * @param process
   * @param accountId
   * @param date
   *          'Transaction Date' for process creation.
   * @param typeId
   * @param user
   * @return
   */
  public ICreateTreatmentProcessTransaction[] createManualProcess(Map process,
      Long accountId, Date date, Long typeId, String user)
      throws TreatmentProcessCreationException;
  
  /**
   * Same basic contract as creating a manual process.
   * @see ITreatmentProcessService#createManualProcess(Map, Long, Date, Long, String)
   * 
   * @param process
   * @param accountId
   * @param date
   *          'Transaction Date' for process creation.
   * @param typeId
   * @param nodeId If it is part of a stream the location must be provided, else null
   * @return
   */
  public ICreateTreatmentProcessTransaction[] createSystemProcess(Map process,
      Long accountId, Date date, Long typeId, String nodeId, Long streamId)
      throws TreatmentProcessCreationException;
  
  public ICreateTreatmentProcessTransaction[] createSystemProcess(Map process,
      Long accountId, Map updatedPlan, Date date, Long typeId, boolean createTreatmentProcess)
      throws TreatmentProcessCreationException;



  /**
   * The only key not required in the map param is the treatment plan id since
   * that can not change, everything else is <b>MANDITORY for the fields in
   * PTRTREATMENT_PROCESS</b> in order to ensure that the update is consistent
   * with the client's intentions. Of course, the at the treatment type level
   * the requirement will depend on the class handling the update.
   * 
   * @param process
   * @return
   */
  public IRunnableTransaction updateTreatmentProcess(Map process, final Date date, final String user)
      throws TreatmentProcessUpdateException;

  
  /**
   * Primary function of this method is to create child treatment process by creating relationship
   * between primary and related treatment action.
   * 
   * @param process
   * @param accountId
   * @param date
   *          'Transaction Date' for process creation.
   * @param typeId
   * @param user
   * @return
   */
  public IRunnableTransaction createSubProcess(Map process,
      Long accountId, Date date, Long typeId, String user)
      throws TreatmentProcessCreationException;

  
  /**
   * To further process updates required after insert/update of treatment process
   * @param accountId TODO
   * @param process
   * @param date
   * @param user
   * @return
   */
  public IRunnableTransaction processPostUpdate(Long accountId, Map process, final Date date, final String user);
  
}
