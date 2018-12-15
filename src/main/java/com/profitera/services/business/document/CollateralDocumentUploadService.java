package com.profitera.services.business.document;

import java.util.Map;

import com.profitera.deployment.rmi.CollateralDocumentUploadServiceIntf;
import com.profitera.descriptor.business.TransferObject;

public class CollateralDocumentUploadService extends AbstractDocumentUploadService 
  implements CollateralDocumentUploadServiceIntf {

  protected String getExernalIdName() {
    return "COLLATERAL_ID";
  }

  protected String getInsertName() {
    return "insertCollateralUploadDocument";
  }

  public TransferObject startDocumentUpload(Long customerId, String fileName,
      String userId, Map attributes, TransferObject t) {
    return startDocumentUpload((Object)customerId, fileName, userId, attributes, t);
  }

}
