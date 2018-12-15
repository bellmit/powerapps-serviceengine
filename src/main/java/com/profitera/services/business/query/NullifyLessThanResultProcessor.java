package com.profitera.services.business.query;

import java.util.Map;

public class NullifyLessThanResultProcessor extends AbstractForEachProcessor {
  private static final String COMPARE_TO = "COMPARE_TO";
  private static final String TARGET = "TARGET";
  protected String getDocumentation() {
		return "<programlisting><![CDATA[" 
		+ "Query result:\n"
		+ "	ACCOUNT # | OUTSTANDING_AMT | DELINQUENT_AMT\n"
		+ "	--------------------------------------------\n"
		+ "	        1 |           100.00|          50.00\n"
		+ "	        2 |            50.00|          50.00\n"
		+ "	        3 |            50.00|         100.00\n"
		+ "	        4 |           (null)|         100.00\n"
		+ "	        5 |           100.00|         (null)\n"
		+ "	<query name='getSomeInformation'>\n"
		+ "	  <processor implementation='com.profitera.services.business.query.NullifyLessThanResultProcessor'>\n"
		+ "	    <properties>\n"
        + "\n"
		+ "	      <property name='TARGET' value='DELINQUENT_AMT'/>\n"
		+ "       <property name='COMPARE_TO' value='OUTSTANDING_AMT'/>\n"
		+ "	    </properties>\n"
		+ "	  </processor>\n"
		+ "	</query>\n"
		+ "\n"
		+ "	Result after processing:\n"
		+ "	ACCOUNT # | OUTSTANDING_AMT | DELINQUENT_AMT\n"
		+ "	--------------------------------------------\n"
		+ "         1 |           100.00|         (null)\n"
		+ "	        2 |            50.00|          50.00\n"
		+ "	        3 |            50.00|         100.00\n"
		+ "	        4 |           (null)|         (null)\n"
		+"]]></programlisting>\n";
	}

	protected String getSummary() {
		return "If the target is less than the compared value, the target value will be replaced by null";
	}

	public NullifyLessThanResultProcessor(){
		addRequiredProperty(TARGET, String.class, "Target Field", "The field that need to campare.");
		addRequiredProperty(COMPARE_TO, String.class, "Compare Field", "The Field that use to campare.");
	}
	
private String targetColumn;
  private String compareToColumn;
  protected void configureProcessor() {
    super.configureProcessor();
    targetColumn = getProperty(TARGET).toString();
    compareToColumn = getProperty(COMPARE_TO).toString();
  }

  protected void processResultRow(Map row, IQueryService qs) {
    Comparable t = (Comparable) row.get(targetColumn);
    Comparable c = (Comparable) row.get(compareToColumn);
    if (c == null){
      row.put(targetColumn, null);
    } else if (t != null && t.compareTo(c) < 0){
      row.put(targetColumn, null);
    }
  }

}
