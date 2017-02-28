package com.capgemini.psdu.presentation;

import javax.servlet.http.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Generic Intercepter to add the supplied Correlation ID in the response header.
 * 
 * @author CG10203
 *
 */
public class HeaderAmenderInterceptor extends HandlerInterceptorAdapter {

	@Value("${defaultCorrelationId:[unspecified]}")
	private String defaultCorrelationId;

	private static final Logger LOGGER = LoggerFactory.getLogger(HeaderAmenderInterceptor.class);


	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		String correlationId = StringUtils.hasText(request.getHeader("CorrelationId")) ? request.getHeader("CorrelationId") : defaultCorrelationId;

		response.setHeader("CorrelationId", correlationId);
		
		LOGGER.debug("CorrelationId set in the response header: {}", correlationId);
		
		return true;
	}

}
