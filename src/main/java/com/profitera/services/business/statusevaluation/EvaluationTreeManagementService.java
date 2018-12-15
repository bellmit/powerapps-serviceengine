package com.profitera.services.business.statusevaluation;

import java.util.Map;

import com.profitera.deployment.rmi.EvaluationTreeManagementServiceIntf;
import com.profitera.deployment.rmi.ListQueryServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.business.statusevaluation.impl.ManagementService;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.document.IDocumentService;
import com.profitera.services.system.lookup.LookupManager;

public class EvaluationTreeManagementService
  extends ProviderDrivenService 
  implements EvaluationTreeManagementServiceIntf {
  public TransferObject createTree(final Map data, String xml){
    IDocumentService documentService = getDocumentService();
    IReadWriteDataProvider p = getReadWriteProvider();
    ManagementService managementService = new ManagementService(log);
    return managementService.createTree(data, xml, documentService, p);
  }
  public TransferObject disableTree(final Long id) {
    IReadWriteDataProvider p = getReadWriteProvider();
    ManagementService managementService = new ManagementService(log);
    return managementService.disableTree(id, p);
  }
  public TransferObject updateTree(final Map selectedTree, final String xml) {
    final IDocumentService documentService = getDocumentService();
    final IReadWriteDataProvider p = getReadWriteProvider();
    ManagementService managementService = new ManagementService(log);
    return managementService.updateTree(selectedTree, xml, documentService, p);
  }
  
  
  public TransferObject getTree(final Long treeId) {
    final IDocumentService documentService = getDocumentService();
    final IReadWriteDataProvider p = getReadWriteProvider();
    ManagementService managementService = new ManagementService(log);
    return managementService.getTree(treeId, documentService, p);
  }
  
  private IDocumentService getDocumentService() {
    final IDocumentService docService = (IDocumentService) LookupManager
        .getInstance().getLookupItem(LookupManager.SYSTEM, "DocumentService");
    return docService;
  }
  private ListQueryServiceIntf getQueryService() {
    LookupManager lm = LookupManager.getInstance();
    Object o = lm.getLookupItem(LookupManager.BUSINESS, "ListQueryService");
    return (ListQueryServiceIntf) o;
  }

  public TransferObject getTreeType(Long treeId) {
    IReadWriteDataProvider p = getReadWriteProvider();
    ManagementService managementService = new ManagementService(log);
    return managementService.getTreeType(treeId, p);
  }
  public TransferObject clearTreeType(Long typeId, Map args) {
    IReadWriteDataProvider p = getReadWriteProvider();
    ManagementService managementService = new ManagementService(log);
    return managementService.clearTreeType(typeId, args, p, getQueryService());
  }
  
}
