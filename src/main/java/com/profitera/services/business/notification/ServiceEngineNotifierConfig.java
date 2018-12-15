package com.profitera.services.business.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.server.ServiceEngine;
import com.profitera.util.ResourceBundles;

/**
 * @author jamison
 */
public class ServiceEngineNotifierConfig implements IScheduleManager, INotifierPropertyProvider {
  private Log log = LogFactory.getLog(ServiceEngineNotifierConfig.class);
  private static final String BATCH_SIZE_PROP_SUFFIX = "_BATCH_SIZE";
  private static final String END_HOUR_PROP_SUFFIX = "_END_HOUR";
  private static final String START_HOUR_PROP_SUFFIX = "_START_HOUR";
  private static final String INTERVAL_PROP_SUFFIX = "_INTERVAL";
  private static final String DAY_OF_MONTH_PROP_SUFFIX = "_DAY_OF_MONTH";
  private static final String DAY_OF_WEEK_PROP_SUFFIX = "_DAY_OF_WEEK";
  private static final Map DAY_OF_WEEK_MAP = new HashMap();
  static {
    DAY_OF_WEEK_MAP.put("SUN", new Integer(Calendar.SUNDAY));
    DAY_OF_WEEK_MAP.put("MON", new Integer(Calendar.MONDAY));
    DAY_OF_WEEK_MAP.put("TUE", new Integer(Calendar.TUESDAY));
    DAY_OF_WEEK_MAP.put("WED", new Integer(Calendar.WEDNESDAY));
    DAY_OF_WEEK_MAP.put("THU", new Integer(Calendar.THURSDAY));
    DAY_OF_WEEK_MAP.put("FRI", new Integer(Calendar.FRIDAY));
    DAY_OF_WEEK_MAP.put("SAT", new Integer(Calendar.SATURDAY));
  }
  
  private class NotificationProps {
    private int intervalMinutes = -1;
    private int startHour = -1;
    private int endHour = -1;
    private int batchSize = -1;
    private int dayOfMonth = -1;
    private String dayOfWeekText = "";
    private int[] dayOfWeek = new int[0];
  }
  
  public ServiceEngineNotifierConfig() {
    
  }

  private NotificationProps loadProperties(String code) {
    ResourceBundle rb = null;
    try {
      rb = ServiceEngine.getConfigAsResourceBundle();
    } catch (MissingResourceException e) {
      return null;
    }
    return loadProperties(rb, code);
  }

  private NotificationProps loadProperties(ResourceBundle rb, String notifierCode) {
    NotificationProps props = new NotificationProps();
    props.intervalMinutes = ResourceBundles.getBundleInt(rb, notifierCode + INTERVAL_PROP_SUFFIX, props.intervalMinutes);
    props.startHour = ResourceBundles.getBundleInt(rb, notifierCode + START_HOUR_PROP_SUFFIX, props.startHour);
    props.endHour = ResourceBundles.getBundleInt(rb, notifierCode + END_HOUR_PROP_SUFFIX, props.endHour);
    props.batchSize = ResourceBundles.getBundleInt(rb, notifierCode + BATCH_SIZE_PROP_SUFFIX, props.batchSize);
    props.dayOfMonth = ResourceBundles.getBundleInt(rb, notifierCode + DAY_OF_MONTH_PROP_SUFFIX, props.dayOfMonth);
    props.dayOfWeekText = ResourceBundles.getBundleString(rb, notifierCode + DAY_OF_WEEK_PROP_SUFFIX, "");
    props.dayOfWeek = getDays(props.dayOfWeekText);
    return props;
  }
  
  private int[] getDays(String daysOfWeek) {
    if (daysOfWeek == null)
      return new int[0];
    String[] days = daysOfWeek.split("[,; ]");
    List dayInts = new ArrayList();
    for (int i = 0; i < days.length; i++) {
      if (days[i].trim().length() >= 3){
        String abbrev = days[i].trim().substring(0,3).toUpperCase();
        Integer dow = (Integer) DAY_OF_WEEK_MAP.get(abbrev);
        if (dow != null){
          dayInts.add(dow);
        }
      }
    }
    int[] ints = new int[dayInts.size()];
    for (int i = 0; i < ints.length; i++) {
      ints[i] = ((Integer)dayInts.get(i)).intValue();
    }
    return ints;
  }

  private boolean isValidTime(NotificationProps props) {
	Calendar c = Calendar.getInstance();
	int currentHour = c.get(Calendar.HOUR_OF_DAY);
	return props.startHour <= currentHour && props.endHour > currentHour;
  }

  /**
   * @see com.profitera.services.business.notification.IScheduleManager#isPermitted(java.lang.String,
   *      long)
   */
  public boolean isPermitted(String code, long elapsed) {
    NotificationProps props = loadProperties(code);
    if (props == null){
      log.error(code + ": unable to load configuration, this notifier will never be triggered.");
      return false;
    }
    if (props.intervalMinutes <= 0) {
      log.error(code + ": interval invalid, this notifier will never be triggered.");
      return false;
    }
    return isValidDay(code, props) && isValidTime(props) && elapsed > 0 && elapsed % props.intervalMinutes == 0;
  }
  
  private boolean isValidDay(String code, NotificationProps props) {
    boolean validDOM = false;
    boolean isCheckingDOM = true;
    boolean validDOW = false;
    boolean isCheckingDOW = true;
    // If the property is set to an invalid value, do not
    // filter on it, i.e. run daily.
    if (props.dayOfMonth < 1 || props.dayOfMonth > 31){
      validDOM = true;
      isCheckingDOM = false;
    } else {
      Calendar c = Calendar.getInstance();
      int currentDayOfMonth = c.get(Calendar.DAY_OF_MONTH);
      validDOM = currentDayOfMonth == props.dayOfMonth;
      isCheckingDOM = true;
    }
    if (props.dayOfWeek.length == 0){
      validDOW = true;
      isCheckingDOW = false;
    } else {
      Calendar c = Calendar.getInstance();
      int currentDayOfWeek = c.get(Calendar.DAY_OF_WEEK);
      int index = Arrays.binarySearch(props.dayOfWeek,currentDayOfWeek);
      validDOW = index > -1 && index < props.dayOfWeek.length && props.dayOfWeek[index] == currentDayOfWeek;
      isCheckingDOW = true;
    }
    
    if (isCheckingDOM && isCheckingDOW){
      log.warn(code + ": notifier is restricted by both day of week (" + props.dayOfWeekText + ") and day of month (" + props.dayOfMonth + ").");
    }
    
    return validDOM && validDOW;
  }

  public String getNotifierProperty(String notifierCode, String propertySuffix, String defaultValue) {
    // Props should always be in form NOTIFIERCODE_SUFFIX, so this just makes
    // sure that
    // the _ is present.
    if (!propertySuffix.startsWith("_"))
      propertySuffix = "_" + propertySuffix;
    return ServiceEngine.getProp(notifierCode + propertySuffix, defaultValue);
  }

  public int getNotifierIntegerProperty(String notifierCode, String propertySuffix, int defaultValue) {
    // Props should always be in form NOTIFIERCODE_SUFFIX, so this just makes
    // sure that
    // the _ is present.
    if (!propertySuffix.startsWith("_"))
      propertySuffix = "_" + propertySuffix;
    return ServiceEngine.getIntProp(notifierCode + propertySuffix, defaultValue);
  }
}