package com.profitera.services.business.statusevaluation.impl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.deployment.rmi.EvaluationTreeManagementServiceIntf;
import com.profitera.deployment.rmi.ListQueryServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.services.system.dataaccess.IDocumentTransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.document.IDocumentService;

public class ManagementService {
  
  private Log log;

  public ManagementService(Log log){
    this.log = log;
  }

  public TransferObject createTree(final Map data, String xml, IDocumentService documentService, final IReadWriteDataProvider p) {
    final IDocumentTransaction creator = documentService.createDocument(IDocumentService.DECISION_TREE_XML, "Created for evaluation tree "+data.get("TREE_NAME"), xml, p);
    final Long[] id = new Long[1];
    try {
      p.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          creator.execute(t);
          Long docId = creator.getId();
          data.put("DOCUMENT_ID", docId);
          data.put("UPDATE_TIME", new Date());
          id[0] = (Long) p.insert("insertEvaluationTree", data, t);
        }});
      return new TransferObject(id[0]);
    } catch (AbortTransactionException e) {
      log.error(e.getMessage(), e);
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
    }
    return new TransferObject(TransferObject.EXCEPTION, "INSERT_TREE_FAILED");
  }

  public TransferObject disableTree(final Long id, final IReadWriteDataProvider p) {
    try {
      p.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          p.insert("disableEvaluationTree", id, t);
        }});
      return new TransferObject();
    } catch (AbortTransactionException e) {
      log.error(e.getMessage(), e);
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
    }
    return new TransferObject(TransferObject.EXCEPTION, "DISABLE_TREE_FAILED");
  }

  public TransferObject updateTree(final Map selectedTree, final String xml,
      final IDocumentService documentService, final IReadWriteDataProvider p) {
    final Long[] id = new Long[1];
    try {
      p.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          Long docId = getTreeDocId(selectedTree, p, t);
          final IDocumentTransaction creator = documentService.updateDocument(docId, IDocumentService.DECISION_TREE_XML, "", xml, p);
          creator.execute(t);
          selectedTree.put("DOCUMENT_ID", docId);
          selectedTree.put("UPDATE_TIME", new Date());
          p.update("updateEvaluationTree", selectedTree, t);
        }});
      return new TransferObject(id[0]);
    } catch (AbortTransactionException e) {
      log.error(e.getMessage(), e);
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
    }
    return new TransferObject(TransferObject.EXCEPTION, "UPDATE_TREE_FAILED");
  }
  
  private Long getTreeDocId(Map selectedTree, IReadWriteDataProvider p,
      ITransaction t) throws SQLException {
    Long treeId = (Long) selectedTree.get("ID");
    return getTreeDocId(treeId, p, t);
  }
  private Long getTreeDocId(Long treeId, IReadWriteDataProvider p, ITransaction t)
      throws SQLException {
    Long docId = (Long) p.queryObject("getEvaluationTreeDocumentId", treeId);
    return docId;
  }

  public TransferObject getTree(final Long treeId, final IDocumentService documentService,
      final IReadWriteDataProvider p) {
    try {
      final StringBuffer buffer = new StringBuffer();
      p.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          Long docId = getTreeDocId(treeId, p, t);
          StringBuffer documentContent;
          try {
            documentContent = documentService.getCharacterDocumentContent(docId, p);
          } catch (IOException e) {
            throw new AbortTransactionException(e);
          }
          buffer.append(documentContent.toString());
        }});
      return new TransferObject(buffer.toString());
    } catch (AbortTransactionException e) {
      log.error(e.getMessage(), e);
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
    }
    return new TransferObject(TransferObject.EXCEPTION, "GET_TREE_FAILED");

  }

  public TransferObject getTreeType(Long treeId, IReadWriteDataProvider p) {
    try {
      Map typeInfo = (Map) p.queryObject("getEvaluationTreeType", treeId);    
      try {
        checkTreeTypeMap(typeInfo);
      } catch (TransferObjectException e){
        return e.getTransferObject();
      }
      return new TransferObject(typeInfo);
    } catch (SQLException e1) {
      log.error("Error retrieving type information for tree " + treeId, e1);
      return new TransferObject(TransferObject.EXCEPTION, "UNABLE_TO_RETRIEVE_TREE_TYPE");
    }
  }
  
  private void checkTreeTypeMap(Map m) throws TransferObjectException {
    checkNull(m, "ID", Long.class);
    checkNull(m, "DESCRIPTION", String.class);
    checkNull(m, EvaluationTreeManagementServiceIntf.UPDATE_FIELD_NAME, String.class);
    checkNull(m, EvaluationTreeManagementServiceIntf.DATE_FIELD_NAME, String.class);
    checkNull(m, EvaluationTreeManagementServiceIntf.QUERY, String.class);
    checkNull(m, EvaluationTreeManagementServiceIntf.UPDATE_FIELD_QUERY, String.class);
    checkNull(m, EvaluationTreeManagementServiceIntf.UPDATE, String.class);
    checkNull(m, EvaluationTreeManagementServiceIntf.INSERT, String.class);
  }
  
  private void checkNull(Map m, String string, Class c) throws TransferObjectException {
    Object id = m.get(string);
    if (id == null){
      TransferObject to = new TransferObject(new Object[]{string}, TransferObject.ERROR, "NULL_TREE_TYPE_FIELD");
      throw new TransferObjectException(to);
    } else if (!id.getClass().equals(c)){
      TransferObject to = new TransferObject(new Object[]{string, c, id.getClass().getName()}, TransferObject.ERROR, "WRONG_TYPE_TREE_TYPE_FIELD");
      throw new TransferObjectException(to);
    }
  }

  private TransferObject getTreeTypes(Map args,
      ListQueryServiceIntf queryService) {
    TransferObject queryList = queryService.getQueryList("getEvaluationTreeTypes", args);
    if (!queryList.isFailed()){
      List l = (List) queryList.getBeanHolder();
      for (Iterator i = l.iterator(); i.hasNext();) {
        Map m = (Map) i.next();
        try {
          checkTreeTypeMap(m);
        } catch (TransferObjectException e){
          return e.getTransferObject();
        }
      }
    }
    return queryList;
  }

  public TransferObject clearTreeType(Long typeId, final Map args, final IReadWriteDataProvider p, ListQueryServiceIntf queryService) {
    TransferObject treeTypes = getTreeTypes(args, queryService);
    if (treeTypes.isFailed()) return treeTypes;
    List l = (List) treeTypes.getBeanHolder();
    String clearQuery = null;
    for (Iterator i = l.iterator(); i.hasNext();) {
      Map t = (Map) i.next();
      if (t.get("ID").equals(typeId)){
        clearQuery = (String) t.get(EvaluationTreeManagementServiceIntf.CLEAR);
      }
    }
    final String statement = clearQuery;
    try {
      p.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          p.update(statement, args, t);
        }});
      return new TransferObject();
    } catch (AbortTransactionException e) {
      log.error("Error clearing information for tree type " + typeId, e);
    } catch (SQLException e) {
      log.error("Error clearing information for tree type " + typeId, e);
    }
    return new TransferObject(TransferObject.EXCEPTION, "UNABLE_TO_EXECUTE_TREE_TYPE_CLEAR");
  }
}
