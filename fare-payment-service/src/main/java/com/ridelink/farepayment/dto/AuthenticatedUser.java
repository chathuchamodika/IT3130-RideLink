package com.ridelink.farepayment.dto;

import org.springframework.security.core.Authentication;

/** Carries the caller identity (JWT subject) + role into the service layer. */
public record AuthenticatedUser(String userId, boolean admin) {

    public static AuthenticatedUser from(org.apache.tomcat.util.net.openssl.ciphers.Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(granted -> granted.getAuthority().equals("ROLE_ADMIN"));
        return new AuthenticatedUser(authentication.getName(), admin); // name == JWT "sub"
    }
}