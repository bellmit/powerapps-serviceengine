package com.profitera.rpm.expression;
/*
 * Created on Jul 20, 2003
 *
 */

/**
 * @author Jamison Masse
 * 
 * Just a normal exception, for Expression that can not be generated
 * from editors (invalid editor state) or parsed by a expression tree
 * walker.
 */
public class InvalidExpressionException extends Exception {
	/**
	 * 
	 */
	public InvalidExpressionException() {
		super();
	}

	/**
	 * @param message
	 */
	public InvalidExpressionException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidExpressionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public InvalidExpressionException(Throwable cause) {
		super(cause);
	}

}
