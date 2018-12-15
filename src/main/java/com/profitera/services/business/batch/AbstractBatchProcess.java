package com.profitera.services.business.batch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.deployment.rmi.ListQueryServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.CollectionUtil;
import com.profitera.util.DateParser;
import com.profitera.util.exception.UnknownPropertyTypeException;
import com.profitera.util.handler.DefaultHandlerFactory;
import com.profitera.util.handler.IHandlerFactory;
import com.profitera.util.struc.PropertyStruct;

public abstract class AbstractBatchProcess implements IBatchProcess {
  public static final String EFFECTIVE_DATE_PARAM_NAME = "EFFECTIVE_DATE";
  private boolean loaded = false;
  //
  private String identifier;
  private Date effectiveDate;
  private Log log;
  
  private Map registeredProperties = new HashMap();

  protected abstract String getBatchSummary();
  protected abstract String getBatchDocumentation();

  private void addProperty(String name, boolean isArray, Class type, boolean isRequired, Object defaultValue, String shortDoc, String longDoc){
    doCanRegisterCheck(name, type);
    PropertyStruct ps = new PropertyStruct();
    ps.name = name;
    ps.type = type;
    ps.isRequired = isRequired;
    ps.defaultValue = defaultValue;
    ps.isArray = isArray;
    ps.shortDoc = shortDoc;
    ps.longDoc = longDoc;
    if (ps.longDoc == null || ps.shortDoc == null)
      throw new RuntimeException("Batch property " + name + " is undocumented for " + getClass().getName());
    registeredProperties.put(name, ps);
  }
  
  protected void addRequiredProperty(String name, Class type, String sDoc, String lDoc){
    addProperty(name, false, type, true, null, sDoc, lDoc);
  }
  
  protected void addProperty(String name, Class type, Object defaultValue, String sDoc, String lDoc){
    addProperty(name, false, type, false, defaultValue, sDoc, lDoc);
  }
  
  protected void addRequiredListProperty(String name, Class type, String sDoc, String lDoc){
    addProperty(name, true, type, true, null, sDoc, lDoc);
  }
  
  protected void addListProperty(String name, Class type, Object defaultValue, String sDoc, String lDoc){
    addProperty(name, true, type, false, defaultValue, sDoc, lDoc);
  }
  private void doCanRegisterCheck(String name, Class type) {
    if (loaded)
      throw new IllegalStateException("Attempted to register property '" + name + "' after properties were loaded");
    if (!getHandlerFactory().canHandle(type))
    	throw new UnknownPropertyTypeException(name, type);
    if (registeredProperties.containsKey(name)){
      throw new IllegalArgumentException("Attempted to register property '" + name + "' twice, properties can only be defined once");
    }
  }

  /**
   * You can override this method to provide new handler
   * factory which can extend the default handler factory
   * and handler more data types
   * @return
   */
  protected IHandlerFactory getHandlerFactory() {
    return DefaultHandlerFactory.getInstance();
  }

  private final void configure(Map p) {
		loaded = true;
		for (Iterator i = registeredProperties.values().iterator(); i.hasNext();) {
			PropertyStruct ps = (PropertyStruct) i.next();
			if (ps.isArray) {
				ps.value = getValue(CollectionUtil.loadDelimitedObjectArray((String)p.get(ps.name)), ps.type);
				if (ps.value == null && ps.isRequired)
					throw new IllegalStateException("Required property " + ps.name + " not provided for batch " + getIdentifier());
				if (ps.value == null)
					ps.value = getValue(CollectionUtil.loadDelimitedObjectArray((String)ps.defaultValue), ps.type);
			} else {
				ps.value = getValue((String)p.get(ps.name), ps.type);
				if (ps.value == null && ps.isRequired)
					throw new IllegalStateException("Required property " + ps.name + " not provided for batch " + getIdentifier());
				if (ps.value == null)
					ps.value = getValue((String)ps.defaultValue, ps.type);
			}
		}
		configured();
	}
  
  protected void configured(){
  }
  
  public Object getPropertyValue(String name) {
    if (!loaded)
      throw new IllegalStateException("Attempted to access property '" + name + "' before batch was configured");
    if (!registeredProperties.containsKey(name))
      throw new IllegalStateException("Attempted to access property '" + name + "' which is not registered with batch");
    PropertyStruct ps = (PropertyStruct) registeredProperties.get(name);
    if (ps.isArray && ps.value != null)
    	return Arrays.asList((Object[])ps.value);
    return ps.value;
  }

  private Object[] getValue(Object[] values, Class type) {
  	if (values == null)
  		return null;
  	Object[] newValues = new Object[values.length];
  	for (int i = 0; i < values.length; i++) {
  		newValues[i] = (getHandlerFactory().getHandler(type).getValue((String)values[i]));
  	}
  	return newValues;
  }

  private Object getValue(String propertyValue, Class type) {
    return getHandlerFactory().getHandler(type).getValue(propertyValue);
  }

  protected String getIdentifier(){
    if (identifier == null)
      throw new IllegalStateException("Identifier not yet set for instance of " + this.getClass().getName());
    return identifier;
  }
  
  protected Date getEffectiveDate(){
    if (effectiveDate == null){
      throw new IllegalStateException("Effective date not yet set for instance of " + this.getClass().getName());
    }
    return effectiveDate;
  }
  
  public TransferObject invoke(String identifier, Date effectiveDate, Map arguments) {
    this.identifier = identifier;
    getLog().info("Starting batch " + getIdentifier());
    Date startOfDay = DateParser.getStartOfDay(effectiveDate);
    if (!startOfDay.equals(effectiveDate)){
      getLog().info("Effective date of batch " + identifier + " adjusted from " + effectiveDate + " to " + startOfDay);
    }
    this.effectiveDate = startOfDay;
    try {
    	configure(arguments);
    	return invoke();
    } catch (IllegalStateException e) {
      getLog().error("Missing required properties for batch " + identifier + " of type " + getClass().getName() + " : " + e.getMessage(), e);
      return new TransferObject(null, TransferObject.ERROR, e.getMessage());
    } finally {
      getLog().info("Ended batch " + getIdentifier());  
    }
    
  }

  protected Log getLog() {
    if (log == null) {
      log = LogFactory.getLog(this.getClass());
    }
    return log;
  }

  public String getPropertyShortDocumentation(String name){
    return ((PropertyStruct)registeredProperties.get(name)).shortDoc;
  }
  
  public String getPropertyLongDocumentation(String name){
    return ((PropertyStruct)registeredProperties.get(name)).longDoc;
  }

  public Class getPropertyType(String name){
	    return ((PropertyStruct)registeredProperties.get(name)).type;
  }
  
  public Object getPropertyDefaultValue(String name){
  	PropertyStruct ps = (PropertyStruct)registeredProperties.get(name);
  	Object value = ps.defaultValue;
  	if(value==null) return null;
  	return ps.isArray? Arrays.asList(value):value;
  }
  
  public final String getBatchShortDocumentation(){
    String s = getBatchSummary();
    if (s == null)
      throw new RuntimeException("Batch summary missing for " + getClass().getName());
    return s;
  }
  
  
  public final String getBatchLongDocumentation(){
    String s = getBatchDocumentation();
    if (s == null)
      throw new RuntimeException("Batch documentation missing for " + getClass().getName());
    return s;
  }

  public List getRequiredProperties(){
    List required = new ArrayList();
    for (Iterator iter = registeredProperties.values().iterator(); iter.hasNext();) {
      PropertyStruct p = (PropertyStruct) iter.next();
      if (p.isRequired)
        required.add(p.name);
    }
    return required;
  }
  
  public List getOptionalProperties(){
    List optional = new ArrayList();
    for (Iterator iter = registeredProperties.values().iterator(); iter.hasNext();) {
      PropertyStruct p = (PropertyStruct) iter.next();
      if (!p.isRequired)
        optional.add(p.name);
    }
    return optional;
  }
  
  protected ListQueryServiceIntf getListQueryService() {
    LookupManager lm = LookupManager.getInstance();
    return (ListQueryServiceIntf) lm.getLookupItem(LookupManager.BUSINESS, "ListQueryService");
  }


  protected abstract TransferObject invoke();
}