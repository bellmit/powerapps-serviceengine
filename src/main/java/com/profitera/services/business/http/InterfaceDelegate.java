package com.profitera.services.business.http;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

import com.profitera.client.RemoteProgressThread;
import com.profitera.deployment.rmi.BatchProcessServiceIntf;
import com.profitera.deployment.rmi.CustomerServiceIntf;
import com.profitera.deployment.rmi.EmployeeServiceIntf;
import com.profitera.deployment.rmi.EvaluationTreeManagementServiceIntf;
import com.profitera.deployment.rmi.HolidayServiceIntf;
import com.profitera.deployment.rmi.ListQueryServiceIntf;
import com.profitera.deployment.rmi.TemplateServiceIntf;
import com.profitera.deployment.rmi.TreatmentWorkpadServiceIntf;
import com.profitera.deployment.rmi.UserAndRoleManagementServiceIntf;
import com.profitera.deployment.rmi.WorkListServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.system.lookup.LookupManager;

class InterfaceDelegate {
  private Map<String, Class<?>> permanentInterfaces = new HashMap<String, Class<?>>();
  {
    permanentInterfaces.put(RemoteProgressThread.TEMPLATE_SERVICE, TemplateServiceIntf.class);
    permanentInterfaces.put(RemoteProgressThread.TREATMENT_WORKPAD_SERVICE, TreatmentWorkpadServiceIntf.class);
    permanentInterfaces.put(RemoteProgressThread.CUSTOMER_SERVICE, CustomerServiceIntf.class);
    permanentInterfaces.put(RemoteProgressThread.EVALUATION_TREE_MANAGEMENT_SERVICE, EvaluationTreeManagementServiceIntf.class);
    permanentInterfaces.put(RemoteProgressThread.USER_AND_ROLE_MANAGEMENT_SERVICE, UserAndRoleManagementServiceIntf.class);
    permanentInterfaces.put(RemoteProgressThread.BATCH_PROCESS_SERVICE, BatchProcessServiceIntf.class);
    permanentInterfaces.put(RemoteProgressThread.HOLIDAY_SERVICE, HolidayServiceIntf.class);
    permanentInterfaces.put(RemoteProgressThread.EMPLOYEE_SERVICE, EmployeeServiceIntf.class);
    permanentInterfaces.put(RemoteProgressThread.WORKLIST_SERVICE, WorkListServiceIntf.class);
    permanentInterfaces.put(RemoteProgressThread.LIST_QUERY_SERVICE, ListQueryServiceIntf.class);
  }
  
  Class[] getInterface(String serviceName) {
    Class[] interfaces = null;
    Object lookupItem = serviceName == null ? null : LookupManager.getInstance().getLookupItem(LookupManager.BUSINESS, serviceName);
    if (lookupItem == null && !permanentInterfaces.containsKey(serviceName)){
      throw getNotFoundException(serviceName);
    } else {
      if (lookupItem != null) {
        interfaces = lookupItem.getClass().getInterfaces(); 
      } else {
        interfaces = new Class[]{permanentInterfaces.get(serviceName)};
      }
      if (interfaces.length == 0){
        throw getNotFoundException(serviceName);
      }
    }
    return interfaces;
  }
  
  public void sendInterface(String serviceName, OutputStream outputStream) throws IOException {
    if (outputStream == null){
      throw new IllegalArgumentException("A target output stream is required");
    }
    try {
      Class[] interfaces = getInterface(serviceName);
      ObjectOutputStream oos = new ObjectOutputStream(outputStream);
      oos.writeObject(new TransferObject(interfaces));
      oos.close();
    } finally {
      outputStream.close();
    }    
  }

  private MissingResourceException getNotFoundException(String serviceName) {
    return new MissingResourceException("No such service '" + serviceName + "'", getClass().getName(), serviceName);
  }
}
