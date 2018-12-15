package com.profitera.services.business.admin;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.deployment.rmi.UserAndRoleServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.admin.BusinessUnitBusinessBean;
import com.profitera.descriptor.db.user.BusinessUnit;
import com.profitera.persistence.SessionManager;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.QueryManager;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.TopLinkQuery;

public class MapUserAndRoleService extends UserAndRoleService implements UserAndRoleServiceIntf {
  private static final String GET_BRANCH_COUNT = "getBranchCount";
  protected static final String UPDATE_BRANCH_AS_ENABLED = "updateBranchAsEnabled";
  protected static final String UPDATE_BRANCH = "updateBranch";
  private static final String INSERT_BRANCH = "insertBranch";
  protected static final String UPDATE_BRANCH_AS_DISABLED = "updateBranchAsDisabled";

  public TransferObject addBusinessUnit(final BusinessUnitBusinessBean bu) {
    final IReadWriteDataProvider p = getReadWriteProvider();
    final String branchId = (String) bu.get("BRANCH_ID");
    bu.put("BRANCH_CREATE_DATE", new Date());
    Long count;
    try {
      count = (Long) p.queryObject(GET_BRANCH_COUNT, branchId);
    } catch (SQLException e1) {
      return returnFailWithTrace("Select failed", GET_BRANCH_COUNT, "select", bu, e1);
    }
    if (count.longValue() > 0){
      IRunnableTransaction t = new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          p.update(UPDATE_BRANCH_AS_ENABLED, branchId, t);
          p.update(UPDATE_BRANCH, bu, t);
        }};
      try {
        p.execute(t);
      } catch (AbortTransactionException e) {
        return returnFailWithTrace("Update failed", UPDATE_BRANCH, "update", bu, e);
      } catch (SQLException e) {
        return returnFailWithTrace("Update failed", UPDATE_BRANCH, "update", bu, e);
      }
    } else {
      IRunnableTransaction t = getInsertTransaction(bu, p);
      try {
        p.execute(t);
      } catch (AbortTransactionException e) {
        return returnFailWithTrace("Insert failed", INSERT_BRANCH, "insert", bu, e);
      } catch (SQLException e) {
        return returnFailWithTrace("Insert failed", INSERT_BRANCH, "insert", bu, e);
      }
    }
    BusinessUnit u = (BusinessUnit) TopLinkQuery.getObject(BusinessUnit.class, new String[]{BusinessUnit.BRANCH_ID}, bu.get("BRANCH_ID"), SessionManager.getClientSession());
    QueryManager queryManager = (QueryManager) lookup.getLookupItem(LookupManager.SYSTEM, "QueryManager");
    queryManager.refreshObject(u);
    return new TransferObject(new Boolean(true));
  }
  
  private IRunnableTransaction getInsertTransaction(final Map emp, final IReadWriteDataProvider p) {
    return new IRunnableTransaction(){
      public void execute(ITransaction t) throws SQLException, AbortTransactionException {
        p.insert(INSERT_BRANCH, emp, t);
      }};
  }



  public TransferObject updateBusinessUnit(final BusinessUnitBusinessBean bu) {
    final IReadWriteDataProvider p = getReadWriteProvider();
    IRunnableTransaction t = new IRunnableTransaction(){
      public void execute(ITransaction t) throws SQLException, AbortTransactionException {
        p.update(UPDATE_BRANCH, bu, t);
      }};
    try {
      p.execute(t);
    } catch (AbortTransactionException e) {
      return returnFailWithTrace("Update failed", UPDATE_BRANCH, "update", bu, e);
    } catch (SQLException e) {
      return returnFailWithTrace("Update failed", UPDATE_BRANCH, "update", bu, e);
    }
    BusinessUnit u = (BusinessUnit) TopLinkQuery.getObject(BusinessUnit.class, new String[]{BusinessUnit.BRANCH_ID}, bu.get("BRANCH_ID"), SessionManager.getClientSession());
    QueryManager queryManager = (QueryManager) lookup.getLookupItem(LookupManager.SYSTEM, "QueryManager");
    queryManager.refreshObject(u);
    return new TransferObject(new Boolean(true));
  }

  


  public TransferObject deleteBusinessUnit(final String branchId) {
    final IReadWriteDataProvider p = getReadWriteProvider();
    Long count;
    try {
      count = (Long) p.queryObject(GET_BRANCH_COUNT, branchId);
    } catch (SQLException e1) {
      return returnFailWithTrace("Select failed", GET_BRANCH_COUNT, "select", branchId, e1);
    }
    if (count.longValue() > 0){
      IRunnableTransaction t = new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          p.update(UPDATE_BRANCH_AS_DISABLED, branchId, t);
        }};
      try {
        p.execute(t);
      } catch (AbortTransactionException e) {
        return returnFailWithTrace("Update failed", UPDATE_BRANCH_AS_DISABLED, "update", branchId, e);
      } catch (SQLException e) {
        return returnFailWithTrace("Update failed", UPDATE_BRANCH_AS_DISABLED, "update", branchId, e);
      }
      return new TransferObject(new Boolean(true));
    } else {
      return new TransferObject(TransferObject.ERROR, "NO_SUCH_BRANCH");
    }
  }
}
