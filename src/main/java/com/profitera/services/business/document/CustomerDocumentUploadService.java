package com.profitera.services.business.document;

import java.util.Map;

import com.profitera.deployment.rmi.CustomerDocumentUploadServiceIntf;
import com.profitera.descriptor.business.TransferObject;

public class CustomerDocumentUploadService extends AbstractDocumentUploadService 
  implements CustomerDocumentUploadServiceIntf {

  protected String getExernalIdName() {
    return "CUSTOMER_ID";
  }

  protected String getInsertName() {
    return "insertCustomerUploadDocument";
  }

  public TransferObject startDocumentUpload(Long customerId, String fileName,
      String userId, Map attributes, TransferObject t) {
    return startDocumentUpload((Object)customerId, fileName, userId, attributes, t);
  }

}
