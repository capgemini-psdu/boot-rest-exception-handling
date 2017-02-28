package com.capgemini.psdu.business.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * When end point service is unavailable return HttpStatus.SERVICE_UNAVAILABLE
 */
@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceUnavailableException extends RuntimeException {

	/** Serial version ID */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            the detail message
	 */
	public ServiceUnavailableException(String message) {
		super(message);
	}
	
	/**
	 * Constructor with Throwable argument.
	 * 
	 * @param exception the original throwable
	 */
	public ServiceUnavailableException(Throwable exception) {
		super(exception);
	}
	
	/**
	 * Constructor with message and Throwable argument.
	 * 
	 * @param message 	the detail message
	 * @param exception the original throwable
	 */
	public ServiceUnavailableException(String message, Throwable exception) {
		super(message, exception);
	}
}
