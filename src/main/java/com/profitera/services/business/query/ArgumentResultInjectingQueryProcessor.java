package com.profitera.services.business.query;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class ArgumentResultInjectingQueryProcessor extends
    AbstractListQueryProcessor {

  private String argKey;
  private String resultKey;

  public Map preprocessArguments(Map arguments, IQueryService qs) throws TransferObjectException {
    return arguments;
  }

  protected void configureProcessor() {
    super.configureProcessor();
    argKey = getRequiredProperty("ARGUMENT_KEY");
    resultKey = getRequiredProperty("RESULT_KEY");
  }

  public List postProcessResults(Map arguments, List result, IQueryService qs) throws TransferObjectException {
    Object value = arguments.get(argKey);
    for (Iterator i = result.iterator(); i.hasNext();) {
      Map element = (Map) i.next();
      element.put(resultKey, value);
    }
    return super.postProcessResults(arguments, result, qs);
  }
  
  

}
