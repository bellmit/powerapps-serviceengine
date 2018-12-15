package com.profitera.services.business.query;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.document.IDocumentService;
import com.profitera.services.system.lookup.LookupManager;

public class DocumentByteRetrievalQueryProcessor extends
    AbstractListQueryProcessor {
  private String docId;
  private String docKey;

  protected void configureProcessor() {
    super.configureProcessor();
    docId = getRequiredProperty("DOCUMENT_ID");
    docKey = getRequiredProperty("DOCUMENT_KEY");
  }

  public Map preprocessArguments(Map arguments, IQueryService qs) throws TransferObjectException {
    return arguments;
  }

  public List postProcessResults(Map arguments, List result, IQueryService qs) throws TransferObjectException {
    for (Iterator i = result.iterator(); i.hasNext();) {
      Map row = (Map) i.next();
      Long documentId = (Long) row.get(docId);
      if (documentId != null){
        try {
          byte[] documentContent = getDocumentService().getDocumentContent(documentId, getReadWriteProvider());
          row.put(docKey, documentContent);
        } catch (SQLException e) {
          getLog().error("Failed to retrieve document: " + documentId, e);
          throw new TransferObjectException(new TransferObject(TransferObject.EXCEPTION, "FAILED_TO_RETRIEVE_DOCUMENT"));
        } catch (IOException e) {
          getLog().error("Failed to retrieve document: " + documentId, e);
          throw new TransferObjectException(new TransferObject(TransferObject.EXCEPTION, "FAILED_TO_RETRIEVE_DOCUMENT"));
        } 
      }
    }
    return result;
  }
  
  protected IDocumentService getDocumentService() {
    final IDocumentService docService = (IDocumentService) LookupManager
        .getInstance().getLookupItem(LookupManager.SYSTEM, "DocumentService");
    return docService;
  }
  
  protected IReadWriteDataProvider getReadWriteProvider() {
    final IReadWriteDataProvider provider = (IReadWriteDataProvider) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "ReadWriteProvider");
    return provider;
  }
}