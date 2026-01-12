package com.MuscleHead.MuscleHead.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LandingPageController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> landingPage() {
        return ResponseEntity.ok(Map.of(
                "message", "Welcome to MuscleHead API",
                "status", "active"));
    }
}
