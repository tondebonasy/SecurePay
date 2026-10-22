package com.moneyguard.api.web;

import com.moneyguard.api.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory fixed-window limiter (per IP + path prefix).
 * Production note: replace with Redis/Bucket4j for multi-instance correctness.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final int limitPerMinute;

    private static class Window {
        long windowStartEpochMinute;
        int count;
    }

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitingFilter(AppProperties props) {
        this.limitPerMinute = props.getRateLimit().getRequestsPerMinute();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String p = request.getRequestURI();
        return p.startsWith("/v1/health") || p.startsWith("/swagger-ui") || p.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        String key = ip;

        long minute = Instant.now().getEpochSecond() / 60;
        if (windows.size() > 10000) windows.entrySet().removeIf(e -> e.getValue().windowStartEpochMinute < minute - 1);
        if (windows.size() > 20000 && !windows.containsKey(key)) { response.setStatus(429); return; }
        Window w = windows.computeIfAbsent(key, k -> new Window());

        synchronized (w) {
            if (w.windowStartEpochMinute != minute) {
                w.windowStartEpochMinute = minute;
                w.count = 0;
            }
            w.count++;
            if (w.count > limitPerMinute) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"error\":{\"code\":\"RATE_LIMITED\",\"message\":\"Too many requests\"}}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
