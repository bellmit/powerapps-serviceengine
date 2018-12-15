package com.profitera.server.impl;

import java.util.HashMap;
import java.util.Map;

import com.profitera.dataaccess.ISqlMapProvider;
import com.profitera.datasource.IDataSourceConfiguration;
import com.profitera.datasource.IDataSourceConfigurationSet;
import com.profitera.datasource.ISqlMapProviderSet;
import com.profitera.services.system.dataaccess.SqlMapReadWriteProvider;

public class DefaultSqlMapProviderSet implements ISqlMapProviderSet {
  
  private IDataSourceConfigurationSet sources;
  private String[] names;
  private Map<String, ISqlMapProvider> providers;

  public DefaultSqlMapProviderSet(ISqlMapProvider defaultProvider, IDataSourceConfigurationSet sources) {
    Map<String, ISqlMapProvider> m = new HashMap<String, ISqlMapProvider>();
    m.put(sources.getDefaultDataSource().getName(), defaultProvider);
    this.sources = sources;
    this.names = new String[sources.getDataSources().length];
    for (int i = 0; i < this.names.length; i++) {
      this.names[i] = sources.getDataSources()[i].getName();
    }
    this.providers = m;
  }

  public String[] getDataSources() {
    return names;
  }

  public ISqlMapProvider getProvider(String dataSourceName) {
    ISqlMapProvider iSqlMapProvider = providers.get(dataSourceName);
    if (iSqlMapProvider != null) {
      return iSqlMapProvider;
    }
    for (int i = 0; i < sources.getDataSources().length; i++) {
      if (dataSourceName.equals(sources.getDataSources()[i].getName())) {
        IDataSourceConfiguration conf = sources.getDataSources()[i];
        SqlMapReadWriteProvider p = new SqlMapReadWriteProvider(conf);
        providers.put(dataSourceName, p);
        return p;
      }
    }
    throw new RuntimeException("Data source requested '" + dataSourceName + "' not found");
  }

  public void reload() {
    for (ISqlMapProvider p : providers.values()) {
      p.reload();
    }
  }

}
