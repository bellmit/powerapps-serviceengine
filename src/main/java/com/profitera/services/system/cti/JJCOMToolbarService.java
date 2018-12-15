package com.profitera.services.system.cti;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.system.SystemService;
import com.profitera.services.system.cti.ICTIService;

public class JJCOMToolbarService extends SystemService implements ICTIService, JJCOMToolbarListener{

	private static final String READY = "READY";
	private static final String NOT_READY = "NOT_READY";
	private static Log log = LogFactory.getLog(JJCOMToolbarService.class);
	
	class Agent{
		
		private LinkedBlockingQueue statusQueue = new LinkedBlockingQueue();
		
		private String status = NOT_READY;
		
		private LinkedBlockingQueue interactionQueue = new LinkedBlockingQueue();
		
		private Map<String, Map> interaction = Collections.synchronizedMap(new HashMap<String, Map>());
		
		public Agent(){
			addStatusToQueue(status);
		}
		
		public void addInteractionToQueue(Map interactionInfo){
			String id = (String)interactionInfo.get("INTERACTION_ID");
			interaction.put(id, interactionInfo);
			interactionQueue.add(interactionInfo);
		}
		
		public List getInteractionFromQueue(){
			List l = new ArrayList();
			while(interactionQueue.peek()!=null){
				Object o = interactionQueue.poll();
				if (o != null) l.add(o);
			}
			if(l.size()==0){
				try{
					l.add(interactionQueue.take());
				}catch(InterruptedException ie){
				}
			}
			return l;
		}
		
		public Map getInteraction(String id){
			return interaction.get(id);
		}
		
		public void removeInteraction(String id){
			interaction.remove(id);
		}
		
		public void setStatus(String status){
			this.status = status;
			addStatusToQueue(status);
		}
		
		public String getStatus(){
			return status;
		}
		
		public void addStatusToQueue(String status){
			statusQueue.add(status);
		}
		
		public String getStatusFromQueue(){
			List l = new ArrayList();
			while(statusQueue.peek()!=null){
				Object o = statusQueue.poll();
				if (o != null) l.add(o);
			}
			if(l.size()==0){
				try{
					l.add(statusQueue.take());
				}catch(InterruptedException ie){
				}
			}
			return (String)l.get(l.size()-1);
		}
	}
	
	Map<String, Agent> loggedAgent = Collections.synchronizedMap(new HashMap<String, Agent>());
	
	public void acw(String agentId) throws Exception {
		// Not support		
	}

	public void answer(String agentId, String interactionId) throws Exception {
		// Not support		
	}

	public void aux(String agentId, String auxReason) throws Exception {
		// Not support		
	}

	public void call(String agentId, String interactionId) throws Exception {
		// Not support		
	}

	public void dialNumber(String id, String number) throws Exception {
		// Not support	
	}

	public Long getAcwTime() {
		// Not support
		return new Long(0);
	}

	public TransferObject getAgentStatus(String agentId) {
		Agent agent = loggedAgent.get(agentId);
		Map map = new HashMap();
		map.put("AGENT_STATUS",agent.getStatusFromQueue());
		map.put("PLACE_ID", agentId);
		return new TransferObject(map);
	}

	public Long getAnswerDelay() {
		// Not support
		return new Long(0);
	}

	public boolean getAutoAnswerMode() {
		return true;
	}

	public boolean getAutoRequestMode() {
		// Not support
		return false;
	}

	public List getCampaignList(String agentId) {
		// Not support
		return new ArrayList();
	}

	public TransferObject getLatestInteraction(String agentId) {
		Agent agent = loggedAgent.get(agentId);
		List interactionList = agent.getInteractionFromQueue();
		if (interactionList.size() == 0){
			return new TransferObject("Retry ... ");
		}
		return new TransferObject(interactionList);
	}

	public void hangup(String agentId, String interactionId) throws Exception {
		// Not support
	}

	public void hold(String agentId, String interactionId) throws Exception {
		// Not support
	}

	public void join(String agentId, String campaignId) throws Exception {
		// Not support
	}

	public String loginAgent(String ext, String agentName, String agentLoginId, String queue, String password) throws Exception {
		if(!loggedAgent.containsKey(agentLoginId)){
			loggedAgent.put(agentLoginId, new Agent());
		}else{
			log.warn("[CTI] Agent "+agentLoginId+" already logged in");
		}
		return agentLoginId;
	}

	public void logoutAgent(String agentId) throws Exception {
		Agent agent = loggedAgent.remove(agentId);
		if(agent!=null) {
			agent.interactionQueue.clear();
			agent.statusQueue.clear();
			agent.interaction.clear();
		}
	}

	public void notReady(String agentId) throws Exception {
		loggedAgent.get(agentId).setStatus(NOT_READY);
	}

	public void quit(String agentId) throws Exception {
		// Not support		
	}

	public void ready(String agentId) throws Exception {
		loggedAgent.get(agentId).setStatus(READY);
	}

	public Map receive(String agentId, String interactionId) throws Exception {
		return loggedAgent.get(agentId).getInteraction(interactionId);
	}

	public void remove(String agentId, String interactionId) throws Exception {
		// Not support		
	}

	public Map request(String agentId) throws Exception {
		// Not support		
		return new HashMap();
	}

	public void retrieve(String agentId, String interactionId) throws Exception {
		// Not support		
	}

	public void setCallResult(String agentId, String interactionId, String callStatus, boolean reschedule, Date date) throws Exception {
		loggedAgent.get(agentId).removeInteraction(interactionId);
	}

	public void skip(String agentId, String interactionId) throws Exception {
		// Not support		
	}

	public void transfer(String agentId, String interactionId, String number, Map attachedData, boolean attachOption) throws Exception {
		// Not support		
	}

	public void updateInteraction(String agentId, Map map) throws JJCOMToolbarException {
		Agent agent = loggedAgent.get(agentId);
		if(agent==null) throw new JJCOMToolbarException("User "+agentId+" not exist");
		if(agent.getStatus().equals(NOT_READY)) throw new JJCOMToolbarException("User "+agentId+" not ready");
		agent.addInteractionToQueue(map);
	}

  @Override
  public void agentLoggedIn(String agentId) {
    // TODO Auto-generated method stub
    
  }

}
