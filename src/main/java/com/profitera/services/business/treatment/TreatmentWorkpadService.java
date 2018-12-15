package com.profitera.services.business.treatment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.deployment.rmi.ListQueryServiceIntf;
import com.profitera.deployment.rmi.TreatmentWorkpadServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.ITreatmentProcess;
import com.profitera.event.ProcessingException;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.business.login.MapLoginService;
import com.profitera.services.business.login.ServerSession;
import com.profitera.services.system.dataaccess.ICreateTreatmentProcessTransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.ITreatmentProcessService;
import com.profitera.services.system.dataaccess.TreatmentProcessCreationException;
import com.profitera.services.system.dataaccess.TreatmentProcessUpdateException;
import com.profitera.services.system.lookup.LookupManager;

public final class TreatmentWorkpadService extends ProviderDrivenService implements TreatmentWorkpadServiceIntf {

  public TransferObject updateBulkTreatmentProcesses(Map[] processes) {
    return updateTreatmentProcesses(processes);
  }

  public TransferObject updateTreatmentProcesses(Map[] processes) {
    final String userId = ((MapLoginService) getLogin()).getSessionUser(ServerSession.THREAD_SESSION.get());
    Date date = new Date();
    List treatmentProcessIds = new ArrayList();
    try {
      ITreatmentProcessService s = getTreatmentProcessService();
      List transactions = new ArrayList();

      for (int i = 0; i < processes.length; i++) {
        Map proc = processes[i];
        if (proc.get(ITreatmentProcess.TREATMENT_PROCESS_ID) == null) {
          Long accountId = (Long) proc.get("ACCOUNT_ID");
          Long typeId = (Long) proc.get(ITreatmentProcess.PROCESS_TYPE_ID);
          ICreateTreatmentProcessTransaction[] ts = s.createManualProcess(processes[i], accountId, date, typeId, userId);
          transactions.addAll(Arrays.asList(ts));
          if (proc.get("PRIMARY_TREATMENT_PROCESS_ID") != null) {
            Map map = processes[i];
            map.put(ITreatmentProcess.TREATMENT_PROCESS_ID, ts[0].getId());
            transactions.add(s.createSubProcess(map, accountId, date, typeId, userId));
          }
        } else {
          transactions.add(s.updateTreatmentProcess(processes[i], date, userId));
        }
      }
      IReadWriteDataProvider provider = getReadWriteProvider();
      try {
        provider.execute(new RunnableTransactionSet((IRunnableTransaction[]) transactions
            .toArray(new IRunnableTransaction[transactions.size()])));

        for (int i = 0; i < transactions.size(); i++) {
          Object trans = transactions.get(i);
          if (trans instanceof ICreateTreatmentProcessTransaction) {
            ICreateTreatmentProcessTransaction ts = (ICreateTreatmentProcessTransaction) transactions.get(i);
            Map map = new HashMap();
            map.put(ITreatmentProcess.TREATMENT_PROCESS_ID, ts.getId());
            treatmentProcessIds.add(map);
          }
        }
      } catch (AbortTransactionException e) {
        log.error("Failure updating treatment processes: " + e.getMessage(), e);
        return new TransferObject(TransferObject.EXCEPTION, "DATABASE_ERROR");
      } catch (SQLException e) {
        log.error("Failure updating treatment processes: " + e.getMessage(), e);
        return new TransferObject(TransferObject.EXCEPTION, "DATABASE_ERROR");
      } catch (ProcessingException e) {
        log.error("Failure updating treatment processes: " + e.getMessage(), e);
        return e.getErrorObject();
      }
      // execute additional processing logic required
      transactions.clear();
      for (int i = 0; i < processes.length; i++) {
        transactions.add(s.processPostUpdate((Long) processes[i].get(ITreatmentProcessService.ACCOUNT_ID), processes[i], date,
            userId));
      }
      try {
        provider.execute(new RunnableTransactionSet((IRunnableTransaction[]) transactions
            .toArray(new IRunnableTransaction[transactions.size()])));
      } catch (AbortTransactionException e) {
        log.error("Failure updating (post) treatment processes: " + e.getMessage(), e);
        return new TransferObject(TransferObject.EXCEPTION, "DATABASE_ERROR");
      } catch (SQLException e) {
        log.error("Failure updating (post) treatment processes: " + e.getMessage(), e);
        return new TransferObject(TransferObject.EXCEPTION, "DATABASE_ERROR");
      }
    } catch (TreatmentProcessUpdateException e) {
      // This represents some kind of validation exception
      log.error("Failure updating treatment processes", e);
      return new TransferObject(TransferObject.ERROR, e.getMessage());
    } catch (TreatmentProcessCreationException e) {
      // This represents some kind of validation exception
      log.error("Failure creating treatment processes", e);
      return new TransferObject(TransferObject.ERROR, e.getMessage());
    }
    return new TransferObject(treatmentProcessIds);
  }

  private ITreatmentProcessService getTreatmentProcessService() {
    ITreatmentProcessService processService = (ITreatmentProcessService) LookupManager.getInstance().getLookupItem(
        LookupManager.SYSTEM, "TreatmentProcessService");
    return processService;
  }

  private ListQueryServiceIntf getListQueryService() {
    ListQueryServiceIntf service = (ListQueryServiceIntf) LookupManager.getInstance().getLookupItem(LookupManager.BUSINESS,
        "ListQueryService");
    return service;
  }

  public TransferObject getTreatmentProcessForEditing(Long id, Long typeId) {
    Map m = new HashMap();
    m.put(ITreatmentProcess.TREATMENT_PROCESS_ID, id);
    m.put(ITreatmentProcess.PROCESS_TYPE_ID, typeId);
    TransferObject to = getListQueryService().getQueryList("getCompleteTreatmentProcess", m);
    return new TransferObject((Map) ((List) to.getBeanHolder()).get(0));
  }
}
