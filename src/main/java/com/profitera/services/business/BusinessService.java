package com.profitera.services.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.datasource.IDataSourceConfigurationSet;
import com.profitera.deployment.rmi.LoginServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.reference.ReferenceService;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.exception.GenericException;

public class BusinessService implements com.profitera.services.Service {
  protected final static Log log = LogFactory.getLog(BusinessService.class);
  protected final LookupManager lookup = LookupManager.getInstance();
  private ReferenceService ref;
  private IDataSourceConfigurationSet dataSources;

  public void setDataSourceConfigurations(IDataSourceConfigurationSet s) {
    this.dataSources = s;
  }

  protected IDataSourceConfigurationSet getDataSourceConfigurations() {
    return this.dataSources;
  }

  protected ReferenceService getRef() {
    if (null == ref) {
      ref = (ReferenceService) LookupManager.getInstance().getLookupItem(LookupManager.BUSINESS, "ReferenceService");
    }
    return ref;
  }

  protected LoginServiceIntf getLogin() {
    Object o = LookupManager.getInstance().getLookupItem(LookupManager.BUSINESS, "LoginService");
    return (LoginServiceIntf) o;
  }

  protected TransferObject getExceptionTransferObject(GenericException ex) {
    return new TransferObject(ex.getMessage(), TransferObject.EXCEPTION, ex.getErrorCode());
  }

  protected TransferObject getErrorTransferObject(String message) {
    return new TransferObject(TransferObject.ERROR, message);
  }
}
