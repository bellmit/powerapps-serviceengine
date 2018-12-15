package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.deployment.rmi.TemplateServiceIntf;
import com.profitera.services.system.document.IDocumentService;
import com.profitera.services.system.lookup.LookupManager;

class TemplateGenerationTreatmentProcessActivity extends
    ConditionalTreatmentProcessActivity {
  private TemplateServiceIntf templateService;

  protected void executeActivity(Long accountId, Map process, Map target,
      Date date, String user, ITransaction t, IReadWriteDataProvider p) throws SQLException, AbortTransactionException {
    if (process.get("TEMPLATE_ID") == null) {
      throw new IllegalArgumentException("Template generation configured for element has missing Template ID");
    }
    String data = (String) getTemplateService()
        .generateDocument(
            (Long) process.get("TEMPLATE_ID"),
            process).getBeanHolder();
    IDocumentTransaction dt;
    if( process.get("DOCUMENT_ID")==null){
     dt = getDocumentService().createDocument(4, "Document generated using template ID "+process.get("TEMPLATE_ID"),
        data, p);
     dt.execute(t);
     target.put("DOCUMENT_ID", dt.getId());
     p.insert(getStatement(), target, t);
    }
    else {
      dt = getDocumentService().updateDocument((Long) process.get("DOCUMENT_ID"), 4, "", data,
          p);
      dt.execute(t);
    }
  }

  private TemplateServiceIntf getTemplateService() {
    if (templateService == null) {
      templateService = (TemplateServiceIntf) LookupManager
      .getInstance().getLookupItem(LookupManager.BUSINESS,
          "TemplateService");
    }
    return templateService;
  }
  
  private IDocumentService getDocumentService() {
    final IDocumentService docService = (IDocumentService) LookupManager
        .getInstance().getLookupItem(LookupManager.SYSTEM,
            "DocumentService");
    return docService;
  }

}
