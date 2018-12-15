package com.profitera.services.business.query;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class ResultValueDivisionProcessor extends BaseListQueryProcessor {
	
	private static final String DENOMINATOR = "DENOMINATOR";
	private static final String NUMERATOR = "NUMERATOR";
	private static final String RESULT_KEY = "RESULT_KEY";

	public ResultValueDivisionProcessor(){
		addRequiredProperty(NUMERATOR, String.class, "The numerator field", "The field whose value is used as the numerator.");
		addRequiredProperty(DENOMINATOR, String.class, "The denominator field", "The field whose value is used as the denominator.");
		addRequiredProperty(RESULT_KEY, String.class, "The result value field", "The field the result of the division is assigned to.");
	}

	public List postProcessResults(Map arguments, List result, IQueryService qs) throws TransferObjectException {
		for (Iterator i = result.iterator(); i.hasNext();) {
			try {
				Map row = (Map) i.next();
				Number n = (Number) row.get(getProperty(NUMERATOR));
				Number d = (Number) row.get(getProperty(DENOMINATOR));
				// Can't divide by nothing or divide nothing by something.
				if (n == null || d == null){
					continue;
				}
				//No divide by 0
				if (d.longValue() == 0){
					continue;
				}
				double answer = n.doubleValue()/d.doubleValue();
				row.put(getProperty(RESULT_KEY), new Double(answer));
			} catch (ClassCastException e){
				getLog().warn(ListQueryService.getLogId(qs) + "Non-numeric division field referenced, no operation performed");
			}
		}
		return super.postProcessResults(arguments, result, qs);
	}

  protected String getDocumentation() {
    return "Divides the numerator by the denomenator and assigns it to another field, any nulls involved result in no operation taking place, as does any conditions that will cause a divide by zero.";
  }

  protected String getSummary() {
    return "Divides one number in a result by another and assigns the value to another field";
  }

}
