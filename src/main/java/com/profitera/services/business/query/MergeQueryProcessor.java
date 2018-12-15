package com.profitera.services.business.query;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class MergeQueryProcessor extends BaseListQueryProcessor {

	protected String getSummary() {
		return "Merge the result from two different query based on a defined key";
	}

	protected String getDocumentation() {
		return "Two utility query processors that merge the results of two queries \n"
		+ "based on a defined key and inject a cached look up value into a result \n"
		+ "based on the configured query and key.";
	}

	private String mergeKey;
	private String mergeQueryName;
	private static final String MERGE_KEY="MERGE_KEY";
	private static final String MERGE_QUERY="MERGE_QUERY";
	
	public MergeQueryProcessor(){
		addRequiredProperty(MERGE_KEY, String.class, "The key use to identify which record to merge", "The key use to identify which record to merge");
		addRequiredProperty(MERGE_QUERY, String.class, "The query that retrieve the records which will be merged into original records", "The query that retrieve the records which will be merged into original records");
	}

	protected void configureProcessor() {
		super.configureProcessor();
		mergeKey = (String)getProperty(MERGE_KEY);
		mergeQueryName = (String)getProperty(MERGE_QUERY);
	}

	public List postProcessResults(Map arguments, List result, IQueryService qs)
			throws TransferObjectException {
		if (result == null || result.size() == 0)
			return result;
		int masterKeyMissing = 0;
		int mergeKeyMissing = 0;
		Map mergeFrom = getMergerResults(arguments, qs);
		for (Iterator i = result.iterator(); i.hasNext();) {
			Map r = (Map) i.next();
			Object keyVal = r.get(mergeKey);
			if (keyVal != null) {
				Map mergeMap = (Map) mergeFrom.get(keyVal);
				if (mergeMap != null) {
					r.putAll(mergeMap);
				} else {
					mergeKeyMissing++;
				}
			} else {
				masterKeyMissing++;
			}
		}
		return result;
	}

	protected Map getMergerResults(Map arguments, IQueryService qs)
			throws TransferObjectException {
		TransferObject to = qs.getQueryList(mergeQueryName, arguments);
		if (to.isFailed())
			throw new TransferObjectException(to);
		List results = (List) to.getBeanHolder();
		// I know how many items I will put inside so I think this load
		// factor and capacity should ensure NO rehashing.
		Map m = new HashMap((int) (results.size() * 1.2), 1.0f);
		for (Iterator i = results.iterator(); i.hasNext();) {
			Map element = (Map) i.next();
			m.put(element.get(mergeKey), element);
		}
		return m;
	}

	public Map preprocessArguments(Map arguments, IQueryService qs)
			throws TransferObjectException {
		return arguments;
	}
}
