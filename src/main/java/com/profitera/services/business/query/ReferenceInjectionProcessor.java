package com.profitera.services.business.query;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class ReferenceInjectionProcessor extends BaseListQueryProcessor {

  protected String getDocumentation() {
	  return 
	  "Sample config :\n"
	  + "<programlisting><![CDATA[\n" 
	  + "<query name='getCustomerAgencyInformation'>\n"
	  + "  <processor implementation='com.profitera.services.business.query.ReferenceInjectionProcessor'>\n"
	  + "    <properties>\n"
	  + "      <property name='REFERENCE_QUERY' value='getAllAgencies'/>\n"
	  + "      <property name='REFERENCE_KEY' value='ID'/>\n"
	  + "      <property name='REFERENCE_VALUE' value='NAME'/>\n"
	  + "      <property name='RESULT_KEY' value='PREVIOUS_AGENCY_ID'/>\n"
	  + "      <property name='RESULT_VALUE' value='PREVIOUS_AGENCY'/>\n"
	  + "      <!-- REFRESH_INTERVAL is in milliseconds and is optional-->\n"
	  + "      <property name='REFRESH_INTERVAL' value='10000'/>\n"
	  + "    </properties>\n"
	  + "  </processor>\n"
	  + "</query>\n"
	  + "\n\n"
	  + "Query used by sample config to populate reference cache: \n"
	  + "\n"
	  + "<resultMap id='getAgency-map' class='hmap'>\n"
	  + "  <result property='ID'/>\n"
	  + "  <result property='NAME'/>\n"
	  + "</resultMap>\n"
	  + "\n"
	  + "<select id='getAllAgencies' resultMap='getAgency-map'>"
	  + "  select AGENCY_ID   as ID,\n"
	  + "       AGENCY_NAME as NAME\n"
	  + "  from PTRAGENCY\n"
	  + "</select>\n"
	  + "\n"
	  + "]]></programlisting>\n"
	  + "Note that the reference query takes no arguments."
	  + "<figure>"
      + "<title>ReferenceInjectionProcessor Processing</title>"
      + "<mediaobject>"
      + "<imageobject>"
      + "<imagedata fileref='ReferenceInjectionProcessor.png' format='PNG'/>"
      + "</imageobject><caption> <para>TODO: An ugly diagram of processing, this idea, but looking good.</para></caption>"
      + "</mediaobject>"
      + "</figure>";
	}

	protected String getSummary() {
		return "Refresh reference data";
	}

private static final String REFRESH_INTERVAL = "REFRESH_INTERVAL";
  private static final String REFERENCE_QUERY = "REFERENCE_QUERY";
  private static final String REFERENCE_VALUE = "REFERENCE_VALUE";
  private static final String REFERENCE_KEY = "REFERENCE_KEY";
  private static final String RESULT_KEY = "RESULT_KEY";
  private static final String RESULT_VALUE = "RESULT_VALUE";
  private String resultKey;
  private String referenceKey;
  private String referenceValue;
  private String referenceQuery;
  private long refreshInterval;
  private String resultValue;
  //
  private long lastRefreshTime = 0;
  private Map cache = new HashMap();

  public ReferenceInjectionProcessor(){
	  addRequiredProperty(RESULT_KEY, String.class, "Result key", "Result key");
	  addRequiredProperty(RESULT_VALUE, String.class, "Result value", "Result value");
	  addRequiredProperty(REFERENCE_KEY, String.class, "Reference key", "Reference key");
	  addRequiredProperty(REFERENCE_QUERY, String.class, "Reference query", "Reference query");
	  addRequiredProperty(REFERENCE_VALUE, String.class, "Reference value", "Reference value");
	  addProperty(REFRESH_INTERVAL, String.class, new Long(5 * 60 * 1000), "The elapsed time for next refresh ", "The elapsed time for next refresh, default time period is 5 minute which are 5*60*1000");
  }
  
  protected void configureProcessor() {
    resultKey = (String)getProperty(RESULT_KEY);
    resultValue = (String)getProperty(RESULT_VALUE);
    referenceKey = (String)getProperty(REFERENCE_KEY);
    referenceValue = (String)getProperty(REFERENCE_VALUE);
    referenceQuery = (String)getProperty(REFERENCE_QUERY);
    refreshInterval = 5 * 60 * 1000;
    try {
    	//Long's constructor does not throw on NPE
      refreshInterval = new Long(getProperty(REFRESH_INTERVAL).toString()).longValue();
    } catch (NumberFormatException e){
      
    }
  }

  public List postProcessResults(Map arguments, List result, IQueryService qs)
      throws TransferObjectException {
    if (result == null || result.size() == 0)
      return result;
    Map m = getCache(qs);
    for (Iterator i = result.iterator(); i.hasNext();) {
      Map r = (Map) i.next();
      Object keyVal = r.get(resultKey);
      if (keyVal != null){
        Object desc = m.get(keyVal);
        r.put(resultValue, desc);
        if (!m.containsKey(keyVal)){
          getLog().debug("Reference data lookup not found for query " + getQueryName() + " on reference query " + referenceQuery + " for value " + keyVal);
        }
      }
    }
    return result;
  }

  private Map getCache(IQueryService s) throws TransferObjectException{
    long elapsed = Math.abs(System.currentTimeMillis() - lastRefreshTime);
    if (elapsed > refreshInterval){
      getLog().debug("Refreshing " + referenceQuery + " for " + getQueryName() + ": " + elapsed + " ms");
      synchronized (cache) {
        if (s == null){
          throw new RuntimeException("Unable to load query service to refresh cache for " + getQueryName());
        }
        TransferObject queryList = s.getQueryList(referenceQuery, new HashMap());
		if(queryList.isFailed()){
			throw new TransferObjectException(queryList, new Throwable("Failed to refresh "+referenceQuery+" for "+getQueryName()));
		}
        List l = (List) queryList.getBeanHolder();
        cache.clear();
        for (Iterator iter = l.iterator(); iter.hasNext();) {
          Map element = (Map) iter.next();
          cache.put(element.get(referenceKey), element.get(referenceValue));
        }
        lastRefreshTime = System.currentTimeMillis();
      }
    }
    return cache;
  }

  public Map preprocessArguments(Map arguments, IQueryService qs) throws TransferObjectException {
    return arguments;
  }

}
