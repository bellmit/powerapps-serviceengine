package com.profitera.services.business.appserver;

import java.util.Map;

import com.profitera.datasource.IDataSourceConfiguration;
import com.profitera.deployment.rmi.ApplicationServerServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.BusinessService;
import com.profitera.services.business.appserver.impl.AppServerService;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.ProtocolLoadedSqlMapProvider;

public class ApplicationServerService extends BusinessService implements ApplicationServerServiceIntf {
  private ProtocolLoadedSqlMapProvider provider;

  public TransferObject addServer(Map serverData){
    IReadWriteDataProvider p = getPrivateProvider();
    return new AppServerService().addServer(serverData, p);
  }
  
  public TransferObject updateServer(Map serverData) {
    return new AppServerService().updateServer(serverData, getPrivateProvider());
  }
  
  public TransferObject disableServer(Long id) {
    return new AppServerService().disableServer(id, getPrivateProvider());
  }

  public TransferObject enableServer(Long id) {
    return new AppServerService().enableServer(id, getPrivateProvider());
  }
  
  private IReadWriteDataProvider getPrivateProvider() {
    if (provider == null) {
      String sqlText = new AppServerService().getSql();
      IDataSourceConfiguration dds = getDataSourceConfigurations().getDefaultDataSource();
      provider = new ProtocolLoadedSqlMapProvider("appserverservice", sqlText, dds, dds.getName() + "-AppServer");
    }
    return provider;
  }

  public TransferObject getServers() {
    return new AppServerService().getServers(getPrivateProvider());
  }

  public TransferObject getServerStoredMemorySettings(Long id) {
    return new AppServerService().getServerStoredMemorySettings(id, getPrivateProvider());
  }

  public TransferObject setServerStoredMemorySettings(Long id, Map data) {
    return new AppServerService().setServerStoredMemorySettings(id, data, getPrivateProvider());
  }

  public TransferObject verifyCurrentServerMemory() {
    return new AppServerService().verifyCurrentServerMemory(getPrivateProvider());
  }
  
  public TransferObject getCurrentServerId() {
    return new AppServerService().getCurrentServerId(getPrivateProvider());
  }
}
