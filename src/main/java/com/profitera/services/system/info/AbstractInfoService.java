package com.profitera.services.system.info;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.descriptor.business.meta.IUser;
import com.profitera.services.system.SystemService;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public abstract class AbstractInfoService extends SystemService {
  private static final String UPDATE_TIME = "UPDATE_TIME";
 
  
  protected abstract String getInformationHistoryStatementName();
  protected abstract String getInformationInsertStatementName();
  protected abstract String getInformationSelectionQueryName();
  protected abstract String getInformationKeyName();
  protected abstract String getInformationUpdateStatementName();
  
  /*
   * Accept a list which in each entry contains account/customer information
   */
  public IRunnableTransaction updateMultipleInformation(List list, String user, Date d, IReadWriteDataProvider p) throws SQLException {
    IRunnableTransaction[] t = new IRunnableTransaction[list.size()];
	  for (int i = 0; i < list.size(); i++) {
		  Map map = (Map) list.get(i);
		  Object id = map.get(getInformationKeyName());
		  t[i] = updateInformation(map, id, user, d, p);
	  }
	  return new RunnableTransactionSet(t);
  }

  public IRunnableTransaction updateInformation(final Map info, Object keyValue, String user, Date d, final IReadWriteDataProvider p) throws SQLException{
    Map existing = null;;
    existing = (Map) p.queryObject(getInformationSelectionQueryName(), keyValue);
    if (existing == null){
      injectAttributes(info, keyValue, user, d);
      return new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          p.insert(getInformationInsertStatementName(), info, t);
        }};
    } else {
      final Map currentValues = existing;
      final Map update = new HashMap(existing);
      for (Iterator i = info.entrySet().iterator(); i.hasNext();) {
        Map.Entry element = (Map.Entry) i.next();
        if (update.containsKey(element.getKey())){
          update.put(element.getKey(), element.getValue());
        }
      }
      injectAttributes(update, keyValue, user, d);
      return new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          String[] required = new String[]{UPDATE_TIME, IUser.USER_ID, getInformationKeyName()};
          for (int i = 0; i < required.length; i++) {
            if (currentValues.get(UPDATE_TIME) == null){
              throw new AbortTransactionException("Missing required audit log value " + required[i]);
            }  
          }
          p.insert(getInformationHistoryStatementName(), currentValues, t);
          p.update(getInformationUpdateStatementName(), update, t);
        }};
    }
  }

  private void injectAttributes(final Map info, Object keyValue, String user, Date d) {
    info.put(IUser.USER_ID, user);
    info.put(getInformationKeyName(), keyValue);
    info.put(UPDATE_TIME, d);
  }
}
