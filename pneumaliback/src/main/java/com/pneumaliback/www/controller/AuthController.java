package com.pneumaliback.www.controller;

import com.pneumaliback.www.dto.AuthResponse;
import com.pneumaliback.www.dto.LoginRequest;
import com.pneumaliback.www.dto.RefreshTokenRequest;
import com.pneumaliback.www.dto.RegisterRequest;
import com.pneumaliback.www.dto.MessageResponse;
import com.pneumaliback.www.dto.ResendVerificationRequest;
import com.pneumaliback.www.dto.ForgotPasswordRequest;
import com.pneumaliback.www.dto.ResetPasswordRequest;
import com.pneumaliback.www.dto.VerificationRequest;
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
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Tentative d'inscription pour l'email: {}", request.email());
        MessageResponse response = authService.register(request);
        log.info("Inscription réussie pour l'email: {}", request.email());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password/request")
    @Operation(summary = "Demander réinitialisation", description = "Envoie un code de réinitialisation (cooldown 20s)")
    public ResponseEntity<MessageResponse> requestPasswordReset(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Demande de réinitialisation pour l'email: {}", request.email());
        MessageResponse response = authService.requestPasswordReset(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password/confirm")
    @Operation(summary = "Confirmer réinitialisation", description = "Valide le code et met à jour le mot de passe")
    public ResponseEntity<MessageResponse> confirmPasswordReset(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Confirmation de réinitialisation pour l'email: {}", request.email());
        MessageResponse response = authService.confirmPasswordReset(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/verify")
    @Operation(summary = "Vérification du compte", description = "Active le compte avec le code reçu par email")
    public ResponseEntity<AuthResponse> verify(@Valid @RequestBody VerificationRequest request) {
        log.info("Vérification de compte pour l'email: {}", request.email());
        AuthResponse response = authService.verifyEmail(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/resend")
    @Operation(summary = "Renvoi du code", description = "Renvoyer un nouveau code après 20 secondes")
    public ResponseEntity<MessageResponse> resend(@Valid @RequestBody ResendVerificationRequest request) {
        log.info("Renvoi de code pour l'email: {}", request.email());
        MessageResponse response = authService.resendVerificationCode(request);
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
