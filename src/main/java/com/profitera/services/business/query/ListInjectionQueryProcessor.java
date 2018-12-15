package com.profitera.services.business.query;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class ListInjectionQueryProcessor extends AbstractListQueryProcessor {

	protected String query;
  protected String key;

  public Map preprocessArguments(Map arguments, IQueryService qs) throws TransferObjectException {
    return arguments;
  }

  public List postProcessResults(Map arguments, List result, IQueryService qs) throws TransferObjectException {
    for (Iterator i = result.iterator(); i.hasNext();) {
      Map r = (Map) i.next();
      TransferObject to = qs.getQueryList(query, r);
      if (to.isFailed())
        throw new TransferObjectException(to);
      List l = (List) to.getBeanHolder();
      r.put(key, l);
    }
    return result;
  }

  protected void configureProcessor() {
    super.configureProcessor();
    query = getRequiredProperty("LIST_QUERY");
    key = getRequiredProperty("LIST_KEY");
  }
  
  

}
