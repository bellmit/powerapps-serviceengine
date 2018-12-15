package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Iterator;

/**
 * @author jamison
 */
public interface IReadOnlyDataProvider {
  public static final Object STREAM_RESULTS = "STREAM";
  public static final Object LIST_RESULTS = "LIST";
  
  public Iterator query(Object strategy, String qName, Object args) throws SQLException;
  public Object queryObject(String qName, Object args) throws SQLException;
  public Object queryObject(String qName) throws SQLException;
  public void reload();
}
