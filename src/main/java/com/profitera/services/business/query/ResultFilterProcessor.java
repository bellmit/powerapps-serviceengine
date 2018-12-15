package com.profitera.services.business.query;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class ResultFilterProcessor extends BaseListQueryProcessor {
  private static final String FILTER_KEY = "FILTER_KEY";
  private static final String FILTER_VALUE = "FILTER_VALUE";
  private static final String IGNORE_CASE = "IGNORE_CASE";
  
  public ResultFilterProcessor(){
    addRequiredProperty(FILTER_KEY, String.class, "Field in results to filter on", "The field that is inspected in each row to derive the value to compare to for filtering.");
    addProperty(FILTER_VALUE, String.class, null, "Value to filter on", "Used in String compare to value in field in each row.");
    addProperty(IGNORE_CASE, Boolean.class, Boolean.FALSE, "Case insensitive?", "If set to true all compares are done ignoring letter cases, e.g. 'IN' is equal to 'in', 'In', 'iN', as well as  'IN'.");
  }

  public List postProcessResults(Map arguments, List result, IQueryService qs) throws TransferObjectException {
    String key = (String) getProperty(FILTER_KEY);
    String value = (String) getProperty(FILTER_VALUE);
    Boolean b = (Boolean) getProperty(IGNORE_CASE);
    int count = 0;
    int removed = 0;
    for(Iterator i = result.iterator(); i.hasNext();){
      Map row = (Map) i.next();
      count++;
      Object o = row.get(key);
      if (value == null){
        if (o == null) {
          i.remove();
          removed++;
        }
      } else if (isStringEqual(b.booleanValue(), value, o)){
        i.remove();
        removed++;
      }
    }
    getLog().debug("Removed " + removed + " of " + count + " for " + getQueryName() + " (" + value + ")");
    return result;
  }

  private boolean isStringEqual(boolean ignoreCase, String value, Object o) {
    if (ignoreCase){
      return value.equalsIgnoreCase(o + "");  
    } else {
      return value.equals(o + "");
    }
  }

  protected String getDocumentation() {
    return "Each row in the result is checked against matching the configured filter value, if the filter value is not assigned it is checked against null. All compares are done using strings, thus the types of the values are not considered.";
  }

  protected String getSummary() {
    return "Filters results based on a text match of a column to aspecific value or null";
  }

}
