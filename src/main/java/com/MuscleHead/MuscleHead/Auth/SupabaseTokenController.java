package com.MuscleHead.MuscleHead.Auth;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.MuscleHead.MuscleHead.config.SecurityUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@RestController
@RequestMapping("api/auth")
public class SupabaseTokenController {

    private static final long ONE_HOUR_MS = 60L * 60L * 1000L;

    @Value("${supabase.jwt.secret}")
    private String supabaseJwtSecret;

    @PostMapping("/supabase-token")
    public ResponseEntity<Map<String, String>> mintSupabaseToken() {
        String userId = SecurityUtils.getCurrentUserSub();
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Date now = new Date();
        Date expiry = new Date(now.getTime() + ONE_HOUR_MS);
        SecretKey key = Keys.hmacShaKeyFor(supabaseJwtSecret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .setSubject(userId)
                .claim("sub", userId)
                .claim("role", "authenticated")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return ResponseEntity.ok(Map.of("token", token));
    }
}
