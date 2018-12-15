package com.profitera.services.system.lookup;

import com.profitera.descriptor.business.TransferObject;

public interface IRemoteServiceRequest {
  public TransferObject execute(IRemoteRunnable r) throws RemoteConnectionException;
  public String getServerAddress();

}
