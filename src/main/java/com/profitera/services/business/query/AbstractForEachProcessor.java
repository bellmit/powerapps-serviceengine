package com.profitera.services.business.query;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public abstract class AbstractForEachProcessor extends BaseListQueryProcessor {

  public List postProcessResults(Map arguments, List result, IQueryService qs) throws TransferObjectException {
    for (Iterator i = result.iterator(); i.hasNext();) {
      Map m = (Map) i.next();
      processResultRow(m, qs);
    }
    return super.postProcessResults(arguments, result, qs);
  }

  protected abstract void processResultRow(Map row, IQueryService qs);
}
