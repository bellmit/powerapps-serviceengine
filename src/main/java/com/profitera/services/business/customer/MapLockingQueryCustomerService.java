package com.profitera.services.business.customer;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.ibatis.common.jdbc.exception.NestedSQLException;
import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.deployment.rmi.CustomerServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.customer.CustomerAccountTreeBusinessBean;
import com.profitera.descriptor.business.customer.CustomerAccountsBusinessBean;
import com.profitera.descriptor.business.meta.ICustomer;
import com.profitera.descriptor.business.meta.IUser;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.util.DateParser;

public class MapLockingQueryCustomerService extends QueryCustomerService implements CustomerServiceIntf {
	
	private static final String LOCK_CUSTOMER_QUERY = "lockCustomer";
  
  protected TransferObject getLockedTransferObject(String lockingUser, Date lock, IReadOnlyDataProvider p) {
    String by = " by " + lockingUser;
    return new TransferObject(TransferObject.ERROR, "The customer is in use" + by + " since " + DateParser.DATETIME.format(lock));
  }
  
  public TransferObject getCustomerTree(final String customerId) {
    IReadWriteDataProvider provider = getReadWriteProvider();
    String userId = getRequestingUser();
    Map cust = null;
    // This list should only be 1 item long:
    try {
      List customers = getCustomer(customerId, provider);
      if (customers == null){
        return new TransferObject(TransferObject.EXCEPTION, "GET_CUSTOMER_ERROR");
      } else if(customers.size() > 0)
        cust = (Map) customers.get(0);
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
    if (cust == null)
      return new TransferObject(null, TransferObject.ERROR, "No Customer found: " + customerId);
    String user = (String) cust.get(IUser.USER_ID);
    Date lock = (Date) cust.get(CUSTOMER_LOCKED_TIME);
    if (isLocked(user, userId, lock))
      return getLockedTransferObject(user, lock, provider);
    try {
      lockCustomer(userId, customerId);
    } catch (TransferObjectException e1) {
      return e1.getTransferObject();
    }
    final CustomerAccountTreeBusinessBean bean = new CustomerAccountTreeBusinessBean();
    bean.putAll(cust);
    // will hold all primary Accounts
    List accounts = Collections.EMPTY_LIST;
    try {
      accounts = getMappedCustomerAccounts(customerId);
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
    final Vector primaryAccounts = new Vector();
    for (int i = 0; i < accounts.size(); i++) {
        Object a = accounts.get(i);
        Map m = (Map) a;
        m.put(CustomerAccountTreeBusinessBean.PAYMENT_DUE_DATE, bean.getPaymentDueDate());
        m.put(CustomerAccountTreeBusinessBean.STATEMENT_DATE, bean.getStatementDate());
        Number parent = getParentAccountId(m);
        if (parent == null)
          primaryAccounts.add(buildCustomerAccountsBusinessBean(m, accounts));
    }
    bean.setPrimaryAccounts(primaryAccounts);
    return new TransferObject(bean);
  }

  
  protected CustomerAccountsBusinessBean buildCustomerAccountsBusinessBean(Map m, List accounts) {
    CustomerAccountsBusinessBean b = new CustomerAccountsBusinessBean();
    b.putAll(m);
    Number id = b.getAccountId();
    Vector children = new Vector();
    for (Iterator i = accounts.iterator(); i.hasNext();) {
        Map element = (Map) i.next();
        Number parentId = getParentAccountId(element);
        if (parentId != null && parentId.equals(id))
          children.add(buildCustomerAccountsBusinessBean(element, accounts));
      }
    b.setChildAccounts(children);
      return b;
    }

	
	protected void lockCustomer(String userId, String customerId) throws TransferObjectException{
    final IReadWriteDataProvider provider = getReadWriteProvider();
    final Map m = new HashMap();
    m.put(IUser.USER_ID, userId);
    m.put(ICustomer.CUSTOMER_ID, customerId);
		try {
			provider.execute(new IRunnableTransaction() {
				public void execute(ITransaction t) throws SQLException, AbortTransactionException {
					provider.update(LOCK_CUSTOMER_QUERY, m, t);
				}
			});
		} catch (AbortTransactionException e) {
      log.error("Customer lock failed", e);
      throw new TransferObjectException(new TransferObject(TransferObject.EXCEPTION, "CUSTOMER_LOCK_DATABASE_ERROR"));
		} catch (SQLException e) {
      log.error("Customer lock failed", e);
      throw new TransferObjectException(new TransferObject(TransferObject.EXCEPTION, "CUSTOMER_LOCK_DATABASE_ERROR"));
		}
  }

  public TransferObject completeCustomer(final String customerId) {
    IReadWriteDataProvider provider = getReadWriteProvider();
    final String userId = getRequestingUser();
    try {
      // Here we try to unlock using the map-based parameter statement and if
      // that fails we fall back to using the single customer ID argument.
      try {
        provider.execute(new IRunnableTransaction(){
          public void execute(ITransaction t) throws SQLException, AbortTransactionException {
            Map m = new HashMap();
            m.put(ICustomer.CUSTOMER_ID, customerId);
            m.put(IUser.USER_ID, userId);
            getReadWriteProvider().update("unlockCustomer", m, t);
          }});
      } catch (NestedSQLException e){
        // Issue a warning that we need an upgrade
        log.warn("Update statement 'unlockCustomer' should be upgraded to use map parameter");
        provider.execute(new IRunnableTransaction(){
          public void execute(ITransaction t) throws SQLException, AbortTransactionException {
            getReadWriteProvider().update("unlockCustomer", customerId, t);
          }});
      }
    } catch (AbortTransactionException e) {
      log.error("Customer unlock failed", e);
      return new TransferObject(TransferObject.EXCEPTION, "CUSTOMER_LOCK_DATABASE_ERROR");
    } catch (SQLException e) {
      log.error("Customer unlock failed", e);
      return new TransferObject(TransferObject.EXCEPTION, "CUSTOMER_LOCK_DATABASE_ERROR");
    }
    return new TransferObject();
  }
  
  
}