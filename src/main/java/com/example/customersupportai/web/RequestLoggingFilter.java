package com.example.customersupportai.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private final boolean includeRequestBody;
    private final int maxBodyLength;

    public RequestLoggingFilter(
            @Value("${customer-support.logging.include-request-body:true}") boolean includeRequestBody,
            @Value("${customer-support.logging.max-body-length:4000}") int maxBodyLength
    ) {
        this.includeRequestBody = includeRequestBody;
        this.maxBodyLength = maxBodyLength;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString();
        long start = System.currentTimeMillis();
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

        response.setHeader("X-Request-Id", requestId);
        log.info("request.start id={} method={} path={} query={} remote={}",
                requestId,
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                request.getRemoteAddr());

        try {
            filterChain.doFilter(wrappedRequest, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            String body = includeRequestBody ? requestBody(wrappedRequest) : "<disabled>";
            log.info("request.end id={} status={} durationMs={} body={}",
                    requestId,
                    response.getStatus(),
                    duration,
                    body);
        }
    }

    private String requestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return "";
        }
        String body = new String(content, StandardCharsets.UTF_8)
                .replace('\n', ' ')
                .replace('\r', ' ')
                .trim();
        if (body.length() <= maxBodyLength) {
            return body;
        }
        return body.substring(0, maxBodyLength) + "...";
    }
}
