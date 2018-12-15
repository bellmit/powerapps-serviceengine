/*
 * Created on Mar 31, 2006
 *
 */
package com.profitera.services.business.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.util.Strings;

public class ResultSplitterQueryProcessor extends AbstractListQueryProcessor {
	private String[] columns;
	private String sharedColumnNames;
	
	public Map preprocessArguments(Map arguments, IQueryService qs)
			throws TransferObjectException {
		return arguments;
	}

	protected void configureProcessor() {
		super.configureProcessor();
    List columnLists = new ArrayList();
    String col = null;
    int i = 0;
    while(true){
      i++;
      col = getProperty("COLUMN_NAMES_" + i);
      if (col == null && i < 100){
        col = getProperty("COLUMN_NAMES_" + Strings.leftPad(""+ i, 2, '0'));  
      }
      if (col == null && i < 1000){
        col = getProperty("COLUMN_NAMES_" + Strings.leftPad(""+ i, 3, '0'));  
      }
      if (col == null){
        break;
      } else {
        columnLists.add(col);
      }
    }
		sharedColumnNames = getRequiredProperty("COLUMN_NAMES");
    columns = (String[]) columnLists.toArray(new String[0]);
	}

	public List postProcessResults(Map arguments, List result, IQueryService qs)
			throws TransferObjectException {
		if (result == null || result.size() == 0){
			return result;
    }
    String[] cNames = sharedColumnNames.split(";");
    Map data = (Map) result.get(0);
    List newResult = new ArrayList(columns.length);
    for (int i = 0; i < columns.length; i++) {
      String[] thisRowNames = columns[i].split(";");
      if(thisRowNames.length!=cNames.length){
        throw new IllegalArgumentException("The column names specified do not have equal amount of names.");
      }
      Map record = new HashMap();
      for (int j = 0; j < cNames.length; j++) {
        if (data.containsKey(thisRowNames[j])) {
          record.put(cNames[j], data.get(thisRowNames[j]));
        }
      }
      newResult.add(record);
    }
		return newResult;
	}
  
}
