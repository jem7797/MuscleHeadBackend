package com.MuscleHead.MuscleHead.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.MuscleHead.MuscleHead.User.User;
import com.MuscleHead.MuscleHead.User.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Runs after JWT authentication. Blocks users under 13 from all authenticated
 * API access. Users who exist in the DB but are under 13 receive 403 Forbidden.
 */
@Component
public class UnderAgeAccessFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(UnderAgeAccessFilter.class);
    private static final int MIN_AGE = 13;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String subId = auth.getPrincipal().toString();
        var userOpt = userRepository.findById(subId);
        if (userOpt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        User user = userOpt.get();
        if (user.getAgeInYears() < MIN_AGE) {
            logger.warn("Blocked under-age user (sub={}) from API access", subId);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("status", HttpStatus.FORBIDDEN.value());
            errorResponse.put("error", "Forbidden");
            errorResponse.put("message", "Access denied. You must be at least 13 years old to use this service.");
            errorResponse.put("path", request.getRequestURI());

            objectMapper.writeValue(response.getOutputStream(), errorResponse);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
