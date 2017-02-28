package com.capgemini.psdu.business.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * When unhandled returns HttpStatus.NOT_FOUND
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

	/** Serial version ID */
	private static final long serialVersionUID = 1L;
	
	public ResourceNotFoundException() {}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            the detail message
	 */
	public ResourceNotFoundException(String message) {
		super(message);
	}
	
	/**
	 * Constructor with Throwable argument.
	 * 
	 * @param exception the original throwable
	 */
	public ResourceNotFoundException(Throwable exception) {
		super(exception);
	}
	
	/**
	 * Constructor with message and Throwable argument.
	 * 
	 * @param message 	the detail message
	 * @param exception the original throwable
	 */
	public ResourceNotFoundException(String message, Throwable exception) {
		super(message, exception);
	}
}
