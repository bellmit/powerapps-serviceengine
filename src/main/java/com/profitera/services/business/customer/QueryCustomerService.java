package com.profitera.services.business.customer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import oracle.toplink.sessions.Session;

import com.profitera.deployment.rmi.CustomerServiceIntf;
import com.profitera.deployment.rmi.ListQueryServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.customer.CustomerAccountsBusinessBean;
import com.profitera.descriptor.business.meta.ICustomer;
import com.profitera.services.business.login.LoginService;
import com.profitera.services.business.login.ServerSession;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.services.system.lookup.ServiceLookup;

/**
 * @author jamison
 */
public abstract class QueryCustomerService extends CustomerService implements CustomerServiceIntf {

  static protected final String CUSTOMER_LOCKED_TIME = "CUSTOMER_LOCKED_TIME";
  private static final String ACCOUNT_PARENT_ID = "ACCOUNT_PARENT_ID";
  
  private static final String GET_TREE_ACCOUNTS_QUERY_NAME = "getCustomerAccounts";

  public List getMappedCustomerAccounts(String customerId) throws TransferObjectException {
    Map m = new HashMap();
    m.put(ICustomer.CUSTOMER_ID, customerId);
    TransferObject to = getListQueryService().getQueryList(GET_TREE_ACCOUNTS_QUERY_NAME, m);
    if (to.isFailed()){
      throw new TransferObjectException(to);
    }
    return (List) to.getBeanHolder();
  }
  
  private static final String GET_TREE_CUSTOMER_QUERY_NAME = "getCustomer";
  //private static final String[] GET_TREE_ACCOUNTS_REQUIRED = new String[]{"ACCOUNT_ID", ACCOUNT_PARENT_ID};
  //private static final String[] GET_TREE_CUSTOMER_REQUIRED = new String[]{ICustomer.CUSTOMER_ID, IUser.USER_ID, CUSTOMER_LOCKED_TIME};
  protected String getRequestingUser() {
    Long userSession = ServerSession.THREAD_SESSION.get();
    LoginService l = (LoginService) getLogin();
    String userId = (String) l.getSessionUser(userSession);
    return userId;
  }


  protected List getCustomer(final String customerId, IReadWriteDataProvider provider) throws TransferObjectException {
    Map m = new HashMap();
    m.put(ICustomer.CUSTOMER_ID, customerId);
    TransferObject queryList = getListQueryService().getQueryList(GET_TREE_CUSTOMER_QUERY_NAME, m);
    if (queryList.isFailed()){
      throw new TransferObjectException(queryList);
    }
    return (List) queryList.getBeanHolder();
  }
  protected Number getParentAccountId(Map m){
    return (Number) m.get(ACCOUNT_PARENT_ID);
  }

  protected CustomerAccountsBusinessBean buildCustomerAccountsBusinessBean(Map m, List accounts, Session session) {
	CustomerAccountsBusinessBean b = new CustomerAccountsBusinessBean();
	b.putAll(m);
	Number id = b.getAccountId();
  Vector children = new Vector();
	for (Iterator i = accounts.iterator(); i.hasNext();) {
      Map element = (Map) i.next();
      Number parentId = getParentAccountId(element);
      if (parentId != null && parentId.equals(id))
      	children.add(buildCustomerAccountsBusinessBean(element, accounts, session));
    }
	b.setChildAccounts(children);
    return b;
  }

  
  private ListQueryServiceIntf getListQueryService() {
    return (ListQueryServiceIntf) getLookup().getService("ListQueryService");
  }

  private ServiceLookup getLookup() {
    return LookupManager.getInstance().getLookup(LookupManager.BUSINESS);
  }
}
