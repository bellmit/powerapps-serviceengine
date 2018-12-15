/*
 * Created by IntelliJ IDEA.
 * User: wso
 * Date: Mar 12, 2004
 * Time: 11:33:30 AM
 */
package com.profitera.server;

import com.profitera.services.Service;
import com.profitera.util.interceptor.Interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

/**
 * This is the Generic Proxy for Service that is invoked by the stub {@link ServiceProxy_Stub} that is bound to the RMI Registry
 * This class is public because RMI needs it to be
 */
public final class ServiceProxy extends UnicastRemoteObject implements RemoteService {
    private static final long serialVersionUID = -1831890218371078773L;
    private final transient Service real; // The service for which we are proxying
    private final transient Interceptor[] si; // Any interceptor
    /**
     * The methods of the service that need to be invoked, arranged in accordance with
     * {@link ProxyUtil#indexMethods(java.lang.reflect.Method[])}
     */
    private final transient Method[] methods;

    /**
     * Used when we don't want or need any Interceptors for this service
     * @param service what you want to proxy
     * @throws RemoteException needed by RMI runtime
     */
    ServiceProxy(final Service service) throws RemoteException {
        this(service, null);
    }

    /**
     * The constructor is not public to allow only ServiceEngine to instantiate this
     */
    ServiceProxy(final Service service, final Interceptor[] interceptors) throws RemoteException {
        real = service;
        si = interceptors;
        methods = ProxyUtil.getAllMethods(real.getClass().getInterfaces());
    }

    /**
     * @see RemoteService#invoke(int, java.util.Map)
     */
    public Object invoke(final int ind, Map context) throws RemoteException {
        return invoke(ind, null, context);
    }

    /**
     * @see RemoteService#invoke(int, java.lang.Object[], java.util.Map)
     */
    public Object invoke(final int ind, final Object[] args, Map context) throws RemoteException {
        final Method method = methods[ind];
        Object result;
        callBefore(this, method, args, context);
        try {
            result = method.invoke(real, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            cause.printStackTrace();
            callAfter(this, method, args, context, cause);
            throw new RemoteException(cause.getMessage());
        } catch (IllegalAccessException e) {
            callAfter(this, method, args, context, e);
            e.printStackTrace();
            throw new RuntimeException("Illegal Access Exception: " + e.getMessage());
        }
        callAfter(this, method, args, context, result);
        return result;
    }

    /**
     * Template method that steps through interceptors in increasing sequential order
     * @param service
     * @param m       needed so Interceptor knows what method it's working on
     * @param args
     */
    private void callBefore(final ServiceProxy service, final Method m, final Object[] args, Map context) {
        if (null == si) return;
        for (int i = 0; i < si.length; i++) {
            si[i].beforeInvoke(service, m, args, context);
        }
    }

    private void callAfter(final ServiceProxy service, final Method m, final Object[] args, Map context,  Object result) {
        if (null == si) return;
        for (int i = si.length - 1; 0 <= i; i--) {
            si[i].afterInvoke(service, m, args, context, result);
        }
    }

    public Service getReal() {
        return real;
    }
}