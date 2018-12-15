/**
 * 
 */
package com.profitera.services.business.worklistmanager;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.profitera.descriptor.business.meta.ICustomer;
import com.profitera.descriptor.business.meta.IDecisionTreeNode;
import com.profitera.descriptor.business.meta.IWorkList;
import com.profitera.descriptor.business.meta.IWorkListAssignment;
import com.profitera.descriptor.business.meta.IWorkListDistribution;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.util.DateParser;
import com.profitera.util.RandomKeyGenerator;

/**
 * @author cmlow
 *
 */
public class ChargeOffAccountWorklistGenerator extends
		AllAccountWorklistGenerator implements IWorkListGenerator {
	
  private static final Log LOG = LogFactory.getLog(ChargeOffAccountWorklistGenerator.class);
  private static final String ACCOUNT_STATUS_CODE = "DEBTREC_STATUS_CODE";
  private static final String CHARGE_OFF_STATUS_CODE = "5";
  private static final Long CHARGE_OFF_WORK_LIST_TYPE = new Long(2);

  public IRunnableTransaction getBuildTree(Long treeId, Map oldRoot, final Map root, Map parameters, final IReadWriteDataProvider provider) {
      //final Long oldRootId = (Long) oldRoot.get("NODE_ID");
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
            Long id = (Long) provider.insert("insertDecisionTreeNode", k, t);
            List workLists = DecisionTreeUtil.getNodeWorkLists(k);
            if (workLists != null){
              for (Iterator wi = workLists.iterator(); wi.hasNext();) {
                Map wl = (Map) wi.next();
                wl.put("NODE_ID", id);
                provider.insert("insertDecisionTreeNodeWorkListRelation", wl, t);
                provider.insert("insertDecisionTreeNodeWorkListDistributionRelation", wl, t);
                List users = DecisionTreeUtil.getNodeUsers(wl);
                if (users != null){
                  for (Iterator ui = users.iterator(); ui.hasNext();) {
                    Map u = (Map) ui.next();
                    u.put("NODE_ID", id);
                    u.put(IWorkList.WORK_LIST_ID, wl.get(IWorkList.WORK_LIST_ID));
                    provider.insert("insertDecisionTreeNodeUserWorkListRelation", u, t);
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
            provider.delete("deleteDecisionTreeNodeWorkListDistributionRelations", id, t);
            provider.delete("deleteDecisionTreeNodeUserWorkListRelations", id, t);
            provider.delete("deleteDecisionTreeNodeWorkListRelations", id, t);
            provider.delete("deleteDecisionTreeNode", id, t); 
          }
        }};
  }
	
  protected NodeRunnableTransaction process(List thisCustomerAccounts, Map treeRoot, boolean customerMode, Date assignmentDate, List preferredWorkListIds, IReadWriteDataProvider p) {
	  List newCOAccountList = new ArrayList();
	  List assignmentTrans = new ArrayList();
	  List preferredWLAssignment = null;
	  for (Iterator i = thisCustomerAccounts.iterator(); i.hasNext();){
		  Map a = (Map) i.next();
	      if (!a.containsKey(IWorkList.WORK_LIST_ID)){
	    	  throw new RuntimeException("Primary generation query missing required column: " + IWorkList.WORK_LIST_ID);
	      }
	      if (!a.containsKey(IWorkList.WORK_LIST_TYPE_ID)){
	    	  throw new RuntimeException("Primary generation query missing required column: " + IWorkList.WORK_LIST_TYPE_ID);
	      }
	      if (!a.containsKey(ACCOUNT_STATUS_CODE)){
	    	  throw new RuntimeException("Primary generation query missing required column: " + ACCOUNT_STATUS_CODE);
	      }
		  if (a.get(ACCOUNT_STATUS_CODE) != null && a.get(ACCOUNT_STATUS_CODE).equals(CHARGE_OFF_STATUS_CODE)){
			  if (a.get(IWorkList.WORK_LIST_TYPE_ID) != null && a.get(IWorkList.WORK_LIST_TYPE_ID).equals(CHARGE_OFF_WORK_LIST_TYPE)){
				  // an existing charge-off account already exists
				  if (preferredWorkListIds.size() == 0){
					  if (isMultipleAccountWorklist()){
						  // get preferred work list assignments
						 preferredWLAssignment = getRelevantAccountWLAssignment(a, WorkListService.GET_ACCOUNT_WORK_LIST_ASSIGNMENT);
					  }
					  preferredWorkListIds.add(a.get(IWorkList.WORK_LIST_ID));
				  }
			  } else {
				  // check if the account already exist in the list
				  boolean bDuplicate = false;
				  for (Iterator it = newCOAccountList.iterator(); it.hasNext();){
					 Map m = (Map)it.next();
					 if (m.get("ACCOUNT_ID").equals(a.get("ACCOUNT_ID"))){
						 bDuplicate = true;
					 }
				  }
				  if (!bDuplicate){
					  newCOAccountList.add(a);
				  }
			  }
		  }
	  }
	  
	  if (preferredWorkListIds.size()> 0){
		  Long wlId = (Long)preferredWorkListIds.get(0);
		  for (Iterator i = newCOAccountList.iterator(); i.hasNext();){
			  Map a = (Map)i.next();
			  if (isMultipleAccountWorklist()){
				  a.put(IWorkListAssignment.ASSIGNMENT_TYPE, "IS_2PWL"); 
				  a.put("PREFERRED_WL_ASSIGNMENT", preferredWLAssignment);
				  // need to check if an existing work list id exists
				  if (a.get(IWorkList.WORK_LIST_ID) != null){
					  List owlist = getRelevantAccountWLAssignment(a, WorkListService.GET_ACCOUNT_WORK_LIST_ASSIGNMENT);
					  a.put("OLD_WORK_LIST_ASSIGNMENT_LIST", owlist);
				  }
			  }
			  a.put(IWorkList.WORK_LIST_ID, wlId);
			  assignmentTrans.add(getSetAccountWorkListTransaction(a, getDate(), p));
		  }
	  } else {
		  NodeRunnableTransaction t = processInternal(newCOAccountList, treeRoot, customerMode, assignmentDate, preferredWorkListIds, p);
		  if (t != null){
			  assignmentTrans.add(t);
		  }
	  }
	  
	  IRunnableTransaction[] trans = (IRunnableTransaction[]) assignmentTrans.toArray(new IRunnableTransaction[0]);
	  return new NodeRunnableTransaction(null, null, new RunnableTransactionSet(trans));
  }

  private NodeRunnableTransaction processInternal(List thisCustomerAccounts, Map treeRoot, boolean customerMode, Date assignmentDate, List preferredWorkListIds, IReadWriteDataProvider p) {
    if (customerMode){
      Long preferredNodeId = null; 
      if (preferredWorkListIds.size() > 0){
    	  Map m = (Map)preferredWorkListIds.get(0);
    	  preferredNodeId = (Long)m.get("PREFERRED_NODE_ID");
      }
      Map traverseTo = treeRoot;      
      //Loop until no node is selected, the previous node is then your choice.
      while (true){
        List kids = DecisionTreeUtil.getNodeChildren(traverseTo);
        Map selectedChild;
        if (preferredNodeId != null){
        	selectedChild = getPreferredChild(kids, preferredNodeId);
	        if (selectedChild != null){
	          traverseTo = selectedChild;
	          break;
	        }
	        
        } else {
        	selectedChild = getSelectedChild(thisCustomerAccounts, kids);
	        if (selectedChild == null)
	          break;
        }
        traverseTo = selectedChild;
      }
      return assignTo(thisCustomerAccounts, traverseTo, assignmentDate, preferredWorkListIds, p);
    } else {
      IRunnableTransaction[] trans = new IRunnableTransaction[thisCustomerAccounts.size()];
      for (int i = 0; i < trans.length; i++) {
        List l = new ArrayList(1);
        l.add(thisCustomerAccounts.get(i));
        NodeRunnableTransaction nrt = processInternal(l, treeRoot, true, assignmentDate, preferredWorkListIds, p);
        if (nrt.workListId != null){
        	Map m = new HashMap();
        	m.put("PREFERRED_NODE_ID", nrt.nodeId);
        	m.put("PREFFERED_WORK_LIST_ID", nrt.workListId);
        	preferredWorkListIds.add (m);
        }
        trans[i] = nrt; 
      }
      return new NodeRunnableTransaction(null, null, new RunnableTransactionSet(trans));
    }
  }
  
  private Map getPreferredChild(List kids, Long preferredNodeId) {
    for (Iterator i = kids.iterator(); i.hasNext();) {
      Map node = (Map) i.next();
      if (node.get("ID").equals(preferredNodeId)){
    	  return node;
      }
    }
    return null;
  }
  
  protected NodeRunnableTransaction assignTo(List thisCustomerAccounts, Map node, Date assignmentDate, List preferredWorkListIds, final IReadWriteDataProvider p) {
    IRunnableTransaction[] trans = new IRunnableTransaction[thisCustomerAccounts.size()];
    List worklists = DecisionTreeUtil.getNodeWorkLists(node);
    if (worklists.size() == 0)
    	return null;
    
    Long wlId = null;
    boolean bFirstTime = false;
    synchronized (worklists){
      Map wl = null;
   	  for (Iterator i = worklists.iterator(); i.hasNext();){
   		  Map m = (Map)i.next();
   		  Long totalNodeAccount = (Long)m.get("TCOUNT");
   		  if (totalNodeAccount == null){
   			  totalNodeAccount = new Long(0);
   			  m.put("TCOUNT", totalNodeAccount);
   		  }
   		  if (totalNodeAccount.longValue() == 0){
   			  bFirstTime = true;
   		  }
   		  Long count = (Long) m.get("COUNT");
   		  if (count == null){
   			  count = new Long(0);
   			  m.put("COUNT", count);
   		  }
   		  
   		  if (!bFirstTime){
   			  Double d = (Double)m.get(IWorkListDistribution.DISTRIBUTION_PERC);
   			  Double currPercentage = new Double((count.doubleValue()/ totalNodeAccount.doubleValue()) * 100);
   			  m.put("CURR_PERCENTAGE", currPercentage);
   			  Double diff = new Double(currPercentage.doubleValue() - d.doubleValue());
   			  m.put("DIFF_PERCENTAGE", diff);
   		  }
   	  }
   	  
   	  if(preferredWorkListIds.size() > 0){
   		Map m = (Map)preferredWorkListIds.get(0);
	      for (Iterator iter = worklists.iterator(); iter.hasNext();) {
	        Map nodeWorkList = (Map) iter.next();
       		if (m.get("PREFFERED_WORK_LIST_ID").equals(nodeWorkList.get(IWorkList.WORK_LIST_ID))){
	          wlId = (Long) nodeWorkList.get(IWorkList.WORK_LIST_ID);
	          wl = nodeWorkList;
	          getLog().debug("Found preferred worklist " + wlId + " at node " + node.get("ID") + " for " + ((Map)thisCustomerAccounts.get(0)).get(ICustomer.CUSTOMER_ID));          
	          break;
       		}
	      }
   	  }

      for (int i = 0; i < thisCustomerAccounts.size(); i++){
          final Map account = (Map) thisCustomerAccounts.get(i);    	  
	      if (wlId == null){
	    	  // create a duplicate worklist list for processing
	    	  List worklists2 = new ArrayList();
		   	  for (Iterator it = worklists.iterator(); it.hasNext();){
		   		  worklists2.add(it.next());
		   	  } 
	    	  if (bFirstTime){
					  Collections.sort(worklists2, new Comparator(){
					  	public int compare(Object o1, Object o2) {
							Map m1 = (Map) o1;
							Map m2 = (Map) o2;
					   		Double d1 = (Double)m1.get(IWorkListDistribution.DISTRIBUTION_PERC);
					   		Double d2 = (Double)m2.get(IWorkListDistribution.DISTRIBUTION_PERC);
					   		if (d1.doubleValue() == d2.doubleValue()){
					   			return Integer.parseInt(RandomKeyGenerator.generateRandomKey(3)) % 2;
					   		}
							return d2.compareTo(d1);
					   	}
					  }); 
					  // assign to the work list with highest distribution percentage
					  Map m = (Map)worklists2.get(0);
					  wl = m;
	    	  } else {
					  Collections.sort(worklists2, new Comparator(){
					  	public int compare(Object o1, Object o2) {
								Map m1 = (Map) o1;
								Map m2 = (Map) o2;
					   		Double d1 = (Double)m1.get(IWorkListDistribution.DISTRIBUTION_PERC);
					   		Double d2 = (Double)m2.get(IWorkListDistribution.DISTRIBUTION_PERC);
					   		// sort list to have the work list from with most negative different to allocated percentage comes first
	    			    Double diff1 = (Double) m1.get("DIFF_PERCENTAGE");
	    			    Double diff2 = (Double) m2.get("DIFF_PERCENTAGE");
	    			    if (diff1.doubleValue() == diff2.doubleValue()){
	    			    	 // if different is the same, put the higher distribution percentage first
	    			    	 if (d1.doubleValue() == d2.doubleValue()){
	    			    		 return Integer.parseInt(RandomKeyGenerator.generateRandomKey(3)) % 2;
	    			    	 } else {
	    			    		 if (d2.doubleValue() > d1.doubleValue()){
	    			    			 return diff2.compareTo(diff1);
	    			    		 }
	    			    	 }
	    			    }
	    			    return diff1.compareTo(diff2);	    			    
				  		}
					  });
				  
		    	  while (wl == null){			    	  
					    Double prevDPC = null;			  
		  	    	Long newAllocation = null;			  
			    	  for (Iterator it = worklists2.iterator(); it.hasNext();){
			    		  Map m = (Map)it.next();
						  /* If the allocation to the first work list in the list, check if the allocation is larger than the 
						   	 allocation of a work list with larger percentage
						  */
			    		  Double currDPC = (Double) m.get(IWorkListDistribution.DISTRIBUTION_PERC);
			    		  Long currAllocated = (Long) m.get("COUNT");
			    		  if (currDPC != null && currDPC.doubleValue() != 0.0){
			    			  if (prevDPC == null) {
		  	    				  prevDPC = currDPC;
			    				  newAllocation = new Long(currAllocated.longValue() + thisCustomerAccounts.size()); 
			    				  wl = m;
			    			  } else {
			    				  if (currDPC.doubleValue() > prevDPC.doubleValue()
			    						  && newAllocation.longValue() > currAllocated.longValue()){
			    					  worklists2.remove(m);
			    					  wl = null;
			    					  break;
			    				  }
			    			  }
		    			  }
			    	  } // end for
		    	  } // end while (wl == null)
	    	  } // end if(bFirstTime)
	      } // end if (wlId == null)
    	  // set count
    	  Long cnt = (Long)wl.get("COUNT");
    	  wl.put("COUNT", new Long(cnt.longValue() + 1));
    	  wlId = (Long) wl.get(IWorkList.WORK_LIST_ID);
    	  account.put(IWorkList.WORK_LIST_ID, wlId);
    	  trans[i] = getSetAccountWorkListTransaction(account, assignmentDate, p); 
      }
      // set total count
   	  for (Iterator it = worklists.iterator(); it.hasNext();){
   		  Map m = (Map)it.next();
   		  Long totalCnt = (Long)m.get("TCOUNT");
   		  m.put("TCOUNT", new Long(totalCnt.longValue() + thisCustomerAccounts.size()));
   	  }
    }

    if (isDebugMode()){
	    // print status after processing this customer/account
    	if (isUseCustomerMode())
    		getLog().debug("Generation status after processing customer " + ((Map)thisCustomerAccounts.get(0)).get(ICustomer.CUSTOMER_ID));
    	else 
    		getLog().debug("Generation status after processing account " +  ((Map)thisCustomerAccounts.get(0)).get("ACCOUNT_ID") + " of " + ((Map)thisCustomerAccounts.get(0)).get(ICustomer.CUSTOMER_ID));
	    for (Iterator x = worklists.iterator(); x.hasNext();){
		    Map wlm = (Map) x.next();
		    getLog().debug("WL ID = " + wlm.get(IWorkList.WORK_LIST_ID));
		    getLog().debug("WL DISTRIBUTION PERC = " + wlm.get(IWorkListDistribution.DISTRIBUTION_PERC));
		    getLog().debug("WL COUNT ASSIGNED = " + wlm.get("COUNT"));
		    getLog().debug("WL TOTAL ACCOUNT = " + wlm.get("TCOUNT"));
		    getLog().debug("PERCENTAGE COUNT = " + ((Long)wlm.get("COUNT")).doubleValue()/((Long)wlm.get("TCOUNT")).doubleValue() * 100);
		    getLog().debug("\n");
	    }
    }
    
    final IRunnableTransaction tr = new RunnableTransactionSet(trans);
    return new NodeRunnableTransaction((Long) node.get("ID"), wlId, tr);
  }

  protected IRunnableTransaction processTransfer(List thisCustomerAccounts, List transferToList, Map args, boolean customerMode, Date assignmentDate, List preferredWorkListIds, IReadWriteDataProvider p) {
	  if (customerMode){
		  return transferTo(thisCustomerAccounts, transferToList, args, assignmentDate, preferredWorkListIds, p);
	  } else {
	      IRunnableTransaction[] trans = new IRunnableTransaction[thisCustomerAccounts.size()];
	      for (int i = 0; i < trans.length; i++) {
	        List l = new ArrayList(1);
	        l.add(thisCustomerAccounts.get(i));
	        IRunnableTransaction nrt = processTransfer(l, transferToList, args, true, assignmentDate, preferredWorkListIds, p);
	        trans[i] = nrt; 
	      }
	      return new RunnableTransactionSet(trans);
	  }
  }

  protected IRunnableTransaction transferTo(List thisCustomerAccounts, List transferToList, Map args, Date assignmentDate, List preferredWorkListIds, final IReadWriteDataProvider p) {
    IRunnableTransaction[] trans = new IRunnableTransaction[thisCustomerAccounts.size()];
    Long wlId = null;
    Map wl = null;
    boolean isColumnExists = true;
    
    Map f = (Map)thisCustomerAccounts.get(0); 
    String type = (String) args.get(IWorkListDistribution.DISTRIBUTION_TYPE);
    if (!f.containsKey(type)) {
    	isColumnExists = false;
    }
    
    // get total value to be processed
    double totalVal = 0; 
    if (isColumnExists){
	    for (Iterator i = thisCustomerAccounts.iterator(); i.hasNext();){
	    	Map a = (Map)i.next();
	    	Double val = (Double)a.get(type);
	    	if (val == null){
	    		val = new Double(0);
	    	}
	    	totalVal += val.doubleValue();
	    }
    } else {
    	totalVal = thisCustomerAccounts.size();
    }    
    
    for (Iterator i = transferToList.iterator(); i.hasNext();){
    	Map m = (Map)i.next();
   		// set total value processed so far
    	Double totalValProcessed = (Double)m.get("TOTAL_VALUE_PROCESSED");
    	if (totalValProcessed == null){
    		totalValProcessed = new Double(0);
    	}
   		m.put("TOTAL_VALUE_PROCESSED", new Double(totalValProcessed.doubleValue() + new Double(totalVal).doubleValue()));       		
    }
   	  
    if(preferredWorkListIds.size() > 0){
	    for (Iterator it = transferToList.iterator(); it.hasNext();) {
	    	Map wlm = (Map) it.next();	    	
	    	if(preferredWorkListIds.contains(wlm.get(IWorkList.WORK_LIST_ID))){
       			wlId = (Long) wlm.get(IWorkList.WORK_LIST_ID);
       			wl = wlm;
       			getLog().debug("Found preferred worklist " + wlId + " for " + ((Map)thisCustomerAccounts.get(0)).get(ICustomer.CUSTOMER_ID));
       			break;
	    	}
	    }
    }
    
    for (int i = 0; i < thisCustomerAccounts.size(); i++){
    	final Map account = (Map) thisCustomerAccounts.get(i);
    	if (wlId == null){
    		Double sd = null;
    		for (Iterator it = transferToList.iterator(); it.hasNext();){
		    	Map m = (Map)it.next();
		    	Double dpc = (Double) m.get(IWorkListDistribution.DISTRIBUTION_PERC);
		    	
		    	Double totalValProcessed = (Double) m.get("TOTAL_VALUE_PROCESSED");    		  
		    	Double accumulatedValAssigned = (Double) m.get("ACCUMULATED_VALUE_ASSIGNED");
		    	if (accumulatedValAssigned == null){
		    		accumulatedValAssigned = new Double(0);
		    		m.put("ACCUMULATED_VALUE_ASSIGNED", accumulatedValAssigned);
		    	}
		    	
		    	if (dpc != null && dpc.doubleValue() != 0.0){
		    		if (sd == null){
		    			sd = new Double(((accumulatedValAssigned.doubleValue() + new Double(totalVal).doubleValue()) / totalValProcessed.doubleValue()) - (dpc.doubleValue()/100.0));
		    			wl = m;
		    		} else {
		    			Double sd2 = new Double(((accumulatedValAssigned.doubleValue() + new Double(totalVal).doubleValue()) / totalValProcessed.doubleValue()) - (dpc.doubleValue()/100.0));
		    			if (Math.pow(sd2.doubleValue(),2) < Math.pow(sd.doubleValue(),2)){
		    				sd = sd2;
		    				wl = m;
		    			}
		    		}
		    	}
    		}
    	}
    	
    	// set value assigned
    	Double accumVal = (Double)wl.get("ACCUMULATED_VALUE_ASSIGNED");
    	if (isColumnExists){
    		Double val = (Double)account.get(type);
    		wl.put("ACCUMULATED_VALUE_ASSIGNED", new Double(accumVal.doubleValue() + val.doubleValue()));
    	} else {
    		wl.put("ACCUMULATED_VALUE_ASSIGNED", new Double(accumVal.doubleValue() + 1));
    	}
    		
    	wlId = (Long) wl.get(IWorkList.WORK_LIST_ID);
    	
    	// set preferred worklist
    	preferredWorkListIds.add(wlId);
    	
    	account.put(IWorkList.WORK_LIST_ID, wlId);
    	
    	boolean isTemporary = ((Boolean)args.get(IWorkListAssignment.IS_TEMPORARY)).booleanValue();
    	if (isTemporary) {   
    		// check if the temporary assignment is effective today
    		Date tempAssignmentDate = (Date)args.get(IWorkListAssignment.ASSIGNMENT_DATE);
    		Date expirationDate = (Date)args.get(IWorkListAssignment.EXPIRATION_DATE);
    		if (DateParser.isEqualDate(tempAssignmentDate, assignmentDate)){
    			account.put(IWorkListAssignment.ASSIGNMENT_TYPE, "IS_P2T");
    			account.put(IWorkListAssignment.EXPIRATION_DATE, expirationDate);
    			trans[i] = getSetAccountWorkListTransaction(account, tempAssignmentDate, p);
    		} else { 
	   			trans[i] = getSetAccountTemporaryWorkListTransaction(account, tempAssignmentDate, expirationDate, p);
    		}
    	} else {
    		trans[i] = getSetAccountWorkListTransaction(account, assignmentDate, p);
    	}
    }
    
    if (isDebugMode()){
	    // print status after processing this customer/account
    	if (isTransferAtCustomerMode())
    		getLog().debug("Transfer status after processing customer " + ((Map)thisCustomerAccounts.get(0)).get(ICustomer.CUSTOMER_ID));
    	else 
    		getLog().debug("Transfer status after processing account " +  ((Map)thisCustomerAccounts.get(0)).get("ACCOUNT_ID") + " of " + ((Map)thisCustomerAccounts.get(0)).get(ICustomer.CUSTOMER_ID));
	    DecimalFormat df = new DecimalFormat("##,###,###,###,###.##");
	    for (Iterator x = transferToList.iterator(); x.hasNext();){
		    Map wlm = (Map) x.next();
		    getLog().debug("WL ID = " + wlm.get(IWorkList.WORK_LIST_ID));
		    getLog().debug("WL DISTRIBUTION PERC = " + wlm.get(IWorkListDistribution.DISTRIBUTION_PERC));
		    getLog().debug("WL VALUE ASSIGNED = " + df.format(wlm.get("ACCUMULATED_VALUE_ASSIGNED")));
		    getLog().debug("WL TOTAL VALUE PROCESSED = " + df.format(wlm.get("TOTAL_VALUE_PROCESSED")));
		    getLog().debug("PERCENTAGE VALUE ASSIGNED = " + (((Double)wlm.get("TOTAL_VALUE_PROCESSED")).doubleValue()==0?0.0:((Double)wlm.get("ACCUMULATED_VALUE_ASSIGNED")).doubleValue()/((Double)wlm.get("TOTAL_VALUE_PROCESSED")).doubleValue() * 100));
		    getLog().debug("\n");
	    }
    }
    return new RunnableTransactionSet(trans);
  }
  
  public static Log getLog() {
	return LOG;
  }
  
}
