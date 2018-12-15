package com.profitera.services.business.customer;

import java.util.Date;
import java.util.List;

import com.profitera.deployment.rmi.CustomerServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.MapVerifyingMapCar;

public abstract class CustomerService extends ProviderDrivenService implements CustomerServiceIntf {
    // Amount of time a lock is held, converted from minutes to milliseconds
    public static final long lockTime = (ServiceEngine.getIntProp("CustomerLock", 30) + 1) * 60000;
    
    protected boolean isLocked(String user, String requestingUser, Date lock) {
      final long now = System.currentTimeMillis();
      if (user != null && lock != null && lock.getTime() + lockTime > now) { // lock is still in effect!
        if (!user.equals(requestingUser)) { // current user does not hold the lock
          return true;
        }
      }
      return false;
    }
    

    public final TransferObject getAccount(final Double accountId) {
      Long id = new Long(accountId.longValue());
      IReadOnlyDataProvider provider = getReadOnlyProvider();
      try {
        List list = executeListQuery(IReadOnlyDataProvider.LIST_RESULTS, "getAccount", id, new MapVerifyingMapCar(new String[]{}), provider);
        if (list.size() == 0) {
          return new TransferObject(null);
        }
        return new TransferObject(list.get(0));
      } catch (TransferObjectException e) {
        return e.getTransferObject();
      }
    }

    /**
     * @see com.profitera.deployment.rmi.CustomerServiceIntf#getCustomerDelinquencyProfile(java.lang.String)
     */
    public TransferObject getCustomerDelinquencyProfile(String customerId) {
      final IReadOnlyDataProvider provider = getReadOnlyProvider();
      return executeQuery("getCustomerDelinquencyProfile", customerId, new MapVerifyingMapCar(new String[]{"ACCOUNT_ID"}), provider);
    }
}
