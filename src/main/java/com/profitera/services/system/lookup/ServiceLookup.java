package com.profitera.services.system.lookup;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.LogFactory;

import com.profitera.services.Service;

public final class ServiceLookup {
  private final Map<String, Service> servicesMap;
  private final Properties props;
  private final Set<String> failedServices = new HashSet<String>();

  public ServiceLookup(final Properties inProperties) {
    servicesMap = new ConcurrentHashMap<String, Service>();
    props = inProperties;
    final Enumeration<?> e = props.propertyNames();
    while (e.hasMoreElements()) {
      getService((String)e.nextElement());
    }
  }

  public Service getService(final String name) {
    Service service = (Service) servicesMap.get(name);
    if (service == null) {
      final String value = props.getProperty(name);
      if (value == null) {
        return null;
      }
      try {
        servicesMap.put(name, (Service) Class.forName(value.trim()).newInstance());
        LogFactory.getLog(ServiceLookup.class).debug("Initialized " + name + " - " + value);
      } catch (Throwable e) { //NOPMD - Need to catch everything here
        failedServices.add(value);
        LogFactory.getLog(ServiceLookup.class).fatal(e.getClass().getName(), e);
      }
    }
    return service;
  }

  /**
   * @return the Service names in alphabetical order
   */
  public String[] getAllServiceNames() {
    final Set<String> set = servicesMap.keySet();
    String[] array = set.toArray(new String[set.size()]);
    Arrays.sort(array);
    return array;
  }

  public void setService(String name, Service service) {
    servicesMap.put(name, service);
  }

  public Set<String> getFailedServices() {
    return failedServices;
  }
}
