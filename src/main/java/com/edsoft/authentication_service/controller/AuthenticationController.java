package com.edsoft.authentication_service.controller;

import com.edsoft.authentication_service.model.AppUser;
import com.edsoft.authentication_service.repository.AppUserRepository;
import com.edsoft.authentication_service.service.RefreshTokenService;
import com.edsoft.authentication_service.util.JwtUtil;
import lombok.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.jwt.access-exp-ms}")
    private long accessExpMs;

    @Value("${app.jwt.refresh-exp-ms}")
    private long refreshExpMs;

    public AuthenticationController(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AppUser user) {
        if (appUserRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User already exists"));
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(Set.of("ROLE_CUSTOMER"));
        }

        appUserRepository.save(user);
        return ResponseEntity.ok(Map.of("msg", "User registered successfully"));
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AppUser user) {
        AppUser existing = appUserRepository.findByUsername(user.getUsername()).orElse(null);
        if (existing == null || !passwordEncoder.matches(user.getPassword(), existing.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        String access = jwtUtil.generateToken(existing.getUsername(), accessExpMs);
        String refresh = UUID.randomUUID().toString();
        refreshTokenService.store(refresh, existing.getId(), refreshExpMs);

        return ResponseEntity.ok(Map.of(
                "accessToken", access,
                "refreshToken", refresh,
                "roles", existing.getRoles()
        ));
    }

    // REFRESH
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String refresh = body.get("refreshToken");
        if (refresh == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing refresh token"));
        }

        Optional<Long> opt = refreshTokenService.getUserIdFor(refresh);
        if (opt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid refresh token"));
        }

        AppUser user = (AppUser) appUserRepository.findById(opt.get()).orElseThrow();
        String access = jwtUtil.generateToken(user.getUsername(), accessExpMs);

        return ResponseEntity.ok(Map.of("accessToken", access));
    }

    // LOGOUT
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> body) {
        String refresh = body.get("refreshToken");
        if (refresh != null) refreshTokenService.revoke(refresh);
        return ResponseEntity.ok(Map.of("msg", "Logged out"));
    }

}
