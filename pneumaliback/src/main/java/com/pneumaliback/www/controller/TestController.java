package com.pneumaliback.www.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Test", description = "API de test pour vérifier la sécurité")
@SecurityRequirement(name = "bearerAuth")
public class TestController {
    
    @GetMapping("/public")
    @Operation(summary = "Endpoint public", description = "Accessible sans authentification")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("Cet endpoint est public - accessible à tous");
    }
    
    @GetMapping("/authenticated")
    @Operation(summary = "Endpoint authentifié", description = "Accessible uniquement aux utilisateurs connectés")
    public ResponseEntity<String> authenticatedEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("Utilisateur authentifié: {}", username);
        return ResponseEntity.ok("Bonjour " + username + " ! Vous êtes authentifié.");
    }
    
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Endpoint admin", description = "Accessible uniquement aux administrateurs")
    public ResponseEntity<String> adminEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("Accès admin par: {}", username);
        return ResponseEntity.ok("Bonjour " + username + " ! Vous avez accès aux fonctionnalités admin.");
    }
    
    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Endpoint client", description = "Accessible uniquement aux clients")
    public ResponseEntity<String> clientEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("Accès client par: {}", username);
        return ResponseEntity.ok("Bonjour " + username + " ! Vous avez accès aux fonctionnalités client.");
    }
    
    @GetMapping("/influenceur")
    @PreAuthorize("hasRole('INFLUENCEUR')")
    @Operation(summary = "Endpoint influenceur", description = "Accessible uniquement aux influenceurs")
    public ResponseEntity<String> influenceurEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("Accès influenceur par: {}", username);
        return ResponseEntity.ok("Bonjour " + username + " ! Vous avez accès aux fonctionnalités influenceur.");
    }
}
