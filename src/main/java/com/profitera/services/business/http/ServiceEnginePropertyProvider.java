/**
 * 
 */
package com.profitera.services.business.http;

import com.profitera.server.ServiceEngine;

final class ServiceEnginePropertyProvider implements
    ThreadPoolPropertyMonitor.IPropertyProvider {
  public int getIntProperty(String name, int defaultValue) {
    return ServiceEngine.getIntProp(WebServerService.SERVICE + "." + name, defaultValue);
  }

  public void refresh() {
    ServiceEngine.refreshConfig();
  }
}