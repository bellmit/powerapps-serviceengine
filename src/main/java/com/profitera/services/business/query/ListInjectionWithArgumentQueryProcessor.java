/*
 * Created on Jun 28, 2006
 *
 */
package com.profitera.services.business.query;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class ListInjectionWithArgumentQueryProcessor extends ListInjectionQueryProcessor {
	public List postProcessResults(Map arguments, List result, IQueryService qs) throws TransferObjectException {
	    for (Iterator i = result.iterator(); i.hasNext();) {
	      Map r = (Map) i.next();
	      r.putAll(arguments);
	      TransferObject to = qs.getQueryList(query, r);
	      if (to.isFailed())
	        throw new TransferObjectException(to);
	      List l = (List) to.getBeanHolder();
	      r.put(key, l);
	    }
	    return result;
	  }
}
