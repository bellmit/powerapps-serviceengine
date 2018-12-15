package com.profitera.services.business.http;

import java.io.IOException;
import java.util.MissingResourceException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InterfaceServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private InterfaceDelegate delegate = new InterfaceDelegate();
  private static Log LOG;
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String pathInfo = req.getPathInfo();
    if (pathInfo.startsWith("/")){
      pathInfo = pathInfo.substring(1);
    }
    try {
      delegate.sendInterface(pathInfo, resp.getOutputStream());
      resp.setStatus(HttpServletResponse.SC_OK);
    } catch (MissingResourceException e){
      getLog().fatal("Failure sending interface for " + pathInfo, e);
      resp.sendError(HttpServletResponse.SC_NOT_FOUND, "");
    } catch (Exception e){
      getLog().fatal("Failure sending interface for " + pathInfo, e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "");
    }
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
