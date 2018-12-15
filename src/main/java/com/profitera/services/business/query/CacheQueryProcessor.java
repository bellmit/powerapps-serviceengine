package com.profitera.services.business.query;

import java.util.List;
import java.util.Map;

import com.profitera.listquery.IListQueryResult;
import com.profitera.listquery.ListQueryResult;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class CacheQueryProcessor extends BaseListQueryProcessor {

  private static final String SECONDS = "SECONDS";
  private Long cacheTime;

  {
    addRequiredProperty(SECONDS, Integer.class, "Seconds before values expire", "The number of seconds a client can safely cache these results");
  }
  
  protected void configureProcessor() {
    super.configureProcessor();
    Integer seconds = (Integer) getProperty(SECONDS);
    this.cacheTime = new Long(seconds.longValue() * 1000);
  }
  
  public List postProcessResults(Map arguments, List result, IQueryService qs)
      throws TransferObjectException {
    ListQueryResult r = new ListQueryResult(result);
    r.setHeader(IListQueryResult.EXPIRES, cacheTime);
    return r;
  }



  protected String getDocumentation() {
    return "Sets the cache expiry for the query and processor results "
    + "returned by requests to the registered query. This query processor" +
    		" does <emphasis>not</emphasis> cache the result, it sends a header" +
    		" in the result to indicate that it can be cached by the client. The" +
    		" use of this query processor is to indicate to the client that it is" +
    		" safe to assume that this query result will be the same for the same arguments" +
    		" for a given period of time.";
  }

  protected String getSummary() {
    return "Sets the cache expiry for this query result";
  }
}
