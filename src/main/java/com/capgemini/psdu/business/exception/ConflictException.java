package com.capgemini.psdu.business.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * When request is duplicate returns HttpStatus.CONFLICT
 */
@ResponseStatus(value = HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {

	private static final long serialVersionUID = -1939653586361877902L;

	public ConflictException() {
		super();
	}

	public ConflictException(String message) {
		super(message);
	}
	
	public ConflictException(Throwable ex) {
		super(ex);
	}
	
	public ConflictException(String message, Throwable ex) {
		super(message, ex);
	}
}