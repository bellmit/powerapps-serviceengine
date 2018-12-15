package com.profitera.services.business.batch;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.deployment.rmi.ListQueryServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.MapVerifyingMapCar;
import com.profitera.util.MapCar;

public class SupervisorAccountEvaluationBatch extends AbstractProviderBatch {
  private static String GET_SUPERVISOR_ACCOUNT_CONDITIONS = "getAllSupervisoryAccountsCondition";
  private static String GET_EXISTING_SUPERVISOR_ACCOUNTS = "getExistingSupervisoryAccounts";
  private static String GET_ALL_EXISTING_SUPERVISOR_ACCOUNTS = "getAllExistingSupervisoryAccounts";
  private static String GET_PAID_ACCOUNTS = "getAllPaidAccounts";
  private static String GET_ACCOUNT_MIA_3OR4_NO_TREATMENT_IN_3DAYS = "getAccountWithMIA3Or4AndNoTreatmentIn3Days";
  private static String GET_ACCOUNT_MIA_1OR2_NO_TREATMENT_IN_5DAYS = "getAccountWithMIA1Or2AndNoTreatmentIn5Days";
  private static String GET_BROKEN_PROMISE_ACCOUNTS = "getBrokenPromiseAccounts";
  private static String GET_OVERDRAFT_ACCOUNTS_WITH_2PTP_IN_ONE_MONTH = "getOverdraftAccountsWith2PTPInAMonth";
  private static String GET_ACCOUNTS_WITH_REVISED_PTP = "getAccountsWithRevisedPTP";
  private static String GET_ACCOUNTS_WITH_PTP_DURATION_GREATER_THAN_7DAYS = "getAccountsPTPDurationGreaterThan7Days";
  private static String GET_ACCOUNTS_WITH_NO_PTP_BY_21ST = "getAccountsWithNoPTPBy21st";
  private static String GET_ACCOUNTS_WITH_NO_FIELD_VISIT_IN_5DAYS = "getAccountsWithNoFieldVisitIn5Days";
  private static String GET_ACCOUNTS_WITH_NO_SITE_VISIT_IN_1DAY = "getAccountsWithNoSiteVisitIn1Day";
  private static String GET_ACCOUNTS_WITH_PTP_AND_PROGRESSED_SITE_VISIT = "getAccountsWithPTPAndProgressedSiteVisit";
  private static String UPDATE_SUPERVISOR_ACCOUNTS_LOG = "updateSupervisorAccountLog";
  private static String INSERT_SUPERVISOR_ACCOUNTS_LOG = "insertSupervisorAccountLog";
  private static final String COMMIT_SIZE = "commitsize";
  private static final int DEFAULT_COMMIT = 10;
  private static final Log log = LogFactory.getLog(SupervisorAccountEvaluationBatch.class);
  private boolean isFirstOfMonth = false;
  
  public SupervisorAccountEvaluationBatch(){
    addProperty(COMMIT_SIZE, Integer.class, DEFAULT_COMMIT + "", "Commit size", "Number of records to include in a database commit");
  }

  public TransferObject invoke() {
    TransferObject to = updatePaidAccounts(getEffectiveDate());
    if (to.isFailed()){
      return to;
    }
    return evaluateSupervisorAccounts(getEffectiveDate());
  }
  
  private TransferObject updatePaidAccounts(final Date effectiveDate){
    final IReadWriteDataProvider provider = getReadWriteProvider();
    // update paid accounts
    List trans = new ArrayList();
    try {
    	IRunnableTransaction rt = updatePaidSupervisorAccounts(effectiveDate);
        if (rt != null){
          trans.add(rt);
        }
    } catch (SQLException e) {
    	log.error("Error occurred when updating paid supervisor accounts : " + e.getMessage(), e);
    	return new TransferObject(TransferObject.ERROR, "Error occurred when updating paid supervisor accounts = " + e.getMessage());
    }

    if (trans.size() > 0){
      commitTransactionList(provider, trans);
    }
    return new TransferObject();
  }
  
  private TransferObject evaluateSupervisorAccounts(final Date effectiveDate){
    final int commitSize = getCommitSize();
    log.info("Batch Supervisor Account Evaluation commit size: " + commitSize);
    final IReadWriteDataProvider provider = getReadWriteProvider();
    setFirstOfMonth(effectiveDate);
    // evaluate supervisor accounts condition   
    Iterator i  = null;
    try {
      i = getReadWriteProvider().query(IReadOnlyDataProvider.STREAM_RESULTS, GET_SUPERVISOR_ACCOUNT_CONDITIONS, null);
    } catch (SQLException e) {
      log.fatal("Failed to execute query: " + GET_SUPERVISOR_ACCOUNT_CONDITIONS, e);
      return new TransferObject(TransferObject.ERROR, "QUERY_FAILED - " + GET_SUPERVISOR_ACCOUNT_CONDITIONS);
    }
    
    List transactions = new ArrayList();
    while(i.hasNext()){
      try {
        IRunnableTransaction rt = evaluateSupervisorAccountsCondition((Map)i.next());
        if (rt != null){
          transactions.add(rt);
        }
      } catch (SQLException e){
    	log.error("Error occurred when evaluating supervisor accounts : " + e.getMessage(), e);
    	return new TransferObject(TransferObject.ERROR, "Error occurred when evaluating supervisor accounts of condition code = " + e.getMessage());
      }
    	
      if (transactions.size() >= commitSize){
        commitTransactionList(provider, transactions);
      }
    }
    if (transactions.size() > 0){
      commitTransactionList(provider, transactions);
    }
    return new TransferObject();
  }
  
  private void commitTransactionList(final IReadWriteDataProvider provider, List transactions){
    try {
      provider.execute(new RunnableTransactionSet((IRunnableTransaction[]) transactions.toArray(new IRunnableTransaction[0])));
    } catch (AbortTransactionException e) {
      log.error("Failed to update supervisor accounts evaluation, transaction aborted", e);
    } catch (SQLException e) {
      log.error("Failed to update supervisor accounts evaluation, transaction failed", e);
    }
  }

  private int getCommitSize() {
    return ((Integer)getPropertyValue(COMMIT_SIZE)).intValue();
  }
  
  private RunnableTransactionSet updatePaidSupervisorAccounts(Date d) throws SQLException {
	List existingAccounts = getAllExistingCompliantAccounts();
    List paidAccounts = getAllPaidAccounts(d);
    List al = new ArrayList();
    final IReadWriteDataProvider p = getReadWriteProvider();
    
    // update exit date of accounts in existingAccounts that are in paidAccounts
    for (Iterator it = existingAccounts.iterator(); it.hasNext();){   		
      boolean bFound = false;
      final Map m = (Map)it.next();
      for (Iterator it2 = paidAccounts.iterator(); it2.hasNext();){
        Map m2 = (Map)it2.next();
        if (m.get("ACCOUNT_ID").equals(m2.get("ACCOUNT_ID"))){
          bFound = true;
          break;
        }
      }
      if(bFound){
        al.add(new IRunnableTransaction(){
          public void execute(ITransaction t) throws SQLException, AbortTransactionException {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.DATE, -1);
            m.put("EXIT_DATE", c.getTime());
            p.update(UPDATE_SUPERVISOR_ACCOUNTS_LOG, m, t);
          }
        });
      }
    }
    
	IRunnableTransaction[] trans = (IRunnableTransaction[]) al.toArray(new IRunnableTransaction[0]);
	return new RunnableTransactionSet(trans);
  }
  
  private List getAllPaidAccounts(Date d) throws SQLException {
    List res = executeQuery(GET_PAID_ACCOUNTS, d, new MapVerifyingMapCar(new String[]{"ACCOUNT_ID"}), getReadOnlyProvider());;
    return res;
  }
  
  private RunnableTransactionSet evaluateSupervisorAccountsCondition(final Map condition) throws SQLException {
    List existingAccounts = getExisitingCompliantAccounts((String)condition.get("CODE"));
    List allAccounts = getAllCompliantAccounts((String)condition.get("CODE"));
    List al = new ArrayList();
    final IReadWriteDataProvider p = getReadWriteProvider();
    
    // update exit date of accounts in existingAccounts that are not in allAccounts
   	if (isFirstOfMonth() || isDailyExitEvaluationRequired((String)condition.get("CODE"))){
      for (Iterator it = existingAccounts.iterator(); it.hasNext();){   		
        boolean bFound = false;
        final Map m = (Map)it.next();
        for (Iterator it2 = allAccounts.iterator(); it2.hasNext();){
          Map m2 = (Map)it2.next();
          if (m.get("ACCOUNT_ID").equals(m2.get("ACCOUNT_ID"))){
            bFound = true;
            break;
          }
        }
        if(!bFound){
          al.add(new IRunnableTransaction(){
            public void execute(ITransaction t) throws SQLException, AbortTransactionException {
              Calendar c = Calendar.getInstance();
              c.setTime(new Date());
              c.add(Calendar.DATE, -1);
              m.put("EXIT_DATE", c.getTime());
              p.update(UPDATE_SUPERVISOR_ACCOUNTS_LOG, m, t);
            }
          });
        }
      }
    }
    
    // insert into PTRSUPERVISOR_ACCOUNT_LOG for accounts in allAccounts that are not in existingAccounts
    for (Iterator it = allAccounts.iterator(); it.hasNext();){
      boolean bFound = false;
      final Map m = (Map)it.next();
      for (Iterator it2 = existingAccounts.iterator(); it2.hasNext();){
        Map m2 = (Map)it2.next();
        if (m.get("ACCOUNT_ID").equals(m2.get("ACCOUNT_ID"))){
          bFound = true;
          break;
        }
      }
      
      if(!bFound){
        al.add(new IRunnableTransaction(){
          public void execute(ITransaction t) throws SQLException, AbortTransactionException {
            Map map = new HashMap();
            map.put("ACCOUNT_ID", m.get("ACCOUNT_ID"));
            map.put("CONDITION_ID", condition.get("ID"));
            map.put("ENTRY_DATE", new Date());
            p.insert(INSERT_SUPERVISOR_ACCOUNTS_LOG, map, t);
          }
        });
      }
    }
		
	if (al.size()== 0) {
	  return null;
	}
	
	IRunnableTransaction[] trans = (IRunnableTransaction[]) al.toArray(new IRunnableTransaction[0]);
	return new RunnableTransactionSet(trans);
  }
  
  private List getExisitingCompliantAccounts(String sCode) throws SQLException {
    List res = executeQuery(GET_EXISTING_SUPERVISOR_ACCOUNTS, sCode, new MapVerifyingMapCar(new String[]{"ACCOUNT_ID"}), getReadOnlyProvider());;
    return res;
  }

  private List getAllExistingCompliantAccounts() throws SQLException {
    List res = executeQuery(GET_ALL_EXISTING_SUPERVISOR_ACCOUNTS, null, new MapVerifyingMapCar(new String[]{"ACCOUNT_ID"}), getReadOnlyProvider());;
    return res;
  }
  
  private List getAllCompliantAccounts(String sCode) throws SQLException {
	List res = new ArrayList();
	int iCode = 0;
	try {
	  iCode = Integer.parseInt(sCode);
	} catch(NumberFormatException nfe){
	  log.error("Invalid supervisor account condition code.");
	  throw nfe;
	}
	ListQueryServiceIntf lqs = getListQueryService();
	TransferObject to = null;
	switch (iCode){
	  case 1:
		to = lqs.getQueryList(GET_ACCOUNT_MIA_3OR4_NO_TREATMENT_IN_3DAYS, new HashMap());
		break;
	  case 2:
		to = lqs.getQueryList(GET_ACCOUNT_MIA_1OR2_NO_TREATMENT_IN_5DAYS, new HashMap());
		break;
	  case 3:
	    res = executeQuery(GET_BROKEN_PROMISE_ACCOUNTS, null, new MapVerifyingMapCar(new String[]{"ACCOUNT_ID"}), getReadOnlyProvider());
		break;
	  case 4:
	    res = executeQuery(GET_OVERDRAFT_ACCOUNTS_WITH_2PTP_IN_ONE_MONTH, null, new MapVerifyingMapCar(new String[]{"ACCOUNT_ID"}), getReadOnlyProvider());
		break;
	  case 5:
	    res = executeQuery(GET_ACCOUNTS_WITH_REVISED_PTP, null, new MapVerifyingMapCar(new String[]{"ACCOUNT_ID"}), getReadOnlyProvider());
		break;
	  case 6:
	    res = executeQuery(GET_ACCOUNTS_WITH_PTP_DURATION_GREATER_THAN_7DAYS, null, new MapVerifyingMapCar(new String[]{"ACCOUNT_ID"}), getReadOnlyProvider());
		break;
	  case 7:
	    res = executeQuery(GET_ACCOUNTS_WITH_NO_PTP_BY_21ST, null, new MapVerifyingMapCar(new String[]{"ACCOUNT_ID"}), getReadOnlyProvider());
		break;  
	  case 8:
	    res = executeQuery(GET_ACCOUNTS_WITH_NO_FIELD_VISIT_IN_5DAYS, new HashMap(), new MapVerifyingMapCar(new String[]{"ACCOUNT_ID"}), getReadOnlyProvider());
		break;
	  case 9:
	    res = executeQuery(GET_ACCOUNTS_WITH_NO_SITE_VISIT_IN_1DAY, new HashMap(), new MapVerifyingMapCar(new String[]{"ACCOUNT_ID"}), getReadOnlyProvider());
		break;
	  case 10:
	    res = executeQuery(GET_ACCOUNTS_WITH_PTP_AND_PROGRESSED_SITE_VISIT, null, new MapVerifyingMapCar(new String[]{"ACCOUNT_ID"}), getReadOnlyProvider());
		break;
	  default:
		log.info("No implementation for supervisor account condition code = " + iCode);
	    break;
	}
	
	if (to != null && !to.isFailed()) {
		res = (List) to.getBeanHolder();		
	}
	return res;
  }
	
  protected List executeQuery(String qName, Object args, MapCar car, IReadOnlyDataProvider p) throws SQLException {
	Iterator i = p.query(IReadWriteDataProvider.LIST_RESULTS, qName, args);
	List l = new ArrayList();
	MapCar.map(car, i, l);
	return l;
  }

  public boolean isFirstOfMonth() {
	return isFirstOfMonth;
  }

  public void setFirstOfMonth(Date d) {	  
	Calendar c = Calendar.getInstance();
	c.setTime(d);
	if (c.get(Calendar.DAY_OF_MONTH) == 1)
	  isFirstOfMonth = true; 
  }

  public boolean isDailyExitEvaluationRequired(String code) {
	return code.equals("1") || code.equals("2") || code.equals("3") || code.equals("8") || code.equals("9");
  }
  
	protected String getBatchDocumentation() {
		return "Batch program to evaluate accounts for Supervisor Tab";
	}

	protected String getBatchSummary() {
		return "This batch program evaluates all the accounts in the system to determine where it should go in the 'Supervisor Tab'";
	}
  
}
