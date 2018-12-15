package com.profitera.services.business.appserver.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.deployment.rmi.ApplicationServerServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.network.AddressResolver;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.util.ArrayUtil;
import com.profitera.util.Strings;

public class AppServerService {
  private static final String SERVER_NOT_DEFINED = "SERVER_NOT_DEFINED";
  static final String GET_APPLICATION_SERVERS = "getApplicationServers";
  protected static final String VERIFIED_FLAGS = "VERIFIED_FLAGS";
  protected static final String MEMORY_FLAGS = "MEMORY_FLAGS";
  static final String INSERT_APP_MEM = "insertApplicationServerMemorySettings";
  static final String DELETE_APP_MEM = "deleteApplicationServerMemorySettings";
  public static final String APP_SERVER_ID = "APP_SERVER_ID";
  //
  static final String ENABLE_UPDATE = "enableApplicationServer";
  static final String DISABLE_UPDATE = "disableApplicationServer";
  static final String GET_APPLICATION_SERVER_ID_BY_NAME = "getApplicationServerIdByName";
  static final String DELETE_APP_NET = "deleteApplicationServerNetworkAddresses";
  static final String INSERT_APP_NET = "insertApplicationServerNetworkAddress";
  static final String INSERT_APPLICATION_SERVER = "insertApplicationServer";
  static final String UPDATE_APPLICATION_SERVER = "updateApplicationServer";
  static final String SELECT_MAX_APPLICATION_SERVER_ID = "selectMaxApplicationServerId";
  static final String GET_APPLICATION_SERVER_MEMORY_SETTINGS = "getApplicationServerMemorySettings";
  //
  private static Log LOG;
  
  private Log getLog(){
    if (LOG == null) {
      LOG = LogFactory.getLog(this.getClass());
    }
    return LOG;
  }
  
  public TransferObject addServer(final Map serverData, final IReadWriteDataProvider p) {
    try {
      final List addresses = getServerNetworkAddresses(serverData);
      final Object serverName = getServerName(serverData);
      verifyServerNetworkAddresses(addresses);
      final Long[] id = new Long[1];
      p.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          verifyServerNameNotDuplicate(null, serverName, p);
          id[0] = (Long) p.queryObject(SELECT_MAX_APPLICATION_SERVER_ID);
          id[0] = new Long((id[0] == null ? 0 : id[0].longValue()) + 1);
          serverData.put(ApplicationServerServiceIntf.ID, id[0]);
          p.insert(INSERT_APPLICATION_SERVER, serverData, t);
          for (Iterator i = addresses.iterator(); i.hasNext();) {
            Map address = (Map) i.next();
            address.put(APP_SERVER_ID, id[0]);
            p.insert(INSERT_APP_NET, address, t);
          }
        }});
      return new TransferObject(id[0]);
    } catch (AbortTransactionException e) {
      getLog().error("Failed to add application server details", e);
      return new TransferObject(TransferObject.ERROR, e.getMessage());
    } catch (SQLException e) {
      getLog().error("Failed to add application server details", e);
      return new TransferObject(TransferObject.EXCEPTION, "DATABASE_ERROR");
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
  }
  
  public TransferObject updateServer(final Map serverData, final IReadWriteDataProvider p) {
    try {
      final Long id = getIdForExistingAppServer(serverData);
      final List addresses = getServerNetworkAddresses(serverData);
      final Object serverName = getServerName(serverData);
      verifyServerNetworkAddresses(addresses);
      p.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          verifyServerNameNotDuplicate(id, serverName, p);
          p.update(UPDATE_APPLICATION_SERVER, serverData, t);
          p.delete(DELETE_APP_NET, id, t);
          for (Iterator i = addresses.iterator(); i.hasNext();) {
            Map address = (Map) i.next();
            address.put(APP_SERVER_ID, id);
            p.insert(INSERT_APP_NET, address, t);
          }
        }});
      return new TransferObject();
    } catch (AbortTransactionException e) {
      getLog().error("Failed to update  application server details", e);
      return new TransferObject(TransferObject.ERROR, e.getMessage());
    } catch (SQLException e) {
      getLog().error("Failed to update  application server details", e);
      return new TransferObject(TransferObject.EXCEPTION, "DATABASE_ERROR");
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
  }

  private Long getIdForExistingAppServer(final Map serverData)
      throws TransferObjectException {
    Long id = (Long) serverData.get("ID");
    if (id == null) {
      throw new TransferObjectException(
          new TransferObject(TransferObject.ERROR, 
              "APP_SERVER_ID_MISSING"));
    }
    return id;
  }
  
  private void verifyServerNetworkAddresses(final List addresses)
      throws TransferObjectException {
    for (Iterator i = addresses.iterator(); i.hasNext();) {
      Map address = (Map) i.next();
      if (address.get(ApplicationServerServiceIntf.IP_ADDRESS) == null && address.get(ApplicationServerServiceIntf.HOST_NAME) == null){
        throw new TransferObjectException(new TransferObject(TransferObject.ERROR, "APP_SERVER_NETWORK_ADDRESS_MISSING"));
      }
    }
  }
  private Object getServerName(final Map serverData)
      throws TransferObjectException {
    final Object serverName = serverData.get(ApplicationServerServiceIntf.SERVER_NAME);
    if (serverName == null){
      throw new TransferObjectException(new TransferObject(TransferObject.ERROR, "APP_SERVER_NAME_REQUIRED"));
    }
    return serverName;
  }
  private List getServerNetworkAddresses(final Map serverData)
      throws TransferObjectException {
    Object o = serverData.get(ApplicationServerServiceIntf.NETWORK_ADDRESS_LIST);
    if (!(o instanceof List) || ((List)o).size() == 0){
      throw new TransferObjectException(new TransferObject(TransferObject.ERROR, "NETWORK_ADDRESS_REQUIRED"));
    }
    final List addresses = (List) o;
    return addresses;
  }
  private void verifyServerNameNotDuplicate(Long currentId, final Object serverName,
      final IReadWriteDataProvider p) throws SQLException,
      AbortTransactionException {
    Iterator query = p.query(IReadWriteDataProvider.LIST_RESULTS, GET_APPLICATION_SERVER_ID_BY_NAME, serverName);
    // If this is a new server then it can not be a dupe name at all
    if (currentId == null && query.hasNext()){
      throw new AbortTransactionException("APP_SERVER_NAME_DUPLICATE");
    } else if (query.hasNext()){
      // If it is an existing server then the name can of course be unchanged
      Long existing = (Long) query.next();
      if (!existing.equals(currentId)){
        throw new AbortTransactionException("APP_SERVER_NAME_DUPLICATE");
      }
    }
  }

  public TransferObject disableServer(final Long id, final IReadWriteDataProvider p) {
    return updateServerStatus(id, DISABLE_UPDATE, p);
  }
  
  public TransferObject enableServer(final Long id, final IReadWriteDataProvider p) {
    return updateServerStatus(id, ENABLE_UPDATE, p);
  }

  private TransferObject updateServerStatus(final Long id, final String statement,
      final IReadWriteDataProvider p) {
    try {
      p.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          p.update(statement, id, t);
        }});
      return new TransferObject();
    } catch (AbortTransactionException e) {
      // This is unreachable code
      return new TransferObject(TransferObject.EXCEPTION, "DATABASE_ERROR");
    } catch (SQLException e) {
      getLog().error("Failed to update  application server status", e);
      return new TransferObject(TransferObject.EXCEPTION, "DATABASE_ERROR");
    }
  }
  
  public TransferObject getServers(IReadWriteDataProvider privateProvider) {
    try {
      Iterator i = privateProvider.query(IReadWriteDataProvider.LIST_RESULTS, GET_APPLICATION_SERVERS, null);
      Map servers = new HashMap();
      while (i.hasNext()) {
        Map row = (Map) i.next();
        Long id = (Long) row.get(ApplicationServerServiceIntf.ID);
        if (!servers.containsKey(id)){
          Map server = new HashMap();
          copy(row, server, ApplicationServerServiceIntf.ID);
          copy(row, server, ApplicationServerServiceIntf.SERVER_NAME);
          copy(row, server, ApplicationServerServiceIntf.DESCRIPTION);
          copy(row, server, ApplicationServerServiceIntf.ACTIVE_SERVER);
          server.put(ApplicationServerServiceIntf.NETWORK_ADDRESS_LIST, new ArrayList());
          if (server.get(ApplicationServerServiceIntf.ACTIVE_SERVER).equals("N")){
            server.put(ApplicationServerServiceIntf.ACTIVE_SERVER + "_COLOR", "eaeaea");
            server.put(ApplicationServerServiceIntf.ACTIVE_SERVER + "_SELECTED_COLOR", "a7a2a2");
          }
          servers.put(id, server);
        }
        Map net = new HashMap();
        copy(row, net, ApplicationServerServiceIntf.IP_ADDRESS);
        copy(row, net, ApplicationServerServiceIntf.HOST_NAME);
        Map server = (Map) servers.get(id);
        List l = (List) server.get(ApplicationServerServiceIntf.NETWORK_ADDRESS_LIST);
        l.add(net);
      }
      List result = new ArrayList();
      result.addAll(servers.values());
      return new TransferObject(result);
    } catch (SQLException e) {
      getLog().error("Failed to retrieve application server list", e);
      return new TransferObject(TransferObject.EXCEPTION, "DATABASE_ERROR");
    }
  }

  private void copy(Map row, Map server, String key) {
    server.put(key, row.get(key));
  }
  
  private static String[] GC_TYPE_FLAGS = {
    "-XX:+UseSerialGC", //       Copy + MarkSweepCompact
    "-XX:+UseParNewGC", //       ParNew + MarkSweepCompact
    "-XX:+UseConcMarkSweepGC",// ParNew + ConcurrentMarkSweep
    "-XX:+UseParallelGC", //     PS Scavenge + PS MarkSweep
    "-XX:+UseParallelOldGC", //  PS Scavenge + PS MarkSweep
    "-XX:+CMSClassUnloadingEnabled", // Relates to ConcMarkSweep
    "-XX:+CMSPermGenSweepingEnabled", // Relates to ConcMarkSweep
  };
  private static final String MAX_HEAP_PREFIX = "-Xmx"; // -Xmx4000m
  private static final String MIN_HEAP_PREFIX = "-Xms"; // -Xms1000m
  private static final String MAX_PERM_PREFIX = "-XX:MaxPermSize="; //-XX:MaxPermSize=64m
  //
  private static final String MAX_HEAP = "MAX_HEAP";
  private static final String MIN_HEAP = "MIN_HEAP";
  private static final String PERM_GEN = "PERM_GEN";
  private static final String UNIT_SUFFIX = "_UNIT";
  private static final String SIZE_SUFFIX = "_SIZE";
  private static final String VERIFY_SUFFIX = "_VERIFY";
  private static final String[] FIELD_PREFIXES = {MAX_HEAP, MIN_HEAP, PERM_GEN};
  public TransferObject getServerStoredMemorySettings(Long serverId, IReadWriteDataProvider privateProvider) {
    try {
      Map data = (Map) privateProvider.queryObject(GET_APPLICATION_SERVER_MEMORY_SETTINGS, serverId);
      if (data == null) {
        data = new HashMap();
      }
      String memArgs = (String) data.get(MEMORY_FLAGS);
      if (memArgs == null){
        memArgs = "";
      }
      String verifiedMemArgs = (String) data.get(VERIFIED_FLAGS);
      if (verifiedMemArgs == null){
        verifiedMemArgs = "";
      }
      Map memoryData = getMemorySettings(memArgs.split(" "));
      Map verifiedData = getMemorySettings(verifiedMemArgs.split(" "));
      for (int i = 0; i < GC_TYPE_FLAGS.length; i++) {
        Boolean b = (Boolean) verifiedData.get(GC_TYPE_FLAGS[i]);
        memoryData.put(GC_TYPE_FLAGS[i] + VERIFY_SUFFIX, b);
      }
      for (int i = 0; i < FIELD_PREFIXES.length; i++) {
        Object o = verifiedData.get(FIELD_PREFIXES[i] + UNIT_SUFFIX);
        memoryData.put(FIELD_PREFIXES[i] + VERIFY_SUFFIX, new Boolean(o != null));
      }
      return new TransferObject(memoryData);
    } catch (SQLException e) {
      getLog().error("Failed to retrieve application server memory settings", e);
      return new TransferObject(TransferObject.EXCEPTION, "DATABASE_ERROR");
    }
  }
  
  public TransferObject setServerStoredMemorySettings(final Long serverId, Map data,
      final IReadWriteDataProvider p) {
    String flags = "";
    String verify = "";
    for (int i = 0; i < FIELD_PREFIXES.length; i++) {
      Number s = (Number) data.get(FIELD_PREFIXES[i] + SIZE_SUFFIX);
      Object u = data.get(FIELD_PREFIXES[i] + UNIT_SUFFIX);
      if (s != null && u != null) {
        String value = getFieldPrefixArgPrefix(FIELD_PREFIXES[i]) + s.intValue() + u.toString().toUpperCase();
        flags = flags + value + " ";
        Boolean b = (Boolean) data.get(FIELD_PREFIXES[i] + VERIFY_SUFFIX);
        if (b!=null && b.booleanValue()){
          verify = verify + value + " ";
        }
      }
    }
    for (int i = 0; i < GC_TYPE_FLAGS.length; i++) {
      Boolean b = (Boolean) data.get(GC_TYPE_FLAGS[i]);
      if (b != null && b.booleanValue()){
        flags = flags + GC_TYPE_FLAGS[i] + " ";
        b = (Boolean) data.get(GC_TYPE_FLAGS[i] + VERIFY_SUFFIX);
        if (b != null && b.booleanValue()){
          verify = verify + GC_TYPE_FLAGS[i] + " ";
        }
      }
    }
    final Map insert = new HashMap();
    insert.put(APP_SERVER_ID, serverId);
    insert.put(MEMORY_FLAGS, flags.trim());
    insert.put(VERIFIED_FLAGS, verify.trim());
    try {
      p.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          p.delete(DELETE_APP_MEM, serverId, t);
          p.insert(INSERT_APP_MEM, insert, t);
        }});
      return new TransferObject();
    } catch (AbortTransactionException e) {
      // This is unreachable code
      return new TransferObject(TransferObject.EXCEPTION, "DATABASE_ERROR");
    } catch (SQLException e) {
      getLog().error("Failed to store application server memory settings", e);
      return new TransferObject(TransferObject.EXCEPTION, "DATABASE_ERROR");
    }
  }
  
  private String getFieldPrefixArgPrefix(String fieldPrefix) {
    if (fieldPrefix.equals(MAX_HEAP)){
      return MAX_HEAP_PREFIX;
    }
    if (fieldPrefix.equals(MIN_HEAP)){
      return MIN_HEAP_PREFIX;
    }
    return MAX_PERM_PREFIX;
  }
  
  private Map getMemorySettings(String[] inputArguments) {
    Map data = new HashMap();
    for (int i = 0; i < GC_TYPE_FLAGS.length; i++) {
      if (ArrayUtil.indexOf(GC_TYPE_FLAGS[i], inputArguments) != -1){
        data.put(GC_TYPE_FLAGS[i], Boolean.TRUE);
      } else {
        data.put(GC_TYPE_FLAGS[i], Boolean.FALSE);
      }
    }
    String permSize = null;
    String maxHeap = null;
    String minHeap = null;
    for (String arg : inputArguments) {
      if (arg.contains(MAX_PERM_PREFIX)){
        permSize = arg.substring(MAX_PERM_PREFIX.length());
      } else if (arg.contains(MIN_HEAP_PREFIX)) {
        minHeap = arg.substring(MIN_HEAP_PREFIX.length());
      } else if (arg.contains(MAX_HEAP_PREFIX)) {
        maxHeap = arg.substring(MAX_HEAP_PREFIX.length());
      }
    }
    addMemoryData(permSize, PERM_GEN, data);
    addMemoryData(minHeap, MIN_HEAP, data);
    addMemoryData(maxHeap, MAX_HEAP, data);
    return data;
  }
  public TransferObject getServerCurrentMemorySettings() {
    return new TransferObject(getCurrentMemorySettings());
  }
  private Map getCurrentMemorySettings() {
    String[] inputArguments = getRuntimeInputArguments();
    Map data = getMemorySettings(inputArguments);
    return data;
  }

  protected String[] getRuntimeInputArguments() {
    RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    List<String> inputArguments = runtimeMXBean.getInputArguments();
    String[] args = new String[inputArguments.size()];
    inputArguments.toArray(args);
    return args;
  }

  private void addMemoryData(String permSize, String prefix, Map data) {
    if (permSize != null) {
      int count = getMemoryAmount(permSize);
      String unit = getMemoryUnit(permSize);
      data.put(prefix + SIZE_SUFFIX, new Long(count));
      data.put(prefix + UNIT_SUFFIX, unit);
    }
  }
  
  private String getMemoryUnit(String permSize) {
    return permSize.substring(permSize.length() - 1).toUpperCase();
  }

  private int getMemoryAmount(String permSize) {
    Pattern p = Pattern.compile("^(\\d+)\\D*");
    Matcher matcher = p.matcher(permSize);
    matcher.matches();
    return Integer.parseInt(matcher.group(1));
  }
  
  public TransferObject verifyCurrentServerMemory(IReadWriteDataProvider p){
    TransferObject serverIdTo = getCurrentServerId(p);
    if (serverIdTo.isFailed()) {
      if (serverIdTo.getMessage().equals(SERVER_NOT_DEFINED)) {
        return new TransferObject();
      } else {
        return serverIdTo;
      }
    }
    Long serverId = (Long) serverIdTo.getBeanHolder();
    Map currentMem = getCurrentMemorySettings();
    TransferObject assignedMemorySettings = getServerStoredMemorySettings(serverId, p);
    if (assignedMemorySettings.isFailed()){
      return assignedMemorySettings;
    }
    Map storedMem = (Map) assignedMemorySettings.getBeanHolder();
    List<String> badFlags = getFailedFlagVerification(currentMem, storedMem);
    List<String> badMins = getBadMemoryMinimums(currentMem, storedMem);
    badFlags.addAll(badMins);
    if (badFlags.isEmpty()) {
      return new TransferObject();
    } else {
      return new TransferObject(badFlags.toArray(), TransferObject.ERROR, ApplicationServerServiceIntf.VERIFICATION_FAILED);
    }
  }

  public TransferObject getCurrentServerId(IReadWriteDataProvider p) {
    TransferObject servers = getServers(p);
    if (servers.isFailed()) {
      return servers;
    }
    List serverList = (List) servers.getBeanHolder();
    List<String> addressList = new ArrayList<String>();
    try {
      addressList = getLocalNetworkAdddresses();
    } catch (SocketException e) {
      // This is virtually unreachable code
      getLog().error("Failed to retrieve network interface details for server", e);
      return new TransferObject(TransferObject.EXCEPTION, "NETWORK_INTERFACE_FAILED");
    }
    Long serverId = getServerIdForAddresses(serverList, addressList);
    if (serverId == null) {
      getLog().warn("This server is not defined based on available network interfaces: " + Strings.getListString(addressList, ", "));
      return new TransferObject(TransferObject.ERROR, SERVER_NOT_DEFINED);
    }
    return new TransferObject(serverId);
  }
  
  private List<String> getBadMemoryMinimums(Map currentMem, Map storedMem) {
    List<String> badMins = new ArrayList();
    for (int i = 0; i < FIELD_PREFIXES.length; i++) {
      String uf = FIELD_PREFIXES[i] + UNIT_SUFFIX;
      String sf = FIELD_PREFIXES[i] + SIZE_SUFFIX;
      String vf = FIELD_PREFIXES[i] + VERIFY_SUFFIX;
      if (storedMem.get(sf) != null) {
        String storedUnit = (String) storedMem.get(uf);
        Number storedSize = (Number) storedMem.get(sf);
        Boolean verified = (Boolean) storedMem.get(vf);
        String storedArg = getFieldPrefixArgPrefix(FIELD_PREFIXES[i]) + storedSize + storedUnit;
        if (currentMem.get(sf) == null && verified.booleanValue()) {
          badMins.add(storedArg);
          getLog().error("Memory parameter not set but assigned for verification at value '" + storedArg + "'");
        } else if (currentMem.get(sf) == null && !verified.booleanValue()) {
          getLog().info("Memory parameter not set but assigned value '" + storedArg + "' but is not set for verification");
        } else {
          String curUnit = (String) currentMem.get(uf);
          Number curSize = (Number) currentMem.get(sf);
          String curArg = getFieldPrefixArgPrefix(FIELD_PREFIXES[i]) + curSize + curUnit;
          if (curUnit.equals(storedUnit) && curSize.intValue() >= storedSize.intValue()){
            getLog().info("Memory parameter of '" + curArg + "' meets or exceeds value of '" + storedArg + "'");
          } else {
            if (verified.booleanValue()) {
              badMins.add(storedArg);
              getLog().error("Memory parameter of '" + curArg + "' failed verification against '" + storedArg + "'");
            } else {
              getLog().warn("Memory parameter of '" + curArg + "' failed verification against '" + storedArg + "' but is not set for verification");
            }
          }
        }
      }
    }
    return badMins;
  }
  private List<String> getFailedFlagVerification(Map currentMem, Map storedMem) {
    List<String> badFlags = new ArrayList();
    for (int i = 0; i < GC_TYPE_FLAGS.length; i++) {
      String f = GC_TYPE_FLAGS[i];
      Boolean stored = (Boolean) storedMem.get(f);
      Boolean current = (Boolean) currentMem.get(f);
      Boolean doVerify = (Boolean) storedMem.get(f + VERIFY_SUFFIX);
      if (!stored.equals(current)) {
        if (doVerify.booleanValue()) {
          badFlags.add(f);
          getLog().error("Memory flag '" + f + "' does not match stored memory settings");
        } else {
          getLog().warn("Memory flag '" + f + "' does not match stored memory settings but is not verified");
        }
      } else {
        getLog().info("Memory flag '" + f + "' matches stored memory settings");
      }
    }
    return badFlags;
  }
  private List<String> getLocalNetworkAdddresses()
      throws SocketException {
    AddressResolver resolver = new AddressResolver();
    List<String> addressList = new ArrayList<String>();
    List<NetworkInterface> interfaces = resolver.getNetworkingInterfaces();
    for (Iterator u = interfaces.iterator(); u.hasNext();) {
      NetworkInterface networkInterface = (NetworkInterface) u.next();
      String displayName = networkInterface.getDisplayName();
      List interfaceAddresses = new ArrayList();
      List<InetAddress> addressses = resolver.getInterfaceAddresses(networkInterface);
      for (Iterator i = addressses.iterator(); i.hasNext();) {
        InetAddress inetAddress = (InetAddress) i.next();
        interfaceAddresses.add(inetAddress.getHostAddress());
        interfaceAddresses.add(inetAddress.getHostName());
        interfaceAddresses.add(inetAddress.getCanonicalHostName());
      }
      getLog().info("Network interface " + displayName + " found with addresses " + Strings.getListString(interfaceAddresses, ", "));
      addressList.addAll(interfaceAddresses);
    }
    return addressList;
  }
  
  private Long getServerIdForAddresses(List serverList, List<String> addressList) {
    toUpperCase(addressList);
    for (Iterator i = serverList.iterator(); i.hasNext();) {
      Map server = (Map) i.next();
      List<String> serverConfiguredAddresses = getServerConfiguredAddresses(server);
      toUpperCase(serverConfiguredAddresses);
      for (Iterator iter = addressList.iterator(); iter
          .hasNext();) {
        String address = (String) iter.next();
        if (serverConfiguredAddresses.contains(address)){
          return (Long) server.get(ApplicationServerServiceIntf.ID);
        }
      }
    }
    return null;
  }
  private void toUpperCase(List<String> addressList) {
    for (int i = 0; i < addressList.size(); i++) {
      addressList.set(i, addressList.get(i).toUpperCase());
    }
  }
  
  public static List<String> getServerConfiguredAddresses(Map server) {
    List addresses = (List) server.get(ApplicationServerServiceIntf.NETWORK_ADDRESS_LIST);
    if(addresses==null) return Collections.EMPTY_LIST;
    List<String> serverAddressList = new ArrayList();
    for (Iterator iter = addresses.iterator(); iter.hasNext();) {
      Map m = (Map) iter.next();
      String host = (String) m.get(ApplicationServerServiceIntf.HOST_NAME);
      String ip = (String) m.get(ApplicationServerServiceIntf.IP_ADDRESS);
      if (host != null) {
        serverAddressList.add(host);
      }
      if (ip != null){
        serverAddressList.add(ip);
      }
    }
    return serverAddressList;
  }

  public String getSql() {
    return new AppServerSQL().getAppServerSQL();
  }
}
