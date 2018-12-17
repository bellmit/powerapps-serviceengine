package com.profitera.services.business.http;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.kollect.etl.util.StringUtils;
import com.powerapps.http.helpers.EventNameValidator;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.log.DefaultLogProvider;
import com.profitera.log.ILogProvider;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.services.system.lookup.ServiceLookup;

public class RequestDispatcherServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private ILogProvider log;
  
  public RequestDispatcherServlet() {
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    requestHandler(request, response);
  }
  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    requestHandler(request, response);
  }
  
  
  private void requestHandler(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {    
    
    //parseRequest();
    //validateRequest();
    TransferObject transfer = null;
    // set response headers
    response.setContentType("application/json");
    //response.addHeader("WWW-Authenticate", "Basic realm=\"User Visible Realm\"");
    response.setCharacterEncoding("UTF-8");
    final String eventName = request.getParameter("eventName");
    final String auth = request.getHeader("Authorization");
    System.out.println(auth);
    boolean isAuthorized  = (auth.equals("am9zaHVhOnZpdmFsZWU=")) ? true : false;
    if (!isAuthorized) {
      response.addHeader("WWW-Authenticate", "Basic realm=\"User Visible Realm\"");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error unauthorized");
      
      //response.setHeader(name, value);
      return;
    }
    EventNameValidator validateEvent = new EventNameValidator(new StringUtils());
    boolean isValid = validateEvent.isValid(eventName);
    
    if(!isValid) {
      Exception ex = new FileNotFoundException(MessageFormat.format("{0} does not exist", eventName));
      transfer = new TransferObject(ex.getMessage(), TransferObject.EXCEPTION,"EVENT_FILE_NOT_EXISTS" );
    }else {
      
      Map<String, Object> eventParameters = new HashMap<String, Object>();
      
      //build event parameter map
      eventParameters.put("phoneNo", request.getParameter("phoneNo"));
      eventParameters.put("accountNo", request.getParameter("accountNo"));
      eventParameters.put("agentId", request.getParameter("agentId"));
      eventParameters.put("WORK_LIST_ID", Long.parseLong(request.getParameter("workListId")));
      eventParameters.put("MODULE_NAME", request.getParameter("moduleName"));
      
     
      //validate request parameters
      //cast request params
      transfer = invokeEvent(eventParameters,eventName);
    }
    
    //deserialize to JSON
    String jsonText = new Gson().toJson(transfer);
    
    //write response to output stream
    ServletOutputStream outStream = response.getOutputStream();
    outStream.write(jsonText.getBytes());
    outStream.flush();
    outStream.close();
  }
  
  
  private TransferObject invokeEvent(final Map<String, Object> callParameters, final String eventName) {
    ServiceLookup system = LookupManager.getInstance().getLookup(LookupManager.SYSTEM);
    IMessageHandler stub = (IMessageHandler) system.getService("MessageHandler");
    Map<String, Object> arguments = new HashMap<String, Object>();
    arguments.putAll(callParameters);
    Object result = stub.handleMessage(this, "EventService", "sendEvent", new Class<?>[]{String.class, Map.class},
        new Object[]{eventName, arguments}, new HashMap<String, Object>());
    return (TransferObject) result;
  }
  
  

  private ILogProvider getLog() {
    if (log == null) {
      ILogProvider p = new DefaultLogProvider();
      p.register(new CallAgentLogClient());
      log = p;
    }
    return log;
  }

}
