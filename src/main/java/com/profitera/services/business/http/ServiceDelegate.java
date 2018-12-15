package com.profitera.services.business.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.MissingResourceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.client.http.ServiceRequest;
import com.profitera.deployment.rmi.LoginServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.login.UserBean;
import com.profitera.server.LicenseException;
import com.profitera.services.business.login.ServerSession;
import com.profitera.services.business.login.SessionRequiredException;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.interceptor.Interceptor;
import com.profitera.util.reflect.AccessDeniedException;
import com.profitera.util.reflect.InvocationTargetException;
import com.profitera.util.reflect.MethodNotFoundException;
import com.profitera.util.reflect.NoSuchClassException;
import com.profitera.util.reflect.ParameterMatchException;
import com.profitera.util.reflect.Reflect;
import com.profitera.util.reflect.ReflectionException;

class ServiceDelegate {
  private static final String LOGIN_SERVICE = "LoginService";
  private static Log log;
  private final Interceptor[] interceptors;
  public ServiceDelegate(Interceptor[] interceptorArray) {
    this.interceptors = interceptorArray == null ? new Interceptor[0] : interceptorArray;
  }
  public void serviceRequest(final String serviceName, InputStream is, OutputStream os, String[] sources)
    throws IOException, ClassNotFoundException {
    if (serviceName == null) {
      throw new MissingResourceException("No service name provided", this.getClass().getName(), "null");
    }
    if (is == null || os == null) {
      throw new IllegalArgumentException("Input and output streams are both required to service requests");
    }
    ServiceRequest request = new ServiceRequest(getRequestDetails(is));
    request.getContext().put("MODULE", serviceName);
    request.getContext().put("sourceiplist", sources);
    Object returnValue = null;
    Object lookupItem = findLookupItem(serviceName);
    Method method = Reflect.getMethodMatching(lookupItem.getClass(), request.getMethod());
    if (method == null) {
      throw new MissingResourceException("No method '" + request.getMethod() + "' for service " + serviceName, this.getClass().getName(), serviceName);
    }
    try {
      boolean isLoginService = LOGIN_SERVICE.equals(serviceName);
      boolean isLogin = isLoginService && (("login".equals(request.getMethod())) || ("loginViaSingleSignOnTicket".equals(request.getMethod())));
      boolean isLogoff = isLoginService && "logoff".equals(request.getMethod());
      try {
        for (int i = 0; i < interceptors.length; i++) {
          interceptors[i].beforeInvoke(lookupItem, method, request.getArguments(), request.getContext());
        }
        if (isLogin) {
          returnValue = invokeService(lookupItem, serviceName, method, request.getTypes(), request.getArguments());
          
          // If a login is successful ask the login event
          if (returnValue instanceof TransferObject && !((TransferObject) returnValue).isFailed()) {
            
            TransferObject transfer = (TransferObject) returnValue;
  
            System.out.println(transfer.getFlag());
            System.out.println(transfer.getMessage());
            System.out.println(transfer.getBeanHolder().toString());
            
            returnValue = executeLoginEvent(serviceName, request, lookupItem, returnValue);
          }
        } else if (isLogoff){
          executeLogoffEvent(serviceName, request, lookupItem);
          returnValue = invokeService(lookupItem, serviceName, method, request.getTypes(), request.getArguments());
        } else {
          returnValue = handleMessage(lookupItem, serviceName, request.getMethod(), request.getTypes(), request.getArguments(), request.getContext());
          if (returnValue == null) {
            returnValue = invokeService(lookupItem, serviceName, method, request.getTypes(), request.getArguments());
          }
        }
      } catch (SessionRequiredException e) {
        if (method != null && method.getReturnType().equals(TransferObject.class)) {
          returnValue = new TransferObject(e.getMessage(), TransferObject.ERROR, e.getMessage());
        } else {
          throw e;
        }
      } catch (LicenseException e) {
        if (method != null && method.getReturnType().equals(TransferObject.class)) {
          returnValue = new TransferObject(e.getData(), TransferObject.ERROR, e.getCode());
        } else {
          throw e;
        }
      } finally {
        for (int i = 0; i < interceptors.length; i++) {
          interceptors[i].afterInvoke(lookupItem, method, request.getArguments(), request.getContext(), returnValue);
        }
      }
      ObjectOutputStream oos = new ObjectOutputStream(os);
      oos.writeObject(returnValue);
      oos.close();
    } catch (ReflectionException e1) {
      throw new MissingResourceException(e1.getMessage(), this.getClass().getName(), serviceName);
    }
  }
  private Object findLookupItem(final String serviceName) {
    Object lookupItem = LookupManager.getInstance().getLookupItem(LookupManager.BUSINESS, serviceName);
    if (lookupItem == null) {
      // Pull up an interface and see if we can work something out:
      Class<?>[] interfaces = new InterfaceDelegate().getInterface(serviceName);
      lookupItem = Proxy.newProxyInstance(getClass().getClassLoader(), interfaces, new InvocationHandler() {
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
          throw new IllegalArgumentException("No such service: " + serviceName);
        }
      });
    }
    return lookupItem;
  }
  private Object executeLoginEvent(final String serviceName, ServiceRequest request, Object lookupItem,
      Object result) {
    UserBean user = (UserBean) ((TransferObject) result).getBeanHolder();
    request.getContext().put("session", user.getSession());
    Object postLoginResult = handleMessage(lookupItem, serviceName, request.getMethod(), request.getTypes(), request.getArguments(), request.getContext());
    Object returnValue = result;
    if (postLoginResult instanceof TransferObject && ((TransferObject) postLoginResult).isFailed()) {
      try {
        // If the post-login event returns an error you are logged off, if that fails we
        // ignore it
        ServerSession.THREAD_SESSION.set(user.getSession());
        getLoginService().logoff(user.getUserId(), user.getSession());
      } catch (Exception t) { //Print a stacktrace for lack of a better idea
        getLog().warn("Error logging out user after event-based login rejection", t);
      }
      returnValue = postLoginResult;
    }
    return returnValue;
  }
  private void executeLogoffEvent(final String serviceName, ServiceRequest request, Object lookupItem) {
    //UserBean user = (UserBean) ((TransferObject) returnValue).getBeanHolder();
    //request.getContext().put("session", user.getSession());
    handleMessage(lookupItem, serviceName, request.getMethod(), request.getTypes(), request.getArguments(), request.getContext());
  }

  private LoginServiceIntf getLoginService() {
    LookupManager m = LookupManager.getInstance();
    return (LoginServiceIntf) m.getLookupItem(LookupManager.BUSINESS, LOGIN_SERVICE);
  }

  private Object handleMessage(Object lookupItem, String serviceName,
      String methodName, Class<?>[] paramTypes, Object[] args, Map<String, Object> context) {
    IMessageHandler h = getMessageHandler();
    return h.handleMessage(lookupItem, serviceName, methodName, paramTypes, args, context);
  }

  private IMessageHandler getMessageHandler() {
    LookupManager m = LookupManager.getInstance();
    return (IMessageHandler) m.getLookupItem(LookupManager.SYSTEM, "MessageHandler");
  }
  private Object invokeService(Object lookupItem, String serviceName, Method method, Class<?>[] paramTypes,
      Object[] args) throws MethodNotFoundException, ParameterMatchException,
      AccessDeniedException, NoSuchClassException {
		Object returnValue = null;
		try {
			returnValue = Reflect.invokeMethod(method.getName(), paramTypes, lookupItem, args);
		} catch (InvocationTargetException e1) {
			if (method.getReturnType().equals(TransferObject.class)) {
				getLog().error(e1.getMessage(), e1);
				returnValue = new TransferObject(new Object[] {serviceName, method.getName(), e1.getMessage()},
				    TransferObject.EXCEPTION, "UNEXPECTED_EXCEPTION");
			} else {
				throw new RuntimeException(e1);
			}
		}
		return returnValue;
	}

  private TransferObject getRequestDetails(InputStream is) throws IOException, ClassNotFoundException {
    ObjectInputStream ois = null;
    try {
      ois = new ObjectInputStream(is);
      return (TransferObject) ois.readObject();
    } catch (ClassCastException e) {
      throw new IOException("Expected input parameter of type " + TransferObject.class.getName());
    } finally {
      if (ois != null) {
        ois.close();
      }
    }
  }
  private Log getLog() {
    if (log == null) {
      log = LogFactory.getLog(getClass());
    }
    return log;
  }
  public void init() {
    getMessageHandler();
  }
}
