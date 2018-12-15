package com.profitera.services.business.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class AppendListArgumentQueryProcessor extends
    AbstractListQueryProcessor {
  
  private boolean doRemoveSource;
  private String sourceKey;
  private String destinationKey;


  protected void configureProcessor() {
    super.configureProcessor();
    destinationKey = getRequiredProperty("DESTINATION_KEY");
    sourceKey = getRequiredProperty("SOURCE_KEY");
    String removeSource = getProperty("REMOVE_SOURCE");
    doRemoveSource = removeSource == null || removeSource.toUpperCase().startsWith("T");
  }


  public Map preprocessArguments(Map arguments, IQueryService qs) throws TransferObjectException {
    List source = (List) arguments.get(sourceKey);
    List dest = (List) arguments.get(destinationKey);
    if (source == null){
      source = Collections.EMPTY_LIST;
    }
    if (dest == null){
      dest = new ArrayList(source.size());
      arguments.put(destinationKey, dest);
    }
    dest.addAll(source);
    if (doRemoveSource)
      arguments.remove(sourceKey);
    return arguments;
  }

}
