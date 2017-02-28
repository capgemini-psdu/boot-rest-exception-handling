package com.capgemini.psdu.business.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {

	/** Serial version ID */
	private static final long serialVersionUID = 1L;

	public ForbiddenException() {
		super();
	}

	public ForbiddenException(String message) {
		super(message);
	}

	public ForbiddenException(Throwable ex) {
		super(ex);
	}
	
	public ForbiddenException(String message, Throwable ex) {
		super(message, ex);
	}
}