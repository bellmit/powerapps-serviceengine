package com.profitera.services.business.batch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.profitera.deployment.rmi.BatchProcessServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.BusinessService;
import com.profitera.services.business.login.MapLoginService;
import com.profitera.services.business.login.ServerSession;
import com.profitera.util.reflect.Reflect;
import com.profitera.util.reflect.ReflectionException;

public class BatchProcessService extends BusinessService implements BatchProcessServiceIntf {
  private static final String IMPLEMENTATION = "implementation";
  private static final String MODULE_NAME = "batchprocessservice";
  public TransferObject executeBatch(String identifier, Date effectiveDate, Map<String, Object> arguments) {
    String implKey = MODULE_NAME + "." + identifier + "." + IMPLEMENTATION;
    String authKey = MODULE_NAME + "." + identifier + "." + "auth";
    Properties config = ServiceEngine.getConfig(true);
    String implName = config.getProperty(implKey);
    String auth = config.getProperty(authKey);
    Map<String, String> additionalProps = getBatchProps(identifier, config);
    for (Iterator<Map.Entry<String, String>> i = additionalProps.entrySet().iterator(); i.hasNext();) {
      Map.Entry<String, String> element = i.next();
      if (!arguments.containsKey(element.getKey())){
        arguments.put(element.getKey(), element.getValue());
      }
    }
    if (implName == null || implName.equals("")){
      log.error("Batch not defined: " + identifier);
      return new TransferObject(TransferObject.ERROR, "BATCH_NOT_DEFINED");
    }
    if (auth != null) {
      long authorizationId = Long.parseLong(auth);
      long session = ServerSession.THREAD_SESSION.get();
      MapLoginService ls = (MapLoginService) getLogin();
      if (!ls.isAuthorized(session, authorizationId)) {
        return new TransferObject(TransferObject.ERROR, "BATCH_NOT_AUTHORIZED");
      }
    }
    try {
      IBatchProcess b = (IBatchProcess) Reflect.invokeConstructor(implName, null, null);
      return b.invoke(identifier, effectiveDate, arguments);
    } catch (ClassCastException c){
      log.fatal("Wrong type of implementation for batch: " + identifier, c);
      return new TransferObject(TransferObject.EXCEPTION, "BATCH_IMPLEMENTATION_WRONG_TYPE");
    } catch (ReflectionException e) {
      log.fatal("Failed to create implementation for batch: " + identifier, e);
      return new TransferObject(TransferObject.EXCEPTION, "BATCH_IMPLEMENTATION_CONSTRUCTOR_FAILED");
    } catch (Throwable t){
      log.fatal("Unexpected error encountered for batch: " + identifier, t);
      return new TransferObject(TransferObject.EXCEPTION, "UNEXPECTED_ERROR");
    }
  }

  private Map<String, String> getBatchProps(String id, Properties config) {
    Map<String, String> m = new HashMap<String, String>();
    for (Iterator i = config.entrySet().iterator(); i.hasNext();) {
      Map.Entry element = (Map.Entry) i.next();
      String k = element.getKey().toString();
      if (k.startsWith(MODULE_NAME + "." + id + ".")){
        m.put(k.substring((MODULE_NAME + "." + id + ".").length()), element.getValue() + "");
      }
    }
    return m;
  }

  public TransferObject getConfiguredBatches() {
    Set<String> batches = new HashSet<String>();
    Properties config = ServiceEngine.getConfig(true);
    for(Iterator i = config.keySet().iterator(); i.hasNext();){
      String key = (String) i.next();
      if (key.startsWith(MODULE_NAME + ".") && key.endsWith("." + IMPLEMENTATION)){
        batches.add(key.substring(MODULE_NAME.length() + 1,key.length() - IMPLEMENTATION.length() - 1));
      }
    }
    return new TransferObject(new ArrayList<String>(batches));
  }

}
