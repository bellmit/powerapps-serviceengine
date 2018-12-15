package com.profitera.services.business.http;

import com.profitera.log.ILogClient;
import com.profitera.log.ILogProvider;
import com.profitera.log.ILogProvider.Level;

public class CallAgentLogClient implements ILogClient {

  public static final String CALL_DATA_ERROR = "CALL_DATA_ERROR";
  public static final String CALL_HTTP_HANDLED = "CALL_HTTP_HANDLED";

  @Override
  public void registerMessages(ILogProvider provider) {
    provider.registerMessage(this, CALL_DATA_ERROR, Level.E, "Error retrieving call data using {0} with {1}, {2}", 
        "An error occurred pre-processing call data being marshalled from an external CTI system.");
    provider.registerMessage(this, CALL_HTTP_HANDLED, Level.D, "Processed CTI request for {0}, command {1} in {2" + ILogClient.LONG_FORMAT + "}ms", 
        "Logging the duration of processing for a requestion from an external CTI system.");
  }

  @Override
  public String getName() {
    return "Call Agent";
  }

}
