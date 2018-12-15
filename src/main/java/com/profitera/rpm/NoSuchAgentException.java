/*
 * Created on Jul 22, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.profitera.rpm;

/**
 * @author jamison
 *
 */
public class NoSuchAgentException extends AgentFailureException {
	private String agentCode = null;
	public NoSuchAgentException(String msg, String agentCode){
		super(msg);
		this.agentCode = agentCode;
	}

	/**
	 * @return
	 */
	public String getAgentCode() {
		return agentCode;
	}

	/**
	 * @param string
	 */
	public void setAgentCode(String string) {
		agentCode = string;
	}

}
