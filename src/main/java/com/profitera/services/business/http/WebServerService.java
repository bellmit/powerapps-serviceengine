package com.profitera.services.business.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.Servlet;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.HashSessionManager;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.SessionHandler;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.thread.QueuedThreadPool;

import com.profitera.map.MapUtil;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.BusinessService;
import com.profitera.services.system.cti.ICallAgentEventListener;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.PasswordGenerator;
import com.profitera.util.Utilities;
import com.profitera.util.reflect.Reflect;

public class WebServerService extends BusinessService {
	private static final int FIVE_MINUTES = 1000 * 60 * 5;
	static final String SERVICE = "webserverservice";
	private Server server = new Server();
	private int currentPort;

	public WebServerService() {
		System.setProperty("org.mortbay.log.class", JettyLogWrapper.class.getName());
		boolean isSecureConnection = isSecureConnection();
    currentPort = ServiceEngine.getIntProp(SERVICE + ".port",	isSecureConnection ? 443 : 80);
	}

  private String getKeystore() {
    String keystore = ServiceEngine.getProp(SERVICE + ".keystore");
    return keystore;
  }

	public void start() {
		try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException("Unable to start WebServerService webserver on port " + getPort(), e);
		}
	}

	public void stop() {
		try {
			server.stop();
		} catch (Exception e) {
			throw new RuntimeException("Unable to stop WebServerService webserver");
		}
	}

	public boolean isSecureConnection() {
	  String keystore = getKeystore();
		return keystore != null && keystore.trim().length() > 0;
	}
	
	public int getPort() {
	  return currentPort;
	}
	
	private String getPassword() throws IOException{
		String passwordFile = ServiceEngine.getProp(SERVICE + ".password");
		String keyFile = ServiceEngine.getProp(SERVICE + ".passwordkey");
		BufferedReader reader = new BufferedReader(new FileReader(passwordFile));
		String encryptedPassword = reader.readLine();
		reader.close();
		return PasswordGenerator.reverse(keyFile, encryptedPassword);
	}

  public void startServing() {
    try {
      String keystore = getKeystore();
      if (isSecureConnection()) {
        SslSocketConnector ssl = new SslSocketConnector();
        ssl.setPort(getPort());
        ssl.setKeystore(keystore);
        ssl.setKeyPassword(getPassword());
        server.addConnector(ssl);
      } else {
        SocketConnector sc = new SocketConnector();
        sc.setPort(getPort());
        server.addConnector(sc);
      }
      String warPath = ServiceEngine.getProp(SERVICE + ".webservicewar", "../lib/event.war");
      if (new File(warPath).exists()) {
        WebAppContext wac = new WebAppContext();
        wac.setContextPath("/event");
        wac.setWar(warPath);
        server.addHandler(wac);
      }
      String enableWebClient = ServiceEngine.getProp(SERVICE + ".webclient", "false");
      if (enableWebClient.equals("true")) {
        addWebClientServlets();
      }
      Object o = LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "CtiService");
      if (o instanceof ICallAgentEventListener) {
        addCtiRelayServlet((ICallAgentEventListener)o);
      }
      
      //add rest template servlet
      addRequestDispatcherServlet();

      //
      final Context context = new Context();
      server.addHandler(context);
      String path = ServiceEngine.getProp(SERVICE + ".path", "../webstart");
      context.setResourceBase(path);
      String hidedir = ServiceEngine.getProp(SERVICE + ".hidedirectories", null);
      ServletHolder holder = new ServletHolder(new DefaultServlet());
      if (hidedir != null) {
        holder.setInitParameter("dirAllowed", hidedir.equals("false") + "");
      }
      context.addServlet(holder, "/");
      context.addServlet(new ServletHolder(new InterfaceServlet()), "/interface/*");
      context.addServlet(new ServletHolder(new ServiceServlet()), "/service/*");
      //
      String servletConfigList = ServiceEngine.getProp(SERVICE+".servletlist");
      if(servletConfigList!=null && !servletConfigList.trim().equals("")){
        String[] servletConfig = servletConfigList.split(";");
        for(int i=0;i<servletConfig.length;i++){
          Properties props = Utilities.load(servletConfig[i]);
          String implementor = null;
          String pathSpec = null;
          Map<String, String> parameters = new HashMap<String, String>();
          for(Map.Entry<Object, Object> m : props.entrySet()){
            String key = (String)m.getKey();
            String value = (String)m.getValue();
            if(key.equals("CLASS")) implementor = value;
            if(key.equals("PATH")) pathSpec = value;
            if(key.startsWith("PARAMETER.")){
              key = key.substring("PARAMETER.".length());
              parameters.put(key, value);
            }
          }
          Servlet clazz = (Servlet)Reflect.invokeConstructor(implementor, null, null);
          ServletHolder servlet = new ServletHolder(clazz);
          servlet.setInitParameters(parameters);
          context.addServlet(servlet, pathSpec);
        }
      }     
      
      QueuedThreadPool pool = new QueuedThreadPool();
      pool.setName("http-client");
      ThreadPoolPropertyMonitor.IPropertyProvider prop = new ServiceEnginePropertyProvider();
      new ThreadPoolPropertyMonitor(prop, pool, FIVE_MINUTES);
      server.setThreadPool(pool);
      server.start();
    } catch (Exception e) {
      throw new RuntimeException("Unable to start WebServerService on port "
          + getPort(), e);
    }    
  }

  private void addCtiRelayServlet(ICallAgentEventListener o) {
    final Context context = new Context();
    context.setContextPath("/cti/call");
    // No sessions, stateless
    //context.setSessionHandler(new SessionHandler(new HashSessionManager()));
    server.addHandler(context);
    Servlet servlet = new CallAgentEventListeningServlet(o);
    context.addServlet(new ServletHolder(servlet), "/");

  }
  
  
  private void addRequestDispatcherServlet() {
    final Context context = new Context();
    context.setContextPath("/api/v1.0");
    server.addHandler(context);
    Servlet servlet = new RequestDispatcherServlet();
    context.addServlet(new ServletHolder(servlet), "/");
  }
  
  
  private void addRequestDispatcherServlet2() {
    String webappDirLocation = "../webapp/";
    final WebAppContext context = new WebAppContext();
    context.setContextPath("/");
    context.setDescriptor(webappDirLocation+"/WEB-INF/web.xml");
    context.setResourceBase(webappDirLocation);
    context.setParentLoaderPriority(true);
    server.addHandler(context);
  }

  private void addWebClientServlets() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
      InvocationTargetException {
    String yuiPath = ServiceEngine.getProp(SERVICE + ".yuipath", null);
    File webformpath = new File(ServiceEngine.getProp(SERVICE + ".webformpath", "../config/forms"));
    if (!(webformpath.exists() && webformpath.isDirectory())) {
      throw new IllegalArgumentException("Server property " + SERVICE + ".webformpath" + " points to illegal path, should be directory containing wfrm files");
    }
    final Context context = new Context();
    // Sessions are saved as Profitera Web Client sessions
    context.setInitParams(MapUtil.create("org.mortbay.jetty.servlet.SessionCookie", "PWCSESSION")); 
    context.setContextPath("/app");
    context.setSessionHandler(new SessionHandler(new HashSessionManager()));
    server.addHandler(context);
    Class<?> webClientServletClass = Class.forName("com.profitera.services.business.http.WebClientServlet");
    
    Servlet webClientServlet = (Servlet) webClientServletClass.getConstructors()[0].newInstance(webformpath);
    context.addServlet(new ServletHolder(webClientServlet), "/");
    if (yuiPath != null) {
      final Context yuicontext = new Context();
      yuicontext.setContextPath("/yui");
      server.addHandler(yuicontext);
      YUI3ComboLoader yuiComboServlet = new YUI3ComboLoader(new File(yuiPath));
      yuicontext.addServlet(new ServletHolder(yuiComboServlet), "/");
    }
  }
}
