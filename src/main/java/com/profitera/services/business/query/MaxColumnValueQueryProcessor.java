package com.profitera.services.business.query;

/**
 * @author Avitus
 *
 */
public class MaxColumnValueQueryProcessor extends AbstractColumnValueQueryProcessor {
	protected boolean compare(Object obj, Object resultObj) throws ClassCastException {
		// null will be treated as lower than all other values
		if (resultObj == null) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		// returns true if obj is greater than resultObj
		int result = ((Comparable) obj).compareTo((Comparable) resultObj);
		if (result > 0) {
			return true;
		}
		return false;
	}

  protected String getDocumentation() {
    return "Assigns the maximum value of the selected fields to the assignment field, null is considered smaller than all other values. If incompatible result types are compared a fatal error will occur.";
  }

  protected String getSummary() {
    return "Assigns the maximum value of the selected fields to the assignment field.";
  }
}
