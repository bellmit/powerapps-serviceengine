package com.profitera.services.business.query;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

/**
 * @author jamison
 * @deprecated This class should not be extended for new processors, 
 * use BaseListQueryProcessor instead, it exposes better configuration functions.
 */
public abstract class AbstractListQueryProcessor implements IListQueryProcessor {
  private static Log LOG = LogFactory.getLog(AbstractListQueryProcessor.class); 
  private String queryName;
  private Properties props;
  /** 
   * I am final on purpose, I call configureProcessor, override that 
   * if you want to do any fail-fast configuration. 
   */
  public final void configure(String queryName, Properties properties) {
    this.queryName = queryName;
    props = properties;
    configureProcessor();
  }
  
  protected Log getLog(){
    return LOG;
  }

  protected void configureProcessor() {
  }

  public String getQueryName(){
    return queryName;
  }
  
  public Properties getProperties(){
    return props;
  }

  protected String getRequiredProperty(String propertyName){
    String value = getProperties().getProperty(propertyName);
    if (value == null){
      throw new IllegalArgumentException("Required configuration property " + propertyName + " not set for " + getQueryName() + " " + getClass().getName());
    }
    return value;
  }

  protected String getProperty(String propertyName){
    return getProperties().getProperty(propertyName);
  }
  
  public List postProcessResults(Map arguments, List result, IQueryService qs) throws TransferObjectException {
    return result;
  }

}
