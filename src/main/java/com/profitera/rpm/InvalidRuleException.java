package com.profitera.rpm;

/**
 * @author jamison
 */
public class InvalidRuleException extends RuleEngineException {

	/**
	 * 
	 */
	public InvalidRuleException() {
		super();
	}

	/**
	 * @param message
	 */
	public InvalidRuleException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidRuleException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidRuleException(String message, Throwable cause) {
		super(message, cause);
	}

}
