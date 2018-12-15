package com.profitera.services.business.http.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import com.profitera.event.IRequestInformation;
import com.profitera.log.DefaultLogProvider;
import com.profitera.log.ILogProvider;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.dsp.DspLogClient;
import com.profitera.services.business.dsp.DspMessagingService;
import com.profitera.services.business.login.ISingleSignOnImplementation;
import com.profitera.services.system.dsp.handler.DSPHandlerException;
import com.profitera.silverlake.ISilverLakeMessagingProvider;
import com.profitera.silverlake.InvalidSilverLakeMessageConfiguration;
import com.profitera.silverlake.SilverLakeMessageException;
import com.profitera.util.reflect.Reflect;

final class DefaultSilverLakeMessagingProvider implements ISilverLakeMessagingProvider {

  private DspMessagingService dspService;
  private DefaultLogProvider log;
  private DspMessagingService getService() {
    if (dspService == null) {
      dspService = new DspMessagingService();
    }
    return dspService;
  }
  private ILogProvider getLog() {
    if (log == null) {
      log = new DefaultLogProvider();
      log.register(new DspLogClient());
    }
    return log;
  }
  
  @Override
  public Map<String, Object> send(String transactionCode, Map<String, Object> data, Document header, Document body) throws SilverLakeMessageException, InvalidSilverLakeMessageConfiguration {
    Map<String, Object> map;
    try {
      map = getService().sendRequest(transactionCode, data, header, body, -1, null);
    } catch (IOException e) {
      throw new SilverLakeMessageException(e);
    } catch (DSPHandlerException e) {
      throw new InvalidSilverLakeMessageConfiguration (transactionCode, e.getMessage(), e);
    }
    if(!map.get("RESPONSE_RESULT_CODE").equals("AA")){
      //TODO: RESPONSE_ERROR_CODE_1 of MBM2001 is treated specially by legacy version, I am guessing this is "no records"
      getLog().emit(DspLogClient.DSP_RETURNED_ERROR, transactionCode, map.get("RESPONSE_ERROR_CODE_1"), map.get("RESPONSE_REASON_FOR_CODE_1"));
    }
    return map;
  }
  
  @Override
  public List<Map<String, Object>> getFieldList(String fileName) throws InvalidSilverLakeMessageConfiguration {
    try {
      return getService().getFieldList(fileName);
    } catch (IOException e) {
      throw new InvalidSilverLakeMessageConfiguration(fileName, e.getMessage(), e);
    }
  }
  @Override
  public String[] getAvailableTransactions(String messageType) {
    return getService().getAvailableTransactions(messageType);
  }
  @Override
  public List<Map<String, Object>> getAccessItNowUsers(Date from, Date to, String organization, String branch, IRequestInformation req) throws SilverLakeMessageException {
    try {
      ISingleSignOnImplementation i = (ISingleSignOnImplementation) Reflect.invokeConstructor("com.profitera.services.business.sso.SilverLakeAccessItNowSingleSignOn", null, null);
      i.setProperties(ServiceEngine.getConfig(true));
      return i.getBulkUserInformation(from, to, organization, branch, req.getUser(), req.getSessionAttachedData());
    } catch (Exception e) {
      throw new SilverLakeMessageException(e);
    }
  }
}