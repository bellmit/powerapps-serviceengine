package com.profitera.services.business.worklistmanager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.descriptor.business.meta.IWorkList;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public class UnassignedAccountNoHistoryWorklistGenerator extends
    AllAccountWorklistGenerator implements IWorkListGenerator {

  protected NodeRunnableTransaction process(List thisCustomerAccounts, Map treeRoot, boolean customerMode, Date assignmentDate, List preferredWorkListIds, IReadWriteDataProvider p) {
    List assignmentTrans = new ArrayList();
    List remainingAccounts = new ArrayList();
    for(Iterator i = thisCustomerAccounts.iterator(); i.hasNext();){
      Map a = (Map) i.next();
      if (!a.containsKey(IWorkList.WORK_LIST_ID)){
        throw new RuntimeException("Primary generation query missing required column: " + IWorkList.WORK_LIST_ID);
      }
      // Work list assigned means no transactions for you at all.
      if (a.get(IWorkList.WORK_LIST_ID) == null){
        remainingAccounts.add(a);
      } else {
        preferredWorkListIds.add(a.get(IWorkList.WORK_LIST_ID));
      }
    }
    if (remainingAccounts.size() > 0){
      NodeRunnableTransaction t = processInternal(remainingAccounts, treeRoot, customerMode,
          assignmentDate, preferredWorkListIds, p);
      assignmentTrans.add(t);
      
    }
    IRunnableTransaction[] trans = (IRunnableTransaction[]) assignmentTrans.toArray(new IRunnableTransaction[0]);
    return new NodeRunnableTransaction(null, null, new RunnableTransactionSet(trans));
  }
  
  protected NodeRunnableTransaction processInternal(List thisCustomerAccounts, Map treeRoot, boolean customerMode, Date assignmentDate, List preferredWorkListIds, IReadWriteDataProvider p) {
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
      NodeRunnableTransaction t = assignTo(thisCustomerAccounts, traverseTo, assignmentDate, preferredWorkListIds, p);
      return acceptTransaction(t);
      
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

  private NodeRunnableTransaction acceptTransaction(NodeRunnableTransaction t) {
    if (t.workListId != null){
      return t;
    } else {
      return new NodeRunnableTransaction(null, null, new IRunnableTransaction(){

        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          // NOOP
        }}){};
    }
  }

}
