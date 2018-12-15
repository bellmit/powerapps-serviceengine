package com.profitera.services.business.query;

import java.util.Map;

import com.profitera.math.MathUtil;

public class SumQueryProcessor extends AbstractForEachProcessor {
  
  private Object result;
  private String[] sumFields;

  public SumQueryProcessor(){
    addRequiredProperty("SUM", String.class, "Fields to sum in results", "Fields to sum delimited by semi-colons, null values are interpreted as zero");
    addRequiredProperty("RESULT", String.class, "Target field for result of sum", "Target field for result of sum.");
  }

  protected void processResultRow(Map row, IQueryService qs) {
    Object o = null;
    String field = null;
    String[] f = getSumFields();
    Number[] n = new Number[f.length];
    try {     
      for (int i = 0; i < f.length; i++) {
        field = f[i];
        o = row.get(field);
        n[i] = (Number) o;
      }
    } catch (ClassCastException e){
      throw new IllegalArgumentException("Found non-numeric value for " + field + " of type " + o.getClass());
    }
    row.put(getResultField(), MathUtil.sum(n));
  }

  private Object getResultField() {
    if (result == null) {
      result = getProperty("RESULT");
    }
    return result;
  }

  private String[] getSumFields() {
    if (sumFields == null) {
      sumFields = getProperty("SUM").toString().split("[;]");
    }
    return sumFields;
  }

  
  protected String getDocumentation() {
    return "Calculates the sum of the fields specified and 'expands' the type of the destination field based on the fields to be added together in an effort to ensure that no accuracy is lost.";
  }

  protected String getSummary() {
    return "Sums the fields specified and places the result in the specified field";
  }

}
