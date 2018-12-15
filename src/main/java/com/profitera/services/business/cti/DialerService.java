package com.profitera.services.business.cti;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import com.profitera.cti.ICTIProvider;
import com.profitera.dataaccess.ISqlMapProvider;
import com.profitera.deployment.rmi.DialerServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.ICtiAgent;
import com.profitera.log.DefaultLogProvider;
import com.profitera.log.ILogProvider;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.business.cti.impl.DialerServiceLogClient;
import com.profitera.services.system.cti.ICTIService;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.NoopMapCar;

public class DialerService extends ProviderDrivenService implements DialerServiceIntf {
  public static final String AGENT_NOT_EXIST = "AGENT_NOT_EXIST";
  private ILogProvider log;

  private final Map<String, Agent> agents = new HashMap<String, Agent>();
  private final Map<String, String> agentIdMap = new HashMap<String, String>();
  
  private class Agent {

    private String agentId;
    private boolean expired = false;
    private final LinkedBlockingQueue<Map<String, String>> agentQueue = new LinkedBlockingQueue<Map<String, String>>();
    private final LinkedBlockingQueue<Map<String, Object>> interactionQueue = new LinkedBlockingQueue<Map<String, Object>>();
    private String lastStatus = "";
    private void addAgentEvent(Map<String, String> event) {
      if (event != null && event.get(ICtiAgent.STATUS) != null) {
        lastStatus = event.get(ICtiAgent.STATUS);
      }
      agentQueue.add(event);
    }
    /**
     * This method is intended for "bulk" status requests, not for checking the absolute current 
     * agent status.
     */
    public String getLastStatus() {
      return lastStatus;
    }
    private Map<String, String> getAgentEvent() {
      List<Map<String, String>> l = new ArrayList<Map<String, String>>();
      while (agentQueue.peek() != null) {
        Map<String, String> o = agentQueue.poll();
        if (o != null) {
          l.add(o);
        }
      }
      if (l.isEmpty()) {
        try {
          l.add(agentQueue.poll(5000, java.util.concurrent.TimeUnit.MILLISECONDS));
        } catch (InterruptedException ie) {
        }
      }
      return l.get(l.size() - 1);
    }

    private void addInteractionEvent(Map<String, Object> event) {
      interactionQueue.add(event);
    }

    private List<Map<String, Object>> getInteractionEvent() {
      List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
      while (interactionQueue.peek() != null) {
        Map<String, Object> o = interactionQueue.poll();
        if (o != null) {
          l.add(o);
        }
      }
      if (l.isEmpty()) {
        try {
          l.add(interactionQueue.take());
        } catch (InterruptedException ie) {
        }
      }
      return l;
    }

    private void markExpired() {
      expired = true;
    } // last call!
  }

  private ILogProvider getLog() {
    if (log == null) {
      log = new DefaultLogProvider();
      log.register(new DialerServiceLogClient());
    }
    return log;
  }

  public TransferObject getAutoAnswerMode() {
    return new TransferObject(getCtiService().getAutoAnswerMode());
  }

  public TransferObject getAutoRequestMode() {
    return new TransferObject(getCtiService().getAutoRequestMode());
  }

  public TransferObject
      login(String userId, String ext, String agentName, String loginId, String queue, String password) {
    try {
      String agentId = getCtiService().loginAgent(ext, agentName, loginId, queue, password);
      if (agentId == null) {
        agentId = userId;
      }
      Agent existingAgentRecord = agents.get(userId);
      boolean userHasAgentInSystem = existingAgentRecord != null && !existingAgentRecord.expired;
      String agentIdUserMapping = agentIdMap.get(agentId);
      // We only create a new agent if we don't have it already or if the user to agent mapping has changed.
      if (!userHasAgentInSystem || agentIdUserMapping == null || !agentIdUserMapping.equals(userId)) {
        Agent agent = new Agent();
        agents.put(userId, agent);
        agent.agentId = agentId;
        agentIdMap.put(agentId, userId);
      }
      // Post-login hook for underlying service
      getCtiService().agentLoggedIn(agentId);
    } catch (Exception e) {
      getLog().emit(DialerServiceLogClient.DIALER_SYSTEM_ERROR, e, "");
      agents.remove(userId);
      return new TransferObject(TransferObject.ERROR, "Failed to login : " + e.getMessage());
    }
    return new TransferObject();
  }

  public TransferObject logout(String userId) {
    try {
      Agent agent = agents.get(userId);
      if (agent != null) {
        agent.markExpired();
      }
      getCtiService().logoutAgent(agent.agentId);
    } catch (Exception e) {
      getLog().emit(DialerServiceLogClient.DIALER_SYSTEM_ERROR, e, "");
      return new TransferObject(TransferObject.ERROR, "Failed to logout : " + e.getMessage());
    }
    return new TransferObject();
  }

  public TransferObject dialNumber(String userId, String number) {
    try {
      Agent agent = agents.get(userId);
      if (agent == null) {
        return userNotLoggedIn(userId);
      }
      getCtiService().dialNumber(agent.agentId, number);
    } catch (Exception e) {
      getLog().emit(DialerServiceLogClient.DIALER_SYSTEM_ERROR, e, "");
      return new TransferObject(TransferObject.ERROR, "Failed to dial this number " + number + " : " + e.getMessage());
    }
    return new TransferObject();
  }

  public TransferObject hangup(String userId, String interactionId) {
    try {
      Agent agent = agents.get(userId);
      if (agent == null) {
        return userNotLoggedIn(userId);
      }
      getCtiService().hangup(agent.agentId, interactionId);
    } catch (Exception e) {
      getLog().emit(DialerServiceLogClient.DIALER_SYSTEM_ERROR, e, "");
      return new TransferObject(TransferObject.ERROR, "Failed to end this call : " + e.getMessage());
    }
    return new TransferObject();
  }

  public TransferObject hold(String userId, String interactionId) {
    try {
      Agent agent = agents.get(userId);
      if (agent == null) {
        return userNotLoggedIn(userId);
      }
      getCtiService().hold(agent.agentId, interactionId);
    } catch (Exception e) {
      getLog().emit(DialerServiceLogClient.DIALER_SYSTEM_ERROR, e, "");
      return new TransferObject(TransferObject.ERROR, "Failed to hold call : " + e.getMessage());
    }
    return new TransferObject();
  }

  public TransferObject retrieve(String userId, String interactionId) {
    try {
      Agent agent = agents.get(userId);
      if (agent == null) {
        return userNotLoggedIn(userId);
      }
      getCtiService().retrieve(agent.agentId, interactionId);
    } catch (Exception e) {
      getLog().emit(DialerServiceLogClient.DIALER_SYSTEM_ERROR, e, "");
      return new TransferObject(TransferObject.ERROR, "Failed to retrieve call : " + e.getMessage());
    }
    return new TransferObject();
  }

  public TransferObject answer(String userId, String interactionId) {
    try {
      Agent agent = agents.get(userId);
      if (agent == null) {
        return userNotLoggedIn(userId);
      }
      getCtiService().answer(agent.agentId, interactionId);
    } catch (Exception e) {
      getLog().emit(DialerServiceLogClient.DIALER_SYSTEM_ERROR, e, "");
      return new TransferObject(TransferObject.ERROR, "Failed to answer call : " + e.getMessage());
    }
    return new TransferObject();
  }

  public TransferObject transfer(String userId, String interactionId, String number, boolean option, Long accountId) {
    try {
      Agent agent = agents.get(userId);
      if (agent == null) {
        return userNotLoggedIn(userId);
      }
      Map<String, Object> account = queryForAccountInformation(accountId);
      getCtiService().transfer(agent.agentId, interactionId, number, account, option);
    } catch (Exception e) {
      getLog().emit(DialerServiceLogClient.DIALER_SYSTEM_ERROR, e, "");
      return new TransferObject(TransferObject.ERROR, "Failed to transfer call : " + e.getMessage());
    }
    return new TransferObject();
  }

  private Map<String, Object> queryForAccountInformation(Long accountId) throws TransferObjectException {
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> result = super.executeListQuery(ISqlMapProvider.LIST, "getAccount", accountId, new NoopMapCar(), getReadOnlyProvider());
    if (result == null || result.isEmpty()) {
      return new HashMap<String, Object>();
    }
    return result.get(0);
  }

  public TransferObject getCampaign(String userId) {
    Agent agent = agents.get(userId);
    if (agent == null) {
      return userNotLoggedIn(userId);
    }
    List<String> l = getCtiService().getCampaignList(agent.agentId);
    return new TransferObject(l);
  }

  public TransferObject join(String userId, String campaignId) {
    try {
      Agent agent = agents.get(userId);
      if (agent == null) {
        return userNotLoggedIn(userId);
      }
      getCtiService().join(agent.agentId, campaignId);
    } catch (Exception e) {
      getLog().emit(DialerServiceLogClient.DIALER_SYSTEM_ERROR, e, "");
      return new TransferObject(TransferObject.ERROR, "Failed to join preview campaign : " + e.getMessage());
    }
    return new TransferObject("Joined " + campaignId);
  }

  public TransferObject quit(String userId) {
    try {
      Agent agent = agents.get(userId);
      if (agent == null) {
        return userNotLoggedIn(userId);
      }
      getCtiService().quit(agent.agentId);
    } catch (Exception e) {
      getLog().emit(DialerServiceLogClient.DIALER_SYSTEM_ERROR, e, "");
      return new TransferObject(TransferObject.ERROR, "Failed to quit preview campaign : " + e.getMessage());
    }
    return new TransferObject("Quited the campaign");
  }

  public TransferObject request(String userId) {
    Map<String, Object> map = new HashMap<String, Object>();
    try {
      Agent agent = agents.get(userId);
      if (agent == null) {
        return userNotLoggedIn(userId);
      }
      map = getCtiService().request(agent.agentId);
    } catch (Exception e) {
      getLog().emit(DialerServiceLogClient.DIALER_SYSTEM_ERROR, e, "");
      return new TransferObject(TransferObject.ERROR, "Failed to request record : " + e.getMessage());
    }
    return new TransferObject(map);
  }

  public TransferObject skip(String userId, String interactionId) {
    try {
      Agent agent = agents.get(userId);
      if (agent == null) {
        return userNotLoggedIn(userId);
      }
      getCtiService().skip(agent.agentId, interactionId);
    } catch (Exception e) {
      getLog().emit(DialerServiceLogClient.DIALER_SYSTEM_ERROR, e, "");
      return new TransferObject(TransferObject.ERROR, "Failed to skip record : " + e.getMessage());
    }
    return new TransferObject("Record skipped");
  }

  public TransferObject remove(String userId, String interactionId) {
    try {
      Agent agent = agents.get(userId);
      if (agent == null) {
        return userNotLoggedIn(userId);
      }
      getCtiService().remove(agent.agentId, interactionId);
    } catch (Exception e) {
      getLog().emit(DialerServiceLogClient.DIALER_SYSTEM_ERROR, e, "");
      return new TransferObject(TransferObject.ERROR, "Failed to remove record : " + e.getMessage());
    }
    return new TransferObject("Record skipped");
  }

  public TransferObject call(String userId, String interactionId) {
    try {
      Agent agent = agents.get(userId);
      if (agent == null) {
        return userNotLoggedIn(userId);
      }
      getCtiService().call(agent.agentId, interactionId);
    } catch (Exception e) {
      getLog().emit(DialerServiceLogClient.DIALER_SYSTEM_ERROR, e, "");
      return new TransferObject(TransferObject.ERROR, "Failed to call this record's contact number : " + e.getMessage());
    }
    return new TransferObject();
  }

  public TransferObject setCallResult(String userId, String interactionId, String callStatus, boolean reschedule,
      Date date) {
    try {
      Agent agent = agents.get(userId);
      if (agent == null) {
        return userNotLoggedIn(userId);
      }
      getCtiService().setCallResult(agent.agentId, interactionId, callStatus, reschedule, date);
    } catch (Exception e) {
      getLog().emit(DialerServiceLogClient.DIALER_SYSTEM_ERROR, e, "");
      return new TransferObject(TransferObject.ERROR, "Failed to set call result status : " + e.getMessage());
    }
    return new TransferObject("Call result status updated");
  }

  public TransferObject getLatestInteraction(String userId) {
    Agent agent = agents.get(userId);
    if (agent != null) {
      List<Map<String, Object>> event = agent.getInteractionEvent();
      return new TransferObject(event);
    }
    return new TransferObject();
  }

  public TransferObject ready(String userId) {
    try {
      Agent agent = agents.get(userId);
      if (agent == null) {
        return userNotLoggedIn(userId);
      }
      getCtiService().ready(agent.agentId);
    } catch (Exception e) {
      getLog().emit(DialerServiceLogClient.DIALER_SYSTEM_ERROR, e, "");
      return new TransferObject(TransferObject.ERROR, "Failed to put agent in ready workmode : " + e.getMessage());
    }
    return new TransferObject("Agent ready");
  }

  public TransferObject notReady(String userId) {
    try {
      Agent agent = agents.get(userId);
      if (agent == null) {
        return userNotLoggedIn(userId);
      }
      getCtiService().notReady(agent.agentId);
    } catch (Exception e) {
      getLog().emit(DialerServiceLogClient.DIALER_SYSTEM_ERROR, e, "");
      return new TransferObject(TransferObject.ERROR, "Failed to put agent in not-ready workmode : " + e.getMessage());
    }
    return new TransferObject("Agent not ready");
  }

  public TransferObject acw(String userId) {
    try {
      Agent agent = agents.get(userId);
      if (agent == null) {
        return userNotLoggedIn(userId);
      }
      getCtiService().acw(agent.agentId);
    } catch (Exception e) {
      getLog().emit(DialerServiceLogClient.DIALER_SYSTEM_ERROR, e, "");
      return new TransferObject(TransferObject.ERROR, "Failed to put agent in acw workmode : " + e.getMessage());
    }
    return new TransferObject("Agent in acw mode");
  }

  public TransferObject aux(String userId, String auxReason) {
    try {
      Agent agent = agents.get(userId);
      if (agent == null) {
        return userNotLoggedIn(userId);
      }
      getCtiService().aux(agent.agentId, auxReason);
    } catch (Exception e) {
      getLog().emit(DialerServiceLogClient.DIALER_SYSTEM_ERROR, e, "");
      return new TransferObject(TransferObject.ERROR, "Failed to put agent in aux workmode : " + e.getMessage());
    }
    return new TransferObject("Agent in aux mode with reason provided : " + auxReason);
  }

  public TransferObject getAcwTime() {
    return new TransferObject(getCtiService().getAcwTime());
  }

  public TransferObject getAnswerDelay() {
    return new TransferObject(getCtiService().getAnswerDelay());
  }

  public TransferObject getAgentStatusFromServer(String userId) {
    Agent agent = agents.get(userId);
    if (agent != null) {
      if (agent.expired) {
        agent = agents.remove(userId);
      }
      Map<String, String> event = agent.getAgentEvent();
      return new TransferObject(event);
    }
    return new TransferObject();
  }

  public void updateInteraction(final String agentId, final Map<String, Object> event) throws EventUpdateException {
    String userId = (String) agentIdMap.get(agentId);
    Agent agent = agents.get(userId);
    if (agent == null) {
      throw new EventUpdateException(AGENT_NOT_EXIST);
    }
    agent.addInteractionEvent(event);
  }

  public void updateAgent(String agentId, Map<String, String> event) throws EventUpdateException {
    String userId = (String) agentIdMap.get(agentId);
    Agent agent = agents.get(userId);
    if (agent == null) {
      throw new EventUpdateException(AGENT_NOT_EXIST);
    }
    agent.addAgentEvent(event);
  }
  public Collection<Map<String, Object>> getAllLoggedInAgents() {
    Agent[] array = agents.values().toArray(new Agent[0]);
    List<Map<String, Object>> l = new ArrayList<>();
    for (int i = 0; i < array.length; i++) {
      Map<String, Object> agentData = new HashMap<>();
      Agent agent = array[i];
      agentData.put(ICtiAgent.AGENT, agent.agentId);
      agentData.put(ICtiAgent.STATUS, agent.getLastStatus());
      agentData.put(ICTIProvider.USER_ID, agentIdMap.get(agent.agentId));
      l.add(agentData);
    }
    return l;
  }

  private ICTIService getCtiService() {
    return (ICTIService) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "CtiService");
  }

  private TransferObject userNotLoggedIn(String userId) {
    return new TransferObject(TransferObject.ERROR, "User " + userId + " not logged in.");
  }
}
