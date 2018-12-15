package com.profitera.services.business.customer;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.IUser;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public abstract class AbstractInformationService extends ProviderDrivenService {
  private static final String UPDATE_TIME = "UPDATE_TIME";
  
  protected abstract String getInformationHistoryStatementName();
  protected abstract String getInformationInsertStatementName();
  protected abstract String getInformationSelectionQueryName();
  protected abstract String getInformationKeyName();
  protected abstract String getInformationUpdateStatementName();
  
  /*
   * Accept a list which in each entry contains account/customer information
   */
  public TransferObject updateMultipleInformation(List list, String user) {
	  for (int i = 0; i < list.size(); i++) {
		  Map map = (Map) list.get(i);
		  Object id = map.get(getInformationKeyName());
		  updateInformation(map, id, user);
	  }
	  return new TransferObject(list);
  }

  public TransferObject updateInformation(final Map info, Object keyValue, String user){
    if(info==null) {
    	log.error("NO_UPDATE_INFO_PROVIDED");
    	return new TransferObject(TransferObject.ERROR, "NO_UPDATE_INFO_PROVIDED");
    }
	  final IReadWriteDataProvider p = getReadWriteProvider();
    Map existing = null;;
    try {
      existing = (Map) p.queryObject(getInformationSelectionQueryName(), keyValue);
    } catch (SQLException e1) {
      return sqlFailure("OBJECT", getInformationSelectionQueryName(), info, e1);
    }
    if (existing == null){
      injectAttributes(info, keyValue, user);
      try {
        p.execute(new IRunnableTransaction(){
          public void execute(ITransaction t) throws SQLException, AbortTransactionException {
            p.insert(getInformationInsertStatementName(), info, t);
          }});
      } catch (AbortTransactionException e) {
        return sqlFailure("insert", getInformationInsertStatementName(), info, e);
      } catch (SQLException e) {
        return sqlFailure("insert", getInformationInsertStatementName(), info, e);
      }
      
    } else {
      final Map currentValues = existing;
      final Map update = new HashMap(existing);
      for (Iterator i = info.entrySet().iterator(); i.hasNext();) {
        Map.Entry element = (Map.Entry) i.next();
        if (update.containsKey(element.getKey())){
          update.put(element.getKey(), element.getValue());
        }
      }
      injectAttributes(update, keyValue, user);
      try {
        p.execute(new IRunnableTransaction(){
          public void execute(ITransaction t) throws SQLException, AbortTransactionException {
            String[] required = new String[]{UPDATE_TIME, IUser.USER_ID, getInformationKeyName()};
            for (int i = 0; i < required.length; i++) {
              if (currentValues.get(UPDATE_TIME) == null){
                throw new AbortTransactionException("Missing required audit log value " + required[i]);
              }  
            }
            p.insert(getInformationHistoryStatementName(), currentValues, t);
            p.update(getInformationUpdateStatementName(), update, t);
          }});
      } catch (AbortTransactionException e) {
        return sqlFailure("insert/update", getInformationHistoryStatementName() + "/" + getInformationUpdateStatementName(), update, e);
      } catch (SQLException e) {
        return sqlFailure("insert/update", getInformationHistoryStatementName() + "/" + getInformationUpdateStatementName(), info, e);
      }
    }
    return new TransferObject();
  }

  private void injectAttributes(final Map info, Object keyValue, String user) {
    info.put(IUser.USER_ID, user);
    info.put(getInformationKeyName(), keyValue);
    info.put(UPDATE_TIME, new Date());
  }
}
