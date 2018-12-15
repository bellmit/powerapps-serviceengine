package com.profitera.services.business.worklistmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.descriptor.business.meta.IWorkList;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public class UnassignedAccountWorklistGenerator extends
    AllAccountWorklistGenerator implements IWorkListGenerator {

  protected NodeRunnableTransaction process(List thisCustomerAccounts, Map treeRoot, boolean customerMode, Date assignmentDate, List preferredWorkListIds, IReadWriteDataProvider p) {
    List assignmentTrans = new ArrayList();
    List remainingAccounts = new ArrayList();
    for(Iterator i = thisCustomerAccounts.iterator(); i.hasNext();){
      Map a = (Map) i.next();
      if (!a.containsKey(IWorkList.WORK_LIST_ID)){
        throw new RuntimeException("Primary generation query missing required column: " + IWorkList.WORK_LIST_ID);
      }
      if (a.get(IWorkList.WORK_LIST_ID) != null){
        assignmentTrans.add(getSetAccountWorkListTransaction(a, getDate(), p));
      } else {
        remainingAccounts.add(a);
      }
    }
    if (remainingAccounts.size() > 0){
      NodeRunnableTransaction t = super.process(remainingAccounts, treeRoot, customerMode,
          assignmentDate, preferredWorkListIds, p);
      assignmentTrans.add(t);
    }
    IRunnableTransaction[] trans = (IRunnableTransaction[]) assignmentTrans.toArray(new IRunnableTransaction[0]);
    return new NodeRunnableTransaction(null, null, new RunnableTransactionSet(trans));
  }

}
