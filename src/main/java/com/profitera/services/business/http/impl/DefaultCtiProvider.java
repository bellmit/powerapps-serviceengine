package com.profitera.services.business.http.impl;

import java.util.Collection;
import java.util.Map;

import com.profitera.cti.ICTIProvider;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.cti.DialerService;
import com.profitera.services.system.lookup.LookupManager;

class DefaultCtiProvider implements ICTIProvider {
  private DialerService getService() {
    LookupManager lm = LookupManager.getInstance();
    return (DialerService) lm.getLookupItem(LookupManager.BUSINESS, "DailerService");
  }

  @Override
  public Collection<Map<String, Object>> getAllLoggedInAgents() {
    return getService().getAllLoggedInAgents();
  }

  @Override
  public TransferObject loginAgent(String userId, String extension, String queue, Agent agent) {
    return getService().login(userId, extension, agent.agentName, agent.agentLoginId, queue, agent.password);
  }
  @Override
  public TransferObject logoutAgentByUserId(String userId) {
    return getService().logout(userId);
  }

  @Override
  public TransferObject setAgentStatusByUserId(String user, ReadyStatus newStatus, String reason) {
    if (newStatus == ReadyStatus.READY) {
      return getService().ready(user);
    } else if (newStatus == ReadyStatus.NOT_READY) {
      return getService().notReady(user);
    } else if (newStatus == ReadyStatus.AUX) {
      return getService().aux(user, reason);
    } else {
      return getService().acw(user);
    }
  }
}