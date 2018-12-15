/**
 * 
 */
package com.profitera.services.business.worklistmanager;

import java.sql.SQLException;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;

public class NodeRunnableTransaction implements IRunnableTransaction{
  final Long nodeId;
  final Long workListId;
  final IRunnableTransaction rt;
  public NodeRunnableTransaction(Long node, Long wl, IRunnableTransaction tr){
    nodeId = node;
    workListId = wl;
    rt = tr;
  }
  public void execute(ITransaction t) throws SQLException, AbortTransactionException {
    rt.execute(t);
  }
}