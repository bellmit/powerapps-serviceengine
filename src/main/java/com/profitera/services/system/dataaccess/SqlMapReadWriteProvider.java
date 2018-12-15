package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.dataaccess.SqlMapProvider;
import com.profitera.datasource.IDataSourceConfiguration;
import com.profitera.server.ServiceEngine;
import com.profitera.services.Service;

public class SqlMapReadWriteProvider extends SqlMapProvider implements Service, IReadWriteDataProvider, IReadOnlyDataProvider {
  private static final Log LOG = LogFactory.getLog(IReadWriteDataProvider.class);
  private static final String SQL_MAP_CONFIG_PROPERTY = "DB_XML_CONFIG_FILE";
  private IDataSourceConfiguration dataSource;
  
  public SqlMapReadWriteProvider() {
    this((IDataSourceConfiguration)null);
  }
  public SqlMapReadWriteProvider(
      IDataSourceConfiguration iDataSourceConfiguration) {
    this.dataSource = iDataSourceConfiguration;
  }
  /**
   * @throws SQLException
   * @see com.profitera.services.system.dataaccess.IReadOnlyDataProvider#query(java.lang.Object, java.lang.String, java.lang.Object)
   */
  public Iterator query(Object strategy, String qName, Object args) throws SQLException {
    // Use ==, should be exact instance. Also protects from NPEs
    if (strategy == STREAM_RESULTS){
      return super.query(STREAM, qName, args);
    } else {
      return super.query(LIST, qName, args);
    }
  }

  protected Log getLog() {
    return LOG;
  }

  protected String getConfigFileName(){
    Properties connectionProps = getConnectionProperties();
    if (connectionProps != null){
      return (String) connectionProps.get(SQL_MAP_CONFIG_PROPERTY);
    }
    return null;
  }

  /**
   * @see com.profitera.dataaccess.SqlMapProvider#getConnectionProperties()
   */
  protected Properties getConnectionProperties() {
    Properties connectionProps = getDataSource().getProperties();
    return connectionProps;
  }

  private IDataSourceConfiguration getDataSource() {
    if (dataSource == null) {
      dataSource = ServiceEngine.getDataSourceConfigurations().getDefaultDataSource();
    }
    return dataSource;
  }
  
  public boolean isInsertStatement(String qName){
    return getSqlType(qName) == SQL_TYPE_INSERT;
  }
  public boolean isUpdateStatement(String qName){
    return getSqlType(qName) == SQL_TYPE_UPDATE;
  }
  public boolean isDeleteStatement(String qName){
    return getSqlType(qName) == SQL_TYPE_DELETE;
  }
  @Override
  protected String getDataSourceName() {
    return getDataSource().getName();
  }

}
