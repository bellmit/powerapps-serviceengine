package com.profitera.services.system.cti;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ICTIService {

	public String loginAgent(String ext, String agentName, String agentLoginId, String queue, String password) throws Exception;
  public void agentLoggedIn(String agentId);
	public void logoutAgent(String agentId) throws Exception;
	public void dialNumber(String id, String number) throws Exception;
	public void hangup(String agentId, String interactionId) throws Exception;
	public void retrieve(String agentId, String interactionId) throws Exception;
	public void hold(String agentId, String interactionId) throws Exception; 
	public void answer(String agentId, String interactionId) throws Exception;
	public List<String> getCampaignList(String agentId);
	public void join(String agentId, String campaignId) throws Exception;
	public void quit(String agentId) throws Exception;
	public Map<String, Object> request(String agentId) throws Exception;
	public void skip(String agentId, String interactionId) throws Exception;
	public void remove(String agentId, String interactionId) throws Exception;
	public void call(String agentId, String interactionId) throws Exception;
	public void setCallResult(String agentId, String interactionId, String callStatus, boolean reschedule, Date date) throws Exception;
	public void transfer(String agentId, String interactionId, String number, Map<String, Object> attachedData, boolean attachOption) throws Exception;
	public void ready(String agentId) throws Exception;
	public void notReady(String agentId) throws Exception;
	public void acw(String agentId) throws Exception;
	public void aux(String agentId, String auxReason) throws Exception;
	public Long getAcwTime();
	public Long getAnswerDelay();
	public boolean getAutoAnswerMode();
	public boolean getAutoRequestMode();

}
