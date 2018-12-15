/*
 * Created on May 12, 2005
 */
package com.profitera.descriptor.db.history;

import java.io.Serializable;

/**
 * @author jambu
 */

public class TreatmentProcessHistoryRemark implements Serializable {
  
  public static final String ID = "id";
  public static final String PROCESS_REMARKS = "processRemarks";
  
  private Double id;
  private String processRemarks;

  public Double getId() {
    return id;
  }
  public void setId(Double id) {
    this.id = id;
  }
  public String getProcessRemarks() {
    return processRemarks;
  }
  public void setProcessRemarks(String processRemarks) {
    this.processRemarks = processRemarks;
  }
}