/*
 * Created on Jun 23, 2006
 *
 */
package com.profitera.services.business.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class ForEachResultReplacementQueryProcessor  extends AbstractListQueryProcessor {
	private String replacementQueryName;
	
	public Map preprocessArguments(Map arguments, IQueryService qs) throws TransferObjectException {
		return arguments;
	}

	protected void configureProcessor() {
		super.configureProcessor();
		replacementQueryName = getRequiredProperty("REPLACEMENT_QUERY");	
	}
	public List postProcessResults(Map arguments, List result, IQueryService qs) throws TransferObjectException {
		List results = new ArrayList();
		for (Iterator i = result.iterator(); i.hasNext();) {
			TransferObject to = qs.getQueryList(replacementQueryName,
					(Map) i.next());
			if (to.isFailed())
				throw new TransferObjectException(to);
			List list = (List) to.getBeanHolder();
			
			if(list!=null&&list.size()!=0)
				results.addAll(list);				
		}
		
		return results;
	}
}
