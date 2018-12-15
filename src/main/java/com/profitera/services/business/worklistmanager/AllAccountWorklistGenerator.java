/**
 * 
 */
package com.profitera.services.business.worklistmanager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

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
import com.profitera.server.ServiceEngine;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.DateParser;

public class AllAccountWorklistGenerator implements IWorkListGenerator {
  private static final Log LOG = LogFactory.getLog(AllAccountWorklistGenerator.class);
  private static final String GET_DT_ROOT_QUERY_NAME = "getDecisionTreeRoot";
  protected static final String INSERT_DECISION_TREE_NODE = "insertDecisionTreeNode";
  private Map tree;
  private Date date;
  private boolean useCustomerMode;
  private boolean transferAtCustomerMode;
  private String code;
  private boolean multipleAccountWorklist;
  private boolean debugMode;
  private String[] fields;
  
  private Log getLog(){
    return LOG;
  }

  public void setDecisionTree(Map tree) {
    this.tree = tree;    
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public void setUseCustomerMode(boolean useCustomerMode) {
    this.useCustomerMode = useCustomerMode;
  }

  public void setTransferAtCustomerMode(boolean transferAtCustomerMode) {
    this.transferAtCustomerMode = transferAtCustomerMode;
  }
  
  public void setMultipleAccountWorklist(boolean multipleAccountWorklist) {
	this.multipleAccountWorklist = multipleAccountWorklist;
  }
  
  public void setDebugMode(boolean debugMode) {
	this.debugMode = debugMode;
  }
  
  public String getCustomerAccountQuery() {
    String q = getGeneratorProperty("accountquery");
    if (q == null){
      q = "getCustomerAccountsForAssignment";
    }
    return q;
  }
  
  public String getUserQuery() {
    String q = getGeneratorProperty("userquery");
    if (q == null){
      q = "getAllCollectorUsers";
    }
    return q;
  }

  public String getDeleteUserWorkListAssignmentQuery() {
    String q = getGeneratorProperty("deleteuserassignmentquery");
    if (q == null){
      q = "deleteExistingWorkListAssignments";
    }
    return q;
  }
  
  public NodeRunnableTransaction process(List thisCustomerAccounts, IReadWriteDataProvider p) {
    return process(thisCustomerAccounts, tree, useCustomerMode, date, new ArrayList(4), p);
  }
  
  /**
   * This trick here is that processing at account-level is the same as processing at 
   * customer-level where every customer has 1 account, so we just call this method 
   * recursively in account mode with an account list only 1 long.
   */
  protected NodeRunnableTransaction process(List thisCustomerAccounts, Map treeRoot, boolean customerMode, Date assignmentDate, List preferredWorkListIds, IReadWriteDataProvider p) {
    if (customerMode){
      Map traverseTo = treeRoot;
      //Loop until no node is selected, the previous node is then your choice.
      while (true){
        List kids = DecisionTreeUtil.getNodeChildren(traverseTo);
        Map selectedChild = getSelectedChild(thisCustomerAccounts, kids);
        if (selectedChild == null)
          break;
        traverseTo = selectedChild;
      }
      return assignTo(thisCustomerAccounts, traverseTo, assignmentDate, preferredWorkListIds, p);
    } else {
      IRunnableTransaction[] trans = new IRunnableTransaction[thisCustomerAccounts.size()];
      for (int i = 0; i < trans.length; i++) {
        List l = new ArrayList(1);
        l.add(thisCustomerAccounts.get(i));
        NodeRunnableTransaction nrt = process(l, treeRoot, true, assignmentDate, preferredWorkListIds, p);
        if (nrt.workListId != null){
          preferredWorkListIds.add(nrt.workListId);
        }
        trans[i] = nrt; 
      }
      return new NodeRunnableTransaction(null, null, new RunnableTransactionSet(trans));
    }
  }
  
  /**
   * Loops through the nodes passed in, checking each node <b>against each account</b>
   * and the first node that 'returns' true is selected for traversal.
   * NOTE: This means that in account mode the list is always 1 account long.
   * ALSO NOTE: This means that at any given level of the tree ANY account can be used to 
   * evaluate, i.e. if you have a tree like this:
   * <pre>
   * [root]
   *   - Call in progress  (A)
   *   |-- Ptp in progress (B)
   *   |-- Else            (C) 
   *   - Ptp in progress   (D)
   * </pre>
   * Then if you have a customer with 3 accounts and one has a call and another has
   * a ptp in progress and a third has no actions in progress it will end up at (B),
   * every time because no matter what order the accounts are checked the condition
   * at A and B will be checked at some point against those accounts that meet it.
   */
  protected Map getSelectedChild(List thisCustomerAccounts, List kids) {
    for (Iterator i = kids.iterator(); i.hasNext();) {
      Map node = (Map) i.next();
      String column = (String) node.get(IDecisionTreeNode.COLUMN_NAME);
      Long value = (Long) node.get(IDecisionTreeNode.COLUMN_VALUE);
      int op = ((Number) node.get(IDecisionTreeNode.COMPARE_OPERATOR)).intValue();
      for (Iterator ai = thisCustomerAccounts.iterator(); ai.hasNext();) {
        Map a = (Map) ai.next();
        Long accountValue = (Long) a.get(column);
        if (op == com.profitera.rpm.expression.Expression.EQUAL){
          if (value == null && accountValue == null){
            return node;
          } else if (value != null && accountValue != null && value.equals(accountValue)){
            return node;
          }
        } else if (value == null || accountValue == null){
          // try the next account
        } else if (op == com.profitera.rpm.expression.Expression.LESS_THAN 
            && accountValue.compareTo(value) < 0){
          return node;
        } else if (op == com.profitera.rpm.expression.Expression.GREATER_THAN
            && accountValue.compareTo(value) > 0){
          return node;
        }
      }
    }
    return null;
  }
  protected NodeRunnableTransaction assignTo(List thisCustomerAccounts, Map node, Date assignmentDate, List preferredWorkListIds, final IReadWriteDataProvider p) {
    IRunnableTransaction[] trans = new IRunnableTransaction[thisCustomerAccounts.size()];
    List worklists = DecisionTreeUtil.getNodeWorkLists(node);
    Long wlId = null;
    synchronized (worklists){
      Map wl = null;
      for (Iterator iter = worklists.iterator(); iter.hasNext();) {
        Map nodeWorkList = (Map) iter.next();
        if (preferredWorkListIds.contains(nodeWorkList.get("WORK_LIST_ID"))){
          wlId = (Long) nodeWorkList.get("WORK_LIST_ID");
          wl = nodeWorkList;
          getLog().debug("Found preferred worklist " + wlId + " at node " + node.get("ID") + " for " + ((Map)thisCustomerAccounts.get(0)).get(ICustomer.CUSTOMER_ID));
        }
      }
      if (wlId == null && worklists.size() > 0){
        Collections.shuffle(worklists);
        Collections.sort(worklists, new Comparator(){
          public int compare(Object o1, Object o2) {
            Map m1 = (Map) o1;
            Map m2 = (Map) o2;
            Long c1 = (Long) m1.get("COUNT");
            Long c2 = (Long) m2.get("COUNT");
            if (c1 == null){
              c1 = new Long(0);
              m1.put("COUNT", c1);
            }
            if (c2 == null){
              c2 = new Long(0);
              m2.put("COUNT", c2);
            }
            return c1.compareTo(c2);
          }});
        wl = (Map) worklists.get(0);
        wlId = (Long) wl.get("WORK_LIST_ID");
      }
      if (wlId != null){
        Long count = (Long) wl.get("COUNT");
        if (count == null){
          count = new Long(0);
        }
        wl.put("COUNT", new Long(count.longValue() + thisCustomerAccounts.size()));
      }
    }
    for (int i = 0; i < trans.length; i++){
      final Map account = (Map) thisCustomerAccounts.get(i);
      account.put("WORK_LIST_ID", wlId);
      trans[i] = getSetAccountWorkListTransaction(account, assignmentDate, p); 
    }
    final IRunnableTransaction tr = new RunnableTransactionSet(trans);
    return new NodeRunnableTransaction((Long) node.get("ID"), wlId, tr);
  }
  
  public IRunnableTransaction getSetAccountWorkListTransaction(final Map args, final Date assignmentDate, final IReadWriteDataProvider p) {
    if (isMultipleAccountWorklist()){
    	// these values had to be queried out first to avoid dead lock at execution time
    	if (args.get(IWorkList.WORK_LIST_ID) != null){ // IS_P2P, IS_N2P, IS_T2P or IS_P2T
	    	String assignmentType = (String)args.get(IWorkListAssignment.ASSIGNMENT_TYPE);
	    	List pwlist = getRelevantAccountWLAssignment(args, WorkListService.GET_ACCOUNT_PERMANENT_WORK_LIST_BY_TYPE);
		   	if (pwlist!= null && pwlist.size() > 0){
		   		Map pwl = (Map)pwlist.get(0);
		   		args.put("PERMANENT_ASSIGNMENT_ID", pwl);
		   	}
	    	if (assignmentType != null && assignmentType.equals("IS_P2T")){
		    	List twlist = getRelevantAccountWLAssignment(args, WorkListService.GET_ACCOUNT_TEMPORARY_WORK_LIST_BY_TYPE);
			   	if (twlist!= null && twlist.size() > 0){
			   		Map twl = (Map)twlist.get(0);
			   		args.put("TEMPORARY_ASSIGNMENT_ID", twl);
			   	}
	    	}
    	}
    }
    return new IRunnableTransaction(){
      public void execute(ITransaction t) throws SQLException, AbortTransactionException {
        args.put(IWorkList.WORK_LIST_ID, args.get(IWorkList.WORK_LIST_ID));
        args.put(IWorkListAssignment.ASSIGNMENT_DATE, assignmentDate);
        int count = p.update(WorkListService.UPDATE_ACCOUNT_WORK_LIST_HISTORY, args, t);
        if (count == 0){
          p.insert(WorkListService.INSERT_ACCOUNT_WORK_LIST_HISTORY, args, t);
        }
        p.update(WorkListService.SET_ACCOUNT_WORK_LIST, args, t);        
        if (isMultipleAccountWorklist()){
        	String assignmentType = (String)args.get(IWorkListAssignment.ASSIGNMENT_TYPE);
        	if (args.get(IWorkList.WORK_LIST_ID) != null){ // IS_P2P, IS_N2P, IS_N2PT, IS_P2PT, IS_T2P or IS_P2T
        		Map pwl = (Map)args.get("PERMANENT_ASSIGNMENT_ID");
	   			if (assignmentType != null && assignmentType.equals("IS_T2P")){
	        		// for type IS_T2P, element "ASSIGNMENT_ID_TO_BE_DELETED" is expected
	        		p.delete(WorkListService.DELETE_ACCOUNT_WORK_LIST_ASSIGN_BY_ID, args.get("ASSIGNMENT_ID_TO_BE_DELETED"), t);
	       			if (pwl != null){ // activate the permanent work list
		        		pwl.put(IWorkListAssignment.STATUS_ID, IWorkListAssignment.ACTIVE_STATUS);
		        		p.update(WorkListService.UPDATE_ACCOUNT_WORK_LIST_ASSIGN_STATUS, pwl, t);
	       			}
	        	} else if (assignmentType != null && assignmentType.equals("IS_P2T")){
	        		// activate the temporary work list
	        		Map twl = (Map)args.get("TEMPORARY_ASSIGNMENT_ID");
	        		if (twl != null){
		        		twl.put(IWorkListAssignment.STATUS_ID, IWorkListAssignment.ACTIVE_STATUS);
		        		p.update(WorkListService.UPDATE_ACCOUNT_WORK_LIST_ASSIGN_STATUS, twl, t);
	        		} else {
	        			args.put(IWorkListAssignment.STATUS_ID, IWorkListAssignment.ACTIVE_STATUS);
	        			args.put(IWorkListAssignment.EXPIRATION_DATE, args.get("EXPIRATION_DATE"));
	        			p.insert(WorkListService.INSERT_ACCOUNT_WORK_LIST_ASSIGN, args, t);
	        		}
	       			if (pwl != null){ // deactivate the permanent work list
		        		pwl.put(IWorkListAssignment.STATUS_ID, IWorkListAssignment.INACTIVE_STATUS);
		        		p.update(WorkListService.UPDATE_ACCOUNT_WORK_LIST_ASSIGN_STATUS, pwl, t);
	       			}
	       		} else if (assignmentType != null && assignmentType.equals("IS_2PWL")){
	       			//delete old work list assignment for IS_P2P with originating P from a diferrent work list type
	        		if (args.get("OLD_WORK_LIST_ASSIGNMENT_LIST") != null){
	        			List owl = (List)args.get("OLD_WORK_LIST_ASSIGNMENT_LIST");
	        			for (Iterator iter = owl.iterator(); iter.hasNext();){
	        				Map owlm = (Map)iter.next();
	        				p.delete(WorkListService.DELETE_ACCOUNT_WORK_LIST_ASSIGN_BY_ID, owlm.get("ID"), t);
	        			}
	        		}
	        		List pwllist  = (List) args.get("PREFERRED_WL_ASSIGNMENT");
	        		if (pwllist != null && pwllist.size()> 0){
	        			for (Iterator i = pwllist.iterator(); i.hasNext();){
	        				Map pwlm  = (Map)i.next();
			       			pwlm.put("ACCOUNT_ID", args.get("ACCOUNT_ID"));
			       			if (DateParser.compareDate((Date)args.get(IWorkListAssignment.ASSIGNMENT_DATE), (Date)pwlm.get(IWorkListAssignment.ASSIGNMENT_DATE)) >= 0){
			       				pwlm.put(IWorkListAssignment.ASSIGNMENT_DATE, args.get(IWorkListAssignment.ASSIGNMENT_DATE));
			       			}
			       			p.insert(WorkListService.INSERT_ACCOUNT_WORK_LIST_ASSIGN, pwlm, t);
	        			}
	        		}
	        	} else { // default - IS_P2P or IS_N2P
        			if (pwl != null){  //IS_P2P
        				p.delete(WorkListService.DELETE_ACCOUNT_WORK_LIST_ASSIGN_BY_ID, pwl.get("ID"), t);
	        			// insert new permanent work list assignment with status of previous work list
	        			args.put(IWorkListAssignment.STATUS_ID, pwl.get("STATUS_ID"));
	        			p.insert(WorkListService.INSERT_ACCOUNT_WORK_LIST_ASSIGN, args, t);
        			} else { // IS_N2P  
	        			// insert new permanent work list assignment with status Active
	        			args.put(IWorkListAssignment.STATUS_ID, IWorkListAssignment.ACTIVE_STATUS);
	        			p.insert(WorkListService.INSERT_ACCOUNT_WORK_LIST_ASSIGN, args, t);
        			}
        		} 
        	} else { // IS_P2N
       			// remove account from work list 
        		// this will never happen as this method is currently used for generation and account transfers
        	}
        }
      }};
  }

  private WorkListServiceIntf getWorkListService() {
	return (WorkListServiceIntf) LookupManager.getInstance().getLookup(LookupManager.BUSINESS).getService("WorkListService");
  }
  
  protected List getRelevantAccountWLAssignment(Map args, String qName){
	  TransferObject result = getWorkListService().getAccountWorkListAssignment(args, qName);
	  if (result.isFailed()){
		  return null;
	  }
	  return (List)result.getBeanHolder();
  }
  
  protected Date getDate() {
    return date;
  }

  protected Map getTree() {
    return tree;
  }

  public boolean isUseCustomerMode() {
    return useCustomerMode;
  }

  public boolean isTransferAtCustomerMode() {
    return transferAtCustomerMode;
  }

  public boolean isMultipleAccountWorklist() {
    return multipleAccountWorklist;
  }
  
  public boolean isDebugMode() {
	return debugMode;
  }

  public IRunnableTransaction getUserWorkListAssignmentTransaction(final String userId,final Object[] workListIds,final Date date, final IReadWriteDataProvider provider ) {
	  return new IRunnableTransaction(){
	        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
	            provider.delete(getDeleteUserWorkListAssignmentQuery(), userId, t);
	            Map args = new HashMap();
	            args.put(IUser.USER_ID, userId);
	            args.put("DATE", date);
	            for (int j = 0; j < workListIds.length; j++) {
	              args.put(IWorkList.WORK_LIST_ID, workListIds[j]);
	              provider.insert("insertUserWorkListAssignment", args, t);
	            }
	          }};
  }

  public void setGeneratorCode(String code) {
    this.code = code;
  }
  
  protected String getGeneratorProperty(String prop){
    return ServiceEngine.getProp(WorkListService.MODULE_NAME + "." + WorkListService.GENERATOR + "." + code + "." + prop);
  }

  public String getWorkListQuery() {
    String q = getGeneratorProperty("worklistquery");
    if (q == null){
      q = "getAllWorkLists";
    }
    return q;
  }

  public String getDecisionTreeOptionsQuery() {
    String q = getGeneratorProperty("optionsquery");
    if (q == null){
      q = "getWorkListGenerationOptions";
    }
    return q;
  }

  protected String getDecisionTreeRootQuery() {
	String q = getGeneratorProperty("treerootquery");
	if (q == null){
		q = GET_DT_ROOT_QUERY_NAME;
	}
	return q;
  }

  private ListQueryServiceIntf getListQueryService() {
	return (ListQueryServiceIntf) LookupManager.getInstance().getLookup(LookupManager.BUSINESS).getService("ListQueryService");
  }
  
  public TransferObject getWorkListGenerationDecisionTree(Long treeId, Map arguments, final IReadWriteDataProvider provider) {
    if (arguments == null) arguments = new HashMap();
	arguments.put("TREE_ID", treeId);
	TransferObject result = getListQueryService().getQueryList(getDecisionTreeRootQuery(), arguments);
	if (result.isFailed())
        return result;
    List l = (List) result.getBeanHolder();
    if (l == null || l.size() == 0){
        return getEmptyTree(treeId);
    }
    Map resultMap = (Map)l.get(0);
    return new TransferObject(resultMap);  
  }

  private TransferObject getEmptyTree(Long root) {
    // Root is not yet defined, this is bad!
    return new TransferObject(new Object[]{root}, TransferObject.ERROR, "DECISION_TREE_ROOT_NOT_DEFINED");
  }      
    
  public IRunnableTransaction getBuildTree(Long treeId, Map oldRoot, final Map root, Map parameters, final IReadWriteDataProvider provider) {
      final Long oldRootId = (Long) oldRoot.get("ID");
      return new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          List kids = DecisionTreeUtil.getNodeChildren(root);
          insertChildren(oldRootId, kids, t, provider);
        }

        private void insertChildren(Long parentId, List kids, ITransaction t, IReadWriteDataProvider provider) throws SQLException {
          if (kids == null) return;
          for (Iterator i = kids.iterator(); i.hasNext();) {
            Map k = (Map) i.next();
            k.put(IDecisionTreeNode.PARENT_NODE, parentId);
            //System.err.println(k);
			if (k.get(IDecisionTreeNode.COMPARE_OPERATOR) == null)
				k.put(IDecisionTreeNode.COMPARE_OPERATOR, new Long(com.profitera.rpm.expression.Expression.EQUAL));
            Long id = (Long) provider.insert(INSERT_DECISION_TREE_NODE, k, t);
            List workLists = DecisionTreeUtil.getNodeWorkLists(k);
            if (workLists != null){
              for (Iterator wi = workLists.iterator(); wi.hasNext();) {
                Map wl = (Map) wi.next();
                wl.put("NODE_ID", id);
                provider.insert(INSERT_DECISION_TREE_NODE + "WorkListRelation", wl, t);
                List users = DecisionTreeUtil.getNodeUsers(wl);
                if (users != null){
                  for (Iterator ui = users.iterator(); ui.hasNext();) {
                    Map u = (Map) ui.next();
                    u.put("NODE_ID", id);
                    u.put(IWorkList.WORK_LIST_ID, wl.get(IWorkList.WORK_LIST_ID));
                    provider.insert(INSERT_DECISION_TREE_NODE + "UserWorkListRelation", u, t);
                  }
                }
              }
            }
            insertChildren(id, (List) DecisionTreeUtil.getNodeChildren(k), t, provider);
          }
        }};
  }
  
  public IRunnableTransaction getDeleteCurrentTree(final Map oldRoot, final IReadWriteDataProvider provider) {
      final List ids = getAllDescendantIds(oldRoot);
      return new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          for (Iterator i = ids.iterator(); i.hasNext();) {
            Long id = (Long) i.next();
            provider.delete("deleteDecisionTreeNodeUserWorkListRelations", id, t);
            provider.delete("deleteDecisionTreeNodeWorkListRelations", id, t);
            provider.delete("deleteDecisionTreeNode", id, t); 
          }
        }};
  }

  protected List getAllDescendantIds(Map oldRoot) {
      List children = DecisionTreeUtil.getNodeChildren(oldRoot);
      List childIds = new ArrayList();
      if (children == null || children.size() == 0)
        return Collections.EMPTY_LIST;
      for (Iterator i = children.iterator(); i.hasNext();) {
        Map m = (Map) i.next();
        // NB: The order here is very important because this list is used to 
        // delete nodes, and the children need to be deleted first.
        childIds.addAll(getAllDescendantIds(m));
        childIds.add(m.get("ID"));
      }
      return childIds;
  }

  public IRunnableTransaction processTransfer(List thisCustomerAccounts, List transferToList, Map args, IReadWriteDataProvider p) {
	  return processTransfer(thisCustomerAccounts, transferToList, args, transferAtCustomerMode, date, new ArrayList(4), p);
  }
  
  protected IRunnableTransaction processTransfer(List thisCustomerAccounts, List transferToList, Map args, boolean customerMode, Date assignmentDate, List preferredWorkListIds, IReadWriteDataProvider p) {
      return new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          // NOOP
        }};
  }

  public IRunnableTransaction getSetAccountTemporaryWorkListTransaction(final Map args, final Date assignmentDate, final Date expirationDate, final IReadWriteDataProvider p) {
    // this has to be query out first to avoid dead lock at execution time
   	List twlist = getRelevantAccountWLAssignment(args, WorkListService.GET_ACCOUNT_TEMPORARY_WORK_LIST_BY_TYPE);
   	if (twlist!= null && twlist.size() > 0){
   		Map twl = (Map)twlist.get(0);
   		args.put("TEMPORARY_ASSIGNMENT_ID", twl);
   	}
    return new IRunnableTransaction(){
      public void execute(ITransaction t) throws SQLException, AbortTransactionException {
    	  // get existing temporary work list of the same work list type 
    	  Map twl = (Map)args.get("TEMPORARY_ASSIGNMENT_ID");
    	  // delete temporary worklist assignment (of the same work list type) if exists
    	  if (twl != null){
    		  p.delete(WorkListService.DELETE_ACCOUNT_WORK_LIST_ASSIGN_BY_ID, twl.get("ID"), t);    		  
    	  }
    	  // insert new temporary work list assignment
    	  args.put(IWorkListAssignment.ASSIGNMENT_DATE, assignmentDate);
    	  args.put(IWorkListAssignment.EXPIRATION_DATE, expirationDate);
    	  args.put(IWorkListAssignment.STATUS_ID, IWorkListAssignment.INACTIVE_STATUS);
    	  p.insert(WorkListService.INSERT_ACCOUNT_WORK_LIST_ASSIGN, args, t);
      }};
  }
  
  public TransferObject extractWorkListGenerationDecisionTree(Map root) throws SAXException, IOException, TransformerConfigurationException{
	  WorkListGenerationDecisionTreeExtractor w = new WorkListGenerationDecisionTreeExtractor();
	  byte[] xmlFile = w.exportToXML(root);
	  return new TransferObject(xmlFile); 
  }
  
  public TransferObject importWorkListGenerationDecisionTree(byte[] source) throws SAXException, IOException, ParserConfigurationException, Exception{
	  Map root = new WorkListGenerationDecisionTreeParser().getWorkListEntitiesFromXML(source);
	  return new TransferObject(root);
  }

  public String[] getDataGroupingFields() {
    return fields;
  }

  public void setDataGroupingFields(String[] fields) {
    this.fields = fields;
  }
 
}