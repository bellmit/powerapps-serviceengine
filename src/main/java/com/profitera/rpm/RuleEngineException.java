package com.profitera.rpm;

/**
 * @author jamison
 */
public class RuleEngineException extends Exception {

	/**
	 * 
	 */
	public RuleEngineException() {
		super();
	}

	/**
	 * @param message
	 */
	public RuleEngineException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public RuleEngineException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RuleEngineException(String message, Throwable cause) {
		super(message, cause);
	}
}
