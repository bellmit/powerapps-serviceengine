package com.profitera.services.business.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class ResultArgumentReplacementProcessor extends
    BaseListQueryProcessor {

  public ResultArgumentReplacementProcessor() {
    addRequiredProperty("ARGUMENT", String.class, "The argument to use as the data", "The argument whose value is used to substitute for the data returned by the query processor.");
    addProperty("APPEND", Boolean.class, Boolean.FALSE, "If true the argument data is appended to the exisitng results", 
      "When true the argument data does not really replace the result, instead it is added to the result list.");
  }
  @Override
  protected String getDocumentation() {
    return "Replaces the current results with the content of an argument to the query." +
    		" The argument must be itself a list of maps or null, in which case it is interpreted as an empty list.";
  }

  @Override
  protected String getSummary() {
    return "Replaces the query results with the value of an argument";
  }
  @Override
  public List postProcessResults(Map arguments, List result, IQueryService qs)
      throws TransferObjectException {
    String arg = (String) getProperty("ARGUMENT");
    Object argValue = arguments.get(arg);
    if (argValue == null) {
      argValue = new ArrayList();
    } else if (!(argValue instanceof List)) {
      getLog().error(ListQueryService.getLogId(qs) + " argument value for " + arg + " was not a list, found '" + argValue + "' of type " + argValue.getClass().getName());
      throw new TransferObjectException(new TransferObject(TransferObject.ERROR, "INVALID_DATA_TYPE"));
    }
    List argValueList = (List) argValue;
    int len = argValueList.size();
    Boolean append = (Boolean) getProperty("APPEND");
    if (append.booleanValue()) {
      len = len + result.size();
    }
    List newResult = new ArrayList(len);
    if (append.booleanValue()) {
      newResult.addAll(result);
    }
    newResult.addAll(argValueList);
    return newResult;
  }
  
  

}
