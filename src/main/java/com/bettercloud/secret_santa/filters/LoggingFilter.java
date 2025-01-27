package com.bettercloud.secret_santa.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import org.slf4j.MDC;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * Filter to log details of each request/response, including MDC for traceId
 * and use of ContentCaching wrappers to read both request and response data.
 * <p>
 * This filter is triggered once per request.
 */
@Slf4j
@Component
@Order(1)
public class LoggingFilter extends OncePerRequestFilter {

    private static final String TRACK_ID_HEADER = "x-track-id";
    private static final String REMOTE_HOST = "remoteHost";

    /**
     * Intercepts each request to log incoming headers and response status using MDC (Mapped Diagnostic Context).
     * Wraps the request and response using ContentCaching wrappers to capture their content.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String traceId = request.getHeader(TRACK_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = generarNuevoTraceId(); // Genera uno nuevo si no viene en el header
        }
        MDC.put(TRACK_ID_HEADER, traceId);
        MDC.put(REMOTE_HOST, request.getRemoteAddr());

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        filterChain.doFilter(wrappedRequest, wrappedResponse);
        logRequestDetails(wrappedRequest, wrappedResponse);

        wrappedResponse.copyBodyToResponse();
        MDC.clear();
    }

    private String generarNuevoTraceId() {
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * Logs the method, URL, selected headers, and response status.
     * Adjust as needed to include additional log info.
     */
    private void logRequestDetails(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        Enumeration<String> headerNames = request.getHeaderNames();
        List<String> headersPermitidos = Arrays.asList(TRACK_ID_HEADER, "user-agent", "host", "origin");

        StringBuilder headersValues = new StringBuilder();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (headersPermitidos.stream().anyMatch(h -> h.equalsIgnoreCase(headerName))) {
                headersValues.append('"')
                        .append(headerName)
                        .append("\": \"")
                        .append(request.getHeader(headerName))
                        .append("\", ");
            }
        }

        String logMessage = String.format(
                "Request URL: %s | Method: %s | Headers: {%s} | Response Status: %d",
                request.getRequestURL(),
                request.getMethod(),
                headersValues.toString(),
                response.getStatus()
        );

        if (response.getStatus() == 200) {
            log.info(logMessage);
        } else {
            log.error(logMessage);
        }
    }
}