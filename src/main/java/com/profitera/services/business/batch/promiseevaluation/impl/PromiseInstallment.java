package com.profitera.services.business.batch.promiseevaluation.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import com.profitera.services.system.dataaccess.RPMDataManager;

public class PromiseInstallment implements Comparable {
  public static final long INPROGRESS_INST_STATUS = 60601;
  public static final long COMPLETED_INST_STATUS = 60603;
  public static final long BROKEN_INST_STATUS = 60604;

  private static final String INSTALLMENT_DUE_DATE = "INSTALLMENT_DUE_DATE";
  private static final String INSTALLMENT_DUE_AMOUNT = "INSTALLMENT_DUE_AMOUNT";
  private static final String INSTALLMENT_MIN_AMOUNT = "INSTALLMENT_MIN_AMOUNT";
  private static final String INSTALLMENT_PAID_AMOUNT = "INSTALLMENT_PAID_AMOUNT";
  private static final String INSTALLMENT_STATUS_DATE = "INSTALLMENT_STATUS_DATE";
  public static final String INSTALLMENT_STATUS_ID = "INSTALLMENT_STATUS_ID";
  private boolean isDoubleMode;
  private BigDecimal dueAmount;
  private BigDecimal minAmount;
  private Date dueDate;
  private Map data;
  
  public PromiseInstallment(Map data) {
    this.data = data;
    Object due = data.get(INSTALLMENT_DUE_AMOUNT);
    isDoubleMode = due instanceof Double;
    dueAmount = getValue(data, INSTALLMENT_DUE_AMOUNT);
    minAmount = getValue(data, INSTALLMENT_MIN_AMOUNT);
    dueDate = (Date) data.get(INSTALLMENT_DUE_DATE);
  }
  private BigDecimal getValue(Map data, String key) {
    Object v = data.get(key);
    if (isDoubleMode && v != null) {
      try {
        Double d = (Double) v;
        BigDecimal bd = new BigDecimal(d.doubleValue());
        v = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
      } catch (ClassCastException e) {
        throw getWrongNumericType(key, v);
      }
    }
    try {
      return (BigDecimal) v;
    } catch (ClassCastException e) {
      throw getWrongNumericType(key, v);
    }
  }
  
  private IllegalArgumentException getWrongNumericType(String key, Object v) {
    return new IllegalArgumentException("Promise installment values must consistently be " 
        + Double.class.getName() + " or " + BigDecimal.class.getName() + ", " + key + " was inconsistent as " + v.getClass().getName());
  }
  
  public Map getData(){
    return data;
  }
  public int compareTo(Object o) {
    PromiseInstallment p = (PromiseInstallment) o;
    return this.dueDate.compareTo(p.dueDate);
  }
  public BigDecimal getDueAmount() {
    return dueAmount;
  }
  public Date getDueDate() {
    return dueDate;
  }
  
  public boolean updateStatus(Date effectiveDate, long sts) {
    long current = getInstallmentStatus(data);
    if (current == sts)
      return false;
    setInstallmentStatus(new Long(sts));
    // Does anything ever get changed TO in progress?
    if (sts != RPMDataManager.INPROGRESS_INST_STATUS.longValue()){
      setInstallmentStatusDate(effectiveDate);
    } else {
      // Don't bother with an dirty flag if all we do is update the
      // null in progress to the "real" in progress, that was the previous
      // behaviour and it prevents the audit trail from being cluttered with
      // PTP eval NOOPs
      if (current == -1) 
        return false;
    }
    return true;
  }

  private long getInstallmentStatus(Map installment) {
    Long v = (Long) installment.get(INSTALLMENT_STATUS_ID);
    return v == null ? -1 : v.longValue();
  }
  
  private void setInstallmentStatus(Long statusId) {
    data.put(INSTALLMENT_STATUS_ID, statusId);    
  }

  private void setInstallmentStatusDate(Date date) {
    data.put(INSTALLMENT_STATUS_DATE, date);
  }
  
  public void setInstallmentPaidAmount(BigDecimal amount) {
    Object o = amount;
    if (isDoubleMode) {
      o = new Double(amount.doubleValue());
    }
    data.put(INSTALLMENT_PAID_AMOUNT, o);
  }
  
  public BigDecimal getMinimumAmount() {
    return minAmount;
  }
}
