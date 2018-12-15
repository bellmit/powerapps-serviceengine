package com.profitera.services.business.query;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.util.DateParser;

/**
 * @author jamison
 */
public class FirstOfMonthProcessor extends BaseListQueryProcessor {

  private static final String DEFAULT_NAME = "FIRST_OF_MONTH";
  

  /**
   * @see com.profitera.services.business.query.IListQueryProcessor#preprocessArguments(java.util.Map, IQueryService)
   */
  public Map preprocessArguments(Map arguments, IQueryService qs) throws TransferObjectException {
    arguments.put(getDateArgumentName(), getArgumentDate());
    return arguments;
  }

  protected String getDateArgumentName() {
    return DEFAULT_NAME;
  }
  protected Date getArgumentDate() {
    Calendar c = Calendar.getInstance();
    c.set(Calendar.DAY_OF_MONTH, 1);
    return DateParser.getStartOfDay(c.getTime());
  }

  protected String getSummary() {
    return "Assigns the first of the current month to the argument 'FIRST_OF_MONTH' and sets the time of that date to midnight.";
  }
	
  protected String getDocumentation() {
    return "Adds a <emphasis>new</emphasis> java.util.Date argument to the"
				+ "arguments, setting its value to midnight on the first day of the current"
				+"month. The name of the argument is FIRST_OF_MONTH. Sample config:<programlisting>"
				+
"<![CDATA[<query name=\"getTreatmentThisMonth\">\n"
  +"<processor implementation=\"com.profitera.services.business.query.FirstOfMonthProcessor\"/>"
  +"</query>]]></programlisting></para>\n"
			+"<para><programlisting><![CDATA[<select id=\"getTreatmentThisMonth\" parameterClass=\"map\" resultclass=\"long\">\n"
+" select count(*) from ptracc_treatment_plan plan\n" 
      +" inner join ptrtreatment_process proc on plan.treatment_plan_id = proc.treatment_plan_id and proc.process_type_id = 2"
      + " inner join PTRPAYMENT_INSTALLMENT inst on proc.TREATMENT_PROCESS_ID = inst.TREATMENT_PROCESS_ID"
    + " where account_id = #ACCOUNT_ID# and CREATED_DATE > #FIRST_OF_MONTH#"
  +"</select>]]></programlisting>"
				+"If the argument FIRST_OF_MONTH already exists it will be overwritten with this value.";
  }

}
