package com.profitera.services.system.loan.impl;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.financial.PostingScheduleParseConfig;
import com.profitera.financial.PostingScheduleParseConfig.EntryParseConfig;
import com.profitera.services.system.loan.impl.PostingSchedule.PostingType;
import com.profitera.util.MapListUtil;

public class PostingScheduleParser {
  private final PostingScheduleParseConfig conf;
  public PostingScheduleParser(PostingScheduleParseConfig config) {
    conf = config;
  }
  
  public PostingType getScheduleType(Map<String, Object> target) throws AbortTransactionException {
    String code = (String) target.get(conf.getScheduleTypeField());
    if (code == null) {
      throw new AbortTransactionException("Schedule posting type specified by '" + conf.getScheduleTypeField() + "' is not defined");
    }
    return PostingSchedule.getAccountCodePostingType(code);
  }
  
  public PostingSchedule parse(Map<String, Object> target) throws AbortTransactionException {
    Date end = null;
    if (conf.getEndDateField() != null){
      end = (Date) target.get(conf.getEndDateField());
    }
    Date start = null;
    if (conf.getStartDateField() != null){
      start = (Date) target.get(conf.getStartDateField());
    }
    
    PostingSchedule s;
    Object o = target.get(conf.getEntryListField());
    if (o == null || !(o instanceof List)) {
      throw new AbortTransactionException("Field " + conf.getEntryListField() + " was not a list and could not be processed as the list of entries: " + o);
    }
    List<Map<String, Object>> entries = (List<Map<String, Object>>) o;
    if (start == null) {
      int index = MapListUtil.findMin(conf.getEntryConfig().getEntryDateField(), entries);
      if (index == -1) {
        throw new AbortTransactionException("Either a specific start date is required or one or more posting schedule entries in " + conf.getEntryListField() + " to establish start date");
      }
      Map<String, Object> temp = entries.get(index);
      start = (Date) temp.get(conf.getEntryConfig().getEntryDateField());
    }
    s = new PostingSchedule(null, start, end);
    for (Iterator<Map<String, Object>> i = entries.iterator(); i.hasNext();) {
      Map<String, Object> m = i.next();
      addEntryFromTarget(s, m);
    }
    return s;
  }

  private void addEntryFromTarget(PostingSchedule s, Map<String, Object> m)
      throws AbortTransactionException {
    EntryParseConfig e = conf.getEntryConfig();
    Date entryDate = (Date) m.get(e.getEntryDateField());
    BigDecimal entryRate = (BigDecimal) m.get(e.getEntryRateField());
    String cycle = null;
    if (e.getEntryCycleField() != null) {
      cycle = getBillingCycleCode(m.get(e.getEntryCycleField()), e.getEntryCycleField());
    }
    String postingType = (String) m.get(e.getEntryPostField());
    s.addSchedule(entryDate, entryRate, cycle, postingType);
  }

  private String getBillingCycleCode(Object value, String field) throws AbortTransactionException {
    if (value instanceof String || value == null) {
      return (String) value;
    }
    if (value instanceof Date) {
      Date d = (Date) value;
      Calendar c = Calendar.getInstance();
      c.setTime(d);
      int day = c.get(Calendar.DAY_OF_MONTH);
      return day + "";
    }
    if (value instanceof Number) {
      Number n = (Number) value;
      return "" + n.intValue();
    }
    throw new AbortTransactionException("Value for billing cycle specified by " + field + " is not of a valid type: " + value + "/" + value.getClass().getName());
  }
}
