package com.profitera.services.business.document;

import java.util.Map;

import com.profitera.deployment.rmi.DocumentUploadServiceIntf;
import com.profitera.descriptor.business.TransferObject;

public class DocumentUploadService extends AbstractDocumentUploadService 
  implements DocumentUploadServiceIntf {

  protected String getExernalIdName() {
    return "NONE";
  }

  protected String getInsertName() {
    return "insertUploadDocument";
  }

  public TransferObject startDocumentUpload(String fileName,
      String userId, Map attributes, TransferObject t) {
    return startDocumentUpload(null, fileName, userId, attributes, t);
  }

}
