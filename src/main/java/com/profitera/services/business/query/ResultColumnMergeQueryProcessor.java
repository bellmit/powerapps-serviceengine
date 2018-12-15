/*
 * Created on Jan 22, 2007
 *
 */
package com.profitera.services.business.query;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.util.CollectionUtil;
import com.profitera.util.MapListUtil;
import com.profitera.util.Strings;

public class ResultColumnMergeQueryProcessor extends BaseListQueryProcessor {
	private String mergeKey;
	private String[] columns;
	private String separator;
	private static final String MERGE_KEY = "MERGE_KEY";
	private static final String COLUMN_NAMES = "COLUMN_NAMES";
	private static final String COLUMN_VALUE_SEPARATOR = "COLUMN_VALUE_SEPARATOR";
	private static final String DEFAULT_SEPARATOR = "";	
	
	public ResultColumnMergeQueryProcessor() {
		addRequiredProperty(MERGE_KEY, String.class, "The merge key", "Specifies the boundaries of the merge");
		addRequiredProperty(COLUMN_NAMES, String.class, "Semi-colon delimited list of field names", "Specifies the list of fields to extract and concatenate.");
		addProperty(COLUMN_VALUE_SEPARATOR, String.class, DEFAULT_SEPARATOR, "Value to place between merged entries", "Used to delimit the values in the results, typically a comma or a dash if specified.");
	}
  
	protected void configureProcessor() {
		super.configureProcessor();
	    mergeKey = (String) getProperty(MERGE_KEY);
	    String columnNames = (String) getProperty(COLUMN_NAMES);
		if (columnNames != null) {
			columns = columnNames.split(";");
		}
	    separator = (String)getProperty(COLUMN_VALUE_SEPARATOR);
	}
	
	public List postProcessResults(Map arguments, List result, IQueryService qs)
			throws TransferObjectException {
		
		Map columnMap = new HashMap();
		for (int x = 0; x < columns.length; x++){
			columnMap.put(columns[x], MapListUtil.groupBy(result, mergeKey, columns[x]));
		}
		
		Map mergedResultMap = new HashMap();
		
		for (Iterator i = result.iterator(); i.hasNext();){
			Map r = (Map) i.next();
			Object keyVal = r.get(mergeKey);
			for (int x = 0; x < columns.length; x++){
				Map m = (Map)columnMap.get(columns[x]);
				List l = (List)m.get(keyVal);
				r.put(columns[x], Strings.getListString(l, separator));
			}
			mergedResultMap.put(keyVal, r);
		}
		return CollectionUtil.asList(mergedResultMap.values().iterator());
	}

  protected String getDocumentation() {
    return "If a query returns the results [{A=a,B=99,C=1001},{A=b,B=88,C=1001},{A=c,B=77,C=1022}] and this processor is configured with " + COLUMN_NAMES + " as 'A;B' and a " + COLUMN_VALUE_SEPARATOR + " of '*' with C as " + MERGE_KEY + ", the return result would be [{A=a*b,B=99*88,C=1001},{A=c,B=77,C=1022}] .";
  }

  protected String getSummary() {
    return "Concatenates the multiple columns' value into a single column";
  }
	
}
