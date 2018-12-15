package com.profitera.services.business.worklistmanager;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.deployment.rmi.ListQueryServiceIntf;
import com.profitera.deployment.rmi.WorkListServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.ICustomer;
import com.profitera.descriptor.business.meta.IDecisionTreeNode;
import com.profitera.descriptor.business.meta.IUser;
import com.profitera.descriptor.business.meta.IWorkList;
import com.profitera.descriptor.business.meta.IWorkListAssignment;
import com.profitera.descriptor.business.reference.ReferenceBusinessBean;
import com.profitera.descriptor.business.worklistmanager.WorklistBusinessBean;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.business.worklistmanager.impl.GenerationRunner;
import com.profitera.services.business.worklistmanager.impl.TransactionSetRunner;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.MapVerifyingMapCar;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.AsynchronousUpdateExecutor;
import com.profitera.util.MapCar;
import com.profitera.util.MapListUtil;
import com.profitera.util.reflect.Reflect;
import com.profitera.util.reflect.ReflectionException;

public class WorkListService extends ProviderDrivenService implements WorkListServiceIntf {
  public static final String MODULE_NAME = "worklistservice";
  public static final String GENERATOR = "generator";
  protected static final String INSERT_WORK_LIST = "insertWorkList";
  protected static final String UPDATE_WORK_LIST = "updateWorkList";
  protected static final String DELETE_WORK_LIST = "deleteWorkList";
  static final String GET_ACCOUNT_WORK_LIST_BY_ID = "getAccountWorkListById";
  static final String INSERT_ACCOUNT_WORK_LIST_HISTORY = "insertAccountWorkListHistory";
  static final String UPDATE_ACCOUNT_WORK_LIST_HISTORY = "updateAccountWorkListHistory";
  static final String SET_ACCOUNT_WORK_LIST = "setAccountWorkList";
  //
  protected static final String INSERT_WORK_LIST_BEAN = "insertWorkListBean";
  protected static final String UPDATE_WORK_LIST_BEAN = "updateWorkListBean";
  //
  static final String GET_ACCOUNT_PERMANENT_WORK_LIST_BY_TYPE = "getAccountPermanentWorkListAssignmentByType";
  static final String GET_ACCOUNT_TEMPORARY_WORK_LIST_BY_TYPE = "getAccountTemporaryWorkListAssignmentByType";  
  static final String DELETE_ACCOUNT_WORK_LIST_ASSIGN_BY_ID = "deleteAccountWorkListAssignmentById";
  static final String INSERT_ACCOUNT_WORK_LIST_ASSIGN = "insertAccountWorkListAssignment";
  static final String UPDATE_ACCOUNT_WORK_LIST_ASSIGN_STATUS = "updateAccountWorkListAssignmentStatus";
  static final String GET_ACCOUNT_WORK_LIST_ASSIGNMENT = "getAccountWorkListAssignmentByType";
  //
  private static final String COMMIT_SIZE = "commitsize";
  private static final String THREADS = "threads";
  private static final String CUSTOMER_LEVEL_GENERATION = "generatecustomerlevel";
  private static final String CUSTOMER_LEVEL_TRANSFER = "transfercustomerlevel";
  public static final String MULTIPLE_ACCOUNT_WORKLIST = "multipleaccountworklist";
  private static final String DEBUG = "debug";
  //
  private static final String CLEAR_USER_ASSIGNMENT_HISTORY = "clearUserAssignmentHistory";
  private static final String INSERT_USER_ASSIGNMENT_HISTORY = "insertUserAssignmentHistory";
  //
  private static final String NO_WORKLIST_TO_UPDATE = "NO_WORKLIST_TO_UPDATE";
  private static final String NO_WORKLIST_NAME = "NO_WORKLIST_NAME";
  private static final String DUPLICATE_WORKLIST_NAME = "DUPLICATE_WORKLIST_NAME";
  private static final String NO_MANAGER_FOR_ASSIGNMENT = "NO_MANAGER_FOR_ASSIGNMENT";
  private static final String NO_SUCH_WORKLIST = "NO_SUCH_OBJECT";
  private static final MapCar WORKLIST_BEAN_MAPCAR = new MapCar() {
    public Object map(Object o) {
      return createWorklistBusinessBean((Map) o);
    }
  };
  
  private static abstract class LockedServiceInvoker {
    private Object lock;
    private String runningMessage;
    public LockedServiceInvoker(Object synchronizeOn, String runInProgressMessage){
      lock = synchronizeOn;
      runningMessage = runInProgressMessage;
    }
    protected abstract boolean isRunning();
    protected abstract void markStarted();
    protected abstract void markComplete();
    protected abstract TransferObject invokeMethod();
    public TransferObject invoke(){
      // Prevents double calls, so we need the synch block.
      synchronized (lock) {
        if (isRunning()){
          return new TransferObject(TransferObject.ERROR, runningMessage);
        } else {
          markStarted();
        }
      }
      try {
        return invokeMethod();
      } finally {
        synchronized (lock) {
          markComplete();
        }
      } 
    }
  }
    
  private final Object generatorLock = new Object();
  private boolean isGenerating = false;
  private final Object assignmentLock = new Object();
  private boolean isAssigning = false;
  private final Object transferLock = new Object();
  private boolean isTransfering = false;
  
  private final AsynchronousUpdateExecutor exe = new AsynchronousUpdateExecutor();

  public TransferObject getWorkLists(Long rootId, Map args) {
    IWorkListGenerator workListGenerator = getWorkListGenerator(new Date(), rootId, isCustomerLevel());
    String query = workListGenerator.getWorkListQuery();
    return getWorkLists(query, args);
  }

  public TransferObject getWorkLists(String query, Map args) {
    TransferObject t = getListQueryService().getQueryList(query, args);
    if (t.isFailed()) return t;
    List data = (List) t.getBeanHolder();
    List l = new ArrayList();
    MapCar.map(WORKLIST_BEAN_MAPCAR, data.iterator(), l);
    return new TransferObject(l);
  }
  
    /**
     * Returns Map objects
     *
     * @see com.profitera.deployment.rmi.WorkListServiceIntf#getWorklistUsers(java.lang.Double)
     */
    public TransferObject getWorklistUsers(Long wlistId) {
    	// using list query service here as user teams and roles are lists
    	Map args = new HashMap();
    	args.put(IWorkList.WORK_LIST_ID, wlistId);
    	TransferObject to = getListQueryService().getQueryList("getWorkListUsers", args);
    	return to;
    }

    public TransferObject addWorklist(WorklistBusinessBean _bean, String _managerId) {
        String message = validateWorklist(_bean);
        if (message != null) {
            return new TransferObject(TransferObject.ERROR, message);
        }
    	_bean.setCreatedDate(new java.sql.Timestamp(System.currentTimeMillis()));
    	_bean.setCreatedById(_managerId);
    	final Map values = (Map)_bean;
    	if (_bean.getAgencyType() != null){
    		values.put("AGENCY_TYPE_ID", _bean.getAgencyType().getId());
    	}
    	if (_bean.getTreatmentStage() != null){
    		values.put("TREATMENT_STAGE_ID", _bean.getTreatmentStage().getId());	
    	}
	    try {
	      final IReadWriteDataProvider p = getReadWriteProvider();
	      p.execute(new IRunnableTransaction() {
	        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
	          p.insert(INSERT_WORK_LIST_BEAN, values, t);
	        }
	      });
	    } catch (AbortTransactionException e) {
	      return returnFailWithTrace("", null, INSERT_WORK_LIST_BEAN, values, e);
	    } catch (SQLException e) {
	      return returnFailWithTrace("", null, INSERT_WORK_LIST_BEAN, values, e);
	    }
	    return new TransferObject(Boolean.TRUE);
    }

    public TransferObject setAccountsWorkList(final Long wlistid, final List accountids, final Long fromwlistid) {
        // if nothing to assign, we're already successful.
        if (accountids == null || accountids.size() == 0) {
            return new TransferObject(new Boolean(true));
        }
        final IReadWriteDataProvider p = getReadWriteProvider();
        final Date date = new Date();
        final List accountList = new ArrayList();
        
        for (Iterator it = accountids.iterator(); it.hasNext();) {
        	Number element = (Number) it.next();
        	TransferObject result = executeQuery("getAccountForWorkListAssignment", element, new MapVerifyingMapCar(new String[]{"ACCOUNT_ID"}) , getReadWriteProvider());
        	if (result.isFailed())
        		return result;
        	List al = (List) result.getBeanHolder();
        	if (al != null && al.size() > 0){
        		Map acc = (Map)al.get(0);
	        	acc.put(IWorkList.WORK_LIST_ID, wlistid);
	            acc.put(IWorkListAssignment.ASSIGNMENT_DATE, date);
	            if (isMultipleAccountWorklist()){
	            	if (wlistid != null){
		            	TransferObject res = getAccountWorkListAssignment(acc, GET_ACCOUNT_PERMANENT_WORK_LIST_BY_TYPE); 
		            	if (res.isFailed())
		            		return res;
		            	List l = (List)res.getBeanHolder();
		            	if (l != null && l.size() > 0){
			            	Map m = (Map)l.get(0);
		            		acc.put("EXISTING_WL_ASSIGNMENT_ID", m);
		            	}
	            	} else {
	            		if (fromwlistid != null){
	            			acc.put(IWorkList.WORK_LIST_ID, fromwlistid); // temporarily for processing
	            			TransferObject res = getAccountWorkListAssignment(acc, GET_ACCOUNT_WORK_LIST_ASSIGNMENT);
			            	if (res.isFailed())
			            		return res;
			            	List l = (List)res.getBeanHolder();
			            	if (l != null && l.size() > 0){
			            		boolean isTemporary = false;
			            		for (Iterator i = l.iterator(); i.hasNext();){
					            	Map m = (Map)i.next();
					            	if (fromwlistid.equals(m.get(IWorkList.WORK_LIST_ID))){
						            	if (m.get(IWorkListAssignment.EXPIRATION_DATE) != null){
						            		isTemporary = true;
						            	}
					            	}
					            	if (m.get(IWorkListAssignment.EXPIRATION_DATE) == null) {
					            		acc.put("PERMANENT_WL_ASSIGNMENT", m);
					            	}
			            		}
			            		if (isTemporary){
			            			// remove permanent work list assignment from l
			            			Map tm = (Map)acc.get("PERMANENT_WL_ASSIGNMENT");
			            			tm.put(IWorkListAssignment.STATUS_ID, IWorkListAssignment.ACTIVE_STATUS);
			            			l.remove(tm);
			            			acc.put("EXISTING_WL_ASSIGNMENT", l);
			            			acc.put(IWorkList.WORK_LIST_ID, tm.get(IWorkList.WORK_LIST_ID));
			            			acc.put(IWorkListAssignment.ASSIGNMENT_TYPE, "T2N2P");			            			
			            		} else {
				            		acc.put("EXISTING_WL_ASSIGNMENT", l);
				            		acc.put(IWorkList.WORK_LIST_ID, wlistid);
			            		}
			            	}
	            		}
	            	}
	            }
	            accountList.add(acc);
        	}
        }
        try {
          p.execute(new IRunnableTransaction(){
            public void execute(ITransaction t) throws SQLException, AbortTransactionException {
              for (Iterator iter = accountList.iterator(); iter.hasNext();) {
                Map args = (Map) iter.next();
		        int count = p.update(UPDATE_ACCOUNT_WORK_LIST_HISTORY, args, t);
		        if (count == 0){
		          p.insert(INSERT_ACCOUNT_WORK_LIST_HISTORY, args, t);
		        }
                p.update(SET_ACCOUNT_WORK_LIST, args, t);
                if (isMultipleAccountWorklist()){ // N2P, P2P, P2N or T2N2P 
	        		if (args.get(IWorkList.WORK_LIST_ID) != null && !(args.get(IWorkListAssignment.ASSIGNMENT_TYPE) != null && ((String)args.get(IWorkListAssignment.ASSIGNMENT_TYPE)).equals("T2N2P"))){ // N2P, P2P 
	        			// get existing permanent work list of the same work list type and delete
	        			Map pwl = (Map) args.get("EXISTING_WL_ASSIGNMENT_ID");
	        			if (pwl != null){
	        				p.delete(DELETE_ACCOUNT_WORK_LIST_ASSIGN_BY_ID, pwl.get(IWorkListAssignment.ID), t);
		        			// insert new permanent work list assignment with status of previous work list
	        				args.put(IWorkListAssignment.STATUS_ID, pwl.get(IWorkListAssignment.STATUS_ID));
			        		p.insert(INSERT_ACCOUNT_WORK_LIST_ASSIGN, args, t);
	        			} else {
			        		// insert new permanent work list assignment with status Active
			        		args.put(IWorkListAssignment.STATUS_ID, IWorkListAssignment.ACTIVE_STATUS);
			        		p.insert(INSERT_ACCOUNT_WORK_LIST_ASSIGN, args, t);
		        		}
	        		} else { //P2N or or T2N2P
	        			// remove account from work list
	        			if (args.containsKey("PERMANENT_WL_ASSIGNMENT")){ 
	        				p.update(UPDATE_ACCOUNT_WORK_LIST_ASSIGN_STATUS, args.get("PERMANENT_WL_ASSIGNMENT"), t);
	        			}
	        			List delList = (List)args.get("EXISTING_WL_ASSIGNMENT");
	        			if (delList != null){
		        			for (Iterator i = delList.iterator(); i.hasNext();){
		        				Map dm = (Map)i.next();
		        				p.delete(DELETE_ACCOUNT_WORK_LIST_ASSIGN_BY_ID, dm.get(IWorkListAssignment.ID), t);
		        			}
	        			}
	        		}
                }
              }
            }});
        } catch (SQLException e) {
          return sqlFailure(null, SET_ACCOUNT_WORK_LIST, wlistid, e);
        } catch (AbortTransactionException e) {
          return sqlFailure(null, SET_ACCOUNT_WORK_LIST, wlistid, e);
        }
        return new TransferObject(Boolean.valueOf(true));
    }
        
    public TransferObject assignCustomersToWorklist(Long wlistid, List customerIds, Long fromwlistid) {
        // if nothing to assign, we're already successful.
        if (customerIds == null || customerIds.size() == 0) {
            return new TransferObject(new Boolean(true));
        }
        List l = new ArrayList();
        Iterator i;
        try {
          i = executeIteratorQuery(IReadOnlyDataProvider.LIST_RESULTS, "getCustomerAccountIds", customerIds, getReadOnlyProvider());
        } catch (TransferObjectException e) {
          return e.getTransferObject();
        }
        MapCar.map(new MapCar(){
          public Object map(Object o) {
            return ((Map)o).get("ACCOUNT_ID");
          }}, i, l);
        return setAccountsWorkList(wlistid, l, fromwlistid);
    }

    public TransferObject assignUsersToWorklist(final Long wlistid, final List userIds, final String mgrId, final Date date) {
        if (mgrId == null) {
            return new TransferObject(TransferObject.ERROR, NO_MANAGER_FOR_ASSIGNMENT);
        }
       	final IReadWriteDataProvider p = getReadWriteProvider();
        try{
        	p.execute(new IRunnableTransaction() {
        		public void execute(ITransaction t) throws SQLException, AbortTransactionException {
		        	Map user = new HashMap();
		        	user.put(IWorkList.WORK_LIST_ID, wlistid);
		        	user.put("ASSIGNER_ID", mgrId);
		        	user.put("DATE", date);
		        	for (Iterator i = userIds.iterator(); i.hasNext();){
		        		String userId = (String)i.next();
		        		user.put(IUser.USER_ID, userId);
		        		p.insert("insertUserWorkListAssignment", user, t);
		        	}
        		}
        	});
        } catch (AbortTransactionException e) {
        	log.error(e.getMessage(), e);
        	return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
        } catch (SQLException e) {
        	log.error(e.getMessage(), e);
        	return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
        }
        return new TransferObject(Boolean.TRUE);
    }

    public TransferObject unAssignUsersFromWorklist(final Long wlistid, final List userIds) {
       	final IReadWriteDataProvider p = getReadWriteProvider();
        try{
        	p.execute(new IRunnableTransaction() {
        		public void execute(ITransaction t) throws SQLException, AbortTransactionException {
		        	Map args = new HashMap();
		        	args.put(IWorkList.WORK_LIST_ID, wlistid);
		        	for (Iterator i = userIds.iterator(); i.hasNext();){
		        		String userId = (String)i.next();
		        		args.put(IUser.USER_ID, userId);
		        		p.delete("deleteUserWorkListAssignment", args, t);
		        	}
        		}
        	});
        } catch (AbortTransactionException e) {
        	log.error(e.getMessage(), e);
        	return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
        } catch (SQLException e) {
        	log.error(e.getMessage(), e);
        	return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
        }
        return new TransferObject(Boolean.TRUE);
    }

    public TransferObject updateWorklist(WorklistBusinessBean _bean, String _managerId) {
        String message = validateWorklist(_bean);
        if (message != null) {
            return new TransferObject(TransferObject.ERROR, message);
        }
    	_bean.setCreatedDate(new java.sql.Timestamp(System.currentTimeMillis()));
    	_bean.setCreatedById(_managerId);
    	final Map values = (Map)_bean;
    	if (_bean.getAgencyType() != null){
    		values.put("AGENCY_TYPE_ID", _bean.getAgencyType().getId());
    	}
    	if (_bean.getTreatmentStage() != null){
    		values.put("TREATMENT_STAGE_ID", _bean.getTreatmentStage().getId());	
    	}
	    try {
	      final IReadWriteDataProvider p = getReadWriteProvider();
	      p.execute(new IRunnableTransaction() {
	        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
	          p.update(UPDATE_WORK_LIST_BEAN, values, t);
	        }
	      });
	    } catch (AbortTransactionException e) {
	      return returnFailWithTrace("", null, UPDATE_WORK_LIST_BEAN, values, e);
	    } catch (SQLException e) {
	      return returnFailWithTrace("", null, UPDATE_WORK_LIST_BEAN, values, e);
	    }
	    return new TransferObject(Boolean.TRUE);
    }
    
    /**
     * When I delete a worklist I need to remove the block code, user
     * assignments, and account assignments
     *
     * @see com.profitera.deployment.rmi.WorkListServiceIntf#deleteWorklist(java.lang.Double)
     */
    public TransferObject deleteWorklist(final Long id) {
    	final IReadWriteDataProvider p = getReadWriteProvider();
        try{
        	p.execute(new IRunnableTransaction() {
        		public void execute(ITransaction t) throws SQLException, AbortTransactionException {
	        		p.update(DELETE_WORK_LIST, id, t);
        		}
        	});
    	} catch (AbortTransactionException e) {
    		return returnFailWithTrace("", null, DELETE_WORK_LIST, id, e);
    	} catch (SQLException e) {
    		return returnFailWithTrace("", null, DELETE_WORK_LIST, id, e);
    	}
        return new TransferObject(Boolean.TRUE);
    }

    private String validateWorklist(WorklistBusinessBean _bean) {
        if (_bean == null) {
            return NO_WORKLIST_TO_UPDATE;
        } else if (_bean.getWorklistName() == null || _bean.getWorklistName().trim().length() == 0) {
            return NO_WORKLIST_NAME;
        } else if (_bean.getWorklistId() == null && exist(_bean)) {
            return DUPLICATE_WORKLIST_NAME;
        }
        return null;
    }

    private boolean exist(WorklistBusinessBean _bean) {
    	TransferObject result = executeQuery("getWorkListByName", _bean.getWorklistName(), new MapVerifyingMapCar(new String[]{"WORK_LIST_ID"}) , getReadWriteProvider());
       	List al = (List) result.getBeanHolder();
       	if (al != null && al.size() > 0){
       		return true;
       	} else {
       		return false;       		
       	}
    }

    private static WorklistBusinessBean createWorklistBusinessBean(Map list) {
        WorklistBusinessBean bean = new WorklistBusinessBean();
        bean.putAll(list);
        bean.setCreatedById((String) list.get(WorklistBusinessBean.CREATOR_ID));
        bean.setCreatedDate(new Timestamp(((Date) list.get(WorklistBusinessBean.CREATED)).getTime()));
        bean.setWorklistDesc((String) list.get(WorklistBusinessBean.DESCRIPTION));
        bean.setWorklistId((Long) list.get(WorklistBusinessBean.WORK_LIST_ID));
        bean.setWorklistName((String) list.get("WORK_LIST_NAME"));
        bean.setBlockCodes(Collections.EMPTY_LIST);
        Long tId = (Long) list.get("TREATMENT_STAGE_ID");
        String tCode = (String) list.get("TREATMENT_STAGE_CODE");
        String tDesc = (String) list.get("TREATMENT_STAGE_DESC");
        if (tId != null){
          bean.setTreatmentStage(new ReferenceBusinessBean(new Double(tId.doubleValue()), tCode, tDesc));
        }
        if (list.get("AGENCY_TYPE_ID") != null){
          Long aId = (Long) list.get("AGENCY_TYPE_ID");
          String aCode = (String) list.get("AGENCY_TYPE_CODE");
          String aDesc = (String) list.get("AGENCY_TYPE_DESC");
          bean.setAgencyType(new ReferenceBusinessBean(new Double(aId.doubleValue()), aCode, aDesc));
        }
        bean.setIsForUnassignedAgencyTreatments((Boolean) list.get(WorklistBusinessBean.IS_FOR_UNASSIGNED_AGENCY));
        return bean;
    }

    public TransferObject removeCustomers(Long wlistid, List customerIds) {
      return assignCustomersToWorklist(null, customerIds, wlistid);
    }
    
    private static final Object[] GET_USERS_REQUIRED = new Object[]{IUser.USER_ID};
    public TransferObject getUsers(Long treeId, Map args){
      IWorkListGenerator g = getWorkListGenerator(new Date(), treeId, isCustomerLevel());
      return getUsers(g.getUserQuery(), args);
    }
    
    public TransferObject getUsers(String query, Map args){
      TransferObject t = getListQueryService().getQueryList(query, args);
      if (!t.isFailed()){
        List l = (List) t.getBeanHolder();
        l = MapCar.map(new MapVerifyingMapCar(GET_USERS_REQUIRED), l);
        t = new TransferObject(l);
      }
      return t;
    }
    
    private static final Object[] GET_OPTIONS_REQUIRED = new Object[]{IDecisionTreeNode.COLUMN_NAME_DESCRIPTION, IDecisionTreeNode.COLUMN_NAME, IDecisionTreeNode.COLUMN_VALUE_NAME, IDecisionTreeNode.COLUMN_VALUE};
    public TransferObject getWorkListGenerationOptions(Long treeId){
      IWorkListGenerator g = getWorkListGenerator(new Date(), treeId, isCustomerLevel());
      TransferObject result = executeQuery(g.getDecisionTreeOptionsQuery(), null, new MapVerifyingMapCar(GET_OPTIONS_REQUIRED) , getReadWriteProvider());
      if (result.isFailed())
        return result;
      List options = (List) result.getBeanHolder();
      Map optionHash = new HashMap();
      for (Iterator i = options.iterator(); i.hasNext();) {
        Map opt = (Map) i.next();
        String colName = (String) opt.get(IDecisionTreeNode.COLUMN_NAME);
        List l = (List) optionHash.get(colName);
        if (l == null){
          l = new ArrayList();
          optionHash.put(colName, l);
        }
        l.add(opt);
      }
      options = MapCar.map(new MapCar(){
        public Object map(Object o) {return o;}}, optionHash.values());
      return new TransferObject(options);
    }
    
    public TransferObject getWorkListGenerationDecisionTree(Long treeId, Map parameters) {
      IReadWriteDataProvider provider = getReadWriteProvider();
      IWorkListGenerator g = getWorkListGenerator(new Date(), treeId, isCustomerLevel());
      TransferObject result = g.getWorkListGenerationDecisionTree(treeId, parameters, provider);
      return result;
    }

    /**
     * @see com.profitera.deployment.rmi.WorkListServiceIntf#saveWorkListGenerationDecisionTree(java.util.Map, Long, Map)
     */
    public TransferObject saveWorkListGenerationDecisionTree(Map root, Long treeId, Map parameters) {
      IWorkListGenerator g = getWorkListGenerator(new Date(), treeId, isCustomerLevel());
      TransferObject current = getWorkListGenerationDecisionTree(treeId, parameters);
      if (current.isFailed())
        return current;
      Map oldRoot = (Map) current.getBeanHolder();
      IReadWriteDataProvider provider = getReadWriteProvider();
      RunnableTransactionSet s = new RunnableTransactionSet(new IRunnableTransaction[]{
          g.getDeleteCurrentTree(oldRoot, provider), 
          g.getBuildTree(treeId, oldRoot, root, parameters, provider)
      });
      try {
        provider.execute(s);
      } catch (SQLException e) {
        return sqlFailure("Update/Insert/Delete", "Transaction", null, e);
      } catch (AbortTransactionException e) {
        return sqlFailure("Update/Insert/Delete", "Transaction", null, e);
      }
      printMap(root, "");
      return new TransferObject("");
    }

    private void printMap(Map m, String pad) {
      List mapChildrenKeys = new ArrayList(m.size());
      for (Iterator i = m.entrySet().iterator(); i.hasNext();) {
        Map.Entry element = (Map.Entry) i.next();
        if (element.getValue() != null && element.getValue() instanceof Map){
          mapChildrenKeys.add(element.getKey());
        } else {
          //System.out.println(pad + Strings.pad("" + element.getKey(), 20) + element.getValue());
        }
      }
      for (Iterator i = mapChildrenKeys.iterator(); i.hasNext();) {
        Object key = i.next();
        //System.out.println(pad + Strings.pad("" + key, 20) + " : ");
        printMap((Map) m.get(key), pad + "     ");
      }
      
    }
	
	public TransferObject generateWorkLists(final Date date, final Long treeId, final Map parameters){
    return new LockedServiceInvoker(generatorLock, "Work List Generation in progress"){
      protected boolean isRunning() {
        return isGenerating;
      }
      protected void markStarted() {
        isGenerating = true;
      }
      protected void markComplete() {
        isGenerating = false;
      }
      protected TransferObject invokeMethod() {
        return generateWorkListsInternal(date, treeId, parameters);
      }}.invoke();
  }
  
  private TransferObject generateWorkListsInternal(final Date date, Long treeId, Map parameters){
    int transactionSize = ServiceEngine.getIntProp(MODULE_NAME+"."+COMMIT_SIZE, 100);
    int concurrencyThreshhold  = ServiceEngine.getIntProp(MODULE_NAME+"."+THREADS, 10);
    boolean isCustomerLevel = isCustomerLevel();
    log.info("Work list generation for root: " + treeId);
    log.info("Work list generation for date: " + date);
    log.info(MODULE_NAME+"."+COMMIT_SIZE+": " + transactionSize);
    log.info(MODULE_NAME+"."+THREADS+": " + concurrencyThreshhold);
    log.info(MODULE_NAME+"."+CUSTOMER_LEVEL_GENERATION+": " + isCustomerLevel);
    //
    log.info("Starting Work List Generation");
    Iterator accounts = null;
    Map treeRoot = Collections.EMPTY_MAP;
    IReadWriteDataProvider provider = getReadWriteProvider();
    IWorkListGenerator generator = getWorkListGenerator(date, treeId, isCustomerLevel);
   	log.info(MODULE_NAME + "." + GENERATOR + "." + treeId + "." + CUSTOMER_LEVEL_GENERATION+": " + generator.isUseCustomerMode());
  	try {
      log.info("Loading work list generation decision tree");
      TransferObject tree = getWorkListGenerationDecisionTree(treeId, parameters);
      if (tree.isFailed()) {
        throw new TransferObjectException(tree);
      }
      treeRoot = (Map) tree.getBeanHolder();
      generator.setDecisionTree(treeRoot);
      String query = generator.getCustomerAccountQuery();
        // This query returns a stream of ALL principle accounts in the base, with (if configured properly)
        // all the attributes required to evaluate any node in the decision tree and MUST be ordered by Customer_id.
  	  accounts = executeIteratorQuery(IReadOnlyDataProvider.STREAM_RESULTS, query, parameters, provider);
  	} catch (TransferObjectException e) {
  	  return e.getTransferObject();
  	}
    executeGeneration(transactionSize, concurrencyThreshhold, accounts,
        provider, generator);
    return new TransferObject("");
  }

  private void executeGeneration(int transactionSize,
      int concurrencyThreshhold, Iterator accounts,
      IReadWriteDataProvider provider, IWorkListGenerator generator) {
    GenerationRunner runner = new GenerationRunner();
    TransactionSetRunner transactionSetRunner = new TransactionSetRunner(exe, concurrencyThreshhold);
    runner.executeGeneration(generator.getDataGroupingFields(), accounts, generator, transactionSetRunner, transactionSize, provider);
  }

  private boolean isCustomerLevel() {
    boolean isCustomerLevel = ServiceEngine.getProp(MODULE_NAME + "." + CUSTOMER_LEVEL_GENERATION, "F").toUpperCase().startsWith("T");
    return isCustomerLevel;
  }

  private boolean isMultipleAccountWorklist() {
    boolean isMultipleAccountWorklist = ServiceEngine.getProp(MODULE_NAME + "." + MULTIPLE_ACCOUNT_WORKLIST, "F").toUpperCase().startsWith("T");
    return isMultipleAccountWorklist;
  }
  
  private IWorkListGenerator getWorkListGenerator(final Date date, Long treeId, boolean isCustomerLevel) {
    IWorkListGenerator generator = null;
    if (treeId.equals(FULL_WORK_LIST_GENERATION_DECISION_TREE)){
      generator = new AllAccountWorklistGenerator();
    } else if (treeId.equals(INCREMENTAL_WORK_LIST_GENERATION_DECISION_TREE)){
      generator = new UnassignedAccountNoHistoryWorklistGenerator();
    } else if (treeId.equals(SUPPLEMENTARY_INCREMENTAL_WORK_LIST_GENERATION_DECISION_TREE)){
      generator = new UnassignedAccountWorklistGenerator();
    } else if (treeId.equals(CHARGE_OFF_WORK_LIST_GENERATION_DECISION_TREE)){
      generator = new ChargeOffAccountWorklistGenerator();
    } else {
      ServiceEngine.refreshConfig();
      String generatorImpl = ServiceEngine.getProp(MODULE_NAME + "." + GENERATOR + "." + treeId);
      if (generatorImpl == null){
        throw new IllegalArgumentException("Decision tree root requested has no assigned generator: " + treeId);  
      } else {
        try {
        generator = (IWorkListGenerator) Reflect.invokeConstructor(generatorImpl, null, null);
        } catch (ReflectionException e){
          String msg = "Decision tree root requested implementation invalid: " + treeId + " ('" + generatorImpl + "')";
          log.fatal(msg, e);
          throw new IllegalArgumentException(msg);
        }
      }
    }
    generator.setGeneratorCode(treeId.toString());
    generator.setDate(date);
    
    // set generation mode
    String isCustomerLevelAtGenerator = ServiceEngine.getProp(MODULE_NAME + "." + GENERATOR + "." + treeId + "." + CUSTOMER_LEVEL_GENERATION);
    if (isCustomerLevelAtGenerator != null){
    	isCustomerLevel = ServiceEngine.getProp(MODULE_NAME + "." + GENERATOR + "." + treeId + "." + CUSTOMER_LEVEL_GENERATION, "F").toUpperCase().startsWith("T");
    }
    generator.setUseCustomerMode(isCustomerLevel);
    
    // set generation grouping
    String dataGroup = ServiceEngine.getProp(MODULE_NAME + "." + GENERATOR + "." + treeId + "." + "groupingfields", ICustomer.CUSTOMER_ID);
    generator.setDataGroupingFields(dataGroup.split(";"));
    
    // set transfer mode
    boolean isTransferAtCustomerLevel = ServiceEngine.getProp(MODULE_NAME + "." + GENERATOR + "." + treeId + "." + CUSTOMER_LEVEL_TRANSFER, "F").toUpperCase().startsWith("T");
    generator.setTransferAtCustomerMode(isTransferAtCustomerLevel);
    
    // set multiple account worklist property
    generator.setMultipleAccountWorklist(isMultipleAccountWorklist());
    
    // set debug mode
    boolean isDebugMode = ServiceEngine.getProp(MODULE_NAME + "." + GENERATOR + "." + treeId + "." + DEBUG, "F").toUpperCase().startsWith("T");
    generator.setDebugMode(isDebugMode);
    return generator;
  }

  private void recordAssignmentHistory(final Date date, final IReadWriteDataProvider provider) throws TransferObjectException {
    try {
      provider.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          provider.delete(CLEAR_USER_ASSIGNMENT_HISTORY, date, t);
          provider.insert(INSERT_USER_ASSIGNMENT_HISTORY, date, t);
        }});
    } catch (SQLException e) {
      log.error("Failed to update work list assignment history", e);
      throw new TransferObjectException(new TransferObject(TransferObject.EXCEPTION, e.getMessage()));
    } catch (AbortTransactionException e) {
      log.error("Failed to update work list assignment history", e);
      throw new TransferObjectException(new TransferObject(TransferObject.EXCEPTION, e.getMessage()));
    }
  }

  /**
   * This method is actually the same kind of stuff as was done for the data loader, where
   * commits are asynchonous. It is done in this way in order to get better performance
   * but without introducing the complexity of multithreading the generation code itself.
   * It ain't pretty, but it makes generation about 30% faster on the HQ LAN.
   * NOTE: If you use this method for anything make sure you call waitForAllTransactions()
   * before returning to client, you may still have transactions running.
   */
  private void executeUpdates(final IReadWriteDataProvider provider, final List transactions, int maxConcurr) {
    if (transactions.size() == 0)
      return;
    IRunnableTransaction[] trans = (IRunnableTransaction[]) transactions.toArray(new IRunnableTransaction[0]);
    transactions.clear();
    final RunnableTransactionSet set = new RunnableTransactionSet(trans);
    Runnable object = new Runnable(){
	      public void run() {
	        try {
	          provider.execute(set);
	        } catch (SQLException e) {
	          log.error(e.getMessage(), e);
	        } catch (AbortTransactionException e) {
	          log.error(e.getMessage(), e);
	        } catch (RuntimeException e) {
	          log.error(e.getMessage(), e);
	        }
	      }};
	exe.executeUpdates(object, maxConcurr);
  }
  
  public TransferObject assignWorkLists(final Date date, final Long treeId, final Map parameters){
    return new LockedServiceInvoker(assignmentLock, "Work List Assignment in progress"){
      protected boolean isRunning() {
        return isAssigning;
      }
      protected void markStarted() {
        isAssigning = true;
      }
      protected void markComplete() {
        isAssigning = false;
      }
      protected TransferObject invokeMethod() {
        return assignWorkListsInternal(date, treeId, parameters);
      }}.invoke();
  }

  private TransferObject assignWorkListsInternal(final Date date, Long treeId, Map parameters) {
    int transactionSize = ServiceEngine.getIntProp(MODULE_NAME+"."+COMMIT_SIZE, 1000);
    int concurrencyThreshhold  = ServiceEngine.getIntProp(MODULE_NAME+"."+THREADS, 100);
    log.info(MODULE_NAME+"."+COMMIT_SIZE+": " + transactionSize);
    log.info(MODULE_NAME+"."+THREADS+": " + concurrencyThreshhold);
    //
    log.info("Starting Work List Assignment");
    Map treeRoot = Collections.EMPTY_MAP;
    List collectors = Collections.EMPTY_LIST;
    final IReadWriteDataProvider provider = getReadWriteProvider();
    try {
      log.info("Loading work list generation decision tree");
      TransferObject tree = getWorkListGenerationDecisionTree(treeId, parameters);
      if (tree.isFailed())
        throw new TransferObjectException(tree);
      treeRoot = (Map) tree.getBeanHolder();
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
    
    IWorkListGenerator generator = getWorkListGenerator(date, treeId, isCustomerLevel());    
    try{ 
      log.info("Loading all collector users");
      TransferObject colls = null;
      colls = getUsers(treeId, parameters);
      if (colls.isFailed())
        throw new TransferObjectException(colls);
      collectors = (List) colls.getBeanHolder();
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
    List transactions = new ArrayList();
    for (Iterator i = collectors.iterator(); i.hasNext();) {
      Map user = (Map) i.next();
      final String userId = (String) user.get(IUser.USER_ID);
      List workLists = collectAllWorkLists(userId, treeRoot);
      final Object[] workListIds = MapListUtil.selectDistinct(WorklistBusinessBean.WORK_LIST_ID, workLists);
   	  transactions.add(generator.getUserWorkListAssignmentTransaction(userId,workListIds,date,provider));
    }
    try {
      IRunnableTransaction[] trans = (IRunnableTransaction[]) transactions.toArray(new IRunnableTransaction[0]);
      transactions.clear();
      final RunnableTransactionSet set = new RunnableTransactionSet(trans);
      provider.execute(set);
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
      return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
    } catch (AbortTransactionException e) {
      log.error(e.getMessage(), e);
      return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
    }
    try {
      log.info("Recording work list assignment history");
      recordAssignmentHistory(date, provider);
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
    return new TransferObject("");
  }
  
  private List collectAllWorkLists(String userId, Map treeRoot) {
    List workLists = new ArrayList();
    List children = DecisionTreeUtil.getNodeChildren(treeRoot);
    for (Iterator i = children.iterator(); i.hasNext();) {
      Map element = (Map) i.next();
      workLists.addAll(collectAllWorkLists(userId, element));
    }
    List nodeWorkLists = DecisionTreeUtil.getNodeWorkLists(treeRoot);
    for (Iterator iter = nodeWorkLists.iterator(); iter.hasNext();) {
      Map workList = (Map) iter.next(); 
      List users = DecisionTreeUtil.getNodeUsers(workList);
      for (Iterator i = users.iterator(); i.hasNext();) {
        Map u = (Map) i.next();
        if (u.get(IUser.USER_ID).equals(userId))
          workLists.add(workList);
      }
    }
    return workLists;
  }

  public TransferObject addWorkList(final Map values, String user) {
    values.put(IUser.USER_ID, user);
    values.put("UPDATE_TIME", new Date());
    try {
      final IReadWriteDataProvider p = getReadWriteProvider();
      p.execute(new IRunnableTransaction() {
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          p.insert(INSERT_WORK_LIST, values, t);
        }
      });
    } catch (AbortTransactionException e) {
      return returnFailWithTrace("", null, INSERT_WORK_LIST, values, e);
    } catch (SQLException e) {
      return returnFailWithTrace("", null, INSERT_WORK_LIST, values, e);
    }
    return new TransferObject(Boolean.TRUE);
  }

  public TransferObject updateWorkList(final Map values, String user) {
    values.put(IUser.USER_ID, user);
    values.put("UPDATE_TIME", new Date());
    try {
      final IReadWriteDataProvider p = getReadWriteProvider();
      p.execute(new IRunnableTransaction() {
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          p.update(UPDATE_WORK_LIST, values, t);
        }
      });
    } catch (AbortTransactionException e) {
      return returnFailWithTrace("", null, UPDATE_WORK_LIST, values, e);
    } catch (SQLException e) {
      return returnFailWithTrace("", null, UPDATE_WORK_LIST, values, e);
    }
    return new TransferObject(Boolean.TRUE);
  }
  
  public TransferObject transferCustomersToWorkLists(final Date date, final Long treeId, final List transferToList, final List accountsForTransferList, final Map args){
    return new LockedServiceInvoker(transferLock, "Transfer of Accounts in progress"){
      protected boolean isRunning() {
        return isTransfering;
      }
      protected void markStarted() {
        isTransfering = true;
      }
      protected void markComplete() {
        isTransfering = false;
      }
      protected TransferObject invokeMethod() {
        return transferCustomersToWorkListsInternal(date, treeId, transferToList, accountsForTransferList, args);
      }}.invoke();
  }
  
  private TransferObject transferCustomersToWorkListsInternal(Date date, Long treeId, List transferToList, List accountsForTransferList, Map args) {
	  // if nothing to transfer, we're already successful.
	  if (accountsForTransferList == null || accountsForTransferList.size() == 0) {
		  return new TransferObject(new Boolean(true));
	  }
	  IReadWriteDataProvider provider = getReadWriteProvider();
	  Iterator accounts = accountsForTransferList.iterator();
	  int transactionSize = ServiceEngine.getIntProp(MODULE_NAME+"."+COMMIT_SIZE, 100);
	  int concurrencyThreshhold  = ServiceEngine.getIntProp(MODULE_NAME+"."+THREADS, 10);
	  int accountCount = 0;
	  int customerCount = 0;
	  // Maintains a list of the accounts for the current customer, this
	  // is needed for customer-level generation, the basic algorithm is just
	  // move forward until you see the first of the next customer, then process
	  // what has already been gathered.
	  List thisCustomer = new ArrayList();
	  Map firstOfNextCustomer = null;
	  String currentCustomerId = null;
	  // A list of pending IRunnableTranaction instances that will be run at the next commit.
	  List transactions = new ArrayList();
      IWorkListGenerator generator = getWorkListGenerator(date, treeId, isCustomerLevel());
	  log.info("Starting Transfer of Accounts");
	  while(accounts.hasNext()){
	      if (firstOfNextCustomer != null){
	        thisCustomer.clear();
	        thisCustomer.add(firstOfNextCustomer);
	        currentCustomerId = (String) firstOfNextCustomer.get(ICustomer.CUSTOMER_ID);
	        firstOfNextCustomer = null;
	      }
	      Map a = (Map) accounts.next();
	      String customerId = (String) a.get(ICustomer.CUSTOMER_ID);
	      if (customerId.equals(currentCustomerId)){
	        thisCustomer.add(a);
	      } else {
	        firstOfNextCustomer = a;
	        if (thisCustomer.size() > 0){
	          // This transaction might actually be a Transaction set, i.e. account-based generation
	          // actually creates one for each account and puts them in a set that is returned.
	          IRunnableTransaction customerTrans = generator.processTransfer(thisCustomer, transferToList, args, provider); 
	          transactions.add(customerTrans);
	          customerCount++;
	        }
	      }
	      accountCount++;
	      if (customerCount % transactionSize == 0){
	        executeUpdates(provider, transactions, concurrencyThreshhold);
	      }
	  }
	  if (firstOfNextCustomer != null){
	      thisCustomer.clear();
	      thisCustomer.add(firstOfNextCustomer);
	      currentCustomerId = (String) firstOfNextCustomer.get(ICustomer.CUSTOMER_ID);
	      firstOfNextCustomer = null;
	  }
	  if (thisCustomer.size() > 0){
		  transactions.add(generator.processTransfer(thisCustomer, transferToList, args, provider));
		  customerCount++;
	  }
	  executeUpdates(provider, transactions, concurrencyThreshhold);
	  long waitStart = System.currentTimeMillis();
	  exe.waitForAllTransactions();
	  log.info(accountCount + " accounts processed");
	  log.info(customerCount + " customers processed");
	  log.info((System.currentTimeMillis() - waitStart) + " ms spent waiting for transactions");
	  return new TransferObject("");
  }

  public TransferObject getAccountWorkListAssignment(Map args, String qName){
	  try {
		  IReadWriteDataProvider provider = getReadWriteProvider();
		  Map wl = (Map)provider.queryObject(GET_ACCOUNT_WORK_LIST_BY_ID, args.get(IWorkList.WORK_LIST_ID));
		  Map m = new HashMap();
		  m.put("ACCOUNT_ID", args.get("ACCOUNT_ID"));
		  m.put("WORK_LIST_TYPE_ID", wl.get(IWorkList.WORK_LIST_TYPE_ID));
		  return executeQuery(qName, m, new MapVerifyingMapCar(new String[]{IWorkListAssignment.ID}) , provider);
	  } catch (SQLException e){
		  log.error("Unable to get account work list assignment for account : " +  args, e);
		  return new TransferObject(TransferObject.ERROR, NO_SUCH_WORKLIST);
	  }
  }
  
  private ListQueryServiceIntf getListQueryService() {
	return (ListQueryServiceIntf) LookupManager.getInstance().getLookup(LookupManager.BUSINESS).getService("ListQueryService");
  }
  
  public TransferObject extractWorkListGenerationDecisionTree(Long treeId, Map root){
	  IWorkListGenerator g = getWorkListGenerator(new Date(), treeId, isCustomerLevel());
	  try{	  
		  return g.extractWorkListGenerationDecisionTree(root);
	  }catch(Exception e){
		  log.error("Unable to extract the work list generation decision tree with tree id = "+treeId, e);
		  return new TransferObject(TransferObject.ERROR, "EXTRACTION_FAILED : "+e.getMessage());
	  }
  }
  
  public TransferObject importWorkListGenerationDecisionTree(TransferObject source, Long treeId){
	  IWorkListGenerator g = getWorkListGenerator(new Date(), treeId, isCustomerLevel());
	  try{
		  return g.importWorkListGenerationDecisionTree((byte[])source.getBeanHolder());
	  }catch(Exception e){
		  log.error("Unable to import new work list generation decision tree", e );
		  return new TransferObject(e, TransferObject.ERROR, "IMPORT_FAILED : "+e.getMessage());
	  }
  }
  
  public TransferObject validateWorkListGenerationDecisionTree(Map root, List users, List workLists, List decisionOptions){
	  WorkListGenerationDecisionTreeValidator validator =  new WorkListGenerationDecisionTreeValidator(users, workLists, decisionOptions);
	  validator.checkConflict(root);
	  return new TransferObject(root);
  }
  
}
