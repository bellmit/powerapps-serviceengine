package com.profitera.services.business.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public abstract class BaseListQueryProcessor implements IListQueryProcessor {
  private Log log; 
  private String queryName;
  private Properties props;
  private Map requiredProperties = new HashMap();
  private Map optionalProperties = new HashMap();
  private Map optionalDefaultValues = new HashMap();
  private Map longDocs = new HashMap();
  private Map shortDocs = new HashMap();
  private Map patterns = new HashMap();
  /** 
   * I am final on purpose, I call configureProcessor, override that 
   * if you want to do any fail-fast configuration checks, by the
   * time this method is called all of your config info needs to
   * be registered. 
   */
  public final void configure(String queryName, Properties properties) {
    this.queryName = queryName;
    props = properties;
    for (Iterator i = requiredProperties.entrySet().iterator(); i.hasNext();) {
      Map.Entry e = (Map.Entry) i.next();
      if (props.get(e.getKey()) == null){
        throw new IllegalArgumentException("Required configuration property " + e.getKey() + " not set for " + getQueryName() + " " + getClass().getName());
      }
    }
    for (Iterator i = props.entrySet().iterator(); i.hasNext();) {
      Map.Entry e = (Map.Entry) i.next();
      convertType((String)e.getKey(), (String) e.getValue());
    }
    configureProcessor();
  }
  
  protected void addRequiredProperty(String name, Class type, String shortDoc, String longDoc){
    throwAlreadyLoaded(name);
    requiredProperties.put(name, type);
    longDocs.put(name, longDoc);
    shortDocs.put(name, shortDoc);
  }
  
  private void throwAlreadyLoaded(String name) {
    if (props != null){
      throw new IllegalStateException("Attempted to register new configuration property after init: " + name);
    }
  }

  protected void addProperty(String name, String pattern, Class type, String shortDoc, String longDoc){
    throwAlreadyLoaded(name);
    optionalProperties.put(name, type);
    patterns.put(name, pattern);
    longDocs.put(name, longDoc);
    shortDocs.put(name, shortDoc);
  }
  
  protected void addProperty(String name, Class type, Object defaultValue, String shortDoc, String longDoc){
    throwAlreadyLoaded(name);
    optionalProperties.put(name, type);
    optionalDefaultValues.put(name, defaultValue);
    longDocs.put(name, longDoc);
    shortDocs.put(name, shortDoc);
  }
  
  protected Log getLog(){
    if (log == null) {
      log = LogFactory.getLog(getClass());
    }
    return log;
  }

  protected void configureProcessor() {
  }

  public String getQueryName(){
    return queryName;
  }
  
  private Properties getProperties(){
    return props;
  }

  public Class getRequiredProviderType() {
    return null;
  }
  
  protected Object getProperty(String propertyName){
  	if (!requiredProperties.containsKey(propertyName) && ! optionalProperties.containsKey(propertyName))
      throw new IllegalArgumentException("Property " + propertyName + " is not registered to list query processor " + getClass().getName());
  		
      return convertType(propertyName, getProperties().getProperty(propertyName));
  }
  
  private Object convertType(String propertyName, String propertyValue) {
    Class type = (Class) requiredProperties.get(propertyName);
    if (type == null){
      type = (Class) optionalProperties.get(propertyName);
    }
    if (propertyValue == null){
      return optionalDefaultValues.get(propertyName);
    }
    return convertValue(propertyValue, type);
  }

  private Object convertValue(String propertyValue, Object typ) {
    if (typ == null || typ.equals(String.class)){
      return propertyValue;
    }
    if (typ.equals(Long.class)){
      return new Long(propertyValue);
    }
    if (typ.equals(Integer.class)){
      return new Integer(propertyValue);
    }
    if (typ.equals(Double.class)){
      return new Double(propertyValue);
    }
    if (typ.equals(Boolean.class)){
      return new Boolean(propertyValue);
    }
    throw new IllegalArgumentException("Unsupported property type for " + getClass().getName() + ": " + typ.getClass().getName());
  }

   protected Object getProperty(String propertyName, String patternValue){
    if (optionalProperties.containsKey(propertyName) && patterns.containsKey(propertyName)){
      return convertType(propertyName, getProperties().getProperty(propertyName+patternValue));
    } else {
      throw new IllegalArgumentException("Property " + propertyName + "('" +patternValue + "') is not registered as a pattern property for list query processor " + getClass().getName());
    }
  }
   
  public Map preprocessArguments(Map arguments, IQueryService qs) throws TransferObjectException {
    return arguments;
  }

  public List postProcessResults(Map arguments, List result, IQueryService qs) throws TransferObjectException {
    return result;
  }
  
  public final String getShortDocumentation(){
    String s = getSummary();
    if (s == null)
      throw new RuntimeException("Component summary missing for " + getClass().getName());
    return s;
  }

  public final String getLongDocumentation(){
    String s = getDocumentation();
    if (s == null)
      throw new RuntimeException("Component documentation missing for " + getClass().getName());
    return s;
  }

  protected abstract String getDocumentation();
  
  protected abstract String getSummary();
  
  public List getRequiredProperties(){
    List l = new ArrayList();
    for (Iterator i = requiredProperties.keySet().iterator(); i.hasNext();) {
      l.add(i.next());
    }
    Collections.sort(l);
    return l;
  }
  
  public List getOptionalProperties(){
    List l = new ArrayList();
    for (Iterator i = optionalProperties.keySet().iterator(); i.hasNext();) {
      l.add(i.next());
    }
    Collections.sort(l);
    return l;
  }
  
  public String getPropertyShortDocumentation(String name){
    return (String) shortDocs.get(name);
  }
  
  public String getPropertyLongDocumentation(String name){
    return (String) longDocs.get(name);
  }
  
  public Object getPropertyDefaultValue(String name){
    return optionalDefaultValues.get(name);
  }
  
  public Class getPropertyType(String name){
    if (requiredProperties.containsKey(name))
      return (Class) requiredProperties.get(name);
    else 
      return (Class) optionalProperties.get(name);
  }

}
