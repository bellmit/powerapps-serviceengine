package com.profitera.services.business.query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.profitera.dataaccess.NoSuchStatementException;
import com.profitera.deployment.rmi.ListQueryServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.business.login.MapLoginService;
import com.profitera.services.business.login.ServerSession;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.MapVerifyingMapCar;
import com.profitera.util.Strings;
import com.profitera.util.io.ForEachLine;
import com.profitera.util.reflect.Reflect;
import com.profitera.util.reflect.ReflectionException;
import com.profitera.util.xml.DocumentLoader;
import com.profitera.util.xml.XMLConfigUtil;

public class ListQueryService extends ProviderDrivenService implements ListQueryServiceIntf {  
  private static final String NO_SUCH_QUERY = "NO_SUCH_QUERY";
  private static final String PROCESSOR_CONFIG_FILE_PATH = "listqueryservice.queryprocessorfilepath";
  private static final String LOCKDOWN = "listqueryservice.lockdown";
  private final Map<String, IListQueryProcessor[]> processorLists = new HashMap<String, IListQueryProcessor[]>(200);
  long counter = 0;
  private Boolean isLockedDown;
  
  public ListQueryService(){
    loadQueryProcessors();
  }
  @Override
  public TransferObject getQueryList(String name, Map<String, Object> arguments){
    if (arguments == null) {
      arguments = new HashMap<String, Object>();
    } else {
      // This ensures the arguments are modifiable, which is important
      arguments = new HashMap<String, Object>(arguments);
    }
    Long session = ServerSession.THREAD_SESSION.get();
    MapLoginService l = (MapLoginService)getLogin();
    TransferObject sessionRole = l.getSessionRole(session);
    arguments.put("REQUEST_ROLE_ID", sessionRole.getBeanHolder());
    arguments.put("REQUEST_USER_ID", l.getSessionUser(session));
    long id = ++counter;
    if (!isLockedDown() || this.processorLists.containsKey(name)) {
      return getServiceQueryList(id, name, arguments);
    } else {
      log.debug(getLogIdString(id) + name + " " + NO_SUCH_QUERY + ": Locked down");
      return new TransferObject(new Object[]{name}, TransferObject.ERROR, NO_SUCH_QUERY);      
    }
  }
  private boolean isLockedDown() {
    if (isLockedDown == null) {
      isLockedDown = ServiceEngine.getProp(LOCKDOWN, "true").equals("true");
    }
    return isLockedDown;
  }

  private TransferObject getServiceQueryList(long id, String name, Map<String, Object> arguments){
    try {
      IQueryService qs = getQueryService(id);
      IListQueryProcessor[] processors = getQueryProcessors(name);
      IReadOnlyDataProvider provider = getProvider();
      arguments = preprocessArguments(name, arguments, processors, qs);
      List<Map<String, Object>> result = runQuery(name, arguments, provider);
      return new TransferObject(postProcessResults(name, arguments, result, processors, qs));
    } catch (TransferObjectException e) {
      TransferObject t = e.getTransferObject();
      if (e.getCause() instanceof NoSuchStatementException)
        return new TransferObject(t.getBeanHolder(), t.getFlag(), NO_SUCH_QUERY);
      return t;
    } catch (RuntimeException r){
      return returnFailWithTrace(r.getMessage(), IReadOnlyDataProvider.LIST_RESULTS, name, arguments, r);
    }
  }
  private List<Map<String, Object>>
      runQuery(String name, Map<String, Object> arguments, IReadOnlyDataProvider provider)
          throws TransferObjectException {
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> result = executeListQuery(IReadOnlyDataProvider.LIST_RESULTS, name, arguments, new MapVerifyingMapCar(new Object[0]), provider);
    return result;
  }

  private List<Map<String, Object>> postProcessResults(String name, Map<String, Object> arguments, List<Map<String, Object>> result, IListQueryProcessor[] processors, IQueryService qs) throws TransferObjectException {
    log.debug(getLogId(qs) + "Processing " + name + " " + arguments + " " + result.size() + " records.");
    long allProcessorStart = System.currentTimeMillis();
    List<Map<String, Object>> finalResult = new ArrayList<Map<String, Object>>(result.size());
    finalResult.addAll(result);
    for (int i = 0; i < processors.length; i++) {
      long processorStart = System.currentTimeMillis();
      finalResult = processors[i].postProcessResults(arguments, finalResult, qs);
      long processorTime = System.currentTimeMillis() - processorStart;
      log.debug(getLogId(qs) + "Processor for " + name + " " + processors[i].getClass().getName() + " " + processorTime + "ms " + arguments + " " + result.size() + " records.");
    }
    long allProcessorTime = System.currentTimeMillis() - allProcessorStart;
    log.debug(getLogId(qs) + "All Processors for " + name + " " + allProcessorTime + "ms " + arguments + " " + result.size() + " records.");
    return finalResult;
  }

  public static String getLogId(IQueryService qs) {
    long id = qs.getId();
    return getLogIdString(id);
  }

  private static String getLogIdString(long id) {
    return "LQS-" + Strings.leftPad(""+ id, 10, '0') + " ";
  }

  private IQueryService getQueryService(final long id) {
    return new IQueryService(){
        public TransferObject getQueryList(String name, Map<String, Object> arguments) {
          return getServiceQueryList(id, name, arguments);
        }

        public long getId() {
          return id;
        }};
  }

  private Map<String, Object> preprocessArguments(String qName, Map<String, Object> arguments, IListQueryProcessor[] processors, IQueryService qs) throws TransferObjectException {
    log.debug(getLogId(qs) + qName + " Original Arguments: " + arguments);
    Map<String, Object> finalArguments = arguments;
    for (int i = 0; i < processors.length; i++) {
      finalArguments = processors[i].preprocessArguments(finalArguments, qs);
    }
    log.debug(getLogId(qs) + qName + " Final Arguments: " + finalArguments);
    return finalArguments;
  }

  private IReadOnlyDataProvider getProvider() {
    return getReadWriteProvider();
  }
  
  /**
   * @param queryName
   * @return - NEVER returns null
   */
  private IListQueryProcessor[] getQueryProcessors(String queryName){
    IListQueryProcessor[] p = (IListQueryProcessor[]) processorLists.get(queryName);
    if (p == null) p = new IListQueryProcessor[0];
    return p;
  }
  
  private void loadQueryProcessors() {
    String[] paths = ServiceEngine.getProp(PROCESSOR_CONFIG_FILE_PATH).split("[;]");
    Map<String, IListQueryProcessor[]> processors = new HashMap<String, IListQueryProcessor[]>();
    log.info("Loading query processors from :" + Strings.getListString(paths, ", "));
    for (int i = 0; i < paths.length; i++) {
      String path = paths[i]; 
      processors.putAll(loadQueryProcessors(path));  
    }
    // This clear and putAll operation is NOT synchronized, 
    // so we know that there will be concurrency issues. 
    // Reloading is only intended for development and other than that
    // this will only be called during the constructor of this service.
    processorLists.clear();
    processorLists.putAll(processors);    
  }

  private Map<String, IListQueryProcessor[]> loadQueryProcessors(String path) {
    Map<String, IListQueryProcessor[]> processorMap = new HashMap<String, IListQueryProcessor[]>();
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
    BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
    final StringBuffer buffer = new StringBuffer();
    try {
      new ForEachLine(r){
        protected void process(String line) {
          buffer.append(line + "\n");
        }}.process();
        Document d = DocumentLoader.parseDocument(buffer.toString());
        Element root = d.getDocumentElement();
        NodeList nl = root.getChildNodes();
        for(int i=0; i< nl.getLength(); i++){
          Node n = nl.item(i);
          if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("query")){
            Element q = (Element) n;
            String queryName = q.getAttribute("name");
            IListQueryProcessor[] processors = getProcessors(queryName, q);
            processorMap.put(queryName, processors);
          }
        }
      return processorMap;
    } catch (Exception e) {
      log.fatal("Failed to load processors", e);
      throw new MissingResourceException("File '" + path + "' not found, was empty or malformed, refusing to execute queries with missing configuration information.", this.getClass().getName(), PROCESSOR_CONFIG_FILE_PATH);
    } finally {
      try {
        if (r != null) r.close();
        if (inputStream != null) inputStream.close();
      } catch (IOException e){}
    }
  }

  private IListQueryProcessor[] getProcessors(String name, Element q) {
    NodeList nl = q.getChildNodes();
    List<IListQueryProcessor> procs = new ArrayList<IListQueryProcessor>();
    for(int i = 0; i < nl.getLength(); i++){
      Node n = nl.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("processor")){
        Element p = (Element) n;
        String impl = p.getAttribute("implementation");
        try {
          IListQueryProcessor qp = (IListQueryProcessor) Reflect.invokeConstructor(impl, null, null);
          qp.configure(name, getProperties(p));
          procs.add(qp);
        } catch (ReflectionException e) {
          throw new RuntimeException("Unable to load query processor '" + impl + "' for " + name);
        }
      }
    }
    return (IListQueryProcessor[]) procs.toArray(new IListQueryProcessor[0]);
  }

  private Properties getProperties(Element p) {
    return XMLConfigUtil.getProperties(p);
  }

  public TransferObject reloadQueryProcessors() {
    try {
      loadQueryProcessors();
    } catch (MissingResourceException e){
      return new TransferObject(TransferObject.EXCEPTION, "Failed to load query processors: " + e.getMessage()); 
    }
    return new TransferObject();
  }
}
