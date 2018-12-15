package com.profitera.services.business.query;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class SortingQueryProcessor extends BaseListQueryProcessor {
  private String key;
  
  private String getKey(){
    if (key == null){
      key = (String) getProperty("SORT_KEY");
    }
    return key;
  }
  
  public SortingQueryProcessor(){
    addRequiredProperty("SORT_KEY", String.class, "Result key to sort on", "Key to use to lookup values to sort in the results being processed");
    addRequiredProperty("SORT_ASCENDING", Boolean.class, "Sorting order", "If assigned true sort is ascending, false is descending");
    addProperty("IGNORE_CASE", Boolean.class, Boolean.TRUE, "Ignore case when handling strings", "If assigned true sorts on text are done in a case-insensitive manner");
  }

  protected String getDocumentation() {
    return "Sorts the result data using the data type of the values to compare directly, a special case is text sorting which can be set to consider or ignore text case.";
  }

  protected String getSummary() {
    return "Sorts the results of a request";
  }

  public List postProcessResults(Map arguments, List result, IQueryService qs)
      throws TransferObjectException {
    final String k = getKey();
    final boolean isAscending = ((Boolean)getProperty("SORT_ASCENDING")).booleanValue();
    final boolean isIgnoreCase = ((Boolean)getProperty("IGNORE_CASE")).booleanValue();
    try {
    Collections.sort(result, new Comparator(){
      public int compare(Object c1, Object c2) {
        int result = 0;
        Map m1 = (Map) c1;
        Map m2 = (Map) c2;
        Object v1 = m1.get(k);
        Object v2 = m2.get(k);
        if (v1 == null && v2 == null){
          result = 0;
        } else if (v1 != null && v2 == null){
          result = 1;
        } else if (v1 == null && v2 != null){
          result = -1;
        } else {
          if (v1 instanceof String && isIgnoreCase){
            result = ((String)v1).compareToIgnoreCase((String) v2);
          } else {
            result = ((Comparable)v1).compareTo(v2);
          }
        }
        return result * (isAscending ? 1 : -1);
      }});
    } catch (ClassCastException e){
      getLog().error("Incompatible data types for sorting on " + getKey() + " for " + getQueryName(), e);
      throw new TransferObjectException(new TransferObject(TransferObject.EXCEPTION, "INCOMPATIBLE_SORT_TYPES"), e);
    }
    return result;
  }
  
  

}
