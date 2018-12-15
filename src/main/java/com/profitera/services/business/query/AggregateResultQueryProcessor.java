package com.profitera.services.business.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.math.MathUtil;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class AggregateResultQueryProcessor extends BaseListQueryProcessor {
  
  private String groupKey;
  private String[] sums;
  private String[] mins;
  private String[] maxs;
  
  public AggregateResultQueryProcessor(){
    addRequiredProperty("GROUP_BY_KEY", String.class, "Grouping field", "Field that is used as the basis for agreggating the other fields");
    addProperty("SUM_KEYS", String.class, null, "Fields to sum", "Fields to sum");
    addProperty("MIN_KEYS", String.class, null, "Fields to min", "Fields to min");
    addProperty("MAX_KEYS", String.class, null, "Fields to max", "Fields to max");
    
  }

  protected void configureProcessor() {
    groupKey = (String)getProperty("GROUP_BY_KEY");
    String temp = (String) getProperty("SUM_KEYS");
    if (temp == null){
      sums = new String[0];
    } else {
      sums = temp.split("[;]");
    }
    temp = (String) getProperty("MIN_KEYS");
    if (temp == null){
      mins = new String[0];
    } else {
      mins = temp.split("[;]");
    }
    temp = (String) getProperty("MAX_KEYS");
    if (temp == null){
      maxs = new String[0];
    } else {
      maxs = temp.split("[;]");
    }
  }

  public Map preprocessArguments(Map arguments, IQueryService qs) throws TransferObjectException {
    return arguments;
  }

  public List postProcessResults(Map arguments, List result, IQueryService qs) throws TransferObjectException {
    Map finalResult = new HashMap(result.size());
    for (Iterator i = result.iterator(); i.hasNext();) {
      Map row = (Map) i.next();
      Object group = row.get(groupKey);
      if (group == null){
        getLog().warn("Rows returned with null values in " + "GROUP_BY_KEY" + ", will not be included in results for query " + getQueryName());
        continue;
      }
      Map resultRow = (Map) finalResult.get(group);
      if (resultRow == null){
        finalResult.put(group, row);
        // We don't need to eval further, 1st entry
        continue;
      } else {
        updateAggregateRow(row, resultRow);
      }
    }
    return new ArrayList(finalResult.values());
  }

  private void updateAggregateRow(Map row, Map resultRow) {
    for (int i = 0; i < sums.length; i++) {
      String sumKey = sums[i];
      doSum(sumKey, row, resultRow);
    }
    for (int i = 0; i < mins.length; i++) {
      String minKey = mins[i];
      doCompare(minKey, row, resultRow, -1);
    }
    for (int i = 0; i < maxs.length; i++) {
      String maxKey = maxs[i];
      doCompare(maxKey, row, resultRow, 1);
    }
    
  }

  private void doCompare(String compKey, Map row, Map resultRow, int greaterOrLess) {
    Object rowVal = row.get(compKey);
    Object finalObject = resultRow.get(compKey);
    if (rowVal == null){
      return;
    }
    if (finalObject == null){
      resultRow.put(compKey, rowVal);
      return;
    }
    // We can only add numbers of the same type, lets be strict
    // to make it easier on us:
    if (!finalObject.getClass().equals(rowVal.getClass())){
      throw new ClassCastException("Min for " + compKey + " on query " 
          + getQueryName() + " can not performed on differing types:" 
          + rowVal.getClass().getName() + " and " + finalObject.getClass().getName());
    }
    if (!(finalObject instanceof Comparable)){
      throw new ClassCastException("Min for " + compKey + " on query " 
          + getQueryName() + " can not performed on types not implementing " + Comparable.class.getName() + ": " 
          + rowVal.getClass().getName());
    }
    Comparable c1 = (Comparable) rowVal;
    Comparable c2 = (Comparable) finalObject;
    if (c1.compareTo(c2) * greaterOrLess > 0){
      resultRow.put(compKey, c1);
    }
  }

  private void doSum(String sumKey, Map row, Map resultRow) {
    Object rowVal = row.get(sumKey);
    Object finalObject = resultRow.get(sumKey);
    if (rowVal == null){
      return;
    }
    if (finalObject == null){
      resultRow.put(sumKey, rowVal);
      return;
    }
    resultRow.put(sumKey, MathUtil.sum(
        new Number[]{(Number) rowVal, (Number) finalObject}));
  }

  protected String getDocumentation() {
    return "Aggregates values in the assigned fields of the result, simulates as SQL 'group by' clause with sum, min, and max.";
  }

  protected String getSummary() {
    return "Summarizes field values based on a grouping key";
  }
  
  

}
