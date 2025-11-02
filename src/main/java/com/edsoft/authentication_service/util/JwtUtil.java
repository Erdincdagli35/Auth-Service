package com.edsoft.authentication_service.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret:change-this-in-dev}")
    private String secret;

    private Key key;

    @PostConstruct
    public void init() {
        // Eğer secret "base64:" ile başlıyorsa base64 decode et, değilse direkt byte'larını al.
        // Prod ortamında JWT_SECRET olarak base64 encoding kullanman önerilir.
        if (secret == null || secret.isBlank() || "change-this-in-dev".equals(secret)) {
            // Dev fallback (UYARI: production için kullanma)
            byte[] defaultKey = "change-this-in-dev-change-this-in-dev-change-this-in-dev-change-this!".getBytes(StandardCharsets.UTF_8);
            this.key = Keys.hmacShaKeyFor(defaultKey);
        } else if (secret.startsWith("base64:")) {
            byte[] decoded = Base64.getDecoder().decode(secret.substring("base64:".length()));
            this.key = Keys.hmacShaKeyFor(decoded);
        } else {
            // Eğer kullanıcı zaten base64 vermediyse ve düz string verdiyse, utf-8 byte'larını kullan.
            // NOT: production'da base64 ile 64 byte'lık key kullan.
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            this.key = Keys.hmacShaKeyFor(keyBytes);
        }
    }


    public String generateToken(String subject, long expMs) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expMs);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS512) // 0.11.x API
                .compact();
    }
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            // Log ekleyebilirsin: ex.getMessage()
            return false;
        }
    }
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}