package com.profitera.services.business.document;

import java.util.Map;

import com.profitera.deployment.rmi.AccountDocumentUploadServiceIntf;
import com.profitera.descriptor.business.TransferObject;

public class AccountDocumentUploadService extends AbstractDocumentUploadService 
  implements AccountDocumentUploadServiceIntf {

  protected String getExernalIdName() {
    return "ACCOUNT_ID";
  }

  protected String getInsertName() {
    return "insertAccountUploadDocument";
  }

  public TransferObject startDocumentUpload(Long accountId, String fileName,
      String userId, Map attributes, TransferObject t) {
    return startDocumentUpload((Object)accountId, fileName, userId, attributes, t);
  }

}
