package com.pneumaliback.www.controller;

import com.pneumaliback.www.dto.AuthResponse;
import com.pneumaliback.www.dto.LoginRequest;
import com.pneumaliback.www.dto.RefreshTokenRequest;
import com.pneumaliback.www.dto.RegisterRequest;
import com.pneumaliback.www.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentification", description = "API d'authentification et d'inscription")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouvel utilisateur", description = "Permet à un utilisateur de s'inscrire")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Tentative d'inscription pour l'email: {}", request.email());
        AuthResponse response = authService.register(request);
        log.info("Inscription réussie pour l'email: {}", request.email());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur", description = "Permet à un utilisateur de se connecter")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Tentative de connexion pour l'email: {}", request.email());
        AuthResponse response = authService.login(request);
        log.info("Connexion réussie pour l'email: {}", request.email());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Renouvellement du token", description = "Permet de renouveler le token d'accès")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Renouvellement de token demandé");
        AuthResponse response = authService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Déconnexion", description = "Permet à un utilisateur de se déconnecter")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String refreshToken = authHeader.substring(7);
            authService.logout(refreshToken);
            log.info("Déconnexion réussie");
        }
        return ResponseEntity.ok().build();
    }
}
