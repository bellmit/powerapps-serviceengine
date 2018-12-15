package com.profitera.descriptor.rpm;

/**
 * @author jamison
 */
public class UnsupportedAttributeException extends RuntimeException {

	/**
	 * 
	 */
	public UnsupportedAttributeException() {
		super();
	}

	/**
	 * @param message
	 */
	public UnsupportedAttributeException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public UnsupportedAttributeException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnsupportedAttributeException(String message, Throwable cause) {
		super(message, cause);
	}

}
