package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.descriptor.business.meta.ITreatmentProcess;
import com.profitera.descriptor.business.reference.TreatmentProcessTypeStatusRefBusinessBean;
import com.profitera.server.ServiceEngine;
import com.profitera.util.DateParser;

public abstract class AbstractTreatmentProcessManager implements
		ITreatmentProcessDataManager {
	protected static final Long IN_PROGRESS = new Long(
			TreatmentProcessTypeStatusRefBusinessBean.IN_PROGRESS_TREATMENT_PROCESS_STATUS
					.longValue());

	private Log log;

  private Boolean isMonthlyPlan;

	public Map[] verifyProcessCreation(Map process, Long accountId, Long typeId,
			String user, IReadOnlyDataProvider provider)
			throws TreatmentProcessCreationException {
		// Do nothing for now.
	  return new Map[]{process};
	}

	protected Log getLog() {
    if (log == null){
      log = LogFactory.getLog(this.getClass());
    }
		return log;
	}

	public Map getTreatmentPlanForProcess(Map process, Long accountId,
			Date date, Long typeId, String user, String node, ITransaction t,
			IReadWriteDataProvider p) throws SQLException,
			AbortTransactionException {
    return getTreatmentPlanForProcess(process, accountId, date, typeId, user, node, null, t, p);
  }
  public Map getTreatmentPlanForProcess(Map process, Long accountId,
        Date date, Long typeId, String user, String node, Long streamId, ITransaction t,
        IReadWriteDataProvider p) throws SQLException,
        AbortTransactionException {
    if (process == null) {
      throw new IllegalArgumentException("No process provided for plan request");
    }
    if (p == null){
      throw new IllegalArgumentException("No ReadWriteDataProvider supplied");
    }
    if (date == null){
      throw new IllegalArgumentException("No effective date supplied");
    }
		if (process.get(ITreatmentProcess.TREATMENT_PLAN_ID) != null && node == null && streamId == null) {
			return process;
		}
		
		Map plan = getOpenPlan(accountId, p);
		if (plan == null) {
			//TODO: Backdating must be handled!
			plan = createPlan(accountId, DateParser.getStartOfDay(date), getCreatePlanEndDate(date) ,node, streamId,
					t, p);
		} else if (node != null) {
			String currentNode = (String) plan.get("NODE_LOCATION");
			if (currentNode == null || !currentNode.equals(node)) {
				plan.put("NODE_LOCATION", node);
        plan.put("TREATMENT_STREAM_ID", streamId);
				updateTreatmentPlan(plan, t, p);
			}
		}
		return plan;
	}

    public void updateTreatmentPlan(Map plan, ITransaction t, IReadWriteDataProvider p) throws SQLException {
      p.update("updateTreatmentPlanNodeLocation", plan, t);
    }

  protected Date getCreatePlanEndDate(Date date) {
    if (isMonthlyPlan == null){
      isMonthlyPlan = new Boolean(ServiceEngine.getProp("treatmentprocessservice.monthlyplans", "false").equals("true"));
    }
    if (isMonthlyPlan.booleanValue()){
      return DateParser.getEndOfMonth(date);
    } else {
      return null;
    }
  }


  private Map createPlan(Long accountId, Date startDate, Date endDate, String nodeLocation, Long treatmentStreamId,
			ITransaction t, IReadWriteDataProvider p) throws SQLException {
		Map m = new HashMap();
		m.put("ACCOUNT_ID", accountId);
		m.put("TREATMENT_START_DATE", startDate);
    m.put("TREATMENT_END_DATE", endDate);
		m.put("TREATMENT_STAGE_ID", getMinTreatmentStage(p));
    m.put("TREATMENT_STREAM_ID", treatmentStreamId);
		m.put("NODE_LOCATION", nodeLocation);
		m.put(ITreatmentProcess.TREATMENT_PLAN_ID, p.insert(
				"insertTreatmentPlan", m, t));
		return m;
	}

	private Long getMinTreatmentStage(IReadWriteDataProvider p)
			throws SQLException {
		return (Long) p.queryObject("getFirstTreatmentStageId", null);
	}

	protected Map getOpenPlan(Long accountId, IReadWriteDataProvider p)
			throws SQLException {
		return (Map) p.queryObject("getOpenTreatmentPlan", accountId);
	}

	public Long createTreatmentProcess(Map plan, Map process, Long accountId,
			Date date, Long typeId, String user, ITransaction t,
			IReadWriteDataProvider p) throws AbortTransactionException,
			SQLException {
		if (date == null)
			throw new IllegalArgumentException(
					"No update/create date specified for treatment process");
		if (process == null){
		  throw new IllegalArgumentException("Null process provided for creation");
		}
		Long planId = plan == null ? null : (Long) plan.get(ITreatmentProcess.TREATMENT_PLAN_ID);
		if (planId == null) {
			throw new IllegalArgumentException(
					"No treatment plan specified for treatment process");
		}
		if (typeId == null) {
      throw new IllegalArgumentException(
          "No treatment type specified for treatment process");
    }
		if (p == null){
		  throw new IllegalArgumentException("No ReadWriteDataProvider supplied");
		}
		process.put(ITreatmentProcess.CREATED_DATE, date);
    process.put(ITreatmentProcess.CREATED_USER, user);
		process.put(ITreatmentProcess.TREATMENT_PLAN_ID, planId);
		Long subtype = getProcessSubtype(process, typeId, p);
		process.put(ITreatmentProcess.PROCESS_TYPE_ID, typeId);
		process.put(ITreatmentProcess.PROCESS_SUBTYPE_ID, subtype);
		Long status = getProcessStatus(process, p);
		Long typeStatus = getProcessTypeStatus(process, typeId, status, p);
		//    Long typeStatusStatus = getStatusForTypeStatus(typeStatus, p);
		//    if (!typeStatusStatus.equals(status)){
		//      getLog().info("Assigned status of " + status + " adjusted to " +
		// typeStatusStatus + " for type status " + typeStatus);
		//      status = typeStatusStatus;
		//    }
		process.put(ITreatmentProcess.PROCESS_STATUS_ID, status);
		process.put(ITreatmentProcess.PROCESS_TYPE_STATUS_ID, typeStatus);
		Date expectedStart = getExpectedStartDate(process, date, subtype, p);
		process.put(ITreatmentProcess.EXPECTED_START_DATE, expectedStart);
		Date expectedEnd = getExpectedEndDate(process, expectedStart, subtype,
				p);
		process.put(ITreatmentProcess.EXPECTED_END_DATE, expectedEnd);
		setActualDates(process, date, status);
		//Putting ACCOUNT_ID
		process.put("ACCOUNT_ID", accountId);
		//In case any modification of the information going to treatment_process
		//is needed to be done based on the types and subtype,
		//preprocessTreatmentProcessInformation can be used.
		
		preprocessTreatmentProcessInformationForInsert(process, typeId, t, p);
		// Insert!
		Long processId = (Long) p.insert("insertTreatmentProcess", process, t);
		process.put(ITreatmentProcess.TREATMENT_PROCESS_ID, processId);

		createExtendedProcessInformation(accountId, process, typeId, date, user, t, p);
		processInserted(process, t, p);
		return processId;
	}

  protected void createExtendedProcessInformation(Long accountId, Map process, Long typeId, Date date, String user, ITransaction t, IReadWriteDataProvider p) throws SQLException, AbortTransactionException {
  }
  
  /**
   * This method is useful for doing modification to treatment_process record before it gets inserted
   * A TreatmentManager which extends this default treatment manager can always override this method
   * and do type or subtype based custom massaging of data
   * Remember that TREATMENT_PROCESS_ID is not yet available at this time!
   * @param process Treatment process to be modified/massaged
   * @param typeId Type id indicating process type
   * @param t Transaction object for doing all the updates/inserts in one transaction to ensure integrity
   * @param p Provider for query/insert/update/delete
   * @throws SQLException
   * @throws AbortTransactionException
   */
  protected void preprocessTreatmentProcessInformationForInsert(Map process, Long typeId, ITransaction t, IReadWriteDataProvider p) throws SQLException, AbortTransactionException {
  	//right now for the base treatment process manager, it does nothing.
  }

	/**
	 * This method is provided as a hook for subclasses to add more statement
	 * executions after the intial inserts are completed, in the same
	 * transaction.
	 * 
	 * @param process
	 * @param t
	 * @param p
	 * @throws SQLException
	 * @throws AbortTransactionException
	 */
	protected void processInserted(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException,
			AbortTransactionException {
		// 

	}

	protected void setActualDates(Map process, Date date, Long status) {
		if (!isInProgressStatus(status)) {
			Date actualEnd = (Date) process
					.get(ITreatmentProcess.ACTUAL_END_DATE);
			Date actualStart = (Date) process
					.get(ITreatmentProcess.ACTUAL_START_DATE);
			if (actualStart == null) {
				process.put(ITreatmentProcess.ACTUAL_START_DATE, date);
			}
			if (actualEnd == null) {
				process.put(ITreatmentProcess.ACTUAL_END_DATE, date);
			}
		}
	}

	private boolean isInProgressStatus(Long status) {
		return status.equals(IN_PROGRESS);
	}

	protected Date getExpectedEndDate(Map process, Date expectedStart,
			Long subtype, IReadWriteDataProvider p) throws SQLException {
		Date exp = (Date) process.get(ITreatmentProcess.EXPECTED_END_DATE);
		if (exp == null) {
			Number daysDuration = (Number) p.queryObject(
					"getTreatmentProcessExpectedDuration", subtype);
			if (daysDuration != null && daysDuration.intValue() > 0) {
				Calendar c = Calendar.getInstance();
				c.setTime(expectedStart);
				c.add(Calendar.DATE, daysDuration.intValue());
				exp = c.getTime();
			} else {
				exp = expectedStart;
			}
		}
		return exp;
	}

	protected Date getExpectedStartDate(Map process, Date date, Long subtype,
			IReadWriteDataProvider p) throws SQLException {
		// Lead time defines the default period of time from when a process is
		// created until when it
		// can/should/usually be started. For example, you can allow 1 hour for
		// a escalation action to
		// start by default or 12 hours from the running of treatment batch
		// process until the letter
		// generation batch process.
		// This was never really as useful as I thought it was going to be, I
		// think it originalted from
		// some sort of mythical requirement from the EO, but I don't remember.
		Date exp = (Date) process.get(ITreatmentProcess.EXPECTED_START_DATE);
		if (exp == null) {
			Number leadTime = (Number) p.queryObject(
					"getTreatmentProcessExpectedLeadTime", subtype);
			if (leadTime != null && leadTime.intValue() > 0) {
				Calendar c = Calendar.getInstance();
				c.setTime(date);
				c.add(Calendar.HOUR_OF_DAY, leadTime.intValue());
				exp = c.getTime();
			} else {
				exp = date;
			}
		}
		return exp;
	}

	protected Long getProcessTypeStatus(Map process, Long typeId, Long status,
			IReadWriteDataProvider p) throws SQLException,
			AbortTransactionException {
		Long typeStatus = (Long) process
				.get(ITreatmentProcess.PROCESS_TYPE_STATUS_ID);
		if (typeStatus == null) {
			Map m = new HashMap();
			m.put(ITreatmentProcess.PROCESS_TYPE_ID, typeId);
			m.put(ITreatmentProcess.PROCESS_STATUS_ID, status);
			typeStatus = (Long) p.queryObject("getDefaultTypeStatusId", m);
		}
		if (typeStatus == null)
			throw new AbortTransactionException(
					"Unable to resolve type status for type " + typeId
							+ " and status " + status);
		return typeStatus;
	}

	protected Long getProcessStatus(Map process, IReadWriteDataProvider p) {
		Long status = (Long) process.get(ITreatmentProcess.PROCESS_STATUS_ID);
		if (status == null) {
			status = IN_PROGRESS;
		}
		return status;
	}

	protected Long getProcessSubtype(Map process, Long typeId,
			IReadWriteDataProvider p) throws AbortTransactionException,
			SQLException {
		Long subtype = (Long) process.get(ITreatmentProcess.PROCESS_SUBTYPE_ID);
		if (subtype == null) {
			subtype = getDefaultSubtype(typeId, p);
		}
		if (subtype == null) {
			throw new AbortTransactionException(
					"Unable to resolve subtype for treatment process type "
							+ typeId);
		}
		return subtype;
	}

	protected Long getDefaultSubtype(Long typeId, IReadWriteDataProvider p)
			throws AbortTransactionException, SQLException {
		Long subtypeId = (Long) p.queryObject("getDefaultSubtypeId", typeId);
		return subtypeId;
	}

	/**
	 * The user can be null here, it means the system is updating the treatment
	 * using a batch process.
	 * @see com.profitera.services.system.dataaccess.ITreatmentProcessDataManager#updateTreatmentProcess(Long, java.util.Map, java.util.Date, java.lang.String, com.profitera.dataaccess.ITransaction, com.profitera.services.system.dataaccess.IReadWriteDataProvider)
	 */
	public void updateTreatmentProcess(Long accountId, Map process, Date date,
			String user, ITransaction t, IReadWriteDataProvider p) throws SQLException,
			AbortTransactionException {
	  if (process == null){
      throw new IllegalArgumentException("Process data for update can not be null");
    }
	  if (date == null){
	    throw new IllegalArgumentException("Process date for update can not be null");
	  }
	  if (p == null){
	    throw new IllegalArgumentException("No ReadWriteDataProvider supplied");
	  }
	  process.put(ITreatmentProcess.USER_ID, user);
		Long status = getProcessStatus(process, p);
		setActualDates(process, date, status);
		preprocessTreatmentProcessInformationForUpdate(process, t, p);
		p.update("updateTreatmentProcess", process, t);
		updateExtendedProcessInformation(accountId, process, (Long) process.get(ITreatmentProcess.PROCESS_TYPE_ID), date, user, t, p);
	}

  /**
   * This method is useful for doing modification to treatment_process record before it gets updated
   * A TreatmentManager which extends this default treatment manager can always override this method
   * and do type or subtype based custom massaging of data
   * @param process Treatment process to be modified/massaged
   * @param typeId Type id indicating process type
   * @param t Transaction object for doing all the updates/inserts in one transaction to ensure integrity
   * @param p Provider for query/insert/update/delete
   * @throws SQLException
   * @throws AbortTransactionException
   */
  protected void preprocessTreatmentProcessInformationForUpdate(Map process, ITransaction t, IReadWriteDataProvider p) throws SQLException, AbortTransactionException {
  	
  }
	
	public void verifyProcessForUpdate(Map process, Date date, String user, IReadWriteDataProvider p)
			throws TreatmentProcessUpdateException, SQLException {
	  if (process == null){
	    throw new IllegalArgumentException("Process data for update can not be null");
	  }
		if (process.get("UPDATE_DATE") != null) {
		  if (p == null){
		    throw new IllegalArgumentException("No ReadWriteDataProvider supplied");
		  }
			Map m = (Map) p.queryObject("getLastUpdateDateForTreatmentProcess", process);
	  	Object lastUpdateDate = m == null ? null : m.get("UPDATE_DATE");
			if (lastUpdateDate == null || !lastUpdateDate.equals(process.get("UPDATE_DATE"))) {
				throw new TreatmentProcessUpdateException("VERIFICATION_FOR_UPDATE_FAILURE");
			}
		}
	}
  protected void updateExtendedProcessInformation(Long accountId, Map process, Long typeId, Date date, String user, ITransaction t, IReadWriteDataProvider p) throws SQLException, AbortTransactionException {
    //
  }
  
  public void processPostUpdate(Long accountId, Map process, Date date, String user, ITransaction t, IReadWriteDataProvider p) throws SQLException, AbortTransactionException {
	  
  }
}
