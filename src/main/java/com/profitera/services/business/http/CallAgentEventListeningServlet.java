package com.profitera.services.business.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.io.StreamUtil;
import com.profitera.log.DefaultLogProvider;
import com.profitera.log.ILogProvider;
import com.profitera.services.business.http.impl.ServletUtil;
import com.profitera.services.system.cti.ICallAgentEventListener;
import com.profitera.services.system.cti.ICallAgentEventListener.CallAction;
import com.profitera.services.system.cti.ICallAgentEventListener.Command;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.services.system.lookup.ServiceLookup;
import com.profitera.util.Strings;

public class CallAgentEventListeningServlet extends HttpServlet {
  private static final String CTI_HANDLE_CALL_DATA = "cti.handleCallData";
  private static final long serialVersionUID = 1L;
  private final ICallAgentEventListener listener;
  private ILogProvider log;
  public CallAgentEventListeningServlet(ICallAgentEventListener listener) {
    this.listener = listener;
  }
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    processRequest(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    processRequest(req, resp);
  }

  private void processRequest(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    final long requestStartTime = System.currentTimeMillis();
    ServletUtil.plainTextHeaders(response, Charset.forName("UTF8"));
    OutputStream outputStream = response.getOutputStream();
    StreamUtil.writeText("Request: \n" + request.getServletPath() + "\n" + request.getQueryString() + "\n", outputStream);
    Map<String, String> finalParameters = new HashMap<String, String>();
    @SuppressWarnings("unchecked")
    Map<String, String[]> parameterMap = request.getParameterMap();
    Set<Map.Entry<String, String[]>> parameters = parameterMap.entrySet();
    for (Map.Entry<String, String[]> entry : parameters) {
      StreamUtil.writeText(entry.getKey() + " " + Strings.getListString(entry.getValue(), ", ") + "\n", outputStream);
      if (entry.getValue().length > 0) {
      finalParameters.put(entry.getKey(), entry.getValue()[0]);
      }
    }
    String command = request.getServletPath().replace("/", "");
    StreamUtil.writeText("\nParsed as:\nCommand: '" + command + "'\nArguments: '" + finalParameters + "'\n", outputStream);
    StreamUtil.writeText("\nResults:\n", outputStream);
    ICallAgentEventListener.Command receivedCommand = getCommand(command);
    try {
      if (receivedCommand == null) {
        StreamUtil.writeText("Command: '" + command + "' not recognized", outputStream);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        StreamUtil.closeFinally(outputStream);
        return;
      }
      TransferObject t = invokeCtiAccountEvent(finalParameters);
      if (t.isFailed()) {
        Object errorArguments = t.getBeanHolder();
        if (errorArguments instanceof Object[]) {
          errorArguments = Arrays.asList((Object[])errorArguments);
        }
        getLog().emit(CallAgentLogClient.CALL_DATA_ERROR, CTI_HANDLE_CALL_DATA, finalParameters, t.getMessage(), errorArguments);
        StreamUtil.writeText("Error invoking " + CTI_HANDLE_CALL_DATA + " with " + finalParameters, outputStream);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        StreamUtil.closeFinally(outputStream);
      }
      Map<String, Object> data = getCallData(finalParameters, t.getBeanHolder());
      ICallAgentEventListener l = getListener();
      Collection<CallAction> sent = l.send(receivedCommand, data);
      if (sent.isEmpty()) {
        StreamUtil.writeText("Command resulted in no changes to system state.\n", outputStream);
      }
      for (CallAction callAction : sent) {
        StreamUtil.writeText(callAction.getAction() + " - " + callAction.getId() + ": " + callAction.getDescription() + ".\n", outputStream);
      }
    } finally {
      long duration = System.currentTimeMillis() - requestStartTime;
      getLog().emit(CallAgentLogClient.CALL_HTTP_HANDLED, finalParameters.get("agentId"), command, duration);
    }
  }
  
  @SuppressWarnings("unchecked")
  private Map<String, Object> getCallData(Map<String, ?> finalParameters, Object beanHolder) {
    List<Map<String, Object>> data = (List<Map<String, Object>>) beanHolder;
    if (data == null || data.isEmpty()) {
      return (Map<String, Object>) finalParameters;
    }
    return data.get(0);
  }
  private ICallAgentEventListener getListener() {
    return listener;
  }
  private Command getCommand(String command) {
    switch (command) {
    case "ring":
      return Command.Ring;
    case "connect":
      return Command.Connect;
    case "disconnect":
      return Command.Disconnect;
    default:
      return null;
    }
  }
  @Override
  protected void doTrace(HttpServletRequest req, HttpServletResponse resp) 
  throws ServletException, IOException {
    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
    resp.getOutputStream().close();
  }
  private TransferObject invokeCtiAccountEvent(final Map<String, String> callParameters) {
    ServiceLookup system = LookupManager.getInstance().getLookup(LookupManager.SYSTEM);
    IMessageHandler stub = (IMessageHandler) system.getService("MessageHandler");
    HashMap<String, Object> arguments = new HashMap<String, Object>();
    arguments.putAll(callParameters);
    Object result = stub.handleMessage(this, "EventService", "sendEvent", new Class<?>[]{String.class, Map.class},
        new Object[]{CTI_HANDLE_CALL_DATA, arguments}, new HashMap<String, Object>());
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