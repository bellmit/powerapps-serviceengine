package com.profitera.services.business.query;

import java.util.List;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class ListArgumentDifferenceProcessor extends AbstractListQueryProcessor {

  private String primaryListKey;
  private String subtractListKey;
  private boolean clearSubtract;

  protected void configureProcessor() {
    super.configureProcessor();
    primaryListKey = getRequiredProperty("PRIMARY_LIST_KEY");
    subtractListKey = getRequiredProperty("SUBTRACT_LIST_KEY");
    clearSubtract = !getRequiredProperty("CLEAR_SUBTRACT_ARGUMENT").toUpperCase().startsWith("F");
  }

  public Map preprocessArguments(Map arguments, IQueryService qs) throws TransferObjectException {
    List p = (List) arguments.get(primaryListKey);
    if (p != null){
      List s = (List) arguments.get(subtractListKey);
      if (s != null){
        p.removeAll(s);
      }
    }
    if (clearSubtract){
      arguments.remove(subtractListKey);
    }
    return arguments;
  }

}
