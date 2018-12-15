package com.profitera.services.system.cti;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.profitera.services.system.cti.ICTIService;
import com.profitera.services.system.lookup.LookupManager;

public class JJCOMToolbarServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		Enumeration<String> keys = request.getParameterNames();
		Map m = new HashMap();
		while(keys.hasMoreElements()){
			String key = keys.nextElement();
			String value = request.getParameter(key);
			m.put(key, value.trim().equals("")? null: value);
		}
		if(m.get("CUSTOMER_ID")==null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing Customer ID");
			return;
		}
		if(m.get("AGENT_ID")==null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing Agent ID");
			return;
		}
		if(m.get("ACCOUNT_ID")==null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing Account ID");
			return;
		}
		if(m.get("ACCOUNT_NUMBER")==null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing Account Number");
			return;
		}
		if(m.get("CONTACT_NO") == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing Contact Number");
			return;
		}
		String accountIdText = (String)m.get("ACCOUNT_ID");
		try{
			m.put("ACCOUNT_ID", Long.parseLong(accountIdText));
		}catch(NumberFormatException e){
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Account ID");
			return;
		}
		m.put("STATUS", "R"); // bad hard coded so it automatically popup 
		m.put("INTERACTION_CLASS", "INTERACTION_VOICE_OUTBOUND"); // bad hard code so it automatically popup 
		m.put("INTERACTION_ID", m.get("CONTACT_NO"));
		if(getCtiService() instanceof JJCOMToolbarService){
			JJCOMToolbarService service = (JJCOMToolbarService)getCtiService();
			String agentId = (String)m.get("AGENT_ID");
			try {
				service.updateInteraction(agentId, m);
				resp.getWriter().write("Success");
			} catch (JJCOMToolbarException e) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			}
		}else{
			resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		}
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// doPost(request, response);
    
	}
	
	private ICTIService getCtiService() {
		ICTIService service = (ICTIService) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "CtiService");
		return service;
	}

}
