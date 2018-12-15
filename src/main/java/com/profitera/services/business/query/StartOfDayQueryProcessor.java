package com.profitera.services.business.query;

import java.util.Date;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.util.DateParser;

public class StartOfDayQueryProcessor extends BaseListQueryProcessor {
  private static final String KEY = "KEY";
  private String key;

  public StartOfDayQueryProcessor() {
    addRequiredProperty(KEY, String.class, "Argument key", "The name of the argument to be adjusted to the start of its assigned date");
  }

  protected void configureProcessor() {
    key = (String) getProperty(KEY);
  }

  public Map preprocessArguments(Map arguments, IQueryService qs)
      throws TransferObjectException {
    Object o = arguments.get(key);
    if (!(o instanceof Date)) {
      getLog().warn(
          "Query " + getQueryName() + " configured to adjust date of argument "
              + key + " but argument is not of type " + Date.class.getName());
      return arguments;
    }
    Date d = (Date) o;
    if (d != null) {
      d = DateParser.getStartOfDay(d);
      arguments.put(key, d);
    }
    return arguments;
  }
  
  static String getLongDoc(){
    return "Sample config:<programlisting>" 
    + "<![CDATA[<query name='getTreatmentHistory'>\n"
    + " <processor implementation='com.profitera.services.business.query.EndOfDayQueryProcessor'>\n"
    + "  <properties>\n"
    + "   <property name='KEY' value='END_DATE'/>\n"
    + "  </properties>\n"
    + " <processor implementation='com.profitera.services.business.query.StartOfDayQueryProcessor'>\n"
    + "  <properties>\n"
    + "   <property name='KEY' value='START_DATE'/>\n"
    + "  </properties>\n"
    + " </processor>\n"
    + "</query>]]>\n"
        + "</programlisting>"
        +" </para><para> Query using the end of the day of the argument being adjusted to capture accurate results:"
        + "<programlisting>"
        + "<![CDATA[<select id='getTreatmentHistory' parameterClass='map' resultclass='long'>"
    + " select count(*) from ptracc_treatment_plan plan\n" 
    + " inner join ptrtreatment_process proc on plan.treatment_plan_id = proc.treatment_plan_id and proc.process_type_id = 2\n"
    + " inner join PTRPAYMENT_INSTALLMENT inst on proc.TREATMENT_PROCESS_ID = inst.TREATMENT_PROCESS_ID\n"
    + " where account_id = #ACCOUNT_ID# and CREATED_DATE between #START_DATE# and #END_DATE#\n"
  + "</select>]]></programlisting>"
  + "If the argument to be adjusted is null this processor does nothing, if the argument to be adjusted is not of type java.util.Date if prints a WARN message.";
  }
  
  protected String getDocumentation() {
    return getLongDoc();
  }

  protected String getSummary() {
    return "Adjusts an <emphasis>existing</emphasis> java.util.Date argument to"
    +" the start of the day it is assigned (2005-01-01-09:00:00 becomes"
    + "2005-01-01-00:00:00).";
  }


}
