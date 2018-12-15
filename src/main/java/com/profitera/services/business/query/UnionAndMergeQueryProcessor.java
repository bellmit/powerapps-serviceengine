/*
 * Created on Jun 13, 2006
 *
 */
package com.profitera.services.business.query;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class UnionAndMergeQueryProcessor extends MergeQueryProcessor {
	
	protected String getDocumentation() {
		return "This processor will merge and union those results from the merge query which are\n"
		+ "not found in the main query.";
	}

	protected String getSummary() {
		return "Work the same like MergeQueryProcessor but union the result";
	}

	private static final String MERGE_KEY = "MERGE_KEY";
	private String mergeKey;

	public UnionAndMergeQueryProcessor() {

	}

	protected void configureProcessor() {
		super.configureProcessor();
		mergeKey = (String) getProperty(MERGE_KEY);
	}

	public List postProcessResults(Map arguments, List result, IQueryService qs)
			throws TransferObjectException {

		Map mergeFrom = getMergerResults(arguments, qs);

		for (Iterator i = result.iterator(); i.hasNext();) {
			Map r = (Map) i.next();
			Object keyVal = r.get(mergeKey);
			if (keyVal != null) {
				Map mergeMap = (Map) mergeFrom.get(keyVal);
				if (mergeMap != null) {
					r.putAll(mergeMap);
					mergeFrom.remove(keyVal);
				}
			}
		}

		for (Iterator i = mergeFrom.keySet().iterator(); i.hasNext();) {
			Map mergeMap = (Map) mergeFrom.get(i.next());
			result.add(mergeMap);
		}
		return result;
	}
}
