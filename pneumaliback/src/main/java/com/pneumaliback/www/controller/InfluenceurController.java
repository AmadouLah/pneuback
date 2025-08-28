package com.pneumaliback.www.controller;

import com.pneumaliback.www.entity.User;
import com.pneumaliback.www.repository.UserRepository;
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

import java.util.Optional;

@RestController
@RequestMapping("/api/influenceur")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Influenceur", description = "API des fonctionnalités influenceur")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('INFLUENCEUR')")
public class InfluenceurController {

    private final UserRepository userRepository;

    @GetMapping("/profile")
    @Operation(summary = "Profil influenceur", description = "Récupère le profil de l'influenceur connecté")
    public ResponseEntity<User> getInfluenceurProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        log.info("Récupération du profil de l'influenceur: {}", email);
        Optional<User> user = userRepository.findByEmail(email);

        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile")
    @Operation(summary = "Mettre à jour le profil", description = "Met à jour le profil de l'influenceur connecté")
    public ResponseEntity<User> updateInfluenceurProfile(@RequestBody User updatedProfile) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        log.info("Mise à jour du profil de l'influenceur: {}", email);
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Mise à jour des champs autorisés
            user.setFirstName(updatedProfile.getFirstName());
            user.setLastName(updatedProfile.getLastName());
            user.setPhoneNumber(updatedProfile.getPhoneNumber());

            User savedUser = userRepository.save(user);
            log.info("Profil de l'influenceur {} mis à jour avec succès", email);
            return ResponseEntity.ok(savedUser);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Statistiques influenceur", description = "Récupère les statistiques de l'influenceur")
    public ResponseEntity<InfluenceurStats> getInfluenceurStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        log.info("Récupération des statistiques de l'influenceur: {}", email);

        // Ici vous pourriez ajouter la logique pour récupérer les vraies statistiques
        // comme le nombre de followers, posts, etc.
        InfluenceurStats stats = new InfluenceurStats(
                email,
                0L, // followers
                0L, // posts
                0L, // likes
                0L // comments
        );

        return ResponseEntity.ok(stats);
    }

    public record InfluenceurStats(
            String email,
            long followers,
            long posts,
            long likes,
            long comments) {
    }
}
