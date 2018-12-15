package com.profitera.services.system.lookup;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import com.profitera.services.business.audittrail.Audit;
import com.profitera.services.business.http.WebServerService;
import com.profitera.services.system.lookup.impl.HTTPRemoteServiceRequest;
import com.profitera.services.system.lookup.impl.ServerToServerSessionBuilder;
import com.profitera.util.Strings;
import com.profitera.util.interceptor.Interceptor;

public class RemoteServiceRequestFactory {

  private final Properties properties;
  private final String overrideUser;

  public RemoteServiceRequestFactory(String overrideUser, Properties props) {
    this.overrideUser = overrideUser;
    this.properties = props;
  }

  public IRemoteServiceRequest getRequest(String serverAddress,
      String serviceName) throws MalformedURLException {
    Object lookupItem = getLocalService("WebServerService");
    if (lookupItem == null) {
      throw new RuntimeException("No web server found for server-to-server communication");
    } else {
      WebServerService wss = (WebServerService) lookupItem;
      int port = wss.getPort();
      URL u = getHttpUrlForAddress(serverAddress, port, wss.isSecureConnection());
      return new HTTPRemoteServiceRequest(u, serviceName, getClientSession(), properties);
    }
  }

  private URL getHttpUrlForAddress(String serverAddress, int port, boolean isSecure)
      throws MalformedURLException {
    // Old RMI references will be in the following syntax:
    //  "//hostname:port/", the leading and trailing slash(es) may not be present
    //    //hostname:100/
    // or hostname:100/
    // or //hostname:100
    serverAddress = serverAddress.trim();
    serverAddress = Strings.removePrefix("//", serverAddress);
    serverAddress = Strings.removePrefix("http://", serverAddress);
    serverAddress = Strings.removePrefix("https://", serverAddress);
    serverAddress = serverAddress.replaceAll(":\\d+/{0,1}$", "");
    // If we did not have a port match I need to remove the trailing /
    if (serverAddress.endsWith("/")) {
      serverAddress = serverAddress.substring(0, serverAddress.length() - 1);
    }
    if (isSecure) {
      serverAddress = "https://" + serverAddress;
    } else {
      serverAddress = "http://" + serverAddress;
    }
    URL u = new URL(serverAddress + ":" + port);
    return u;
  }

  private Object getLocalService(String localService) {
    Object lookupItem = LookupManager.getInstance().getLookupItem(LookupManager.BUSINESS, localService);
    return lookupItem;
  }

  private Interceptor getClientSession(){
    String user = overrideUser;
    if (user == null) {
      user = Audit.getSessionUser();
    }
   return new ServerToServerSessionBuilder().getInterceptor(user, getLocalService("LoginService"));
  }

}
