package com.profitera.server;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.profitera.dataaccess.SqlMapProvider;
import com.profitera.datasource.IDataSourceConfiguration;
import com.profitera.ibatis.SQLMapFileRenderer;
import com.profitera.services.system.dataaccess.ProtocolLoadedSqlMapProvider;

public class MapDatabasePropertyProvider implements IDatabasePropertyProvider {
  private ProtocolLoadedSqlMapProvider p;
  public Properties queryProperties(IDataSourceConfiguration conf) {
    if (p == null){
      SQLMapFileRenderer r = new SQLMapFileRenderer();
      String c = r.renderHeader("settings") 
      + r.renderResultMap("getSettings-rmap", HashMap.class, new String[]{"NAME", "VALUE"}, new Class[]{String.class, String.class}) 
      + r.renderSelect("getSettings", "getSettings-rmap", "SELECT NAME, VALUE FROM PTRSETTINGS") 
      + r.renderFooter();
      p = new ProtocolLoadedSqlMapProvider("settings", c, conf, "Props");
    }
    Properties props = new Properties();
    try {
      for(Iterator<Map<String, String>> i = query(); i.hasNext();){
        Map<String, String> m = i.next();
        props.put(m.get("NAME"), m.get("VALUE"));
      }
    } catch (SQLException e) {
      throw new RuntimeException("Unable to load database properties", e);
    }
    return props;
  }
  @SuppressWarnings("unchecked")
  private Iterator<Map<String, String>> query() throws SQLException {
    Iterator<?> o = p.query(SqlMapProvider.LIST, "getSettings", null);
    return (Iterator<Map<String, String>>) o;
  }

}
