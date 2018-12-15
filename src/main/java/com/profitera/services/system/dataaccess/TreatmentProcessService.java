package com.profitera.services.system.dataaccess;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.descriptor.business.meta.ITreatmentProcess;
import com.profitera.server.ServiceEngine;
import com.profitera.services.system.SystemService;
import com.profitera.util.Strings;
import com.profitera.util.io.ForEachLine;
import com.profitera.util.reflect.Reflect;
import com.profitera.util.reflect.ReflectionException;
import com.profitera.util.xml.DocumentLoader;

public class TreatmentProcessService extends SystemService implements ITreatmentProcessService {
  private static final String PROCESSOR_CONFIG_FILE_PATH = "treatmentprocessservice.treatmentmanagerfilepath";
  private static final String DEFAULT_QUERY_PROCESSORS_FILE_NAME = "TreatmentProcessManagers.xml";
  private final Log log = LogFactory.getLog(TreatmentProcessService.class);
  private HashMap managers;
  //
  public TreatmentProcessService() {
	  managers = (HashMap) getManagerMap();
  }
  public ICreateTreatmentProcessTransaction[] createManualProcess(final Map process, final Long accountId, final Date date, final Long typeId, final String user) throws TreatmentProcessCreationException {
    return createProcess(process, accountId, null, date, typeId, true, user, null, null,true);
  }
  private ICreateTreatmentProcessTransaction[] createProcess(final Map process, final Long accountId, final Map updatePlan, final Date date, final Long typeId, boolean manual, final String user, final String nodeId, final Long streamId,final boolean createTreatmentProcess) throws TreatmentProcessCreationException {
    if (date == null)
      throw new IllegalArgumentException("Date for any treatment process creation/modification must not be null.");
    final ITreatmentProcessDataManager manager = getTypeManager(typeId);
    final IReadWriteDataProvider p = getReadWriteProvider();
    updateManualStatus(process, accountId, manual);
    final Map[] processesForCreation = manager.verifyProcessCreation(process, accountId, typeId, user, p);
    ICreateTreatmentProcessTransaction[] createTrans = new ICreateTreatmentProcessTransaction[processesForCreation.length];
    for(int i = 0; i < processesForCreation.length; i++){
      final Map proc = processesForCreation[i];
      createTrans[i] = new ICreateTreatmentProcessTransaction(){
        Long id;
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          Long processAccountId = accountId;
          if (processesForCreation.length >0 && proc.get("ACCOUNT_ID") != null){
            processAccountId = (Long) proc.get("ACCOUNT_ID");
          }
          Map plan = null;
          log.debug("Update plan: " + updatePlan);
          if (updatePlan != null){
            plan = updatePlan;
            manager.updateTreatmentPlan(plan, t, p);
          } else {
            plan = manager.getTreatmentPlanForProcess(proc, processAccountId, date, typeId, user, nodeId, streamId, t, p);
          }
          if (plan == null)
            throw new AbortTransactionException("No plan to assign to treatment process.");
          if (proc.get(ITreatmentProcess.TREATMENT_PROCESS_ID) == null){
            if(createTreatmentProcess) {
              id = manager.createTreatmentProcess(plan, proc, processAccountId, date, typeId, user, t, p);
              log.debug("Created process " + id);
            }
          } else {
            try {
              updateTreatmentProcess(proc, date, user).execute(t);
            } catch (TreatmentProcessUpdateException e) {
              new AbortTransactionException(e);
            }
          }
        }
        public Long getId() {
          return id;
        }
      };
    }
    return createTrans;
  }
  private void updateManualStatus(final Map process, final Long accountId,
      boolean manual) throws TreatmentProcessCreationException {
    if (manual){
      process.put(ITreatmentProcess.MANUAL, Boolean.TRUE);
      try {
        Map os = getAccountTreatmentProcessFinancialHistory(accountId);
        process.putAll(os);
      } catch (SQLException e) {
        throw new TreatmentProcessCreationException("Failed to retrieve account financial amounts", e);
      }
    } else {
      process.put(ITreatmentProcess.MANUAL, Boolean.FALSE);
    }
  }

  private Map getAccountTreatmentProcessFinancialHistory(Long accountId) throws SQLException {
    return (Map) getReadWriteProvider().queryObject("getAccountTreatmentProcessFinancialAmounts", accountId);
  }

  private ITreatmentProcessDataManager getTypeManager(Long typeId) {
    Map managers = getManagerMap();
    ITreatmentProcessDataManager m = (ITreatmentProcessDataManager) managers.get(typeId);
    if (m == null)
      m = (ITreatmentProcessDataManager) managers.get("default");
    if (m == null){
      m = new DefaultTreatmentProcessManager();
      m.configure("default", null);
      managers.put("default", m);
    }
    return m;
  }
  
  private Map getManagerMap() {
    if (managers == null) {
      managers = new HashMap();
      try {
        String path = ServiceEngine.getProp(PROCESSOR_CONFIG_FILE_PATH, DEFAULT_QUERY_PROCESSORS_FILE_NAME);
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
        if (inputStream == null) {
          log.error("Failed to load treatment process managers, file not found at path '" + path + "'");
          return managers;
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        final StringBuffer buffer = new StringBuffer();
        try {
          new ForEachLine(r){
            protected void process(String line) {
              buffer.append(line + "\n");
            }}.process();
            Document d = DocumentLoader.parseDocument(buffer.toString());
            Element root = d.getDocumentElement();
            NodeList nl = root.getChildNodes();
            for(int i=0; i< nl.getLength(); i++){
              Node n = nl.item(i);
              if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("manager")){
                Element q = (Element) n;
                String processType = q.getAttribute("processtypeid");
                ITreatmentProcessDataManager m = loadManager(processType, q);
                try {
                  managers.put(new Long(processType), m);  
                } catch (NumberFormatException e){
                  log.error("Invalid process type id ('" + processType + "') supplied for treatment process manager");          
                }
              } else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("default")){
                Element q = (Element) n;
                ITreatmentProcessDataManager m = loadManager("default", q);
                managers.put("default", m);
              }
            }
        } catch (Exception e) {
          log.fatal("Failed to load treatment process managers", e);
          throw new MissingResourceException("File '" + path + "' was empty or malformed, refusing to execute with missing configuration information.", this.getClass().getName(), PROCESSOR_CONFIG_FILE_PATH);
        }
      } catch (Throwable t){
        log.error("Failed to load treatment process managers", t);
      }
    }
    return managers;
  }
  private ITreatmentProcessDataManager loadManager(Object id, Element q) {
    String impl = q.getAttribute("implementation");
    if (impl == null){
      log.error("No implementation provided for treatment process manager for " + id);
      return null;
    }
    try {
      ITreatmentProcessDataManager m = (ITreatmentProcessDataManager) Reflect.invokeConstructor(impl, null, null);
      m.configure(id, q);
      return m;
    } catch(ClassCastException e){
      log.error("Implementation not of type " + ITreatmentProcessDataManager.class.getName() + "for treatment process manager for " + id);
      return null;
    } catch (ReflectionException e){
      log.error("Implementation '" + impl + "' could not be loaded for treatment process manager for " + id, e);
    }
    return null;
  }

  public IRunnableTransaction updateTreatmentProcess(final Map process, final Date date, final String user) throws TreatmentProcessUpdateException {
    List missing = new ArrayList();
    for (int i = 0; i < ITreatmentProcessService.TREATMENT_PROCESS_FIELDS.length; i++) {
      if (!TREATMENT_PROCESS_FIELDS[i].equals(ITreatmentProcess.TREATMENT_PLAN_ID) 
          && !process.containsKey(TREATMENT_PROCESS_FIELDS[i])){
        missing.add(TREATMENT_PROCESS_FIELDS[i]);
      }
    }
    if (missing.size() > 0) {
      throw new TreatmentProcessUpdateException("Process for update missing fields: " + Strings.getListString(missing, ", "));
    }
    Long typeId = (Long) process.get(ITreatmentProcess.PROCESS_TYPE_ID);
    final ITreatmentProcessDataManager manager = getTypeManager(typeId);
    if (manager == null){
      throw new TreatmentProcessUpdateException("Data Manager for process type could not be found: " + typeId);
    }
    final IReadWriteDataProvider p = getReadWriteProvider();
    Map curr;
    try {
      curr = (Map) p.queryObject("getTreatmentProcess", new Long(((Number) process.get(ITreatmentProcess.TREATMENT_PROCESS_ID)).longValue()));
      if (curr == null){
        throw new TreatmentProcessUpdateException("Treatment process to be updated could not be retrieved ("+ITreatmentProcess.TREATMENT_PROCESS_ID+": " + process.get(ITreatmentProcess.TREATMENT_PROCESS_ID) + ")");
      }
    } catch (SQLException e) {
      throw new TreatmentProcessUpdateException("Unable to retrieve process copy for history.", e);
    }
    try {
    	manager.verifyProcessForUpdate(process, date, user, p);
    } catch (SQLException e){
    	throw new TreatmentProcessUpdateException("Unable to verify process before update.", e);
    }
    Long aId = null;
    try {
    	aId = getAccountIdForProcess(curr.get(ITreatmentProcess.TREATMENT_PROCESS_ID));
      Map os = getAccountTreatmentProcessFinancialHistory(aId);
      process.putAll(os);
    } catch (SQLException e) {
      throw new TreatmentProcessUpdateException("Failed to retrieve account financial amounts", e);
    }
    final Long accountId = aId;
    final Map baseProcessForHistory = curr;
    return new IRunnableTransaction(){
      public void execute(ITransaction t) throws SQLException, AbortTransactionException {
        Long remarkHistId = (Long) p.insert("insertTreatmentProcessRemarksHistory", baseProcessForHistory, t);
        baseProcessForHistory.put("PROCESS_REMARKS_ID", remarkHistId);
        baseProcessForHistory.put("UPDATE_DATE", date);
        p.insert("insertTreatmentProcessHistory", baseProcessForHistory, t);
        manager.updateTreatmentProcess(accountId, process, date, user, t, p);
      }};
    
  }

  private Long getAccountIdForProcess(Object processId) throws SQLException {
    return (Long) getReadWriteProvider().queryObject("getTreatmentProcessAccountId", processId);
  }
  public ICreateTreatmentProcessTransaction[] createSystemProcess(Map process, Long accountId, Date date, Long typeId, String nodeId, Long streamId) throws TreatmentProcessCreationException {
    return createProcess(process, accountId, null, date, typeId, false, null, nodeId, streamId,true);
  }
  public ICreateTreatmentProcessTransaction[] createSystemProcess(Map process, Long accountId, Map updatedPlan, Date date, Long typeId,boolean createTreatmentProcess) throws TreatmentProcessCreationException {
    return createProcess(process, accountId, updatedPlan, date, typeId, false, null, null, null,createTreatmentProcess);
  }
  
/* (non-Javadoc)
 * @see com.profitera.services.system.dataaccess.ITreatmentProcessService#createSubProcess(java.util.Map, java.lang.Long, java.util.Date, java.lang.Long, java.lang.String)
 */
public IRunnableTransaction createSubProcess(final Map process, final Long accountId, final Date date, final Long typeId, final String user) throws TreatmentProcessCreationException {
	final IReadWriteDataProvider p = getReadWriteProvider();
  	
  	return new IRunnableTransaction(){
        
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
         p.insert("insertSubTreatmentProcess",process, t);
         log.debug("Created sub treatment process for type " + typeId);
        }
       };
}

	public IRunnableTransaction processPostUpdate(final Long accountId, final Map process, final Date date, final String user){
		final IReadWriteDataProvider p = getReadWriteProvider();
		Long typeId = (Long) process.get(ITreatmentProcess.PROCESS_TYPE_ID);
		final ITreatmentProcessDataManager manager = getTypeManager(typeId);
		return new IRunnableTransaction(){
			public void execute(ITransaction t) throws SQLException, AbortTransactionException {
				manager.processPostUpdate(accountId, process, date, user, t, p);
      }};
	}
}
