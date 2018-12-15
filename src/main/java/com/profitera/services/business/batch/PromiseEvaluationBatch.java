package com.profitera.services.business.batch;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.ITreatmentProcess;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.batch.promiseevaluation.impl.Evaluator;
import com.profitera.services.business.batch.promiseevaluation.impl.IPromiseGraceCalculator;
import com.profitera.services.business.batch.promiseevaluation.impl.PromiseInstallment;
import com.profitera.services.business.batch.promiseevaluation.impl.PromiseLevelGraceCalculator;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.ITreatmentProcessService;
import com.profitera.services.system.dataaccess.TreatmentProcessUpdateException;
import com.profitera.services.system.lookup.LookupManager;

public class PromiseEvaluationBatch extends AbstractProviderBatch {
  private static final String COMMITSIZE = "commitsize";
  private static final String QUERY = "query";
  private static final String INSTALLMENT_QUERY = "installmentquery";
  private static final String PAYMENT_AMOUNT_QUERY = "paymentamountquery";
  private static final String GRACE = "grace";
  private static final String GRACE_FIELD = "gracefield";
  
  public PromiseEvaluationBatch(){
    addProperty(COMMITSIZE, Integer.class, "1", "Commit size", "Size of transactions for processing.");
    addProperty(GRACE, Integer.class, null, "Grace period in days", "The number of days grace to allow in an installment's fulfillment, larger numbers allow for more leniency.");
    addProperty(GRACE_FIELD, String.class, "GRACE_DAY_COUNT", "Grace period field in promise data in days", "The field in the promise data being evaluated that specifies a grace period to override the global grace period set by the " + GRACE + " property.");
    addProperty(QUERY, String.class, "getAllInProgressPaymentPlans", 
        "Promise retrieval query", 
        "This query retrieves all the promises to pay that should be evaluated by the batch process. " +
        "The query must return all of the base treatment process fields as its results will be passed " +
        "to the treatment process data manager to update it after processing is complete.");
  addProperty(INSTALLMENT_QUERY, String.class, "getPaymentPlanInstallments", 
      "Promise installment retrieval query", 
      "This query retrieves the installments for each payment plan, it is executed once for each row " +
          "returned from the query specified by the " + QUERY + " property. The query is passed only the " +
      		"treatment process ID as a argument.");
  addProperty(PAYMENT_AMOUNT_QUERY, String.class, "getTotalPaymentAmountSince", 
      "Payment amount retrieval query", 
      "This query retrieves the total payment amount for the account to consider in the evaluation of the " +
          "payment plan. The query is passed the " + ITreatmentProcess.TREATMENT_PLAN_ID + " and " + 
          ITreatmentProcess.ACTUAL_START_DATE + " as arguments to allow the query connect from the " +
          "account via the plan ID in the time window starting at the promise start date.");
  }

  protected String getBatchDocumentation() {
    return "Batch program to evaluate the status of payment plan (sometimes called promises to " +
    		"pay or PTPs) treatment processes. This process executes the query specified by " + QUERY +" "+
    		"and each row returned has injected into it the result of the query specified by " + INSTALLMENT_QUERY + " " +
    		"as a list into the field " + Evaluator.INSTALLMENT_LIST + ". When a promise is evaluated each installment " +
    		"have their " + PromiseInstallment.INSTALLMENT_STATUS_ID + " updated as appropriate. " +
    		"The configured treatment process manager is expected to properly update treatments configured in " +
    		"this manner for update.";
  }

  protected String getBatchSummary() {
    return "This batch program goes through a list of in progress payment plans in the system and " +
    		"evalutes to check whether they are successful or broken.";
  }

  public TransferObject invoke() {
    return evaluateAllPromises(getEffectiveDate());
  }

  private TransferObject evaluateAllPromises(Date evalDate) {
    final IPromiseGraceCalculator grace = getGracePeriodAdjuster(evalDate);
    int commitSize = getCommitSize();
    String query = (String) getPropertyValue(QUERY);
    final IReadWriteDataProvider provider = getReadWriteProvider();
    Iterator i = null;
    try {
      // List results to avoid locking problems.
      i = provider.query(IReadOnlyDataProvider.LIST_RESULTS, query, null);
    } catch (SQLException e) {
      getLog().error("Error occurred retrieving promises to evaluate: " + e.getMessage(), e);
      return new TransferObject(TransferObject.EXCEPTION, "DATABASE_ERROR");
    }
    Date eff = getEffectiveDate();
    Evaluator evaluator = getEvaluator(grace, eff, provider);
    List trans = new ArrayList();
    while (i.hasNext()) {
      Map promise = (Map) i.next();
      try {
        trans.add(evaluator.evaluatePromise(promise));
      } catch (AbortTransactionException e) {
        getLog().error("Error occurred evaluating promises: " + e.getMessage(), e);
      } catch (SQLException e) {
        getLog().error("Error occurred evaluating promises: " + e.getMessage(), e);
        e.printStackTrace();
      } catch (TreatmentProcessUpdateException e) {
        getLog().error("Error occurred evaluating promises: " + e.getMessage(), e);
      }
      if (trans.size() >= commitSize){
        commitTransactionList(provider, trans);
        trans.clear();
      }
    }
    if (trans.size() > 0){
      commitTransactionList(provider, trans);
    }
    
    return new TransferObject(Boolean.TRUE);
  }

  protected Evaluator getEvaluator(final IPromiseGraceCalculator grace, Date eff,
      final IReadWriteDataProvider provider) {
    String q = (String) getPropertyValue(INSTALLMENT_QUERY);
    String q2 = (String) getPropertyValue(PAYMENT_AMOUNT_QUERY);
    return new Evaluator(grace, q, q2, eff, provider, getTreatmentProcessService(), getLog());
  }

  private void commitTransactionList(final IReadWriteDataProvider provider, List trans) {
    try {
      provider.execute(new RunnableTransactionSet((IRunnableTransaction[]) trans.toArray(new IRunnableTransaction[0])));
    } catch (AbortTransactionException e) {
      getLog().error("Error occurred committing evaluated promises: " + e.getMessage(), e);
    } catch (SQLException e) {
      getLog().error("Error occurred committing evaluated promises: " + e.getMessage(), e);
    }
  }

  private int getCommitSize() {
    int cSize = ((Integer)getPropertyValue(COMMITSIZE)).intValue();
    getLog().info("Promise Evaluation commit size: " + cSize);
    return cSize;
  }

  private IPromiseGraceCalculator getGracePeriodAdjuster(Date evalDate) {
    int gracePeriod = 0;
    Integer grace = (Integer) getPropertyValue(GRACE);
    if (grace != null) {
      gracePeriod = grace.intValue();
    } else {
      gracePeriod = ServiceEngine.getIntProp("PromiseGracePeriod", 0);
    }
    getLog().info("Promise Evaluation grace period (in days): " + gracePeriod);
    String graceField = (String) getPropertyValue(GRACE_FIELD);
    return new PromiseLevelGraceCalculator(evalDate, gracePeriod, graceField, getLog());
  }

  private ITreatmentProcessService getTreatmentProcessService() {
    LookupManager l = LookupManager.getInstance();
    Object service = l
            .getLookupItem(LookupManager.SYSTEM, "TreatmentProcessService");
    ITreatmentProcessService processService = (ITreatmentProcessService) service;
    return processService;
  }
}
