package com.profitera.services.business.document;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.io.TemporaryDiskBuffer;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.system.dataaccess.IDocumentTransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.document.IDocumentService;
import com.profitera.services.system.lookup.LookupManager;

public abstract class AbstractDocumentUploadService extends ProviderDrivenService {
  private static final String UPLOAD_DATABASE_ERROR = "UPLOAD_DATABASE_ERROR";
  private static final String UPLOAD_IO_ERROR = "UPLOAD_IO_ERROR";
  private static final String UPLOAD_EXPIRED_ERROR = "UPLOAD_EXPIRED_ERROR";

  private class UploadStruct {
    Object id;
    String fileName;
    Map attributes;
    TemporaryDiskBuffer buffer;
    String userId;
    public UploadStruct(Object id, String sourceFile, String userId, Map attribs, TemporaryDiskBuffer buffer){
      this.id = id;
      this.fileName = sourceFile;
      this.userId = userId;
      this.attributes = attribs;
      this.buffer = buffer;
    }
  }
  private SecureRandom random = new SecureRandom();
  private Map currentUploads;
  
  protected abstract String getInsertName();
  protected abstract String getExernalIdName();
  
  public TransferObject getInProgressUploadCount(){
    return new TransferObject(currentUploads == null ? new Long(0) : new Long(currentUploads.size()));
  }
  TransferObject startDocumentUpload(Object id, String fileName, String userId, java.util.Map attributes, TransferObject t){
    if (userId == null){
      return new TransferObject(TransferObject.ERROR, "NO_USER_SUPPLIED");
    }
    try {
      Long newId = new Long(random.nextLong());
      TemporaryDiskBuffer buffer;
      buffer = new TemporaryDiskBuffer(null, getUploadExpiryDuration());
      UploadStruct u = new UploadStruct(id, fileName, userId, attributes, buffer);
      u.buffer.writeToBuffer((byte[]) t.getBeanHolder());
      addUpload(newId, u);
      return new TransferObject(newId);
    } catch (IOException e) {
      log.error("Unable to create disk buffer for upload", e);
      return new TransferObject(TransferObject.ERROR, UPLOAD_IO_ERROR);
    }
  }
  
  public TransferObject addToDocumentUpload(Long uploadId, TransferObject bytes){
    UploadStruct u = getUpload(uploadId);
    if (u == null){
      return new TransferObject(TransferObject.ERROR, UPLOAD_EXPIRED_ERROR);
    }
    try {
      u.buffer.writeToBuffer((byte[]) bytes.getBeanHolder());
      return new TransferObject();
    } catch (IOException e) {
      u.buffer.terminate();
      log.error("Unable to update disk buffer for upload", e);
      return new TransferObject(TransferObject.ERROR, UPLOAD_IO_ERROR);
    } catch (IllegalStateException e){
      log.error("Unable to update disk buffer for upload", e);
      return new TransferObject(TransferObject.ERROR, UPLOAD_EXPIRED_ERROR);
    }
  }
  
  public TransferObject endDocumentUpload(Long uploadId, TransferObject bytes){
    TransferObject upload = addToDocumentUpload(uploadId, bytes);
    if (upload.isFailed()) return upload;
    IDocumentService documentService = getDocumentService();
    final UploadStruct u = getUpload(uploadId);
    final IReadWriteDataProvider p = getReadWriteProvider();
    try {
      InputStream stream = u.buffer.getStream();
      final IDocumentTransaction docTransaction = documentService.createDocument(IDocumentService.UPLOAD_DOCUMENT, u.fileName, stream, p);
      final Long[] insertedId = new Long[1];
      p.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          docTransaction.execute(t);
          Long docId = docTransaction.getId();
          u.attributes.put("DOCUMENT_ID", docId);
          u.attributes.put("USER_ID", u.userId);
          u.attributes.put("FILE_NAME", u.fileName);
          u.attributes.put(getExernalIdName(), u.id);
          p.insert(getInsertName(), u.attributes, t);
          insertedId[0] = docId;
        }});
      return new TransferObject(insertedId[0]);
    } catch (IOException e){
      log.error("Unable to update disk buffer for upload", e);
      return new TransferObject(TransferObject.ERROR, UPLOAD_IO_ERROR);
    } catch (AbortTransactionException e) {
      log.error("Transaction aborted for upload", e);
    } catch (SQLException e) {
      log.error("Transaction aborted for upload", e);
    }
    return new TransferObject(TransferObject.ERROR, UPLOAD_DATABASE_ERROR);
  }

  private IDocumentService getDocumentService() {
    final IDocumentService docService = (IDocumentService) LookupManager
        .getInstance().getLookupItem(LookupManager.SYSTEM, "DocumentService");
    return docService;
  }

  private UploadStruct getUpload(Long uploadId) {
    Object o = currentUploads == null ? null : currentUploads.get(uploadId);
    return (UploadStruct) o;
  }

  private void addUpload(Long id, UploadStruct u){
    if (currentUploads == null){
      currentUploads = new HashMap();
      Timer t = new Timer(true);
      int freq = getExpiryPollingFrequency();
      t.schedule(new TimerTask(){
        public void run() {
          removeExpiredBuffers();
        }}, freq, freq);
    }
    currentUploads.put(id, u);
  }

  protected int getExpiryPollingFrequency() {
    return 1000 * 60 * 20;
  }
  
  protected int getUploadExpiryDuration() {
    return 10 * 60 * 1000;
  }

  private void removeExpiredBuffers() {
    Object[] keys = currentUploads.keySet().toArray();
    for (int i = 0; i < keys.length; i++) {
      UploadStruct u = (UploadStruct) currentUploads.get(keys[i]);
      if (u.buffer.isExpired()) currentUploads.remove(keys[i]);
    }
  }
}
