package com.ridelink.ride_management_service.auth;

import com.ridelink.ride_management_service.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody Map<String, String> request) {

        String username = request.get("username");
        String password = request.get("password");

        // Temporary test credentials
        if ("admin".equals(username) && "admin123".equals(password)) {

            String token = jwtService.generateToken(username);

            return ResponseEntity.ok(
                    Map.of(
                            "token", token,
                            "username", username
                    )
            );
        }

        return ResponseEntity
                .status(401)
                .body(Map.of("error", "Invalid username or password"));
    }
}