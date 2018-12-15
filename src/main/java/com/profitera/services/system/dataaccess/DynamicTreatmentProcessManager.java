package com.profitera.services.system.dataaccess;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.deployment.rmi.ListQueryServiceIntf;
import com.profitera.deployment.rmi.TreatmentWorkpadServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.ITreatmentProcess;
import com.profitera.event.ProcessingException;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.DeepCopier;
import com.profitera.util.xml.DOMDocumentUtil;

public class DynamicTreatmentProcessManager extends
		AbstractTreatmentProcessManager {
  public class UpdateOtherProcess {

    private final String statement;
    private final String updateSelect;

    public UpdateOtherProcess(String statement, String updateSelect) {
      this.statement = statement;
      this.updateSelect = updateSelect; 
    }
  }

  public interface IActivityFactory {
    public IActivity buildActivity();
  }
  
  public interface IActivity {
    void setParent(ITreatmentProcessDataManager m);
    void setStatement(String statement);
    void setProperties(Map conditions);
    void setProperty(String property);
    void setKey(String key);
    void execute(Long accountId, Map process, Long typeId,
        Date date, String user, ITransaction t, IReadWriteDataProvider p) throws SQLException,
        AbortTransactionException, ProcessingException;
  }
  
  private static final String GENERATE_PROCESS_ELEMENT = "generate";
  private static final String SELECT_PROCESS_ELEMENT = "select";
  private static final String INSERT_PROCESS_ELEMENT = "insert";
  private static final String DELETE_PROCESS_ELEMENT = "delete";
  private static final String UPDATE_PROCESS_ELEMENT = "update";
  private static final String EVENT_PROCESS_ELEMENT = "event";
  // Financial activities
  private static final String SIMPLE_TRANSACTION_PROCESS_ELEMENT = "simpletransaction";
  private static final String TRANSACTION_PROCESS_ELEMENT = "transaction";
  private static final String REVERSE_TRANSACTION_PROCESS_ELEMENT = "reversetransaction";
  private static final String PAYMENT_PROCESS_ELEMENT = "payment";
  private static final String POSTING_SCHEDULE_ELEMENT = "postingschedule";
  // Information service activities
  private static final String CUST_INFO_PROCESS_ELEMENT = "customerinfo";
  private static final String ACCOUNT_INFO_PROCESS_ELEMENT = "accountinfo";
  private static final String COLLATERAL_INFO_PROCESS_ELEMENT = "collateralinfo";
  // Special treatment activity
  private static final String UPDATE_OTHER_PROCESS_ELEMENT = "treatmentprocessupdate";
	private static final String KEY_PROCESSOR_ATTRIBUTE = "key";
	private static final String PROPERTY_PROCESSOR_ATTRIBUTE = "property";
	private static final String STATEMENT_PROCESSOR_ATTRIBUTE = "statement";
	private static final String MULTIPLIER_PROCESSOR_ATTRIBUTE = "multiplier";
	private static final String PROPERTIES = "properties";
	private static final String CREATE_CONFIG_ELEMENT = "create";
	private static final String UPDATE_CONFIG_ELEMENT = "update";
	private static final String INSERT_CONFIG_ELEMENT = "insert";
  
	//
	private Map activityFactories = new HashMap();
	{
    addFactory(DynamicTreatmentProcessManager.UPDATE_PROCESS_ELEMENT, new IActivityFactory(){
      public IActivity buildActivity() {
        return new UpdateTreatmentProcessActivity();
      }});
    addFactory(DynamicTreatmentProcessManager.INSERT_PROCESS_ELEMENT, new IActivityFactory(){
      public IActivity buildActivity() {
        return new InsertTreatmentProcessActivity();
      }});
    addFactory(DynamicTreatmentProcessManager.DELETE_PROCESS_ELEMENT, new IActivityFactory(){
      public IActivity buildActivity() {
        return new DeleteTreatmentProcessActivity();
      }});
    addFactory(DynamicTreatmentProcessManager.GENERATE_PROCESS_ELEMENT, new IActivityFactory(){
      public IActivity buildActivity() {
        return new TemplateGenerationTreatmentProcessActivity();
      }});
    addFactory(DynamicTreatmentProcessManager.SELECT_PROCESS_ELEMENT, new IActivityFactory(){
      public IActivity buildActivity() {
        return new SelectTreatmentProcessActivity();
      }});
    addFactory(DynamicTreatmentProcessManager.EVENT_PROCESS_ELEMENT, new IActivityFactory(){
      public IActivity buildActivity() {
        return new EventTreatmentProcessActivity();
      }});
    addFactory(DynamicTreatmentProcessManager.TRANSACTION_PROCESS_ELEMENT, new IActivityFactory(){
      public IActivity buildActivity() {
        return new TransactionTreatmentProcessActivity();
      }});
    addFactory(DynamicTreatmentProcessManager.REVERSE_TRANSACTION_PROCESS_ELEMENT, new IActivityFactory(){
      public IActivity buildActivity() {
        return new TransactionReversalTreatmentProcessActivity();
      }});
    addFactory(DynamicTreatmentProcessManager.SIMPLE_TRANSACTION_PROCESS_ELEMENT, new IActivityFactory(){
      public IActivity buildActivity() {
        return new SimpleTransactionTreatmentProcessActivity();
      }});
    addFactory(DynamicTreatmentProcessManager.PAYMENT_PROCESS_ELEMENT, new IActivityFactory(){
      public IActivity buildActivity() {
        return new PaymentTreatmentProcessActivity();
      }});
    addFactory(DynamicTreatmentProcessManager.CUST_INFO_PROCESS_ELEMENT, new IActivityFactory(){
      public IActivity buildActivity() {
        return new CustomerInformationTreatmentProcessActivity();
      }});
    addFactory(DynamicTreatmentProcessManager.ACCOUNT_INFO_PROCESS_ELEMENT, new IActivityFactory(){
      public IActivity buildActivity() {
        return new AccountInformationTreatmentProcessActivity();
      }});
    addFactory(DynamicTreatmentProcessManager.COLLATERAL_INFO_PROCESS_ELEMENT, new IActivityFactory(){
      public IActivity buildActivity() {
        return new CollateralInformationTreatmentProcessActivity();
      }});
    addFactory(DynamicTreatmentProcessManager.POSTING_SCHEDULE_ELEMENT, new IActivityFactory(){
      public IActivity buildActivity() {
        return new PostingScheduleTreatmentProcessActivity();
      }});
	}

	private IActivity[] insertProcesses;
	private IActivity[] updateProcesses;
  private String multiplier;
  private UpdateOtherProcess[] creationUpdates = new UpdateOtherProcess[0];

	public void configure(Object id, Element e) {
	  if (e == null){
	    throw new IllegalArgumentException("Null configuration element not permitted");
	  }
	  Element create = verifyAndRetreiveOne(id, e, CREATE_CONFIG_ELEMENT);
	  multiplier = null;
	  if (create != null){
	    Attr attribute = create.getAttributeNode(MULTIPLIER_PROCESSOR_ATTRIBUTE);
	    if (attribute != null){
	      multiplier = attribute.getValue();
	    }
	    Element[] updateOther = DOMDocumentUtil.getChildElementsWithName(UPDATE_OTHER_PROCESS_ELEMENT, create);
	    creationUpdates = new UpdateOtherProcess[updateOther.length];
	    for (int i = 0; i < updateOther.length; i++) {
        Element u= updateOther[i];
        Attr updateSelectNode = u.getAttributeNode("updateselect");
        if (updateSelectNode == null){
          throw new IllegalArgumentException("Required configuration attribute '" + "updateselect" + "' not assigned for " + UPDATE_OTHER_PROCESS_ELEMENT);
        }
        if (u.getAttributeNode("statement") == null){
          throw new IllegalStateException("Configured statement not assigned for " + UPDATE_OTHER_PROCESS_ELEMENT);
        }
        String updateSelect = updateSelectNode.getValue();
        String statement = u.getAttributeNode("statement").getValue();
        creationUpdates[i] = new UpdateOtherProcess(statement, updateSelect);
      }
	  }
		Element insert = verifyAndRetreiveOne(id, e, INSERT_CONFIG_ELEMENT);
		Element update = verifyAndRetreiveOne(id, e, UPDATE_CONFIG_ELEMENT);
		insertProcesses = loadProcesses(id, insert);
		updateProcesses = loadProcesses(id, update);

	}
	
	

	public Map[] verifyProcessCreation(Map process, Long accountId, Long typeId,
      String user, IReadOnlyDataProvider provider)
      throws TreatmentProcessCreationException {
	  Collection multiplyBy = null;
	  if (multiplier != null){
	    Object m = process.get(multiplier);
	    if (m instanceof Collection){
	      multiplyBy = (Collection) m;
	    }
	  }
	  List processes = new ArrayList();
	  if (multiplyBy != null){
	    DeepCopier c = new DeepCopier();
	    for (Iterator i = multiplyBy.iterator(); i.hasNext();) {
        Object item = (Object) i.next();
        if (!(item instanceof Map)){
          throw new TreatmentProcessCreationException("Multiplier field '" + multiplier + "' is a collection but does not contain maps, it has " + item.getClass(), "PROCESS_MULTIPLY_ILLEGAL");
        }
        try {
          Map processCopy = (Map) c.copy((Serializable) process);
          Map itemCopy = (Map) c.copy((Serializable) item);
          if (itemCopy.get("ACCOUNT_ID") != null) {
						Long multipliedAccountId = (Long) itemCopy.get("ACCOUNT_ID");
						Long sourceAccountId = (Long) processCopy.get("ACCOUNT_ID");
						if (multipliedAccountId.compareTo(sourceAccountId) != 0 && itemCopy.get(ITreatmentProcess.TREATMENT_PLAN_ID) == null) {							
							try{
								IReadWriteDataProvider p = (IReadWriteDataProvider)provider;
								getAccountTreamentPlan(itemCopy, multipliedAccountId, typeId, user, p);
							}catch(AbortTransactionException e){
								getLog().error("Failed to get treatment plan for account "+multipliedAccountId+" in "+multiplier, e);
								throw new TreatmentProcessCreationException("PROCESS_MULTIPLY_ERROR");
							}catch(SQLException e){
								getLog().error("Failed to get treatment plan for account "+multipliedAccountId+" in "+multiplier, e);
								throw new TreatmentProcessCreationException("PROCESS_MULTIPLY_ERROR");
							}catch(ClassCastException e){
								getLog().error("Failed to get treatment plan for account "+multipliedAccountId+" in "+multiplier, e);
								throw new TreatmentProcessCreationException("PROCESS_MULTIPLY_ERROR");
							}
						}
					}
          processCopy.putAll(itemCopy);
          processes.add(processCopy);
          // The catches here will effectively be unreachable
        } catch (IOException e){
          throw getMultiplierException(item, e);
        } catch (ClassNotFoundException e){
          // This catch is completely unreachable, we have the classes because we just serialized it
          throw getMultiplierException(item, e);
        }
      }
	  } else {
	    processes.add(process);
	  }
    //
    List toUpdate = new ArrayList();
    for (Iterator iterator = processes.iterator(); iterator.hasNext();) {
      Map processCreate = (Map) iterator.next();
      for (int i = 0; i < creationUpdates.length; i++) {
          Map[] maps = execute(creationUpdates[i], processCreate, user, provider);
          toUpdate.addAll(Arrays.asList(maps));
        
      }  
    }
    processes.addAll(toUpdate);
    return (Map[]) processes.toArray(new Map[processes.size()]);
  }

	private void getAccountTreamentPlan(final Map process, final Long accountId, final Long typeId, final String user, final IReadWriteDataProvider p) 
	throws SQLException, AbortTransactionException {
		p.execute(new IRunnableTransaction(){
      public void execute(ITransaction t) throws SQLException, AbortTransactionException {
        process.putAll(getTreatmentPlanForProcess(process, accountId, new Date(), typeId, null, null, t, p));
      }});
	}

  private TreatmentProcessCreationException getMultiplierException(Object item,
      Exception e) {
    getLog().error("Unexpected error occurred mulitplying process on '" + multiplier + "'", e);
    TreatmentProcessCreationException t = new TreatmentProcessCreationException("Multiplier field '" + multiplier + "' is a collection but does not contain maps, it has " + item.getClass(), "PROCESS_MULTIPLY_ERROR");
    return t;
  }

  Map[] execute(UpdateOtherProcess updateOtherProcess, Map process, 
      String user, IReadOnlyDataProvider p) throws TreatmentProcessCreationException {
    TransferObject queryResult = getListQueryService().getQueryList(updateOtherProcess.statement, process);
    if (queryResult.isFailed()){
      throw new TreatmentProcessCreationException("SELECT_OTHER_PROCESS_FAILED");
    }
    List updateProcesses = new ArrayList();
    for (Iterator i = ((List)(queryResult.getBeanHolder())).iterator(); i.hasNext();) {
      Map proc = (Map) i.next();
      Long id = (Long) proc.get(ITreatmentProcess.TREATMENT_PROCESS_ID);
      Long type = (Long) proc.get(ITreatmentProcess.PROCESS_TYPE_ID);
      if (id == null){
        throw new TreatmentProcessCreationException("Invalid result for '" + updateOtherProcess.statement + "': required field not returned: " + ITreatmentProcess.TREATMENT_PROCESS_ID);
      }
      if (type == null){
        throw new TreatmentProcessCreationException("Invalid result for '" + updateOtherProcess.statement + "': required field not returned: " + ITreatmentProcess.PROCESS_TYPE_ID);
      }
      TransferObject complete = getWorkpadService().getTreatmentProcessForEditing(id, type);
      if (complete.isFailed()){
        throw new TreatmentProcessCreationException("RETRIEVE_OTHER_PROCESS_FAILED");
      }
      Map otherProcess = (Map) complete.getBeanHolder();
      queryResult = getListQueryService().getQueryList(updateOtherProcess.updateSelect, proc);
      if (queryResult.isFailed()){
        throw new TreatmentProcessCreationException("UPDATE_SELECT_OTHER_PROCESS_FAILED");
      }
      Map m = (Map) ((List)queryResult.getBeanHolder()).get(0);
      otherProcess.putAll(m);
      updateProcesses.add(otherProcess);
      
    }
    return (Map[])updateProcesses.toArray(new Map[0]);
  }


  private IActivity[] loadProcesses(Object id, Element parent) {
		if (parent == null)
			return new IActivity[0];
		Element[] processElements = DOMDocumentUtil.getChildElements(parent);
		IActivity[] processes = new IActivity[processElements.length];
		for (int i = 0; i < processElements.length; i++) {
			Element element = processElements[i];
			final String name = element.getNodeName();
			if (getFactory(name) != null) {
				final String statement = element
						.getAttribute(STATEMENT_PROCESSOR_ATTRIBUTE);
				// Does not return null
				String prop = element
						.getAttribute(PROPERTY_PROCESSOR_ATTRIBUTE);
				final String property = prop.equals("") ? null
						: prop;
				//Does not return null
				String k = element.getAttribute(KEY_PROCESSOR_ATTRIBUTE);
				final String key = k.equals("") ? null : k;
				
				//Getting conditions
				NodeList properties = element.getElementsByTagName(PROPERTIES);

				Map map = getActivityProperties(properties);
				IActivity newProcessor = createActivity(name, statement,
						property, key, map);
				processes[i] = newProcessor;
			}
			if (processes[i] == null) {
				throw new IllegalArgumentException(getClass().getName()
						+ " configured for '" + id
						+ "' has an unrecognized configuration element: "
						+ name);
			}
		}
		return processes;
	}

  protected IActivityFactory getFactory(String name) {
    return (IActivityFactory) activityFactories.get(name);
  }
  
  protected void addFactory(String name, IActivityFactory f){
    activityFactories.put(name, f);
  }

  private Map getActivityProperties(NodeList propertyElements) {
    Map map = new HashMap();
    if(propertyElements.getLength()!=0){
    	for(int j=0;j<propertyElements.getLength();j++){
    		Node n = propertyElements.item(j);
    		NodeList condition = n.getChildNodes();
    		
    		for(int a=0;a<condition.getLength();a++){
    			Node c = condition.item(a);
    			if(c!=null&&c.getAttributes()!=null&&c.getAttributes().getLength()!=0){
    				String name = c.getAttributes().getNamedItem("name").getNodeValue();
            String value = c.getAttributes().getNamedItem("value").getNodeValue();
            map.put(name,value);
    			}							
    		}
    		
    	}
    }
    return map;
  }
	
	
	private IActivity createActivity(final String name,
			final String statement, final String property, final String key, final Map conditions) {
		IActivity newProcessor = getFactory(name).buildActivity();
		newProcessor.setParent(this);
		newProcessor.setStatement(statement);
		newProcessor.setProperty(property);
		newProcessor.setKey(key);
		newProcessor.setProperties(conditions);
		return newProcessor;
	}

	private Element verifyAndRetreiveOne(Object id, Element e, String tag) {
		Element[] inserts = DOMDocumentUtil.getChildElementsWithName(tag, e);
		if (inserts.length > 1)
			throw new IllegalArgumentException(getClass().getName()
					+ " configured for  '" + id + "' has multiple " + tag
					+ " nodes.");
		return inserts.length == 0 ? null : inserts[0];
	}

  protected void createExtendedProcessInformation(Long accountId, Map process,
			Long typeId, Date date, String user, ITransaction t, IReadWriteDataProvider p) throws SQLException,
			AbortTransactionException {
		super.createExtendedProcessInformation(accountId, process, typeId, date, user, t, p);
		for (int i = 0; i < insertProcesses.length; i++) {
		  insertProcesses[i].execute(accountId, process, typeId, date, user, t, p);
		}
	}

	protected void updateExtendedProcessInformation(Long accountId, Map process,
			Long typeId, Date date, String user, ITransaction t, IReadWriteDataProvider p) throws SQLException,
			AbortTransactionException {
		super.updateExtendedProcessInformation(accountId, process, typeId, date, user, t, p);
		for (int i = 0; i < updateProcesses.length; i++) {
			updateProcesses[i].execute(accountId, process, typeId, date, user, t, p);
		}
	}
	private TreatmentWorkpadServiceIntf getWorkpadService() {
    return (TreatmentWorkpadServiceIntf) LookupManager.getInstance().getLookupItem(LookupManager.BUSINESS, "TreatmentWorkpadService");
  }
  
  private ListQueryServiceIntf getListQueryService() {
    return (ListQueryServiceIntf) LookupManager.getInstance().getLookupItem(LookupManager.BUSINESS, "ListQueryService");
  }

}
