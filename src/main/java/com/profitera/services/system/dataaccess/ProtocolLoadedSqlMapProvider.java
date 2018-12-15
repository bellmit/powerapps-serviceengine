package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;

import com.profitera.dataaccess.SqlMapProvider;
import com.profitera.datasource.IDataSourceConfiguration;
import com.profitera.services.system.ptrsqlmap.Handler;

public class ProtocolLoadedSqlMapProvider extends SqlMapProvider implements IReadWriteDataProvider {
  final String resourceName;
  final IDataSourceConfiguration connectionProps;
  private int maxCheckoutTime = 120000;
  private final String dataSourceName;
  public ProtocolLoadedSqlMapProvider(String resourceName, String content, IDataSourceConfiguration connectionProperties, String dataSourceName){
    this.resourceName = resourceName;
    connectionProps = connectionProperties;
    this.dataSourceName = dataSourceName;
    Handler.RESOURCE_MAP.put(resourceName, content);
    String v = System.getProperty("java.protocol.handler.pkgs");
    String newPackage = Handler.class.getPackage().getName();
    newPackage = newPackage.substring(0, newPackage.lastIndexOf('.'));
    if (v == null || v.length() == 0){
      v = newPackage;
      System.setProperty("java.protocol.handler.pkgs", v);
    } else if (v.indexOf(newPackage) == -1){
      v = v + "|" + newPackage;
      System.setProperty("java.protocol.handler.pkgs", v);
    }
    
  }
  protected String getConfigFileName() {
    return null;
  }

  protected String getSqlMapFileContent() {
    return "<?xml version='1.0' encoding='UTF-8'?>" + "<!DOCTYPE sqlMapConfig PUBLIC '-//ibatis.apache.org//DTD SQL Map Config 2.0//EN' 'http://ibatis.apache.org/dtd/sql-map-config-2.dtd'>"
      + "<sqlMapConfig>"
      + getPropertiesTags()
      + "<settings errorTracingEnabled='true' useStatementNamespaces='false'/>"
      + "<transactionManager type='JDBC'>  <dataSource type='SIMPLE'>  <property name='JDBC.Driver' value='${DRIVER}'/> <property name='JDBC.ConnectionURL' value='${URL}'/> <property name='JDBC.Username' value='${USERNAME}'/> <property name='JDBC.Password' value='${DB_PASSWORD}'/> <property name='JDBC.DefaultAutoCommit' value='false'/>"
      + "<property name='Pool.MaximumActiveConnections' value='" + getMaxActiveConnections() + "'/>"
      + "<property name='Pool.MaximumIdleConnections' value='" + getMaxIdleConnections() + "'/>"
      + "<property name='Pool.MaximumCheckoutTime' value='" + getMaxCheckoutTime() + "'/>"
      + "<property name='Pool.TimeToWait' value='500'/>"
      + "</dataSource>"
      + "</transactionManager>" 
      + "<sqlMap url='ptrsqlmap://" + resourceName + "'/>"
      +"</sqlMapConfig>";
  }
  protected int getMaxIdleConnections() {
    return 1;
  }
  protected int getMaxActiveConnections() {
    return 3;
  }
  protected int getMaxCheckoutTime() {
    return maxCheckoutTime;
  }
  
  protected String getPropertiesTags() {
    return "";
  }

  protected Properties getConnectionProperties() {
    Properties connectionProps = this.connectionProps.getProperties();
    return connectionProps;
  }
  public boolean isDeleteStatement(String name) {
    throw new UnsupportedOperationException();
  }
  public boolean isInsertStatement(String name) {
    throw new UnsupportedOperationException();
  }
  public boolean isUpdateStatement(String name) {
    throw new UnsupportedOperationException();
  }
  @SuppressWarnings("rawtypes")
  public Iterator query(Object strategy, String name, Object args)
      throws SQLException {
    return super.query((String) strategy, name, args);
  }
  @Override
  protected String getDataSourceName() {
    return dataSourceName;
  }
}