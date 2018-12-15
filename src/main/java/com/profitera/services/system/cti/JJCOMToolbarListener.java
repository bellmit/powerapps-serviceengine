package com.profitera.services.system.cti;

import java.util.Map;

public interface JJCOMToolbarListener {

	public void updateInteraction(String agentId, Map map) throws JJCOMToolbarException;
	
}
