package com.profitera.services.system.lookup.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Properties;

import com.profitera.client.http.AddressLookup;
import com.profitera.client.http.HTTPRemoteLookup;
import com.profitera.client.http.IAddressLookup;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.system.lookup.IRemoteRunnable;
import com.profitera.services.system.lookup.IRemoteServiceRequest;
import com.profitera.services.system.lookup.RemoteConnectionException;
import com.profitera.util.interceptor.Interceptor;

public class HTTPRemoteServiceRequest implements IRemoteServiceRequest {
  private IAddressLookup addr;
  private final String serviceName;
  private final Interceptor clientSession;
  private final Properties props;
  
  public HTTPRemoteServiceRequest(URL server, String serviceName, Interceptor interceptor, Properties props) {
    this.serviceName = serviceName;
    this.addr = new AddressLookup(server, 3, 100);
    this.clientSession = interceptor;
    this.props = props;
  }
  
  public TransferObject execute(IRemoteRunnable r)
      throws RemoteConnectionException {
    HTTPRemoteLookup remoteLookup = new HTTPRemoteLookup(){
      {
        setProperties(props);
      }

      @Override
      protected IAddressLookup getAddressLookup() {
        return addr;
      }

      @Override
      protected Interceptor getSession() {
        return clientSession;
      }
    };
    Object service = null;
    try {
      service = remoteLookup.getService(serviceName);
    } catch (MalformedURLException e) {
      throw new RemoteConnectionException(e);
    } catch (RemoteException e) {
      throw new RemoteConnectionException(e);
    } catch (NotBoundException e) {
      throw new RemoteConnectionException(e);
    }
    return r.run(service);
  }

  public String getServerAddress() {
    return addr.getRemoteAddress().toExternalForm();
  }

}
