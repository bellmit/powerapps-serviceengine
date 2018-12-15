package com.profitera.services.system.lookup;

import com.profitera.descriptor.business.TransferObject;

public interface IRemoteRunnable {
  public TransferObject run(Object serviceInstance);

}
