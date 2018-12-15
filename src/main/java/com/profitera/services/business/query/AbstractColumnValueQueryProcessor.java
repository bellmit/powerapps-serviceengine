package com.profitera.services.business.query;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

/**
 * @author Avitus
 *
 */
public abstract class AbstractColumnValueQueryProcessor extends BaseListQueryProcessor {
	private static final String SELECTION_KEYS = "SELECTION_KEYS";
	private static final String RESULT_KEY = "RESULT_KEY";
	
	private String[] selectionKeys;
	private String resultKey;
	
	protected abstract boolean compare(Object obj, Object max) throws ClassCastException;
	
	public AbstractColumnValueQueryProcessor() {
		addRequiredProperty(SELECTION_KEYS, String.class, "Semi-colon delimited list of fields to compare", "Each field in this list is evaluated against the selection condition.");
		addRequiredProperty(RESULT_KEY, String.class, "Key that selected value is assigned to", "Key that selected value is assigned to.");
	}
	
	protected void configureProcessor() {
		String keys = (String) getProperty(SELECTION_KEYS);
		if (keys != null && !keys.trim().equals("")) {
			selectionKeys = keys.split(";");
		}
		resultKey = (String) getProperty(RESULT_KEY);
	}
	
	public List postProcessResults(Map arguments, List result, IQueryService qs) throws TransferObjectException {
		if (selectionKeys != null) {
			if (selectionKeys.length > 1) {
				for (Iterator i = result.iterator(); i.hasNext();) {
					Map row = (Map) i.next();
					String resultField = selectionKeys[0];
					Object resultObj = row.get(resultField);
					for (int j = 1; j < selectionKeys.length; j++) {
						String field = selectionKeys[j];
						Object obj = row.get(field);
						try {
							if (compare(obj, resultObj)) {
								resultObj = obj;
								resultField = field;
							}
						} catch (ClassCastException cce) {
							String errorMessage = resultField + " (" + resultObj.getClass().getName() + ") can't be compared to " + 
												  field + " (" + obj.getClass().getName() + ")";
							getLog().error(errorMessage);
							throw new TransferObjectException(new TransferObject(TransferObject.ERROR, errorMessage));
						}
					}
					if (resultKey != null) {
						row.put(resultKey, resultObj);
					}
				}
			}
		}
		return super.postProcessResults(arguments, result, qs);
	}
}
