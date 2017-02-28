package com.capgemini.psdu.presentation;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.netflix.hystrix.exception.HystrixRuntimeException;

import com.capgemini.psdu.business.exception.ForbiddenException;
import com.capgemini.psdu.business.exception.ServiceUnavailableException;


@ControllerAdvice
@Component
public class LoggingResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(LoggingResponseEntityExceptionHandler.class);
	
	@Value("${defaultCorrelationId}")
	private String defaultCorrelationID;

	private final List<MediaType> supportedMediaTypes = Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML);
	
	/**
	 * Catch all exception types here. This is the initial route into the
	 * exception handler.
	 * 
	 * In all cases respects {@link ResponseStatus} exception types.
	 * 
	 * Unwraps Hystrix exceptions and wraps the underlying cause, where not an
	 * {@link ResponseStatus} exception type, with a ServiceUnavailableException.
	 *  
	 * Respects JSON/XML content negotiation with the client by setting
	 * an appropriate Content-Type on the HTTP response.
	 * 
	 * The application itself should ensure that the exception is thrown with an
	 * appropriate message i.e. JSON or XML depending on what type of request it
	 * is servicing.
	 */
	@ExceptionHandler
	protected ResponseEntity<Object> handle(Exception ex, WebRequest request, HttpServletRequest servletRequest) {
		String correlationId = StringUtils.hasText(request.getHeader("CorrelationId")) ? request.getHeader("CorrelationId") : defaultCorrelationID;
		// Unwrap any HystrixRuntimeException and if appropriate re-wrap the result in a ServiceUnavailableException
		if (ex instanceof HystrixRuntimeException) {
			ex = unwrapException(ex, false);
			if (!isRestExceptionType(ex)) {
				logger.warn("CorrelationId: {} Problem communicating with Hystrix-managed dependency, exception message: [{}]", correlationId, ex.getMessage());
				logger.warn("CorrelationId: " + correlationId + " Caused by ...", ex);
				ex = new ServiceUnavailableException(ex);
			}
		}
		// Wrap any AccessDeniedException so we get a proper 403 RESTfull response
		if (ex instanceof AccessDeniedException) {
			ex = new ForbiddenException("Access is denied");
		}
		// Respect the @ResponseStatus annotation if the exception has one
		// Otherwise delegate first to Spring and ultimately handleExceptionInternal() to handle
		if (isRestExceptionType(ex)) {
			HttpStatus responseStatus = getHttpResponseStatus(ex);
			logger.info("CorrelationId: {} Generating REST response: [{}] for exception: [{}]", correlationId, responseStatus, ex.getClass().getSimpleName());
			logger.info("CorrelationId: " + correlationId + " Caused by ...", ex);
			Object body = getErrorResponseBody(getHttpResponseStatus(ex), ex, servletRequest, correlationId);
			return new ResponseEntity<Object>(body, setContentType(new HttpHeaders(), request), getHttpResponseStatus(ex));
		} else {
			return super.handleException(ex, request);
		}
	}
	
	/**
	 * Override to log the error and set the response body and correlation ID header for internal server errors.
	 */
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {		
		String correlationId = StringUtils.hasText(request.getHeader("CorrelationId")) ? request.getHeader("CorrelationId") : defaultCorrelationID;
		if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
			logger.error("CorrelationId: {} Unexpected exception: ", correlationId, ex);
		}
		setContentType(headers, request);
		HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		body = getErrorResponseBody(status, ex, servletRequest, correlationId);
		return new ResponseEntity<Object>(body, headers, status);
	}
	
	/**
	 * Returns true for {@link ResponseStatus} exception types.
	 */
	private boolean isRestExceptionType(Exception ex) {
		return getHttpResponseStatus(ex) != null ? true : false;
	}
	
	/**
	 * Returns the {@link HttpStatus} for the {@link ResponseStatus} attached to the exception or null if none.
	 */
	private HttpStatus getHttpResponseStatus(Exception ex) {
		ResponseStatus responseStatus = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);
		return responseStatus == null ? null : responseStatus.value();
	}
	
	/**
	 * Constructs a response body similar to that produced by Spring's default error controller.
	 * Provides some consistency between any error view returned by Spring and those returned here ourselves.
	 * The main difference here is that we extend the response to include a correlationId.
	 * @see org.springframework.boot.autoconfigure.web.DefaultErrorAttributes
	 * @see org.springframework.boot.autoconfigure.web.BasicErrorController
	 */
	private Object getErrorResponseBody(HttpStatus status, Exception ex, HttpServletRequest request, String correlationId) {
		ErrorResponseBody errorResponseBody = new ErrorResponseBody();
		errorResponseBody.setCorrelationId(correlationId);
		errorResponseBody.setTimestamp(new Date());
		errorResponseBody.setStatus(status.value());
		errorResponseBody.setError(status.getReasonPhrase());
		ex = unwrapException(ex, true);
		errorResponseBody.setException(ex.getClass().getName());
		errorResponseBody.setMessage(ex.getMessage());
		errorResponseBody.setPath(request.getContextPath() + request.getServletPath());
		return errorResponseBody;
	}
	
	/** 
	 * Helper to unwrap an Exception to its underlying cause.
	 */
	private Exception unwrapException(Exception ex, boolean fully) {
		while (ex.getCause() != null && ex.getCause() instanceof Exception) {
			ex = (Exception)(ex.getCause());
			if (!fully) {
				break; // Stop at the first level
			}
		}
		return ex;
	}
	
	/**
	 * Set content type on response based on accept header in request.
	 */
	private HttpHeaders setContentType(HttpHeaders headers, WebRequest request) {
		String accept = request.getHeader("Accept");
		try {
			List<MediaType> mediaTypes = MediaType.parseMediaTypes(accept);
			for (MediaType mt : mediaTypes) {
				if (supportedMediaTypes.contains(mt)) {
					headers.setContentType(mt);
				}						
			}
		} catch (Exception e) {
			// Well, we tried
		}
		return headers;
	}
}