package com.sabarno.chatomania.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sabarno.chatomania.service.RateLimiterService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    @Autowired
    private RateLimiterService rateLimiter;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String key = resolveKey(request);

        if (!rateLimiter.allow("rest:" + key)) {
            log.warn(
                    "HTTP_RATE_LIMIT user={} ip={} path={}",
                    key, request.getRemoteAddr(), request.getRequestURI());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded");
            return;
        }

        chain.doFilter(request, response);
    }

    private String resolveKey(HttpServletRequest request) {
        if (request.getUserPrincipal() != null) {
            return request.getUserPrincipal().getName();
        }
        return request.getRemoteAddr();
    }
}
