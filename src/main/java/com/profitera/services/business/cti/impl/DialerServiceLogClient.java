package com.profitera.services.business.cti.impl;

import com.profitera.log.ILogClient;
import com.profitera.log.ILogProvider;
import com.profitera.log.ILogProvider.Level;

public class DialerServiceLogClient implements ILogClient {
  public static final String DIALER_SYSTEM_ERROR = "DIALER_SYSTEM_ERROR";
  @Override
  public void registerMessages(ILogProvider provider) {
    provider.registerMessage(this, DIALER_SYSTEM_ERROR, Level.E, "Error occured in CTI system",
        "An internal error occurred in the external CTI system.");
  }
  @Override
  public String getName() {
    return "Dailer";
  }
}
