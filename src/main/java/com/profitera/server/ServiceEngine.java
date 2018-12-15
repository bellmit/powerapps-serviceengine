package com.profitera.server;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.PropertyResourceBundle;

import com.profitera.datasource.DataSourceUtil;
import com.profitera.datasource.IDataSourceConfiguration;
import com.profitera.datasource.IDataSourceConfigurationSet;
import com.profitera.deployment.rmi.ApplicationServerServiceIntf;
import com.profitera.deployment.rmi.EventServiceIntf;
import com.profitera.deployment.rmi.ScheduleListenerIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.event.EventLogClient;
import com.profitera.event.NoSuchEventException;
import com.profitera.log.DefaultLogProvider;
import com.profitera.log.ILogProvider;
import com.profitera.server.impl.ServiceEngineLog;
import com.profitera.services.Service;
import com.profitera.services.business.BusinessService;
import com.profitera.services.business.http.DefaultMessageHandler;
import com.profitera.services.business.http.IMessageHandler;
import com.profitera.services.business.http.WebServerService;
import com.profitera.services.business.http.impl.MessageHandlerFactory;
import com.profitera.services.business.login.LoginService;
import com.profitera.services.business.login.MapLoginService;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.services.system.lookup.ServiceLookup;
import com.profitera.util.Strings;
import com.profitera.util.Utilities;
import com.profitera.util.interceptor.Interceptor;
import com.profitera.util.reflect.Reflect;
import com.profitera.util.reflect.ReflectionException;

public final class ServiceEngine {
  private static final int DEFAULT_RMI_PORT = 1099;

  public class EventServiceStub extends BusinessService implements EventServiceIntf, ScheduleListenerIntf {
    public final TransferObject sendEvent(String name, Map<String, Object> arguments) {
      ServiceLookup system = LookupManager.getInstance().getLookup(LookupManager.SYSTEM);
      IMessageHandler stub = (IMessageHandler) system.getService("MessageHandler");
      Object result = stub.handleMessage(this, "EventService", "sendEvent", new Class<?>[]{String.class, Map.class},
          new Object[]{name, arguments}, new HashMap<String, Object>());
      return (TransferObject) result;
    }
    @Override
    public void invokeScheduledEvent(String id) {
      String param;
      String eventName;
      if (id.contains("/")) {
        param = id.substring(id.lastIndexOf("/") + 1);
        eventName = id.substring(0, id.lastIndexOf("/"));
      } else {
        param = null;
        eventName = id;
      }
      HashMap<String, Object> arguments = new HashMap<String, Object>();
      arguments.put("SCHEDULE_PARAMETER", param);
      TransferObject t = sendEvent(eventName, arguments);
      if (t.isFailed()) {
        DefaultLogProvider log = new DefaultLogProvider();
        log.register(new EventLogClient());
        Object errorArguments = t.getBeanHolder();
        if (errorArguments instanceof Object[]) {
          errorArguments = Arrays.asList((Object[])errorArguments);
        }
        log.emit(EventLogClient.SCHEDULED_EVENT_ERROR, eventName, param, t.getMessage(), errorArguments);
      }
    }

  }
  private static class MessageHandlerServiceStub implements Service, IMessageHandler {
    private final DefaultMessageHandler handler;
    private MessageHandlerServiceStub(DefaultMessageHandler h) {
      this.handler = h;
    }
    @Override
    public Object handleMessage(Object lookupItem, String serviceName,
        String methodName, Class<?>[] paramTypes, Object[] args, Map<String, Object> context) {
      return handler.handleMessage(lookupItem, serviceName, methodName, paramTypes, args, context);
    }
  }

  private static final String VERIFYMEMORYSETTINGS = "verifymemorysettings";
  private static final String REMOTE_SERVICES_CONFIG = "RemoteServices.properties";
  public static final String SERVER_CONFIG = "server.properties";
  private static IDataSourceConfigurationSet dataSources;
  private static String[] propertyFilePaths = new String[] {SERVER_CONFIG};
  private static IDatabasePropertyProvider dbPropProvider;
  private static Interceptor[] serviceInterceptors;
  //
  private static Properties config;

  @SuppressWarnings("unchecked")
  public static Interceptor[] getServiceInterceptors() {
    if (serviceInterceptors == null) {
      serviceInterceptors = loadInterceptors(new Class[] {
          com.profitera.services.business.login.ServerSession.class,
          com.profitera.services.business.audittrail.Audit.class,
          com.profitera.services.system.license.LicenseInterceptor.class});
    }
    return serviceInterceptors;
  }

  private static DefaultLogProvider log;
  static {
    log = new DefaultLogProvider();
    log.register(new ServiceEngineLog());
  }

  private ServiceEngine(String[] propFilePaths) throws Throwable {
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread arg0, Throwable arg1) {
        getLog().emit(ServiceEngineLog.UNCAUGHT_SERVER_ERROR, arg1, arg0.getName());
      }
    });

    if (propFilePaths != null && propFilePaths.length > 0) {
      propertyFilePaths = propFilePaths;
    }
    config = loadAllProperties();
    final LookupManager lookup = LookupManager.getInstance();
    final ServiceLookup business = lookup.getLookup(LookupManager.BUSINESS);
    if (business.getFailedServices().contains("com.profitera.services.business.http.WebServerService")) {
      throw new IllegalArgumentException("Web server startup failed, check logs for configuration error details");
    }
    final LoginService login = getLoginServiceImplementation();
    IDataSourceConfigurationSet availableDataSources = getDataSourceConfigurations();
    login.setDataSourceConfigurations(availableDataSources);
    final String[] serviceNames = business.getAllServiceNames();
    for (int i = 0; i < serviceNames.length; i++) {
      final String serviceName = serviceNames[i];
      try {
        final BusinessService service = (BusinessService) business.getService(serviceName);
        service.setDataSourceConfigurations(availableDataSources);
        // add singleton service to lookup
        getLog().emit(ServiceEngineLog.BOUND, serviceName);
      } catch (Exception ex) {
        getLog().emit(ServiceEngineLog.NOT_BOUND, ex, serviceName);
      }
    }
    EventServiceStub es = new EventServiceStub();
    business.setService("EventService", es);
    // Place LoginService into Business ServiceLookup LAST
    business.setService("LoginService", login);
    WebServerService web = (WebServerService) business.getService("WebServerService");
    if (web == null) {
      web = (WebServerService) business.getService("WebServer");
    }
    try {
      web.startServing();
    } catch (Exception e) {
      throw new IllegalArgumentException("Web server startup failed, check logs for configuration error details", e);
    }
    DefaultMessageHandler defaultMessageHandler = initMessageHandler(login);
    verifyServerMemorySettings(business);
    try {
      Object result = defaultMessageHandler.handleMessage(es, "EventService", "sendEvent", new Class<?>[]{String.class, Map.class},
          new Object[]{"server.start", new HashMap<String, Object>()}, new HashMap<String, Object>());
      if (result instanceof TransferObject && ((TransferObject)result).isFailed()) {
        if (!"NO_SUCH_EVENT".equals(((TransferObject)result).getMessage())) {
        throw new IllegalArgumentException(((TransferObject)result).getMessage());
        }
      }
    } catch (NoSuchEventException e) {
      
    }
    getLog().emit(ServiceEngineLog.STARTED);
  }

  public static DefaultMessageHandler initMessageHandler(final LoginService login) {
    DefaultMessageHandler defaultMessageHandler = new MessageHandlerFactory().build((MapLoginService) login);
    ServiceLookup system = LookupManager.getInstance().getLookup(LookupManager.SYSTEM);
    system.setService("MessageHandler", new MessageHandlerServiceStub(defaultMessageHandler));
    return defaultMessageHandler;
  }

  private static ILogProvider getLog() {
    return log;
  }

  public static IDataSourceConfigurationSet getDataSourceConfigurations() {
    if (dataSources == null) {
      dataSources = buildDataSourceConfigurationSet();
    }
    return dataSources;
  }

  public static IDataSourceConfigurationSet buildDataSourceConfigurationSet() {
    Properties props = getPropertiesFromFileSource();
    return DataSourceUtil.getDataSourceConfigurations(props);
  }

  /**
   * Now verify server memory configuration if the service is enabled
   * @param business
   */
  private void verifyServerMemorySettings(final ServiceLookup business) {
    Service app = business.getService("ApplicationServerService");
    if (app != null) {
      String verify = getProp(VERIFYMEMORYSETTINGS, null);
      if (verify == null || !verify.equals("false")) {
        ApplicationServerServiceIntf appService = (ApplicationServerServiceIntf) app;
        TransferObject verificationResult = appService.verifyCurrentServerMemory();
        if (verificationResult.isFailed()) {
          String list = "";
          if (verificationResult.getMessage().equals(ApplicationServerServiceIntf.VERIFICATION_FAILED)) {
            Object[] flags = (Object[]) verificationResult.getBeanHolder();
            list = ", the following settings are in violation: " + Strings.getListString(flags, ", ");
          }
          getLog().emit(ServiceEngineLog.VERIFY_MEM_FAIL, list, VERIFYMEMORYSETTINGS);
          System.exit(-1);
        }
      } else {
        getLog().emit(ServiceEngineLog.NO_VERIFY_MEM);
      }
    }
  }

  private LoginService getLoginServiceImplementation() {
    String impl = getProp("system.loginservice", LoginService.class.getName());
    try {
      getLog().emit(ServiceEngineLog.BUILD_LOGIN, impl);
      return (LoginService) Reflect.invokeConstructor(impl, null, null);
    } catch (Throwable t) {
      getLog().emit(ServiceEngineLog.BUILD_LOGIN_FAILURE, t, impl, "system.loginservice");
      throw new RuntimeException(t);
    }
  }

  public static int getServerPort() {
    final Properties props = Utilities.loadPropertyFile(REMOTE_SERVICES_CONFIG);
    String rmiport = props.getProperty("RMI-Port");
    if (rmiport == null) {
      return DEFAULT_RMI_PORT;
    }
    return Integer.parseInt(rmiport);
  }

  public static String getBaseServerURL() {
    final Properties props = Utilities.loadPropertyFile(REMOTE_SERVICES_CONFIG);
    String rmiserver = props.getProperty("RMI-Server");
    int port = getServerPort();
    // if not empty convert format to ":portnum/"
    String rmiport = ":" + port + "/";
    // if not empty convert format to "//hostname" or "//hostname/"
    if (rmiserver == null) {
      rmiserver = "//localhost";
    } else {
      rmiserver = "//" + rmiserver;
    }
    return rmiserver + rmiport;
  }

  private static Properties loadAllProperties() {
    Properties allFileProps = getPropertiesFromFileSource();
    Properties dbProps = loadDbProps(allFileProps);
    Properties allProps = new Properties();
    if (dbProps != null) {
      allProps.putAll(dbProps);
    }
    allProps.putAll(allFileProps);
    return allProps;
  }

  private static Properties getPropertiesFromFileSource() {
    Properties[] fileProps = new Properties[propertyFilePaths.length];
    for (int i = 0; i < fileProps.length; i++) {
      fileProps[i] = Utilities.loadOrExit(propertyFilePaths[i]);
    }
    Properties allFileProps = new Properties();
    for (int i = 0; i < fileProps.length; i++) {
      if (fileProps[i] != null) {
        allFileProps.putAll(fileProps[i]);
      }
    }
    return allFileProps;
  }

  private static Properties loadDbProps(Properties fileProps) {
    IDataSourceConfigurationSet confSet = getDataSourceConfigurations();
    IDataSourceConfiguration defaultDataSource = confSet.getDefaultDataSource();
    if (dbPropProvider == null) {
      if (fileProps != null) {
        String impl = fileProps.getProperty("system.databasepropertyprovider");
        if (impl != null) {
          try {
            IDatabasePropertyProvider d = (IDatabasePropertyProvider) Reflect.invokeConstructor(impl, null, null);
            dbPropProvider = d;
          } catch (ReflectionException e) {
            throw new RuntimeException("Unable to load configured DB property provider: " + impl, e);
          }
        } else {
          // Here we have a dummy provider but for backwards-compatibility
          // reasons
          // we need to make a database connection so we can fail if there is a
          // connectivity issue.
          dbPropProvider = new IDatabasePropertyProvider() {
            public Properties queryProperties(IDataSourceConfiguration c) {
              return new Properties();
            }
          };
          try {
            getLog().emit(ServiceEngineLog.DB_CONNECTION_CHECK, defaultDataSource.getName());
            Connection conn = defaultDataSource.getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            getLog().emit(ServiceEngineLog.DB_VERSION, meta.getDatabaseProductVersion());
            getLog().emit(ServiceEngineLog.DB_DRIVER_VERSION, meta.getDriverVersion());
          } catch (SQLException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
    return dbPropProvider.queryProperties(defaultDataSource);
  }

  public static boolean refreshConfig() {
    getLog().emit(ServiceEngineLog.REFRESH_PROPS);
    try {
      config = loadAllProperties();
      return true;
    } catch (Exception ex) {
      getLog().emit(ServiceEngineLog.REFRESH_PROPS_FAIL, ex);
      return false;
    }
  }

  public static Properties getConfig(boolean refresh) {
    if (refresh) {
      refreshConfig();
    } else if (config == null) {
      config = getPropertiesFromFileSource();
    }
    return config;
  }

  public static PropertyResourceBundle getConfigAsResourceBundle() {
    return Utilities.loadPropertyFileAsResourceBundle(SERVER_CONFIG);
  }

  public static int getIntProp(String prop, int defaultVal) {
    try {
      return Integer.parseInt(getConfig(false).getProperty(prop).trim());
    } catch (RuntimeException e) {
      return defaultVal;
    }
  }

  public static String getProp(String prop, String defaultVal) {
    try {
      return getConfig(false).getProperty(prop, defaultVal);
    } catch (RuntimeException e) {
      return defaultVal;
    }
  }

  public static String getProp(String prop) {
    try {
      return getConfig(false).getProperty(prop);
    } catch (RuntimeException e) {
      return null;
    }
  }

  private static Interceptor[] loadInterceptors(final Class<? extends Interceptor>[] classes) {
    List<Interceptor> siList = new ArrayList<Interceptor>();
    Interceptor[] interceptors = null;
    if (classes != null) {
      for (int i = 0; i < classes.length; i++) {
        try {
          Class<? extends Interceptor> intClass = classes[i];
          if (Interceptor.class.isAssignableFrom(intClass)) {
            siList.add((Interceptor) intClass.newInstance());
          }
        } catch (Exception e) {
          String className = null;
          if (classes[i] != null) {
            className = classes[i].getName();
          }
          getLog().emit(ServiceEngineLog.INTERCEPTOR_ERROR, e, className);
          throw new IllegalArgumentException("Unable to load interceptor: " + classes[i].getName(), e);
        }
      }
      interceptors = (Interceptor[]) siList.toArray(new Interceptor[siList.size()]);
    }
    return interceptors;
  }

  public static void main(final String[] args) throws Throwable {
    try {
      new ServiceEngine(args);
    } catch (Throwable t) {
      getLog().emit(ServiceEngineLog.START_FAILED, t);
      System.exit(-1);
    }
  }
}
