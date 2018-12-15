package com.profitera.services.business.legal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.deployment.rmi.LegalCaseServiceIntf;
import com.profitera.deployment.rmi.TreatmentWorkpadServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.ITreatmentProcess;
import com.profitera.descriptor.business.meta.IUser;
import com.profitera.descriptor.business.reference.TreatmentProcessTypeStatusRefBusinessBean;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.system.dataaccess.ICreateTreatmentProcessTransaction;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.ITreatmentProcessService;
import com.profitera.services.system.dataaccess.RPMDataManager;
import com.profitera.services.system.dataaccess.TreatmentProcessCreationException;
import com.profitera.services.system.dataaccess.TreatmentProcessUpdateException;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.MapListUtil;
import com.profitera.util.Strings;

public class LegalCaseService extends ProviderDrivenService implements LegalCaseServiceIntf {

  private static final Long LEGAL_PROCESS_TYPE = new Long(RPMDataManager.LEGAL_ACTION_TREATMENT_PROCESS);

  protected static final String UPDATE_LEGAL_CASE = "updateLegalCase";

  protected static final String INSERT_LEGAL_CASE_HISTORY = "insertLegalCaseHistory";

  private static final String UPDATE_TIME = "UPDATE_TIME";

  protected static final String INSERT_LEGAL_CASE = "insertLegalCase";

  private static final Long IN_PROGRESS = new Long(15007);

  public TransferObject saveLegalCase(final Map bean, String userId) {

    final IReadWriteDataProvider p = getReadWriteProvider();
    try {
      addAuditInfo(bean, userId);
      final Date date = (Date) bean.get(UPDATE_TIME);
      IRunnableTransaction t = null;
      t = saveLegalCaseInternal(bean, userId, date, p);
      p.execute(t);
      return new TransferObject(bean);
    } catch (AbortTransactionException e) {
      return sqlFailure("update", UPDATE_LEGAL_CASE + "/" + INSERT_LEGAL_CASE, bean, e);
    } catch (SQLException e) {
      return sqlFailure("update", UPDATE_LEGAL_CASE + "/" + INSERT_LEGAL_CASE, bean, e);
    } catch (TreatmentProcessUpdateException e) {
      return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
    } catch (TreatmentProcessCreationException e) {
      return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
    }
  }

  private IRunnableTransaction saveLegalCaseInternal(final Map bean, final String userId, final Date date, final IReadWriteDataProvider p)
      throws SQLException, TreatmentProcessUpdateException, TreatmentProcessCreationException {
    IRunnableTransaction t;
    if (bean.get("ID") != null) {
      final Long caseId = (Long) bean.get("ID");
      List newRelatedProcesses = (List) bean.get("ACCOUNT_PROCESS_LIST");
      if (newRelatedProcesses == null) {
        newRelatedProcesses = new ArrayList();
      }
      List existingProcesses = new ArrayList();
      List missingRelatedProcesses = getRelatedProcesses(caseId, p);
      for (Iterator i = missingRelatedProcesses.iterator(); i.hasNext();) {
        Map process = (Map) i.next();
        int index = MapListUtil.firstIndexOf(ITreatmentProcess.TREATMENT_PROCESS_ID, process.get(ITreatmentProcess.TREATMENT_PROCESS_ID),
            newRelatedProcesses);
        if (index != -1) {
          i.remove();
          newRelatedProcesses.remove(index);
          existingProcesses.add(process);
        }
      }
      boolean updateCaseOnly = bean.get("__UPDATE_CASE_ONLY") == null ? false : ((Boolean) bean.get("__UPDATE_CASE_ONLY")).booleanValue();
      final IRunnableTransaction cancelTrans;
      final ICreateTreatmentProcessTransaction[] createTrans;
      if (updateCaseOnly) {
        cancelTrans = new IRunnableTransaction() {
          public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          }
        };
        createTrans = new ICreateTreatmentProcessTransaction[0];
      } else {
        cancelTrans = cancelProcesses(missingRelatedProcesses, date, userId, p);
        ITreatmentProcessService tps = getTreatmentProcessService();
        createTrans = createNewCaseActions(newRelatedProcesses, tps, date, userId);
      }

      IRunnableTransaction updateTrans = null;

      Long typeStatus = (Long) bean.get(ITreatmentProcess.PROCESS_TYPE_STATUS_ID);
      Long status = getStatus(typeStatus, p);
      if (status != null && !status.equals(IN_PROGRESS) && !updateCaseOnly) {
        updateTrans = updateprocessStatus(existingProcesses, date, userId, status, typeStatus);
      }
      final IRunnableTransaction updatetranFinal = updateTrans;

      t = new IRunnableTransaction() {
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          bean.put("USER_ID", userId);
          bean.put("UPDATE_TIME", date);
          p.insert(INSERT_LEGAL_CASE_HISTORY, caseId, t);
          p.update(UPDATE_LEGAL_CASE, bean, t);
          cancelTrans.execute(t);
          for (int i = 0; i < createTrans.length; i++) {
            createTrans[i].execute(t);
            createCaseRelationshipLink(p, caseId, getTreatmentProcessAccountId(createTrans[i], p), createTrans[i], t);
          }
          if (updatetranFinal != null) {
            updatetranFinal.execute(t);
          }
        }
      };
    } else {
      ITreatmentProcessService tps = getTreatmentProcessService();
      List newRelatedProcesses = (List) bean.get("ACCOUNT_PROCESS_LIST");
      if (newRelatedProcesses == null) {
        newRelatedProcesses = new ArrayList();
      }
      final ICreateTreatmentProcessTransaction[] createTrans = createNewCaseActions(newRelatedProcesses, tps, date, userId);
      t = new IRunnableTransaction() {
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          bean.put("USER_ID", userId);
          bean.put("UPDATE_TIME", date);
          Long caseId = (Long) p.insert(INSERT_LEGAL_CASE, bean, t);
          bean.put("ID", caseId);
          for (int i = 0; i < createTrans.length; i++) {
            createTrans[i].execute(t);
            createCaseRelationshipLink(p, caseId, getTreatmentProcessAccountId(createTrans[i], p), createTrans[i], t);
          }
        }
      };
    }
    return t;
  }

  private Long getStatus(Long typeStatus, IReadOnlyDataProvider p) throws SQLException {
    if (typeStatus == null) {
      return IN_PROGRESS;
    }
    return (Long) p.queryObject("getTreatmentProcessStatusForTypeStatus", typeStatus);
  }

  private ICreateTreatmentProcessTransaction[] createNewCaseActions(List newRelatedProcesses, ITreatmentProcessService tps, Date date, String user)
      throws TreatmentProcessCreationException {
    ICreateTreatmentProcessTransaction[] trans = new ICreateTreatmentProcessTransaction[newRelatedProcesses.size()];
    for (int i = 0; i < trans.length; i++) {
      Map proc = (Map) newRelatedProcesses.get(i);
      Long accountId = (Long) (proc).get("ACCOUNT_ID");
      proc.put(ITreatmentProcess.PROCESS_STATUS_ID, new Long(TreatmentProcessTypeStatusRefBusinessBean.IN_PROGRESS_TREATMENT_PROCESS_STATUS
          .longValue()));
      // HACK: Only taking the first, what would we do if there were more?
      trans[i] = tps.createManualProcess(proc, accountId, date, LEGAL_PROCESS_TYPE, user)[0];
    }
    return trans;
  }

  private ITreatmentProcessService getTreatmentProcessService() {
    ITreatmentProcessService processService = (ITreatmentProcessService) lookup.getLookupItem(LookupManager.SYSTEM, "TreatmentProcessService");
    return processService;
  }

  private IRunnableTransaction cancelProcesses(List missingRelatedProcesses, Date date, String user, final IReadWriteDataProvider p)
      throws TreatmentProcessUpdateException, SQLException {
    final Long status = new Long(TreatmentProcessTypeStatusRefBusinessBean.CANCEL_TREATMENT_PROCESS_STATUS.longValue());
    // Set to null so it will default to any status, I don't care.
    Map args = new HashMap(2);
    args.put(ITreatmentProcess.PROCESS_TYPE_ID, LEGAL_PROCESS_TYPE);
    args.put(ITreatmentProcess.PROCESS_STATUS_ID, status);
    final Long typeStatus = (Long) p.queryObject("getDefaultTypeStatusId", args);
    return updateprocessStatus(missingRelatedProcesses, date, user, status, typeStatus);
  }

  private IRunnableTransaction updateprocessStatus(List processes, Date date, String user, final Long status, final Long typeStatus)
      throws TreatmentProcessUpdateException {
    ITreatmentProcessService s = getTreatmentProcessService();
    List trans = new ArrayList();
    for (Iterator i = processes.iterator(); i.hasNext();) {
      Map process = (Map) i.next();
      process.put(ITreatmentProcess.PROCESS_STATUS_ID, status);
      process.put(ITreatmentProcess.PROCESS_TYPE_STATUS_ID, typeStatus);
      trans.add(s.updateTreatmentProcess(process, date, user));
    }
    IRunnableTransaction[] t = (IRunnableTransaction[]) trans.toArray(new IRunnableTransaction[0]);
    return new RunnableTransactionSet(t);
  }

  private List getRelatedProcesses(Long caseId, IReadWriteDataProvider p) throws SQLException {
    List l = new ArrayList();
    Iterator i = p.query(IReadOnlyDataProvider.LIST_RESULTS, "getLegalCaseTreatmentProcesses", caseId);
    for (; i.hasNext();) {
      Map proc = (Map) i.next();
      Long processId = (Long) proc.get(ITreatmentProcess.TREATMENT_PROCESS_ID);
      Long typeId = (Long) proc.get(ITreatmentProcess.PROCESS_TYPE_ID);
      if (typeId == null) {
        typeId = LEGAL_PROCESS_TYPE;
      }
      Map ep = (Map) getTreatmentWorkpadService().getTreatmentProcessForEditing(processId, typeId).getBeanHolder();
      ep.putAll(proc);
      l.add(ep);
    }
    return l;
  }

  private Long getTreatmentProcessAccountId(ICreateTreatmentProcessTransaction createTrans, IReadOnlyDataProvider p) throws SQLException {
    Long tpId = createTrans.getId();
    return (Long) p.queryObject("getTreatmentProcessAccountId", tpId);
  }

  private void addAuditInfo(final Map bean, String userId) {
    bean.put(IUser.USER_ID, userId);
    bean.put(UPDATE_TIME, new Date());
  }

  private TreatmentWorkpadServiceIntf getTreatmentWorkpadService() {
    return (TreatmentWorkpadServiceIntf) lookup.getLookupItem(LookupManager.BUSINESS, "TreatmentWorkpadService");
  }

  private void createCaseRelationshipLink(final IReadWriteDataProvider p, final Long caseId, final Long accountId,
      final ICreateTreatmentProcessTransaction createTrans, ITransaction t) throws SQLException {
    Long tpId = createTrans.getId();
    Map rel = new HashMap();
    rel.put("ACCOUNT_ID", accountId);
    rel.put(ITreatmentProcess.TREATMENT_PROCESS_ID, tpId);
    rel.put("LEGAL_CASE_ID", caseId);
    p.insert("insertLegalCaseTreatmentProcessRel", rel, t);
  }

  public TransferObject saveLegalCases(Map[] legalCase, String userId) {
    IRunnableTransaction[] trans = new IRunnableTransaction[legalCase.length];
    Date d = new Date();
    final IReadWriteDataProvider p = getReadWriteProvider();
    try {
      for (int i = 0; i < trans.length; i++) {
        trans[i] = saveLegalCaseInternal(legalCase[i], userId, d, p);
      }
      p.execute(new RunnableTransactionSet(trans));
      return new TransferObject();
    } catch (AbortTransactionException e) {
      return sqlFailure("update", UPDATE_LEGAL_CASE + "/" + INSERT_LEGAL_CASE, Strings.getListString(legalCase, ","), e);
    } catch (SQLException e) {
      return sqlFailure("update", UPDATE_LEGAL_CASE + "/" + INSERT_LEGAL_CASE, Strings.getListString(legalCase, ","), e);
    } catch (TreatmentProcessUpdateException e) {
      return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
    } catch (TreatmentProcessCreationException e) {
      return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
    }
  }
}
