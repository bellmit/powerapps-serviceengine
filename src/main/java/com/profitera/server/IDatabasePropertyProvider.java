package com.profitera.server;

import java.util.Properties;

import com.profitera.datasource.IDataSourceConfiguration;

public interface IDatabasePropertyProvider {
  public Properties queryProperties(IDataSourceConfiguration c);
}
