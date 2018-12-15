package com.profitera.services.system;

import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.lookup.LookupManager;

/**
 * <p>Title: Profitera Application Suite</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Profitera Corporation Sdn. Bhd.</p>
 * @author Jambugesvarar Marimuthu
 * @version 1.0
 */

public class SystemService implements com.profitera.services.Service
{
  protected IReadWriteDataProvider getReadWriteProvider() {
    final IReadWriteDataProvider provider = (IReadWriteDataProvider) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "ReadWriteProvider");
    return provider;
  }
}