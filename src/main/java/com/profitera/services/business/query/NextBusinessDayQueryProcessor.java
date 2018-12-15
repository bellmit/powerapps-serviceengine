package com.profitera.services.business.query;

import java.util.Calendar;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class NextBusinessDayQueryProcessor extends BaseListQueryProcessor {
  
  private static final String DEFAULT_ARG_NAME = "NEXT_BUSINESS_DAY";
  

  public Map preprocessArguments(Map arguments, IQueryService qs) throws TransferObjectException {
    Calendar c = getNow();
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    c.add(Calendar.DATE, 1);
    int day = c.get(Calendar.DAY_OF_WEEK);
    while (isWeekend(day) || isHoliday(c)){
      c.add(Calendar.DATE, 1);
      day = c.get(Calendar.DAY_OF_WEEK);
    }
    arguments.put(getArgName(), c.getTime());
    return arguments;
  }

/**
 * I am actually only here for testing purposes, I need a way to 
 * assign a date to find the next business day from.
 * It is important that this method returns a new object every time
 * because it will be changed and Calendars are notoriaously non-threadsafe.
 * @return a <b>new</b> Calendar set to today's date
 */
protected Calendar getNow() {
	Calendar c = Calendar.getInstance();
	return c;
}

  private Object getArgName() {
    return DEFAULT_ARG_NAME;
  }

  private boolean isHoliday(Calendar c) {
    // TODO Holiday management integration not done,
    // unsure as to how to do so relieably.
    return false;
  }

  private boolean isWeekend(int day) {
    return day == Calendar.SATURDAY || day == Calendar.SUNDAY;
  }

protected String getDocumentation() {
	return "Advances the date to the next business day, with no consideration of public holidays."+ "It is assumed that the weekend is Saturday and Sunday. The new date argument is set to midnight.";
}

protected String getSummary() {
	return "Assigns the next business day date to the argument " + DEFAULT_ARG_NAME;
}

}
