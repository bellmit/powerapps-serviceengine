package com.profitera.services.business.image;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.deployment.rmi.ImageServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.system.dataaccess.IDocumentTransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.document.IDocumentService;
import com.profitera.services.system.lookup.LookupManager;

public class ImageService extends ProviderDrivenService implements ImageServiceIntf {

  public TransferObject addImage(final String insert, final Map data, final TransferObject imageBytes) {
    final IReadWriteDataProvider p = getReadWriteProvider();
    Exception ex = null;
    final TransferObject[] tos = new TransferObject[1];
    try {
      p.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          byte[] bytes = (byte[]) imageBytes.getBeanHolder();
          InputStream stream = new ByteArrayInputStream(bytes);
          IDocumentTransaction dt = getDocumentService().createDocument(IDocumentService.IMAGE_DOCUMENT_TYPE_ID, "IMAGE DATA", stream, p);
          dt.execute(t);
          Long docId = dt.getId();
          data.put("DOCUMENT_ID", docId);
          p.insert(insert, data, t);
          tos[0] = new TransferObject(docId);
        }});
      return tos[0];
    } catch (AbortTransactionException e) {
      ex = e;
    } catch (SQLException e) {
      ex = e;
    }
    return returnFailWithTrace("IMAGE_ADD_ERROR", "insert", insert, data, ex);
  }

  public TransferObject updateImage(final Long documentId, final TransferObject imageBytes) {
    final IReadWriteDataProvider p = getReadWriteProvider();
    Exception ex = null;
    try {
      p.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          byte[] bytes = (byte[]) imageBytes.getBeanHolder();
          IDocumentTransaction dt = getDocumentService().updateDocument(documentId, IDocumentService.IMAGE_DOCUMENT_TYPE_ID, "IMAGE " + documentId, new ByteArrayInputStream(bytes), p);
          dt.execute(t);
        }});
      return new TransferObject(documentId);
    } catch (AbortTransactionException e) {
      ex = e;
    } catch (SQLException e) {
      ex = e;
    }
    return returnFailWithTrace("IMAGE_UPDATE_ERROR", "update", "", documentId, ex);
  }

  public TransferObject deleteImage(final String deleteStatement, final Long imageId) {
    final IReadWriteDataProvider p = getReadWriteProvider();
    Exception ex = null;
    final TransferObject to = new TransferObject();
    try {
      p.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          IDocumentTransaction dt = getDocumentService().deleteDocument(imageId, p);
          p.delete(deleteStatement, imageId, t);
          dt.execute(t);
        }});
      return to;
    } catch (AbortTransactionException e) {
      ex = e;
    } catch (SQLException e) {
      ex = e;
    }
    return returnFailWithTrace("IMAGE_DELETE_ERROR", "delete", deleteStatement, imageId, ex);
  }

  
  private IDocumentService getDocumentService() {
    final IDocumentService docService = (IDocumentService) LookupManager
        .getInstance().getLookupItem(LookupManager.SYSTEM, "DocumentService");
    return docService;
  }

  
  
  
  
}
