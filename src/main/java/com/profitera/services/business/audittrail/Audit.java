/*
 * Created by IntelliJ IDEA.
 * User: wso
 * Date: Apr 26, 2004
 * Time: 11:15:40 AM
 */
package com.profitera.services.business.audittrail;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.profitera.deployment.rmi.EventServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.server.ServiceEngine;
import com.profitera.server.ServiceProxy;
import com.profitera.services.Service;
import com.profitera.services.business.login.LoginService;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.Strings;
import com.profitera.util.interceptor.Interceptor;

public class Audit implements Interceptor {
  private static final ThreadLocal<Record> AUDIT_LOCAL = new ThreadLocal<Record>();
  private final LookupManager lookup = LookupManager.getInstance();
  private LoginService login;
  private final String[] excluded_services;

  public Audit() {
    String s = ServiceEngine.getConfig(false).getProperty("EXCLUDED_SERVICES");
    if (s == null) {
      excluded_services = null;
    } else {
      excluded_services = s.split(",");
    }
  }

  private LoginService getLogin() {
    if (login == null) {
      login = (LoginService) lookup.getLookupItem(LookupManager.BUSINESS, "LoginService");
    }
    return login;
  }

  public void beforeInvoke(Object service, Method m, Object[] args, Map<String, Object> context) {
    final Long session = (Long) context.get("session");
    final String module;
    if (service instanceof ServiceProxy) {
      final Service real = ((ServiceProxy) service).getReal();
      final String cls = real.getClass().getName();
      module = cls.substring(cls.lastIndexOf('.') + 1);
    } else if (context.containsKey("MODULE")) {
      module = (String) context.get("MODULE");
    } else {
      module = null;
    }
    // don't audit excluded services
    if (module != null && excluded_services != null && Arrays.binarySearch(excluded_services, module) >= 0) {
      return;
    }
    // don't audit message queue checks because they are too frequent and not particularly important.
    if (module != null && module.equals("EventService") && args[0].equals("message.consume")) {
      return;
    }
    final String name = (String) getLogin().getSessionUser(session);
    if (name == null) {
      // We need to destroy the current ThreadLocal value, otherwise this
      // request is logged as a modified copy of the previous request.
      AUDIT_LOCAL.set(null);
      return; // don't do anything if userid cannot be found
    }
    long now = System.currentTimeMillis();
    Record record = new Record(name, module, (String) context.get("hostname"), new Timestamp(now), session);
    AUDIT_LOCAL.set(record);
    getLogin().updateLastSessionActive(session, new Date(now));
  }

  public void afterInvoke(Object service, Method m, Object[] args, Map<String, Object> context, Object result) {
    final Record r = AUDIT_LOCAL.get();
    if (r == null) {
      return; // don't audit if no record found
    }
    final String remarks;
    final int status;
    String methodName = m.getName();
    boolean isListQuery = isListQuery(m);
    if (result instanceof Throwable) { // check if the call threw exceptions
      status = TransferObject.EXCEPTION;
      remarks = ((Throwable) result).getMessage();
    } else if (result instanceof TransferObject && ((TransferObject) result).isFailed()) {
      status = TransferObject.ERROR;
      remarks = ((TransferObject) result).getMessage();
      if (service instanceof EventServiceIntf) {
        String eventName = (String) args[0];
        if (eventName != null) {
          methodName = eventName;
        }
      } else if (isListQuery) {
        String queryName = (String) args[0];
        if (queryName != null) {
          methodName = queryName;
        }
      }
    } else {
      status = TransferObject.SUCCESS;
      if (isListQuery) {
        String query = (String) args[0];
        Map arguments = (Map) args[1];
        methodName = query == null ? m.getName() : query;
        remarks = asRemarks(arguments);
      } else if (service instanceof EventServiceIntf) {
        String eventName = (String) args[0];
        Map arguments = (Map) args[1];
        methodName = eventName == null ? m.getName() : eventName;
        remarks = asRemarks(arguments);
      } else {
        remarks = null;
      }
    }
    getLogin().audit(r.name, r.module, methodName, r.hostname, remarks, status, r.startTime);
    if (r.session != null) {
      getLogin().updateLastSessionActive(r.session, new Date(System.currentTimeMillis()));
    }
  }

  private String asRemarks(Map<Object, Object> arguments) {
    if (arguments == null) {
      return null;
    }
    StringBuilder text = new StringBuilder("{");
    boolean isFirst = true;
    for (Map.Entry<Object, Object> e : arguments.entrySet()) {
      if (isFirst) {
        isFirst = false;
      } else {
        text.append(",");
      }
      text.append(e.getKey());
      text.append("=");
      if (e.getValue() instanceof Collection) {
        text.append("[..]");
      } else {
        text.append(e.getValue());
      }
    }
    text.append("}");
    if (text.length() <= 500) {
      return ensureLessThan500Bytes(text.toString());
    }
    return ensureLessThan500Bytes(text.substring(0, 500));
  }

  private String ensureLessThan500Bytes(String text) {
    return Strings.getTrimStringByBytes(text, 499, Charset.forName("UTF8"));
  }

  private boolean isListQuery(Method m) {
    if (m.getName().equals("getQueryList") && m.getParameterTypes().length == 2
        && m.getParameterTypes()[0].equals(String.class) && m.getParameterTypes()[1].equals(Map.class)) {
      return true;
    }
    return false;
  }

  private static class Record {
    private final String name;
    private final String module;
    private final String hostname;
    private final Timestamp startTime;
    private final Long session;

    public Record(String name, String module, String hostname, Timestamp startTime, Long session) {
      this.name = name;
      this.module = module;
      this.hostname = hostname;
      this.startTime = startTime;
      this.session = session;
    }
  }

  public static String getSessionUser() {
    Record r = AUDIT_LOCAL.get();
    return r == null ? null : r.name;
  }
}
