package com.profitera.services.business.query;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

/**
 * NOTE: Any instances of these classes MUST be threadsafe, i.e.
 * do not maintain any state here 
 * @author jamison
 */
public interface IListQueryProcessor {
  
  /**
   * Set once at processor creation time.
   */
  public void configure(String queryName, Properties properties);
  /**
   * You can create a new list instance or return the same instance
   * as well, either will work.
   */
  public List<Map<String, Object>> postProcessResults(Map<String, Object> arguments, List<Map<String, Object>> result, IQueryService qs) throws TransferObjectException;
  public Map<String, Object> preprocessArguments(Map<String, Object> arguments, IQueryService qs) throws TransferObjectException;
}
