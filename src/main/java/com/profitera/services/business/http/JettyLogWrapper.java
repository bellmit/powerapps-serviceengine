/**
 * 
 */
package com.profitera.services.business.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.log.Logger;

public class JettyLogWrapper implements Logger {
  private final Log log;

  public JettyLogWrapper(){
    this.log = LogFactory.getLog(WebServerService.class);
  }
  public void debug(String arg0, Throwable arg1) {
    log.debug(arg0, arg1);
  }

  public void debug(String arg0, Object arg1, Object arg2) {
    log.debug(arg0 + " " + arg1 + " " + arg2);
    
  }

  public Logger getLogger(String arg0) {
    return this;
  }

  public void info(String arg0, Object arg1, Object arg2) {
    log.info(arg0 + " " + arg1 + " " + arg2);
  }

  public boolean isDebugEnabled() {
    return log.isDebugEnabled();
  }

  public void setDebugEnabled(boolean arg0) {
    // Ignore
  }

  public void warn(String arg0, Throwable arg1) {
    log.debug(arg0, arg1);
  }

  public void warn(String arg0, Object arg1, Object arg2) {
    log.info(arg0 + " " + arg1 + " " + arg2);
  }
}