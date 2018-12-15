package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.w3c.dom.Element;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;

public interface ITreatmentProcessDataManager {
  
  public void configure(Object id, Element e);

  /**
   * This method is permitted to modify the process map (can serve as
   * an optimization) but not to engage in any database altering behaviour.
   * The only "true" purpose of this method is to verify that the information
   * provided is valid and sufficient to produce the treatment process before
   * entering into a real database transaction.
   * The rules for this can vary from implementation to implementation. 
   * @param process
   * @param accountId
   * @param typeId
   * @param user
   * @param provider
   * @throws TreatmentProcessCreationException
   */
  public Map[] verifyProcessCreation(Map process, Long accountId, Long typeId, String user, IReadOnlyDataProvider provider) throws TreatmentProcessCreationException;
  
  
  /**
   * This will create/query/provide the plan information needed for the transaction.
   * In most cases one would expect this to be a query to get the existing plan but
   * it could also create new plans or do just about anything. The returned Map MUST
   * contain @link ITreatmentProcessService#TREATMENT_PLAN_ID as a key that points to the
   * plan this process it to be under. Anything else returned is simply part of message 
   * passing from this process to the actual process creation/update.
   * @param process
   * @param accountId
   * @param date Used if request creates a new plan.
   * @param typeId 
   * @param user 
   * @param node TODO
   * @param t
   * @param p
   * @return
   * @throws SQLException
   * @throws AbortTransactionException
   */
  public Map getTreatmentPlanForProcess(Map process, Long accountId, Date date, Long typeId, String user, String node, ITransaction t, IReadWriteDataProvider p) throws SQLException, AbortTransactionException;
  
  public Map getTreatmentPlanForProcess(Map process, Long accountId, Date date, Long typeId, String user, String node, Long streamId, ITransaction t, IReadWriteDataProvider p) throws SQLException, AbortTransactionException;


  /**
   * Creates a new treatment process associated with the provided plan based on
   * the info in the process Map.
   * 
   * @param plan
   * @param process
   * @param accountId
   * @param date TODO
   * @param typeId
   * @param user
   * @param t
   * @param p
   * @return
   * @throws AbortTransact, ionExceptio
   * @throws SQLException nSQLException 
   */
  public Long createTreatmentProcess(Map plan, Map process, Long accountId, Date date, Long typeId, String user, ITransaction t, IReadWriteDataProvider p) throws AbortTransactionException, SQLException;


  public void updateTreatmentProcess(Long accountId, Map process, Date date, String user, ITransaction t, IReadWriteDataProvider p) throws SQLException, AbortTransactionException;


  public void verifyProcessForUpdate(Map process, Date date, String user, IReadWriteDataProvider p) throws TreatmentProcessUpdateException, SQLException;

  public void updateTreatmentPlan(Map plan, ITransaction t, IReadWriteDataProvider p) throws SQLException;
  
  public void processPostUpdate(Long accountId, Map process, Date date, String user, ITransaction t, IReadWriteDataProvider p) throws SQLException, AbortTransactionException;
}
