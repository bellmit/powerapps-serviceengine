package com.profitera.services.business.query;

import java.util.Date;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.util.DateParser;

public class EndOfDayQueryProcessor extends BaseListQueryProcessor {
  private static final String KEY = "KEY";
  private String key;

  public EndOfDayQueryProcessor() {
    addRequiredProperty(KEY, String.class, "Argument key", "The name of the argument to be adjusted to the end of its assigned date");
  }

  protected void configureProcessor() {
    key = (String) getProperty(KEY);
  }

  public Map preprocessArguments(Map arguments, IQueryService qs)
      throws TransferObjectException {
    Object o = arguments.get(key);
    if (!(o instanceof Date)) {
      getLog().warn(
          "Query " + getQueryName() + " configured to adjust date of argument "
              + key + " but argument is not of type " + Date.class.getName());
      return arguments;
    }
    Date d = (Date) o;
    if (d != null) {
      d = DateParser.getEndOfDay(d);
      arguments.put(key, d);
    }
    return arguments;
  }

  protected String getDocumentation() {
    return StartOfDayQueryProcessor.getLongDoc();
  }

  protected String getSummary() {
    return "Adjusts an <emphasis>existing</emphasis> java.util.Date argument to"
    +" the end of the day it is assigned (2005-01-01-09:00:00 becomes"
    + "2005-01-01-23:59:59).";
  }

}
