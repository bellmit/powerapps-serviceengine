package com.profitera.rpm;

/**
 * @author jamison
 *
 */
public class RuleAgentConstants {
	

	public static final int PPA_AGENT = 0;
	public static final int DELQM_AGENT = 1;
	public static final int PA_AGENT = 2;
	public static final int TRTCL_AGENT = 3;
	public static final int WLGEN_AGENT = 4;
	public static final int COLSC_AGENT = 5;
	public static final int TRTSM_AGENT = 6;
	
	//Codes are from a quick look at the DB
	// Must be order-sync'd with the classes
	public  static String[] AGENT_CODES = { "PPA", "DELQM", "PA", "TRTCL",  "WLGEN", "COLSC", "TRTSM" };
}
