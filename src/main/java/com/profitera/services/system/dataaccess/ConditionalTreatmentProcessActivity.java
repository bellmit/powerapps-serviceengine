/**
 * 
 */
package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.descriptor.business.meta.ITreatmentProcess;
import com.profitera.services.system.dataaccess.DynamicTreatmentProcessManager.IActivity;
import com.profitera.util.reflect.Reflect;

abstract class ConditionalTreatmentProcessActivity implements IActivity {
	  public static final String CONDITION_KEY = "CONDITION_KEY"; 
	  public static final String CONDITION_VALUE = "CONDITION_VALUE";
	  //
    private String statement;
    private Map conditions;
    private String property;
    private String key;
    private Log log;
    private ITreatmentProcessDataManager parent;

    ConditionalTreatmentProcessActivity() {
      
    }
    

    protected Map getProperties() {
      return conditions;
    }
    
    protected Log getLog() {
      if (log == null){
        log = LogFactory.getLog(this.getClass());
      }
      return log;
    }
    

    
    
    
    

    public void execute(Long accountId, final Map process, Long typeId,
    		Date date, String user, ITransaction t, IReadWriteDataProvider p) throws AbortTransactionException,
    		SQLException {
    	Map[] targets = null;
    	if (property == null) {
    		targets = new Map[] { process };
    	} else {
    		Object propValue = process.get(property);
    		if (propValue == null) {
    			targets = new Map[0];
    		} else if (propValue instanceof Map) {
    			targets = new Map[] { (Map) propValue };
    		} else if (propValue instanceof Collection) {
    			Collection c = (Collection) propValue;
    			targets = (Map[]) c.toArray(new Map[c.size()]);
    			targets = mergeQueryArguments(process,targets);
    		}
    	}
    	for (int j = 0; j < targets.length; j++) {
    	  //	Check for conditions
    	  boolean conditionMet = verifyConditions(targets[j]);
    		if(conditionMet) {
    		  executeActivity(accountId, process, targets[j], date, user, t, p);
        }
    		
    	}
    }

    protected abstract void executeActivity(Long accountId, final Map process,
        Map target, Date date, String user, ITransaction t, IReadWriteDataProvider p) throws SQLException,
        AbortTransactionException;
    
    

    private boolean verifyConditions(Map target) {
      boolean conditionMet = true;
      if(conditions != null && conditions.get(CONDITION_KEY)!=null){
        getLog().info("Evaluating conditions for '" + statement + "'");
      	String[] conditionKeys = ((String) conditions.get(CONDITION_KEY)).split(";");
      	String[] conditionValues = ((String) conditions.get(CONDITION_VALUE)).split(";");
      	for(int i=0;i<conditionKeys.length;i++){
      		Object currentVal = target.get(conditionKeys[i]);
      		Object conditionValue = conditionValues[i];
      		
      		if(currentVal==null){
      			conditionMet = false;
      			getLog().info("Form value for " + conditionKeys[i] + " '" + currentVal + "' does not match the condition value '" + conditionValue + "'. Statement '" + statement + "' will not be executed.");							
      			continue;
      		}
      		Class cls = currentVal.getClass();
      		
      		Object val=null;
      		try {
      			val = Reflect.invokeConstructor(cls,new Class[]{String.class},new Object[]{conditionValue});
      		} catch (Exception e) {
      			getLog().error("Error occurred when invoking constructor for class '" + cls.getName() + "' for CONDITION_KEY '" + conditionKeys[i] + "' CONDITION_VALUE '" + conditionValue + "'",e);
      			conditionMet = false;
      			continue;
      		}
      		
      		if(!currentVal.equals(val)){
      			conditionMet = false;
      			getLog().info("Form value for " + conditionKeys[i] + " '" + currentVal + "' does not match the condition value '" + conditionValue + "'. Statement '" + statement + "' will not be executed.");							
      			
      		}	else {
            getLog().info("Form value for " + conditionKeys[i] + " '" + currentVal + "' matches the condition value '" + conditionValue + "' for '" + statement + "'");
          }
      		
      	}
      		
      	if(conditionMet) {
      		getLog().info("All conditions met for '" + statement + "', will execute");
        }
      }
      return conditionMet;
    }
    private Map[] mergeQueryArguments(final Map process, Map[] targets){
      Map[] maps = new Map[targets.length];
      
      for(int i=0;i<targets.length;i++){
        maps[i] = new HashMap(); 
        maps[i].putAll(process);
        maps[i].putAll(targets[i]);
        maps[i].put(ITreatmentProcess.TREATMENT_PROCESS_ID,process.get(ITreatmentProcess.TREATMENT_PROCESS_ID));
        maps[i].put("ACCOUNT_ID",process.get("ACCOUNT_ID"));
      }
      return maps;
    }

    public void setStatement(String statement) {
      this.statement = statement;
    }

    public void setProperties(Map conditions) {
      this.conditions = conditions;
    }

    public void setProperty(String property) {
      this.property = property;
    }

    public void setKey(String key) {
      this.key = key;
    }

    protected String getStatement() {
      return statement;
    }

    protected String getKey() {
      return key;
    }


    protected ITreatmentProcessDataManager getParent() {
      return parent;
    }


    public void setParent(ITreatmentProcessDataManager parent) {
      this.parent = parent;
    }
    
    protected String getRequiredProperty(String propName) throws IllegalArgumentException {
      String p = getProperty(propName);
      if (p == null){
        throw new IllegalArgumentException("Required property value not specified for property '" + propName + "'");
      }
      return p;
    }
    
    protected String getProperty(String propName) throws IllegalArgumentException {
      String p = (String) getProperties().get(propName);
      return p;
    }
    
    protected AbortTransactionException getWrongTypeAbort(String name, String key, Object value, Class expectedType) {
      return new AbortTransactionException("Field value for " + name + " in " + key + " was not a " + expectedType.getName() + ", found " + value.getClass().getName());
    }
  }