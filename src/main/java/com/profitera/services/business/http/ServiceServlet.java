package com.profitera.services.business.http;

import java.io.IOException;
import java.util.MissingResourceException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.server.ServiceEngine;

public class ServiceServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static Log LOG;
  private ServiceDelegate delegate;
  
  public ServiceServlet() {
    // Need to build the delegate so that the msg handler will be registered
    getDelegate();
  }
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String ipAddress = req.getHeader("x-forwarded-for");
    if (ipAddress == null) {
      ipAddress = req.getHeader("X_FORWARDED_FOR");
      if (ipAddress == null) {
        ipAddress = req.getRemoteAddr();
      }
    }
    String[] sources = new String[] {ipAddress};
    if (ipAddress.contains(",")) {
      sources = ipAddress.split("[,]");
    }
    String serviceName = getServiceName(req);
    try {
      getDelegate().serviceRequest(serviceName, req.getInputStream(), resp.getOutputStream(), sources);
      resp.setStatus(HttpServletResponse.SC_OK);
    } catch (MissingResourceException e){
      getLog().fatal(e.getMessage(), e);
      resp.sendError(HttpServletResponse.SC_NOT_FOUND, "");
      resp.getOutputStream().close();
    } catch (ClassNotFoundException e) {
      getLog().fatal(e.getMessage(), e);
      resp.sendError(HttpServletResponse.SC_NOT_FOUND, "");
      resp.getOutputStream().close();
    } catch (Exception e) {
      getLog().fatal(e.getMessage(), e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "");
      resp.getOutputStream().close();
    }
  }
  
  private String getServiceName(HttpServletRequest req) {
    String serviceName = req.getPathInfo();
    if (serviceName.startsWith("/")){
      serviceName = serviceName.substring(1);
    }
    return serviceName;
  }
  
  private ServiceDelegate getDelegate() {
    if (delegate == null) {
      delegate = new ServiceDelegate(ServiceEngine.getServiceInterceptors());
      delegate.init();
    }
    return delegate;
  }
  
  private Log getLog(){
    if (LOG == null) {
      LOG = LogFactory.getLog(getClass());
    }
    return LOG;
  }
  @Override
  protected void doTrace(HttpServletRequest req, HttpServletResponse resp) 
  throws ServletException, IOException {
    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
    resp.getOutputStream().close();
  }
}
