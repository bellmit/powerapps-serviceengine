package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.deployment.rmi.ListQueryServiceIntf;
import com.profitera.descriptor.business.meta.ITreatmentProcess;
import com.profitera.services.system.lookup.LookupManager;

public class PaymentPlanTreatmentProcessManager extends
    DefaultTreatmentProcessManager {

  public Long createTreatmentProcess(Map plan, Map process, Long accountId, Date date, Long typeId, String user, ITransaction t, IReadWriteDataProvider p) throws AbortTransactionException, SQLException {
    if (typeId.intValue() != RPMDataManager.PAYMENT_PLAN_TREATMENT_PROCESS)
      throw new AbortTransactionException("Unable to handle processes that are not payment plans");
    Long typeStatus = (Long) p.queryObject("getPaymentPlanCancelledTypeStatus");
    ListQueryServiceIntf lqs = getListQueryService();
    for (Iterator i = p.query(IReadOnlyDataProvider.LIST_RESULTS, "getInProgressPaymentPlansForCancelling", plan.get(ITreatmentProcess.TREATMENT_PLAN_ID)); i.hasNext(); ){
      List l = (List) lqs.getQueryList("getCompleteTreatmentProcess", (Map) i.next()).getBeanHolder();
      Map ptp = (Map) l.get(0);
      ptp.put(ITreatmentProcess.PROCESS_STATUS_ID, ITreatmentProcess.CANCELLED_STATUS);
      ptp.put(ITreatmentProcess.PROCESS_TYPE_STATUS_ID, typeStatus);
      ptp.put(ITreatmentProcess.USER_ID, process.get(ITreatmentProcess.CREATED_USER));
      updateTreatmentProcess(accountId, ptp, date, user, t, p);
    }
    return super.createTreatmentProcess(plan, process, accountId, date, typeId,
        user, t, p);
  }

  private ListQueryServiceIntf getListQueryService() {
    ListQueryServiceIntf service = (ListQueryServiceIntf) LookupManager.getInstance()
        .getLookupItem(LookupManager.BUSINESS, "ListQueryService");
    return service;
  }

}
