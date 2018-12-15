package com.profitera.services.business.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractNotificationProcessor implements INotificationProcessor {
  private final static Log LOG = LogFactory.getLog(AbstractNotificationProcessor.class);
  private String code;
  private INotifierPropertyProvider props;
  private List ok = new ArrayList();
  private List failed = new ArrayList();
  
  protected Log getLog(){
    return LOG;
  }
  
  protected void addSuccessfulProcess(Map p){
    ok.add(p);
  }
  
  protected void addUnsuccessfulProcess(Map p){
    failed.add(p);
  }
  
  public String getNotifierCode() {
    return code;
  }

  public void setNotifierCode(String code) {
    this.code = code;
  }

  public void setPropertyProvider(INotifierPropertyProvider pp) {
    props = pp;
  }

  public int getBatchSize() {
    return getNotifierIntegerProperty(INotifier.BATCH_SIZE, 0);
  }

  public String getTreatmentProcessQueryName() {
    return getNotifierProperty("QUERY_NAME", null);
  }

  public Map[] getUnsuccessfulTreatmentProcesses() {
    return (Map[]) failed.toArray(new Map[failed.size()]);
  }

  public Map[] getSuccessfulTreatmentProcesses() {
    return (Map[]) ok.toArray(new Map[ok.size()]);
  }

  public Long getUnsuccessfulTypeStatusId() {
    int status = getNotifierIntegerProperty("UNSUCCESSFUL_TYPE_STATUS", Integer.MIN_VALUE);
    if (status == Integer.MIN_VALUE)
      return null;
    return new Long(status);
  }

  public Long getSuccessfulTypeStatusId() {
    int status = getNotifierIntegerProperty("SUCCESSFUL_TYPE_STATUS", Integer.MIN_VALUE);
    if (status == Integer.MIN_VALUE)
      return null;
    return new Long(status);
  }

  protected String getNotifierProperty(String propertySuffix, String defaultValue) {
    return props.getNotifierProperty(getNotifierCode(), propertySuffix, defaultValue);
  }

  protected int getNotifierIntegerProperty(String propertySuffix, int defaultValue) {
    return props.getNotifierIntegerProperty(getNotifierCode(), propertySuffix, defaultValue);
  }


}
