package com.profitera.services.business.document;

import java.util.Map;

import com.profitera.datasource.IDataSourceConfigurationSet;
import com.profitera.deployment.rmi.ApplicationServerServiceIntf;
import com.profitera.deployment.rmi.DocumentArchivingServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.business.document.impl.DocumentArchiveSettings;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.document.IDocumentService;
import com.profitera.services.system.lookup.LookupManager;

public class DocumentArchivingService extends ProviderDrivenService implements DocumentArchivingServiceIntf {
  
  @Override
  public void setDataSourceConfigurations(IDataSourceConfigurationSet s) {
  	super.setDataSourceConfigurations(s);
  	Runnable r = new Runnable(){
      public void run() {
      	int attempt = 0;
      	ApplicationServerServiceIntf app = null;
      	while(app==null && attempt++ < 30){
      		try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            // Ignore
          }
          app = getApplicationServerService();        
      	}
      	if(app==null){
      		log.error("Failed to retrieve ApplicationServerService. Abort document archiving.");
      		return;
      	}
        IReadWriteDataProvider provider = getReadWriteProvider();
        new DocumentArchiveSettings().runArchiver(app, getDocumentService(), provider);
      }};
    Thread t = new Thread(r);
    t.setName("DocumentArchivingThread");
    t.start();
  }
  
  protected ApplicationServerServiceIntf getApplicationServerService() {
    final ApplicationServerServiceIntf provider;
    provider = (ApplicationServerServiceIntf) LookupManager.getInstance().getLookupItem(LookupManager.BUSINESS, "ApplicationServerService");
    return provider;
  }
  
  protected IDocumentService getDocumentService() {
    final IDocumentService provider;
    provider = (IDocumentService) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "DocumentService");
    return provider;
  }

  public TransferObject getServerArchivingSettings(Long id) {
    return new DocumentArchiveSettings().getSettings(id, getReadWriteProvider());
  }

  public TransferObject setServerArchivingSettings(Long id, Map data) {
    return new DocumentArchiveSettings().updateSettings(id, data, getReadWriteProvider());
  }
}
