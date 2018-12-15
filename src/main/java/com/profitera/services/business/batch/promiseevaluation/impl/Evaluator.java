package com.profitera.services.business.batch.promiseevaluation.impl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.descriptor.business.meta.ITreatmentProcess;
import com.profitera.descriptor.business.reference.TreatmentProcessTypeStatusRefBusinessBean;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.ITreatmentProcessService;
import com.profitera.services.system.dataaccess.TreatmentProcessUpdateException;
import com.profitera.util.DateParser;

public class Evaluator {
  public static final String INSTALLMENT_LIST = "INSTALLMENT_LIST";
  private static final Long SUCCESSFUL_PROCESS_STATUS_ID = new Long(TreatmentProcessTypeStatusRefBusinessBean.SUCCESSFUL_TREATMENT_PROCESS_STATUS.longValue());
  private static final Long UNSUCCESSFUL_PROCESS_STATUS_ID = new Long(TreatmentProcessTypeStatusRefBusinessBean.UNSUCCESSFUL_TREATMENT_PROCESS_STATUS.longValue());

  private final IPromiseGraceCalculator grace;
  private final IReadWriteDataProvider provider;
  private final ITreatmentProcessService treatmentProcessService;
  private final Log log;
  private final Date effectiveDate;
  private final String installmentQuery;
  private final String paymentAmountQuery;

  public Evaluator(IPromiseGraceCalculator grace, String installmentQuery, String paymentQuery, Date effectiveDate, IReadWriteDataProvider provider,
      ITreatmentProcessService treatmentProcessService, Log log) {
        this.grace = grace;
        this.effectiveDate = effectiveDate;
        this.provider = provider;
        this.treatmentProcessService = treatmentProcessService;
        this.log = log;
        this.installmentQuery = installmentQuery;
        this.paymentAmountQuery = paymentQuery;
  }
  
  private Log getLog() {
    return log;
  }

  public IRunnableTransaction evaluatePromise(Map promise) throws AbortTransactionException, SQLException, TreatmentProcessUpdateException {
    Long processId = (Long) promise.get(ITreatmentProcess.TREATMENT_PROCESS_ID);
    Long treatmentPlanId = (Long) promise.get(ITreatmentProcess.TREATMENT_PLAN_ID);
    Date startDate = (Date) promise.get(ITreatmentProcess.ACTUAL_START_DATE);
    Date expectedEndDate = (Date) promise.get(ITreatmentProcess.EXPECTED_END_DATE);
    getLog().debug(processId + ": Evaluating promise to pay, ending on "
        + expectedEndDate + " taken on " + startDate);
    if (startDate == null){
      getLog().error(processId + ": Promise to pay has no " + ITreatmentProcess.ACTUAL_START_DATE + " and can not be evaluated");
      return null;
    }
    // TODO: Allow config of whether payments from the start date made before
    // promise time are accepted
    startDate = DateParser.getStartOfDay(startDate);
    BigDecimal remainingPaymentAmount = getPaymentAmountForTreatmentPlanAccount(treatmentPlanId, startDate, getProvider());
    getLog().debug(processId + ": " + remainingPaymentAmount
        + " in payments made since " + startDate);
    Iterator i = getPaymentInstallments(processId, getProvider());
    List<PromiseInstallment> paymentInstallments = new ArrayList();
    while(i.hasNext()) {
      Map d = (Map) i.next();
      paymentInstallments.add(new PromiseInstallment(d));
    }
    Collections.sort(paymentInstallments);
    getLog().debug(processId + ": " + paymentInstallments.size()  + " installments are part of this payment plan");
    Date graceAdjustedDate = getGraceAdjustedDate(promise);
    List<PromiseInstallment> brokenInstallments = new Vector();
    List<PromiseInstallment> keptInstallments = new Vector();
    List<PromiseInstallment> notDueInstallments = new Vector();
    assetInstallments(graceAdjustedDate, processId, remainingPaymentAmount, paymentInstallments,
        brokenInstallments, keptInstallments, notDueInstallments, false);
    if (brokenInstallments.size() > 0) {
      // Now we've determined that the promise is broken, reevaluate is using minimums
      // instead of the set installment amounts, if we keep it then that's good
      brokenInstallments = new Vector();
      keptInstallments = new Vector();
      notDueInstallments = new Vector();
      assetInstallments(graceAdjustedDate, processId, remainingPaymentAmount, paymentInstallments,
          brokenInstallments, keptInstallments, notDueInstallments, true);
    }
    if (keptInstallments.size() == paymentInstallments.size()) {
      // Plan is complete, close it off and mark all the installments
      return setPromiseSuccessful(getEffectiveDate(), promise, keptInstallments, getProvider());
    } else if (brokenInstallments.size() == 0) {
      // Mark off all the kept installments as kept and do nothing
      return setInstallmentsKept(getEffectiveDate(), promise, keptInstallments, notDueInstallments, getProvider());
    } else {
      // Installment broken, close plan off as unsuccessful.
      return setPromiseBroken(getEffectiveDate(), promise, keptInstallments, brokenInstallments, getProvider());
    }
  }

  private void assetInstallments(Date graceAdjustedDate, Long processId,
      BigDecimal remainingPaymentAmount,
      List<PromiseInstallment> paymentInstallments,
      List<PromiseInstallment> brokenInstallments,
      List<PromiseInstallment> keptInstallments,
      List<PromiseInstallment> notDueInstallments, boolean isUsingMinimum) {
    for (Iterator<PromiseInstallment> iter = paymentInstallments.iterator(); iter.hasNext();) {
      PromiseInstallment installment = iter.next();
      BigDecimal installmentDueAmount = installment.getDueAmount();
      BigDecimal installmentMinAmount = installment.getMinimumAmount();
      Date installmentDueDate = installment.getDueDate();
      getLog().debug(processId + ": has installment due on " + installmentDueDate);
      getLog().debug(processId + ": installment amount is " + installmentDueAmount);
      if (isUsingMinimum) {
        if (installmentMinAmount != null) {
          getLog().debug(processId + ": assessing against minimum installment amount of " + installmentMinAmount 
              + " instead of original due amount of " + installmentDueAmount);
          installmentDueAmount = installmentMinAmount;
        } else {
          getLog().debug(processId + ": assessing against minimum installment but no minimum amount specified, assessing against due amount");
        }
      }
      if (graceAdjustedDate.after(installmentDueDate)) {
        if (remainingPaymentAmount.compareTo(installmentDueAmount) < 0) {
          getLog().debug(processId + ": " + installmentDueAmount + " due, only " + remainingPaymentAmount + " paid");
          installment.setInstallmentPaidAmount(remainingPaymentAmount);
          remainingPaymentAmount = BigDecimal.ZERO;
          brokenInstallments.add(installment);
          while (iter.hasNext())
            brokenInstallments.add(iter.next());
        } else {
          getLog().debug(processId + ": " + installmentDueAmount + " due fully paid");
          keptInstallments.add(installment);
          installment.setInstallmentPaidAmount(installmentDueAmount);
          remainingPaymentAmount = remainingPaymentAmount.subtract(installmentDueAmount);
        }
      } else {
        getLog().debug(processId + ": Installment not yet due");
        if (remainingPaymentAmount.compareTo(installmentDueAmount) < 0) {
          installment.setInstallmentPaidAmount(remainingPaymentAmount);
          remainingPaymentAmount = BigDecimal.ZERO;
          notDueInstallments.add(installment);
        } else {
          installment.setInstallmentPaidAmount(installmentDueAmount);
          remainingPaymentAmount = remainingPaymentAmount.subtract(installmentDueAmount);
          keptInstallments.add(installment);
        }
      }
    }
  }

  private Iterator getPaymentInstallments(Long processId, IReadWriteDataProvider p) throws SQLException {
    return p.query(IReadWriteDataProvider.LIST_RESULTS, getInstallmentQuery(), processId);
  }

  private String getInstallmentQuery() {
    return installmentQuery;
  }
  

  private String getPaymentAmountQuery() {
    return paymentAmountQuery;
  }

  private BigDecimal getPaymentAmountForTreatmentPlanAccount(Long plan, Date startDate, IReadWriteDataProvider p) throws SQLException {
    Map args = new HashMap();
    args.put(ITreatmentProcess.TREATMENT_PLAN_ID, plan);
    args.put(ITreatmentProcess.ACTUAL_START_DATE, startDate);
    Number val = (Number) p.queryObject(getPaymentAmountQuery(), args);
    if (val instanceof BigDecimal) {
      return (BigDecimal) val;
    } else {
      return val == null ? BigDecimal.ZERO : new BigDecimal(val.doubleValue());
    }
  }

  public IRunnableTransaction setPromiseSuccessful(Date effectiveDate, Map plan, List<PromiseInstallment> installments, IReadWriteDataProvider p) throws SQLException, TreatmentProcessUpdateException {
    plan.put(ITreatmentProcess.ACTUAL_END_DATE, effectiveDate);
    plan.put(ITreatmentProcess.PROCESS_STATUS_ID, SUCCESSFUL_PROCESS_STATUS_ID);
    plan.put(ITreatmentProcess.PROCESS_TYPE_STATUS_ID, getPromiseStatus(plan, SUCCESSFUL_PROCESS_STATUS_ID, p));
    List<PromiseInstallment> installmentData = new ArrayList();
    long sts = PromiseInstallment.COMPLETED_INST_STATUS;
    for (Iterator<PromiseInstallment> i = installments.iterator(); i.hasNext();) {
      PromiseInstallment installment = i.next();
      installment.updateStatus(effectiveDate, new Long(sts));
      installmentData.add(installment);
    }
    return updatePromise(plan, installmentData, effectiveDate);
  }
  
  private IRunnableTransaction updatePromise(Map process, List<PromiseInstallment> installments, Date date) throws TreatmentProcessUpdateException {
    List<Map> installmentData = new ArrayList();
    for (PromiseInstallment promiseInstallment : installments) {
      installmentData.add(promiseInstallment.getData());
    }
    process.put("INSTALLMENTS", installmentData);
    process.put(INSTALLMENT_LIST, installmentData);
    process.put("PROMISE_EVALUATION", Boolean.TRUE);
    return getTreatmentProcessService().updateTreatmentProcess(process, date, null);
  }

  public IRunnableTransaction setPromiseBroken(Date effectiveDate, Map plan, List<PromiseInstallment> keptInstallments, List<PromiseInstallment> brokenInstallments, IReadWriteDataProvider p) throws SQLException, TreatmentProcessUpdateException {
    plan.put(ITreatmentProcess.ACTUAL_END_DATE, effectiveDate);
    plan.put(ITreatmentProcess.PROCESS_STATUS_ID, UNSUCCESSFUL_PROCESS_STATUS_ID);
    plan.put(ITreatmentProcess.PROCESS_TYPE_STATUS_ID, getPromiseStatus(plan, UNSUCCESSFUL_PROCESS_STATUS_ID, p));
    List<PromiseInstallment> installments = new ArrayList();
    for (Iterator<PromiseInstallment> i = keptInstallments.iterator(); i.hasNext();) {
      PromiseInstallment installment = i.next();
      long sts = PromiseInstallment.COMPLETED_INST_STATUS;
      installment.updateStatus(effectiveDate, sts);
      installments.add(installment);
    }
    for (Iterator<PromiseInstallment> i = brokenInstallments.iterator(); i.hasNext();) {
      PromiseInstallment installment = i.next();
      long sts = PromiseInstallment.BROKEN_INST_STATUS;
      installment.updateStatus(effectiveDate, sts);
      installments.add(installment);
    }
    return updatePromise(plan, installments, effectiveDate);
  }
  
  public IRunnableTransaction setInstallmentsKept(Date date, Map plan, List<PromiseInstallment> keptInstallments, List<PromiseInstallment> pendingInstallments, IReadWriteDataProvider p) throws SQLException, TreatmentProcessUpdateException {
    boolean updated = false;
    List<PromiseInstallment> installments = new ArrayList();
    for (Iterator<PromiseInstallment> i = keptInstallments.iterator(); i.hasNext();) {
      PromiseInstallment installment = i.next();
      long sts = PromiseInstallment.COMPLETED_INST_STATUS;
      if (installment.updateStatus(date, sts)){
        updated = true;
      }
      installments.add(installment);
    }
    for (Iterator<PromiseInstallment> i = pendingInstallments.iterator(); i.hasNext();) {
      PromiseInstallment installment = i.next();
      long sts = PromiseInstallment.INPROGRESS_INST_STATUS;
      if(installment.updateStatus(date, sts)){
        updated = true;
      }
      installments.add(installment);
    }
    if (updated){
      return updatePromise(plan, installments, date);
    } else {
      return null;
    }
  }

  private Long getPromiseStatus(Map promiseProcess, Long status, IReadWriteDataProvider p) throws SQLException {
    Map m = new HashMap();
    m.put("TYPE_ID", promiseProcess.get(ITreatmentProcess.PROCESS_TYPE_ID));
    m.put("STATUS_ID", status);
    Iterator i = p.query(IReadWriteDataProvider.LIST_RESULTS, "getTypeStatusByTypeAndStatus", m);
    if (!i.hasNext()){
      getLog().fatal("No Process type status for successful promise to pay created");
      throw new IllegalStateException("No Process type status for successful promise to pay created");
    }
    Map typeStatus = (Map) i.next();
    return (Long) typeStatus.get("ID");
  }


  private Date getEffectiveDate() {
    return effectiveDate;
  }

  private Date getGraceAdjustedDate(Map promise) {
    return grace.getGraceDate(promise);
  }
  
  private ITreatmentProcessService getTreatmentProcessService() {
    return treatmentProcessService;
  }

  private IReadWriteDataProvider getProvider() {
    return provider;
  }
  
}
