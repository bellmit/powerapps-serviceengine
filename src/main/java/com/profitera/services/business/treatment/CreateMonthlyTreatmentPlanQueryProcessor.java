package com.profitera.services.business.treatment;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.util.CollectionUtil;
import com.profitera.util.DateParser;

public class CreateMonthlyTreatmentPlanQueryProcessor extends
    CreateOpenTreatmentPlanQueryProcessor {
  private Object[] required;

  protected boolean shouldCreatePlan(List result) {
    Date date = DateParser.getEndOfMonth(new Date());
    Date eom = DateParser.getEndOfDay(date); 
    for (Iterator i = result.iterator(); i.hasNext();) {
      Map plan = (Map) i.next();
      Date start = (Date) plan.get("TREATMENT_START_DATE");
      Date end = (Date) plan.get("TREATMENT_END_DATE");
      if (start != null && (start.before(eom)	|| start.compareTo(eom) == 0) 
      		&& (end == null || end.after(eom) || end.compareTo(eom)==0)){
        return false;
      }
    }
    return true;
  }

  public Object[] getRequiredReturnColumns() {
    if (required == null){
      required = CollectionUtil.extendArray(super.getRequiredReturnColumns(), "TREATMENT_START_DATE");
    }
    return required; 
  }

}
