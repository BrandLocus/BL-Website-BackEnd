package com.hvc.brandlocus.security.filter;



import com.hvc.brandlocus.utils.JsonSanitizer;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
//@Component
public class RequestResponseLoggingFilter implements Filter {

    private static final Logger requestLogger = LoggerFactory.getLogger("com.HVC.FoodFlow.request");
    private static final Logger responseLogger = LoggerFactory.getLogger("com.HVC.FoodFlow.response");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper((HttpServletRequest) servletRequest);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper((HttpServletResponse) servletResponse);

        logRequest(wrappedRequest);

        filterChain.doFilter(wrappedRequest, wrappedResponse);



        logRequestBody(wrappedRequest);

        if ( wrappedRequest.getMethod().equals(HttpMethod.POST.name())) {
            logResponse(wrappedResponse);
        }

        wrappedResponse.copyBodyToResponse();
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }



    private void logRequest(ContentCachingRequestWrapper request) {

        requestLogger.info("::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        requestLogger.info("Request Method: {}", request.getMethod());
        requestLogger.info("Request URI: {}", request.getRequestURI());
//
//        log.info("::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
//        log.info("Request Method: " + request.getMethod());
//        log.info("Request URI: " + request.getRequestURI());

    }

    private void logRequestBody(ContentCachingRequestWrapper request) {
        if (HttpMethod.POST.name().equalsIgnoreCase(request.getMethod()) ||
                HttpMethod.PUT.name().equalsIgnoreCase(request.getMethod())) {
            byte[] requestBody = request.getContentAsByteArray();
            if (requestBody.length > 0) {
                String payload = new String(requestBody, StandardCharsets.UTF_8);
                String sanitized = JsonSanitizer.sanitize(payload);
//                log.info("Request Payload: {}", sanitized);
                requestLogger.info("Request Payload: {}", sanitized);
            }
        }
    }

    private void logResponse(ContentCachingResponseWrapper response) {
//        log.info("Response Status: {}", response.getStatus());
        responseLogger.info("Response Status: {}", response.getStatus());


        byte[] responseBody = response.getContentAsByteArray();
        if (responseBody.length > 0) {
            String payload = new String(responseBody, StandardCharsets.UTF_8);
            String sanitized = JsonSanitizer.sanitize(payload);
            if (response.getStatus() >= 400) {
//                log.error("Error Response: {}", sanitized);
                responseLogger.info("Response: {}", sanitized);
            } else {
                responseLogger.info("Response: {}", sanitized);
            }
        }
    }

}



