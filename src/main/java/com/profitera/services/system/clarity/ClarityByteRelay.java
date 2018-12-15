package com.profitera.services.system.clarity;

import com.profitera.util.io.ByteRelay;
import com.profitera.server.ServiceEngine;
import com.profitera.services.system.SystemService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClarityByteRelay extends SystemService {

	public static final String CLARITY_HOST = "Clarity-Host";
	public static final String CLARITY_PORT = "Clarity-Port";
	public static final String RELAY_HOST = "Clarity-Relay-Host";
	public static final String RELAY_PORT = "Clarity-Relay-Port";
	
	private Log log = LogFactory.getLog(ClarityByteRelay.class);
	private ByteRelay byteRelay;
	public ClarityByteRelay() {
		super();
		String clarityHost = ServiceEngine.getProp(CLARITY_HOST, "localhost");
		int clarityPort = ServiceEngine.getIntProp(CLARITY_PORT, 10000);
		int relayPort = ServiceEngine.getIntProp(RELAY_PORT, 10001);
		try {
			byteRelay = new ByteRelay(relayPort, clarityHost, clarityPort);
		} catch (Exception ex) {
			log.error("Unable to start clarity byte relay, communication link for clarity will be disabled!");
		}
	}
}