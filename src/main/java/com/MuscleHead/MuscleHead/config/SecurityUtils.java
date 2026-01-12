package com.MuscleHead.MuscleHead.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class to access the current authenticated user's information
 */
public class SecurityUtils {

    /**
     * Gets the Cognito subject (sub) of the currently authenticated user
     * 
     * @return The Cognito sub ID, or null if not authenticated
     */
    public static String getCurrentUserSub() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        }

        return null;
    }

    /**
     * Checks if a user is currently authenticated
     * 
     * @return true if authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }
}
