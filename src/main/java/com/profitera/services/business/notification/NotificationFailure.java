package com.profitera.services.business.notification;

/**
 * @author jamison
 */
public class NotificationFailure extends Exception {

	/**
	 * 
	 */
	public NotificationFailure() {
		super();
	}

	/**
	 * @param message
	 */
	public NotificationFailure(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NotificationFailure(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public NotificationFailure(Throwable cause) {
		super(cause);
	}
}
