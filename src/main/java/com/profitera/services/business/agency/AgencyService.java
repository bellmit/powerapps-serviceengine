package com.profitera.services.business.agency;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oracle.toplink.expressions.ExpressionBuilder;
import oracle.toplink.queryframework.ReadObjectQuery;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.deployment.rmi.AgencyServiceIntf;
import com.profitera.deployment.rmi.ListQueryServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.IContactNumber;
import com.profitera.descriptor.business.meta.ICustomer;
import com.profitera.descriptor.business.meta.ITreatmentProcess;
import com.profitera.descriptor.business.reference.TreatmentProcessTypeStatusRefBusinessBean;
import com.profitera.descriptor.business.treatment.workpad.reference.AgencyBean;
import com.profitera.descriptor.db.client.Agency;
import com.profitera.descriptor.db.reference.AgencyTypeRef;
import com.profitera.persistence.SessionManager;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.system.dataaccess.ICreateTreatmentProcessTransaction;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.ITreatmentProcessService;
import com.profitera.services.system.dataaccess.MapVerifyingMapCar;
import com.profitera.services.system.dataaccess.RPMDataManager;
import com.profitera.services.system.dataaccess.TreatmentProcessCreationException;
import com.profitera.services.system.dataaccess.TreatmentProcessUpdateException;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.ExtractMapValueMapCar;
import com.profitera.util.MapCar;
import com.profitera.util.TopLinkQuery;

/**
 * @author jamison
 */
public class AgencyService extends ProviderDrivenService implements
    AgencyServiceIntf {
  private static final String GET_AGENCY_CUSTOMERS = "getAgencyCustomers";
  private static final String DB_UPDATE_ERROR = "DB_UPDATE_ERROR";
  private static final MapVerifyingMapCar ID_VERIFIER = new MapVerifyingMapCar(
      new String[] { "ID"});
  private static final String AGENCY_TYPE_ID = "AGENCY_TYPE_ID";
  private static final MapVerifyingMapCar AGENCY_QUERY_VERIFIER = new MapVerifyingMapCar(
              new String[] { "ID", AGENCY_TYPE_ID });
  private static final Long OCA_AGENCY_TYPE_LONG = new Long(
            RPMDataManager.OCA_AGENCY_TYPE);
  private static final String ACCOUNT_ID = "ACCOUNT_ID";
  private static final MapVerifyingMapCar ACCOUNT_ID_VERIFIER = new MapVerifyingMapCar(new String[] { ACCOUNT_ID });
  private static final MapVerifyingMapCar TREATMENT_PROCESS_ID_VERIFIER = new MapVerifyingMapCar(new String[] { "TREATMENT_PROCESS_ID" });
  private static final String INVALID_TREATMENT_SUBTYPE = "INVALID_TREATMENT_SUBTYPE";
  private static final String INVALID_TREATMENT_STATUS = "INVALID_TREATMENT_STATUS";
  private static final Long OCA_PROCESS_TYPE_LONG = new Long(
      RPMDataManager.OUTSOURCE_AGENCY_TREATMENT_PROCESS);
  private static final String NO_SEARCH_CRITERIA_ERROR_CODE = "NO_SEARCH_CRITERIA";
  private static final String SEARCH_NON_AGENCY_ACCOUNTS = "searchNonAgencyAccounts";
  
  private static final ExtractMapValueMapCar EXTRACT_ACCOUNT_ID = new ExtractMapValueMapCar(ACCOUNT_ID);
  private static final String GET_ACCOUNT_IDS_EXPANDED_FOR_PROCESS_CREATION = "getAccountIdsExpandedForProcessCreation";
  private static final String GET_ACCOUNT_IDS_EXPANDED_FOR_PROCESS_UPDATE = "getAccountIdsExpandedForProcessUpdate";
  private static final String GET_OCA_TREATMENT_PROCESSES_BY_ACCOUNT_ID_AND_STATUS = "getOCATreatmentProcessesByAccountId";
  
  static Map statementTree = new HashMap();
  static {
    statementTree.put(GET_AGENCY_CUSTOMERS, new String[]{"getAgencyCustomerAddresses", "getAgencyCustomerAccounts", "getAgencyContactNumbers"});
    statementTree.put("getAgencyCustomerAccounts", new String[]{"getAgencyChildAccounts", "getAgencyAccountTransactions", "getAgencyAccountInvoices", "getAgencyAccountActions"});
  }

  /**
   * @see com.profitera.deployment.rmi.AgencyServiceIntf#getAssignedCustomerDetails(com.profitera.descriptor.business.treatment.workpad.reference.AgencyBean)
   */
  public TransferObject getAssignedCustomerDetails(AgencyBean agencyBean) {
    List details = new ArrayList();
    try {
      IReadOnlyDataProvider p = getReadOnlyProvider();
      Iterator i = p.query(IReadOnlyDataProvider.STREAM_RESULTS, GET_AGENCY_CUSTOMERS, new Long(agencyBean.getAgencyId().longValue()));
      while (i.hasNext()){
	      Map customer = (Map) i.next();
	      String[] statements = (String[]) statementTree.get(GET_AGENCY_CUSTOMERS);
	      fillDataTree(customer, statements, p);
	      details.add(customer);
      }
    } catch (SQLException e) {
      return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
    return new TransferObject(details);
  }

  private void fillDataTree(Map root, String[] statements, IReadOnlyDataProvider p) throws TransferObjectException {
    if (statements == null) return;
    for (int i = 0; i < statements.length; i++) {
      String[] subStatements = (String[]) statementTree.get(statements[i]);
      List l = executeListQuery(IReadOnlyDataProvider.LIST_RESULTS, statements[i], root, new MapVerifyingMapCar(new String[]{}), p);
      root.put(statements[i], l);
      for (Iterator iter = l.iterator(); iter.hasNext();) {
        Map data = (Map) iter.next();
        //deriveResultMap(statements[i], data);
        fillDataTree(data, subStatements, p);
      }
    }
    
  }
/*
  private void deriveResultMap(String statement, Map m) {
    List colList = new ArrayList();
    List typeList = new ArrayList();
    int index = 0;
    for (Iterator iter = m.entrySet().iterator(); iter.hasNext(); index++) {
      Map.Entry e = (Map.Entry) iter.next();
      String key = (String) e.getKey();
      Object v = e.getValue();
      if (v != null){
        colList.add(key.toUpperCase());
        if (v.getClass().equals(BigDecimal.class)){
          typeList.add(Double.class);
        } else if (v.getClass().equals(BigInteger.class)){
          typeList.add(Long.class);
        } else {        
          typeList.add(v.getClass());
        }
      }
    }
    String[] cols = (String[]) colList.toArray(new String[0]);
    Class[] types = (Class[]) typeList.toArray(new Class[0]);
    log.debug(new SQLMapFileRenderer().renderResultMap(statement+"-map", HashMap.class, cols, cols, types));
  }
*/

  private List getUnassignedAgencyCustomer(AgencyBean agencyBean, Date since) throws TransferObjectException {
    Long agencyId = new Long(agencyBean.getAgencyId().longValue());
    Map args = new HashMap();
    args.put("AGENCY_ID", agencyId);
    args.put("ABORTED_SINCE", since);
    return executeListQuery(IReadOnlyDataProvider.LIST_RESULTS, "getAgencyCustomersAbortedSince", args, new MapVerifyingMapCar(new String[]{ICustomer.CUSTOMER_ID}), getReadOnlyProvider());
  }

  /**
   * @see com.profitera.deployment.rmi.AgencyServiceIntf#getUnassignedCustomerIds(com.profitera.descriptor.business.treatment.workpad.reference.AgencyBean,
   *      java.util.Date)
   */
  public TransferObject getUnassignedCustomerIds(AgencyBean agencyBean,
      Date since) {
    try {
      return new TransferObject(getUnassignedAgencyCustomer(agencyBean, since));
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
  }


  public final TransferObject getAgency(Double agencyId) {
    if (agencyId == null)
      return new TransferObject(null);
    ReadObjectQuery query = new ReadObjectQuery(Agency.class,
        new ExpressionBuilder().get(Agency.AGENCY_ID).equal(agencyId));
    query.setName("Get Agency by Agency Id");
    Agency agency = (Agency) TopLinkQuery.asObject(query, SessionManager
        .getClientSession());
    if (agency != null) {
      AgencyBean bean = new AgencyBean();
      bean.setAgencyId(agency.getAgencyId());
      bean.setAgencyName(agency.getAgencyName());
      bean.setDescription(agency.getAgencyDesc());
      AgencyTypeRef ref = agency.getAgencyTypeRef();
      if (ref != null)
        bean.setAgencyTypeCode(ref.getAgyTypeCode());
      return new TransferObject(bean);
    }
    return new TransferObject(null);
  }
  
  protected ListQueryServiceIntf getListQueryService() {
    return (ListQueryServiceIntf) LookupManager.getInstance().getLookup(LookupManager.BUSINESS).getService("ListQueryService");
  }
  
  
  public TransferObject searchNonAgencyAccounts(Map criteria) {
    if (criteria == null || criteria.isEmpty()) {
      return new TransferObject(TransferObject.ERROR,
          NO_SEARCH_CRITERIA_ERROR_CODE);
    }
    IReadOnlyDataProvider provider = getReadOnlyProvider();
    try {
      List accountIds = this.executeListQuery(
          IReadOnlyDataProvider.LIST_RESULTS, SEARCH_NON_AGENCY_ACCOUNTS,
          criteria, new MapVerifyingMapCar(new String[] { ACCOUNT_ID }),
          provider);
      if (accountIds == null || accountIds.size() == 0) {
        return new TransferObject(Collections.EMPTY_LIST);
      }
      accountIds = MapCar.map(EXTRACT_ACCOUNT_ID, accountIds);
      return expandAccountsForDisplay((Long[]) accountIds.toArray(new Long[0]));
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
  }

  public TransferObject getAccountProcessCreationList(Long[] createAccountIds) {
    // TODO Under what exact conditions should we create a process for an
    // account?
    IReadOnlyDataProvider provider = getReadOnlyProvider();
    try {
      Map m = new HashMap();
      m.put("ACCOUNT_ID_LIST", createAccountIds);
      List accountIds = this.executeListQuery(
          IReadOnlyDataProvider.LIST_RESULTS,
          GET_ACCOUNT_IDS_EXPANDED_FOR_PROCESS_CREATION, m,
          new MapVerifyingMapCar(new String[] { ACCOUNT_ID }), provider);
      if (accountIds == null || accountIds.size() == 0) {
        return new TransferObject(Collections.EMPTY_LIST);
      }
      accountIds = MapCar.map(EXTRACT_ACCOUNT_ID, accountIds);
      Long[] accountIdsToQuery = (Long[]) accountIds.toArray(new Long[0]);
      return expandAccountsForDisplay(accountIdsToQuery);
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
  }
  
  private TransferObject expandAccountsForDisplay(Long[] accountIdsToQuery) {
    if (accountIdsToQuery == null || accountIdsToQuery.length == 0){
      return new TransferObject(Collections.EMPTY_LIST);
    }
    List l = Arrays.asList(accountIdsToQuery);
    l = new ArrayList(l);
    Map m = new HashMap();
    // ACCOUNTS is bad but we leave it in the backwards compat, ACCOUNT_ID_LIST is the future!
    m.put("ACCOUNTS", l);
    m.put("ACCOUNT_ID_LIST", l);
    return getListQueryService().getQueryList("getExpandedAgencyInformationDisplay", m);  
  }

  public TransferObject createAgencyProcesses(Map treatmentSubtype,
      Map treatmentTypeStatus, String user, Long[] accountIds) {
    try {
      Boolean disabled = (Boolean) (treatmentSubtype == null ? Boolean.TRUE
          : treatmentSubtype.get("DISABLE"));
      if (disabled != null && disabled.booleanValue()) {
        return new TransferObject(TransferObject.ERROR,
            INVALID_TREATMENT_SUBTYPE);
      }
      disabled = (Boolean) (treatmentTypeStatus == null ? Boolean.TRUE
          : treatmentTypeStatus.get("DISABLE"));
      if (disabled != null && disabled.booleanValue()) {
        return new TransferObject(TransferObject.ERROR,
            "INVALID_TREATMENT_TYPE_STATUS");
      }
      List accountPlanIds = getAccountTreatmentPlanIds(accountIds);
      log.debug("Will create agency processes of type '"
          + treatmentSubtype.get("DESCRIPTION") + "' with type status '"
          + treatmentTypeStatus.get("DESCRIPTION") + "'");
      Long subtypeId = (Long) treatmentSubtype.get("ID");
      Long typeId = (Long) treatmentSubtype.get("TYPE_ID");
      Long typeStatusId = (Long) treatmentTypeStatus.get("ID");
      Long statusId = (Long) treatmentTypeStatus.get("STATUS_ID");
      List transactions = new ArrayList();
      ITreatmentProcessService processService = getTreatmentProcessService();
      Date date = new Date();
      for (Iterator i = accountPlanIds.iterator(); i.hasNext();) {
        Map plan = (Map) i.next();
        plan.put(ITreatmentProcess.PROCESS_SUBTYPE_ID, subtypeId);
        plan.put(ITreatmentProcess.PROCESS_TYPE_ID, typeId);
        plan.put(ITreatmentProcess.PROCESS_TYPE_STATUS_ID, typeStatusId);
        plan.put(ITreatmentProcess.PROCESS_STATUS_ID, statusId);
        IRunnableTransaction t[] = processService.createManualProcess(plan, (Long) plan
            .get(ITreatmentProcessService.ACCOUNT_ID), date, typeId, user);
        transactions.addAll(Arrays.asList(t));
        if (transactions.size() >= getCommitSize()){
          IRunnableTransaction[] tr = (IRunnableTransaction[]) transactions
          .toArray(new IRunnableTransaction[0]);
          getReadWriteProvider().execute(new RunnableTransactionSet(tr));
          transactions.clear();
        }
      }
      IRunnableTransaction[] tr = (IRunnableTransaction[]) transactions
      .toArray(new IRunnableTransaction[0]);
      getReadWriteProvider().execute(new RunnableTransactionSet(tr));
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    } catch (TreatmentProcessCreationException e) {
      return new TransferObject(TransferObject.ERROR, e.getErrorCode());
    } catch (AbortTransactionException e) {
      sqlFailure(null, "Treatment Process Creation", null, e);
      return getExceptionTransferObject(e);
    } catch (SQLException e) {
      sqlFailure(null, "Treatment Process Creation", null, e);
      return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
    }
    return new TransferObject();
  }

  private int getCommitSize() {
    return ServiceEngine.getIntProp("agencyservice.commitsize", 100);
  }

  private ITreatmentProcessService getTreatmentProcessService() {
    ITreatmentProcessService processService = (ITreatmentProcessService) lookup
        .getLookupItem(LookupManager.SYSTEM, "TreatmentProcessService");
    return processService;
  }

  private List getAccountTreatmentPlanIds(Long[] accountIds)
      throws TransferObjectException {
    return executeListQuery(IReadOnlyDataProvider.LIST_RESULTS,
        "getAccountTreatmentPlanIds", Arrays.asList(accountIds),
        new MapVerifyingMapCar(new String[] {
            ITreatmentProcessService.ACCOUNT_ID,
            ITreatmentProcess.TREATMENT_PLAN_ID }),
        getReadWriteProvider());
  }

  public TransferObject getAccountsPendingAssignment(Map treatmentSubtype) {
    if (treatmentSubtype == null) {
      return new TransferObject(TransferObject.ERROR,
          INVALID_TREATMENT_SUBTYPE);
    }
    Long subtypeId = (Long) treatmentSubtype.get("ID");
    Long typeId = (Long) treatmentSubtype.get("TYPE_ID");
    String queryName = null;
    if (typeId.longValue() == RPMDataManager.OUTSOURCE_AGENCY_TREATMENT_PROCESS) {
      queryName = "getUnassignedOutsourceAgencyAccountsBySubtype";
    } else {
      return new TransferObject(TransferObject.ERROR,
          INVALID_TREATMENT_SUBTYPE);
    }
    try {
      List l = executeListQuery(IReadOnlyDataProvider.LIST_RESULTS, queryName, subtypeId, ACCOUNT_ID_VERIFIER, getReadWriteProvider());
      return expandAccountsForDisplay((Long[]) MapCar.map(EXTRACT_ACCOUNT_ID, l).toArray(new Long[0]));
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
  }

  private class MapDefaultValueMapCar extends MapCar {
    private final Object key;
    private final Object defaultValue;

    public MapDefaultValueMapCar(Object key, Object defaultValue) {
      this.key = key;
      this.defaultValue = defaultValue;
    }

    public Object map(Object o) {
      Map m = (Map) o;
      if (m.get(key) == null)
        m.put(key, defaultValue);
      return m;
    }
  };

  public TransferObject getSubtypeAgenciesAndStatuses() {
    final Long inprogress = new Long(
        TreatmentProcessTypeStatusRefBusinessBean.IN_PROGRESS_TREATMENT_PROCESS_STATUS
            .longValue());
    try {
      List ocaSubtypes = getTreatmentProcessSubtypes(OCA_PROCESS_TYPE_LONG);
      //
      MapDefaultValueMapCar defaulter = new MapDefaultValueMapCar("DISABLE",
          Boolean.FALSE);
      List ocaTypeStatii = MapCar.map(defaulter, getTreatmentProcessTypeStatii(
          OCA_PROCESS_TYPE_LONG, inprogress));
      List ocaAgencies = getAgenciesByType(OCA_AGENCY_TYPE_LONG);
      for (Iterator i = ocaSubtypes.iterator(); i.hasNext();) {
        Map m = (Map) i.next();
        m.put("AGENCY_LIST", ocaAgencies);
        m.put("TYPE_STATUS_LIST", ocaTypeStatii);
      }
      List l = new ArrayList(ocaSubtypes.size());
      l.addAll(ocaSubtypes);
      return new TransferObject(l);
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
  }
  
  private List getTreatmentProcessTypeStatii(Long processType, Long[] statii) throws TransferObjectException {
    List typeStatuses = new ArrayList();
    for (int i = 0; i < statii.length; i++) {
      typeStatuses.addAll(getTreatmentProcessTypeStatii(processType, statii[i]));
    }
    return typeStatuses;
  }

  private List getTreatmentProcessTypeStatii(Long processType, Long status)
      throws TransferObjectException {
    Map m = new HashMap();
    m.put("TYPE_ID", processType);
    m.put("STATUS_ID", status);
    return executeListQuery(IReadOnlyDataProvider.LIST_RESULTS,
        "getTypeStatusByTypeAndStatus", m, ID_VERIFIER, getReadWriteProvider());
  }

  private List getAgenciesByType(Long typeId) throws TransferObjectException {
    Map m = new HashMap();
    m.put("AGENCY_TYPE_ID", typeId);
    return executeListQuery(IReadOnlyDataProvider.LIST_RESULTS,
        "getAgenciesByTypeId", m, AGENCY_QUERY_VERIFIER, getReadWriteProvider());
  }

  private List getTreatmentProcessSubtypes(Long typeId)
      throws TransferObjectException {
    return executeListQuery(IReadOnlyDataProvider.LIST_RESULTS,
        "getTreatmentProcessSubtypes", typeId, new MapVerifyingMapCar(
            new String[] { "CODE" }), getReadWriteProvider());
  }

  public TransferObject getAccountProcessUpdateList(Long[] updateAccountIds) {
    IReadOnlyDataProvider provider = getReadOnlyProvider();
    try {
      Map m = new HashMap();
      m.put("ACCOUNT_ID_LIST", updateAccountIds);
      m.put("PROCESS_TYPE_LIST", new Long[] { OCA_PROCESS_TYPE_LONG });
      List accountIds = this.executeListQuery(
          IReadOnlyDataProvider.LIST_RESULTS,
          GET_ACCOUNT_IDS_EXPANDED_FOR_PROCESS_UPDATE, m,
          new MapVerifyingMapCar(new String[] { ACCOUNT_ID }), provider);
      if (accountIds == null || accountIds.size() == 0) {
        return new TransferObject(Collections.EMPTY_LIST);
      }
      accountIds = MapCar.map(EXTRACT_ACCOUNT_ID, accountIds);
      Long[] accountIdsToQuery = (Long[]) accountIds.toArray(new Long[0]);
      return expandAccountsForDisplay(accountIdsToQuery);
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
  }

  public TransferObject assignAgencyToProcesses(Long[] accountIds, Map subtype,
      Map status, Map agency, Date recallDate, String user) {
    if (subtype == null)
      return new TransferObject(TransferObject.ERROR, INVALID_TREATMENT_SUBTYPE);
    Long subtypeId = (Long) subtype.get("ID");
    Long typeId = (Long) subtype.get("TYPE_ID");
    if (subtypeId == null || typeId == null){
      return new TransferObject(TransferObject.ERROR, INVALID_TREATMENT_SUBTYPE);
    }
    if (status == null){
      return new TransferObject(TransferObject.ERROR, INVALID_TREATMENT_STATUS);
    }
    Long typeStatusId = (Long) status.get("ID");
    if (typeStatusId == null){
      return new TransferObject(TransferObject.ERROR, INVALID_TREATMENT_STATUS);
    }
    Long agencyId = agency == null ? null : (Long) agency.get("ID");
    try {
      IReadWriteDataProvider provider = getReadWriteProvider();
      Map arguments = new HashMap();
      arguments
          .put(
              "STATUS_ID",
              new Long(
                  TreatmentProcessTypeStatusRefBusinessBean.IN_PROGRESS_TREATMENT_PROCESS_STATUS
                      .longValue()));
      arguments.put("ACCOUNT_ID_LIST", accountIds);
      List processes = Collections.EMPTY_LIST;
      processes = getTreatmentProcessesByAccountId(typeId, arguments, provider);
      List transactions = new ArrayList();
      ITreatmentProcessService treatmentProcessService = getTreatmentProcessService();
      Date d = new Date();
      for (Iterator i = processes.iterator(); i.hasNext();) {
        Map element = (Map) i.next();
        element.put(ITreatmentProcess.EXPECTED_END_DATE, recallDate);
        element.put(ITreatmentProcess.ACTUAL_START_DATE, d);
        element.put("AGENCY_ID", agencyId);
        transactions.add(treatmentProcessService.updateTreatmentProcess(element, d, user));
        if (transactions.size() >= getCommitSize()){
          IRunnableTransaction[] tr = (IRunnableTransaction[]) transactions
          .toArray(new IRunnableTransaction[0]);
          getReadWriteProvider().execute(new RunnableTransactionSet(tr));
          transactions.clear();
        }
      }
      IRunnableTransaction[] t = (IRunnableTransaction[]) transactions.toArray(new IRunnableTransaction[0]);
      provider.execute(new RunnableTransactionSet(t));
      return new TransferObject();
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    } catch (TreatmentProcessUpdateException e) {
      log.error("Failed to update treatment processes", e);
    } catch (AbortTransactionException e) {
      log.error("Failed to update treatment processes", e);
    } catch (SQLException e) {
      log.error("Failed to update treatment processes", e);
    }
    return new TransferObject(TransferObject.EXCEPTION, DB_UPDATE_ERROR);
    
  }

  private List getTreatmentProcessesByAccountId(Long typeId, Map arguments, IReadWriteDataProvider provider) throws TransferObjectException {
    List processes;
    String qName = null;
    if (typeId.equals(OCA_PROCESS_TYPE_LONG)) {
      qName = GET_OCA_TREATMENT_PROCESSES_BY_ACCOUNT_ID_AND_STATUS;
    } else {
      throw new TransferObjectException(new TransferObject(TransferObject.ERROR, INVALID_TREATMENT_SUBTYPE));
    }
    processes = this.executeListQuery(IReadOnlyDataProvider.LIST_RESULTS,
        qName, arguments, TREATMENT_PROCESS_ID_VERIFIER, provider);
    return processes;
  }

  public TransferObject getAgencyAccounts(Map agency) {
    try {
      List accountIds = executeListQuery(IReadOnlyDataProvider.LIST_RESULTS, "getAgencyAccountIds", agency.get("ID"), ACCOUNT_ID_VERIFIER, getReadWriteProvider());
      accountIds = MapCar.map(EXTRACT_ACCOUNT_ID, accountIds);
      return expandAccountsForDisplay((Long[]) accountIds.toArray(new Long[0]));
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
  }

  public TransferObject getAgencies() {
    Long[] status = new Long[]{
        new Long(TreatmentProcessTypeStatusRefBusinessBean.UNSUCCESSFUL_TREATMENT_PROCESS_STATUS.longValue()),
        new Long(TreatmentProcessTypeStatusRefBusinessBean.SUCCESSFUL_TREATMENT_PROCESS_STATUS.longValue()),
        new Long(TreatmentProcessTypeStatusRefBusinessBean.CANCEL_TREATMENT_PROCESS_STATUS.longValue())
    };
    try {
      List l = new ArrayList();
      List ocaAgencies = getOcaAgencies();
      List ocaStatii = getTreatmentProcessTypeStatii(OCA_PROCESS_TYPE_LONG, status);
      for (Iterator i = ocaAgencies.iterator(); i.hasNext();) {
        Map agency = (Map) i.next();
        agency.put("TYPE_STATUS_LIST", ocaStatii);
      }
      l.addAll(ocaAgencies);
      return new TransferObject(l);
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
  }

  private List getOcaAgencies() throws TransferObjectException {
    List ocaAgencies = getAgenciesByType(OCA_AGENCY_TYPE_LONG);
    return ocaAgencies;
  }

  public TransferObject abortAgencyProcesses(Long[] accountIds, Map agency, Map typeStatus, String userId) {
    if (agency == null){
      return new TransferObject(TransferObject.ERROR, "NO_AGENCY_SUPPLIED");
    }
    if (typeStatus == null){
      return new TransferObject(TransferObject.ERROR, "NO_STATUS_SUPPLIED");
    }
    Long agencyId = (Long) agency.get("ID");
    Long processTypeId = (Long) typeStatus.get("TYPE_ID");
    Long statusId = new Long(TreatmentProcessTypeStatusRefBusinessBean.IN_PROGRESS_TREATMENT_PROCESS_STATUS.longValue());    
    try {
      List processes = getAgencyProcessesForAccounts(accountIds, processTypeId, agencyId, statusId);
      ITreatmentProcessService processService = getTreatmentProcessService();
      List transactions = new ArrayList();
      Date date = new Date();
      Long status = (Long) typeStatus.get("STATUS_ID");
      Long typeStatusId = (Long) typeStatus.get("ID");
      for (Iterator i = processes.iterator(); i.hasNext();) {
        Map process = (Map) i.next();
        process.put(ITreatmentProcess.ACTUAL_END_DATE, date);
        process.put(ITreatmentProcess.PROCESS_STATUS_ID, status);
        process.put(ITreatmentProcess.PROCESS_TYPE_STATUS_ID, typeStatusId);
        // Do this rather than using a special select for agencies, I know the agency
        // so just set it here.
        //TODO: Get agencyId into ptrtreatment process.
        process.put("AGENCY_ID", agencyId);
        try {
          IRunnableTransaction t = processService.updateTreatmentProcess(process, date, userId);
          transactions.add(t);
          if (transactions.size() >= getCommitSize()){
            IRunnableTransaction[] tr = (IRunnableTransaction[]) transactions
            .toArray(new IRunnableTransaction[0]);
            getReadWriteProvider().execute(new RunnableTransactionSet(tr));
            transactions.clear();
          }
        } catch (TreatmentProcessUpdateException e) {
          log.error("Failed to update processes for abort", e);
          return new TransferObject(TransferObject.EXCEPTION, DB_UPDATE_ERROR);
        }
      }
      IRunnableTransaction[] trans = (IRunnableTransaction[]) transactions.toArray(new IRunnableTransaction[0]);
      getReadWriteProvider().execute(new RunnableTransactionSet(trans));
      return new TransferObject();
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    } catch (AbortTransactionException e) {
      log.error("Failed to update processes for abort", e);
      return new TransferObject(TransferObject.EXCEPTION, DB_UPDATE_ERROR);
    } catch (SQLException e) {
      log.error("Failed to update processes for abort", e);
      return new TransferObject(TransferObject.EXCEPTION, DB_UPDATE_ERROR);
    }
  }

  private List getAgencyProcessesForAccounts(Long[] accountIds, Long processTypeId, Long agencyId, Long statusId) throws TransferObjectException {
    Map m = new HashMap();
    m.put("ACCOUNT_ID_LIST", accountIds);
    m.put("PROCESS_TYPE_ID", processTypeId);
    m.put("STATUS_ID", statusId);
    m.put("AGENCY_ID", agencyId);
    return getTreatmentProcessesByAccountId(processTypeId, m, getReadWriteProvider());
  }

  public TransferObject cancelAgencyProcesses(Long[] accountIds, Map subtype, Map agency, final String userId) {
    if (subtype == null)
      return new TransferObject(TransferObject.ERROR, INVALID_TREATMENT_SUBTYPE);
    Long subtypeId = (Long) subtype.get("ID");
    Long typeId = (Long) subtype.get("TYPE_ID");
    if (subtypeId == null || typeId == null){
      return new TransferObject(TransferObject.ERROR, INVALID_TREATMENT_SUBTYPE);
    }
    Long cancelled = new Long(TreatmentProcessTypeStatusRefBusinessBean.CANCEL_TREATMENT_PROCESS_STATUS.longValue());
    Long inprogress = new Long(TreatmentProcessTypeStatusRefBusinessBean.IN_PROGRESS_TREATMENT_PROCESS_STATUS.longValue());
    Map typeStatus = null;
    List statuses;
    try {
      statuses = getTreatmentProcessTypeStatii(typeId, cancelled);
      if (statuses.size() > 0)
        typeStatus = (Map) statuses.get(0);
    } catch (TransferObjectException e1) {
      log.error("Unable to retrieve default cancelled status", e1);
    } 
    if (typeStatus == null){
      return new TransferObject(TransferObject.ERROR, INVALID_TREATMENT_STATUS);
    }
    Long typeStatusId = (Long) typeStatus.get("ID");
    if (typeStatusId == null){
      return new TransferObject(TransferObject.ERROR, INVALID_TREATMENT_STATUS);
    }
    Long agencyId = (Long) (agency == null ? null : agency.get("ID"));
    try {
      IReadWriteDataProvider provider = getReadWriteProvider();
      Map arguments = new HashMap();
      arguments.put("STATUS_ID", inprogress);
      arguments.put("ACCOUNT_ID_LIST", accountIds);
      if (agencyId != null){
        arguments.put("AGENCY_ID", agencyId);  
      }
      List processes = Collections.EMPTY_LIST;
      processes = getTreatmentProcessesByAccountId(typeId, arguments, provider);
      List transactions = new ArrayList();
      ITreatmentProcessService treatmentProcessService = getTreatmentProcessService();
      Date d = new Date();
      for (Iterator i = processes.iterator(); i.hasNext();) {
        Map element = (Map) i.next();
        // If the process was never started then it is started and stopped
        // at the same time, get what I mean?
        if (element.get(ITreatmentProcess.ACTUAL_START_DATE) == null){
          element.put(ITreatmentProcess.ACTUAL_START_DATE, d);
        }
        element.put(ITreatmentProcess.ACTUAL_END_DATE, d);
        element.put(ITreatmentProcess.PROCESS_STATUS_ID, cancelled);
        element.put(ITreatmentProcess.PROCESS_TYPE_STATUS_ID, typeStatusId);
        // Make sure we don't lose the agency ID
        if (agencyId != null){
          element.put("AGENCY_ID", agencyId);
        }
        transactions.add(treatmentProcessService.updateTreatmentProcess(element, d, userId));
      }
      IRunnableTransaction[] t = (IRunnableTransaction[]) transactions.toArray(new IRunnableTransaction[0]);
      provider.execute(new RunnableTransactionSet(t));
      return new TransferObject();
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    } catch (TreatmentProcessUpdateException e) {
      log.error("Failed to update treatment processes", e);
    } catch (AbortTransactionException e) {
      log.error("Failed to update treatment processes", e);
    } catch (SQLException e) {
      log.error("Failed to update treatment processes", e);
    }
    return new TransferObject(TransferObject.EXCEPTION, DB_UPDATE_ERROR);
  }

  public TransferObject createTreatmentProcess(Map bean, String userId){
	  Long subTypeId = (Long)bean.get(ITreatmentProcess.PROCESS_SUBTYPE_ID);
	  Long typeId = (Long)bean.get(ITreatmentProcess.PROCESS_TYPE_ID);
      if (subTypeId == null || typeId == null){
	        return new TransferObject(TransferObject.ERROR, INVALID_TREATMENT_SUBTYPE);
      }
      Long successful = new Long(TreatmentProcessTypeStatusRefBusinessBean.SUCCESSFUL_TREATMENT_PROCESS_STATUS.longValue());
      Map typeStatus = null;
	  List statuses;
	  try {
		  statuses = getTreatmentProcessTypeStatii(typeId, successful);
	      if (statuses.size() > 0)
	          typeStatus = (Map) statuses.get(0);
	  } catch (TransferObjectException e1) {
		  log.error("Unable to retrieve default successful status", e1);
	  } 
	  
	  if (typeStatus == null){
		  return new TransferObject(TransferObject.ERROR, INVALID_TREATMENT_STATUS);
	  }
	  
	  Long typeStatusId = (Long) typeStatus.get("ID");
	  if (typeStatusId == null){
		  return new TransferObject(TransferObject.ERROR, INVALID_TREATMENT_STATUS);
	  }
	  Long accountId = (Long)bean.get(ITreatmentProcessService.ACCOUNT_ID);
	  try {
		List accountPlanIds = getAccountTreatmentPlanIds(new Long[] {accountId});
		Map accountPlanMap = null;
		if (accountPlanIds != null && accountPlanIds.size() > 0){
			accountPlanMap = (Map) accountPlanIds.get(0);
		}
		if (accountPlanMap == null){
			return new TransferObject(TransferObject.ERROR, "No open treatment plan found for account id = " + accountId);
		}
		
        IReadWriteDataProvider provider = getReadWriteProvider();
        ITreatmentProcessService treatmentProcessService = getTreatmentProcessService();
        Date d = new Date();
        if (bean.get(ITreatmentProcess.ACTUAL_END_DATE) == null){
        	bean.put(ITreatmentProcess.ACTUAL_END_DATE, d);
        }
        if (bean.get(ITreatmentProcess.ACTUAL_START_DATE) == null){
        	bean.put(ITreatmentProcess.ACTUAL_START_DATE, bean.get(ITreatmentProcess.ACTUAL_END_DATE));
        }
        bean.put(ITreatmentProcess.PROCESS_STATUS_ID, successful);
        bean.put(ITreatmentProcess.PROCESS_TYPE_STATUS_ID, typeStatusId);
        ICreateTreatmentProcessTransaction[] trans = treatmentProcessService.createManualProcess(bean, (Long) accountPlanMap.get(ITreatmentProcessService.ACCOUNT_ID), d, typeId, userId);
        provider.execute(new RunnableTransactionSet(trans));
        Long newTreatmentProcessId = trans[0].getId();
        // retrieve the newly inserted record
        Map processMap = null;
        try {
            processMap = (Map) provider.queryObject("getTreatmentProcess", newTreatmentProcessId);
          } catch (SQLException e1) {
            return returnFailWithTrace("Select failed", "getTreatmentProcess", "select", newTreatmentProcessId, e1);
          }
        bean.putAll(processMap);
        return new TransferObject(bean);
      } catch (TransferObjectException e) {
        return e.getTransferObject();
      } catch (TreatmentProcessCreationException e) {
    	  log.error("Failed to create treatment processes", e);
          return new TransferObject(TransferObject.ERROR, e.getErrorCode());
      } catch (AbortTransactionException e) {
    	  log.error("Failed to create treatment processes", e);
    	  sqlFailure(null, "Failed to create treatment processes", null, e);
    	  return getExceptionTransferObject(e); 
      } catch (SQLException e) {
    	  log.error("Failed to create treatment processes", e);
    	  sqlFailure(null, "Failed to create treatment processes", null, e);
    	  return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
      }
  }

  public TransferObject getAccountIds(String[] accountNumbers) {
    try {
      return new TransferObject(super.executeListQuery(IReadOnlyDataProvider.LIST_RESULTS, "getAccountIds", Arrays.asList(accountNumbers), new MapVerifyingMapCar(new String[]{ACCOUNT_ID, "ACCOUNT_NUMBER"}), getReadOnlyProvider()));
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
  }

  public TransferObject updateAgency(final Map[] agencies) {
    final IReadWriteDataProvider p = getReadWriteProvider();
    List trans = new ArrayList();
    for (int i = 0; i < agencies.length; i++) {
      final Map agency = agencies[i];
      if (agency.get("CONTACT_NUMBER") == null && agency.get("IDENTIFICATION_NUMBER") == null && agency.get("REMARKS") == null)
        continue;
      IRunnableTransaction t = new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          Long contactId = (Long) agency.get("CONTACT_ID");
          Map number = new HashMap();
          number.put(IContactNumber.CONTACT_ID, contactId);
          number.put(IContactNumber.TYPE_ID, new Long(IContactNumber.OFFICE_CONTACT_NUMBER_TYPE));
          number.put(IContactNumber.PREFERRED_POSITION, new Long(1));
          number.put(IContactNumber.CONTACT_NUMBER, agency.get("CONTACT_NUMBER"));
          number.put("OLD_CONTACT_NUMBER", agency.get("OLD_CONTACT_NUMBER"));
          
          Map addressMap = new HashMap(agency);         
          if (contactId == null){
          	addressMap.put("DISABLE", Boolean.FALSE);
          	contactId = (Long) p.insert("insertAddressDetail", addressMap, t);
            agency.put("CONTACT_ID", contactId);
            number.put(IContactNumber.CONTACT_ID, contactId);
          } else {
          	p.update("updateAddressDetail", addressMap,t);
          }
          
          if (agency.get("OLD_CONTACT_NUMBER") == null){
            if (number.get(IContactNumber.CONTACT_NUMBER) == null){
              number.put(IContactNumber.CONTACT_NUMBER, "-");
            }
            p.insert("insertContactNumber", number, t);
          } else {
            p.update("updateContactNumber", number, t);
          }
          
          //location served
          if(agency.get("LOCATION_SERVED")!=null){
          	p.delete("deleteAgencyLocationServed",agency,t);
          	List l = (List) agency.get("LOCATION_SERVED");
          	for(int i=0;i<l.size();i++){
          		Map m = (Map) l.get(i);
          		m.put("ID", agency.get("ID"));
          		p.insert("insertAgencyLocationServed",m,t);
          	}
          }
          p.update("updateAgency", agency, t);
        }};
      trans.add(t);
    }
    try {
      p.execute(new RunnableTransactionSet((IRunnableTransaction[]) trans.toArray(new IRunnableTransaction[0])));
    } catch (AbortTransactionException e) {
      return sqlFailure(null, "updateAgency, updateContactNumber/updateAgency, insertAddressDetail, insertContactNumber", agencies, e);
    } catch (SQLException e) {
      return sqlFailure(null, "updateAgency, updateContactNumber/updateAgency, insertAddressDetail, insertContactNumber", agencies, e);
    }
    return new TransferObject();
  }
}
