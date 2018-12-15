package com.profitera.services.business.treatment;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.services.business.query.AbstractListQueryProcessor;
import com.profitera.services.business.query.IQueryService;
import com.profitera.services.system.dataaccess.DefaultTreatmentProcessManager;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.MapListUtil;

public class CreateOpenTreatmentPlanQueryProcessor extends
    AbstractListQueryProcessor {
  
  private DefaultTreatmentProcessManager manager;

  public Map preprocessArguments(Map arguments, IQueryService qs) throws TransferObjectException {
    return arguments;
  }

  public List postProcessResults(Map arguments, final List result, IQueryService qs) throws TransferObjectException {
    boolean shouldCreate = shouldCreatePlan(result); 
    if (shouldCreate){
      final Long accountId = (Long) arguments.get("ACCOUNT_ID");
      final IReadWriteDataProvider p = getReadWriteProvider();
      try {
        p.execute(new IRunnableTransaction(){
          public void execute(ITransaction t) throws SQLException, AbortTransactionException {
            result.add(getManager().getTreatmentPlanForProcess(new HashMap(), accountId, new Date(), null, null, null, t, p));
          }});
      } catch (AbortTransactionException e) {
        getLog().error("Treatment plan creation aborted for " + accountId, e);
        throw new TransferObjectException(new TransferObject(TransferObject.EXCEPTION, "TREATMENT_PLAN_NOT_CREATED"));
      } catch (SQLException e) {
        getLog().error("Treatment plan creation failed for " + accountId, e);
        throw new TransferObjectException(new TransferObject(TransferObject.EXCEPTION, "TREATMENT_PLAN_NOT_CREATED"));
      }
    }
    return result;
  }

  protected boolean shouldCreatePlan(final List result) {
    int index = MapListUtil.firstIndexOf("TREATMENT_END_DATE", null, result);
    boolean shouldCreate = index < 0;
    return shouldCreate;
  }

  public Object[] getRequiredReturnColumns() {
    return new String[]{"TREATMENT_PLAN_ID", "TREATMENT_END_DATE"};
  }
  

  protected IReadWriteDataProvider getReadWriteProvider() {
    final IReadWriteDataProvider provider = (IReadWriteDataProvider) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "ReadWriteProvider");
    return provider;
  }

  protected DefaultTreatmentProcessManager getManager() {
    if (manager == null){
      manager = new DefaultTreatmentProcessManager();      
    }
    return manager;
  }
  

}
