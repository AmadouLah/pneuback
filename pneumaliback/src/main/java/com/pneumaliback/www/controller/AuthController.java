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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    
    private ResponseEntity<?> handleException(Exception e) {
        String msg = e.getMessage();
        if (e instanceof IllegalArgumentException) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", msg != null ? msg : "Requête invalide"));
        }
        return ResponseEntity.internalServerError()
                .body(java.util.Map.of("error", "Erreur interne du serveur", "message", msg));
    }
    
    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouvel utilisateur", description = "Permet à un utilisateur de s'inscrire")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inscription réussie", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            log.info("Tentative d'inscription pour l'email: {}", request.email());
            MessageResponse response = authService.register(request);
            log.info("Inscription réussie pour l'email: {}", request.email());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PostMapping("/forgot-password/request")
    @Operation(summary = "Demander réinitialisation", description = "Envoie un code de réinitialisation (cooldown 20s)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Demande envoyée", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            log.info("Demande de réinitialisation pour l'email: {}", request.email());
            MessageResponse response = authService.requestPasswordReset(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PostMapping("/forgot-password/confirm")
    @Operation(summary = "Confirmer réinitialisation", description = "Valide le code et met à jour le mot de passe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mot de passe réinitialisé", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> confirmPasswordReset(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            log.info("Confirmation de réinitialisation pour l'email: {}", request.email());
            MessageResponse response = authService.confirmPasswordReset(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e);
        }
    }
    
    @PostMapping("/verify")
    @Operation(summary = "Vérification du compte", description = "Active le compte avec le code reçu par email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compte vérifié", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> verify(@Valid @RequestBody VerificationRequest request) {
        try {
            log.info("Vérification de compte pour l'email: {}", request.email());
            AuthResponse response = authService.verifyEmail(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e);
        }
    }
    
    @PostMapping("/resend")
    @Operation(summary = "Renvoi du code", description = "Renvoyer un nouveau code après 20 secondes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Code renvoyé", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> resend(@Valid @RequestBody ResendVerificationRequest request) {
        try {
            log.info("Renvoi de code pour l'email: {}", request.email());
            MessageResponse response = authService.resendVerificationCode(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e);
        }
    }
    
    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur", description = "Permet à un utilisateur de se connecter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentification réussie", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Identifiants invalides", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Tentative de connexion pour l'email: {}", request.email());
            AuthResponse response = authService.login(request);
            log.info("Connexion réussie pour l'email: {}", request.email());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage(), "message", "Identifiants invalides"));
        } catch (Exception e) {
            return handleException(e);
        }
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Renouvellement du token", description = "Permet de renouveler le token d'accès")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token renouvelé", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            log.debug("Renouvellement de token demandé");
            AuthResponse response = authService.refreshToken(request.refreshToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e);
        }
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Déconnexion", description = "Permet à un utilisateur de se déconnecter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Déconnexion réussie", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String refreshToken = authHeader.substring(7);
                authService.logout(refreshToken);
                log.info("Déconnexion réussie");
            }
            return ResponseEntity.ok(java.util.Map.of("message", "Déconnexion réussie"));
        } catch (Exception e) {
            return handleException(e);
        }
    }
}

