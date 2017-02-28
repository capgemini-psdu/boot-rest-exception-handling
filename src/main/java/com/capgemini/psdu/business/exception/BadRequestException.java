package com.capgemini.psdu.business.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * When request is not proper returns HttpStatus.BAD_REQUEST
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

	/** Serial version ID */
	private static final long serialVersionUID = 1L;

	public BadRequestException() {
		super();
	}

	public BadRequestException(String message) {
		super(message);
	}
	
	public BadRequestException(Throwable ex) {
		super(ex);
	}
	
	public BadRequestException(String message, Throwable ex) {
		super(message, ex);
	}
}
