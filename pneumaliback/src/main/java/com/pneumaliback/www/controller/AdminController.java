package com.pneumaliback.www.controller;

import com.pneumaliback.www.entity.User;
import com.pneumaliback.www.enums.Role;
import com.pneumaliback.www.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Administration", description = "API d'administration - Accès admin uniquement")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    @Operation(summary = "Liste des utilisateurs", description = "Récupère la liste de tous les utilisateurs")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Récupération de la liste des utilisateurs par l'admin");
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Détails d'un utilisateur", description = "Récupère les détails d'un utilisateur spécifique")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("Récupération des détails de l'utilisateur ID: {}", id);
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/role/{role}")
    @Operation(summary = "Utilisateurs par rôle", description = "Récupère tous les utilisateurs d'un rôle spécifique")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable Role role) {
        log.info("Récupération des utilisateurs avec le rôle: {}", role);
        List<User> users = userRepository.findAll().stream()
                .filter(user -> user.getRole() == role)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/role")
    @Operation(summary = "Modifier le rôle d'un utilisateur", description = "Change le rôle d'un utilisateur")
    public ResponseEntity<User> updateUserRole(@PathVariable Long id, @RequestParam Role newRole) {
        log.info("Modification du rôle de l'utilisateur ID: {} vers: {}", id, newRole);
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setRole(newRole);
            User savedUser = userRepository.save(user);
            log.info("Rôle de l'utilisateur {} modifié vers: {}", user.getEmail(), newRole);
            return ResponseEntity.ok(savedUser);
        }

        return ResponseEntity.notFound().build();
    }

    @PutMapping("/users/{id}/status")
    @Operation(summary = "Modifier le statut d'un utilisateur", description = "Active ou désactive un compte utilisateur")
    public ResponseEntity<User> updateUserStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        log.info("Modification du statut de l'utilisateur ID: {} vers: {}", id, enabled);
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEnabled(enabled);
            User savedUser = userRepository.save(user);
            log.info("Statut de l'utilisateur {} modifié vers: {}", user.getEmail(), enabled);
            return ResponseEntity.ok(savedUser);
        }

        return ResponseEntity.notFound().build();
    }

    @PutMapping("/users/{id}/lock")
    @Operation(summary = "Verrouiller/Déverrouiller un compte", description = "Verrouille ou déverrouille un compte utilisateur")
    public ResponseEntity<User> toggleUserLock(@PathVariable Long id) {
        log.info("Modification du verrouillage de l'utilisateur ID: {}", id);
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean newLockStatus = !user.isAccountNonLocked();
            user.setAccountNonLocked(newLockStatus);

            if (newLockStatus) {
                user.setFailedAttempts(0);
                user.setLockTime(null);
                log.info("Compte de l'utilisateur {} déverrouillé", user.getEmail());
            } else {
                log.info("Compte de l'utilisateur {} verrouillé", user.getEmail());
            }

            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Statistiques des utilisateurs", description = "Récupère les statistiques des utilisateurs")
    public ResponseEntity<UserStats> getUserStats() {
        log.info("Récupération des statistiques des utilisateurs");
        List<User> allUsers = userRepository.findAll();

        UserStats stats = new UserStats(
                allUsers.size(),
                allUsers.stream().filter(u -> u.getRole() == Role.ADMIN).count(),
                allUsers.stream().filter(u -> u.getRole() == Role.CLIENT).count(),
                allUsers.stream().filter(u -> u.getRole() == Role.INFLUENCEUR).count(),
                allUsers.stream().filter(User::isEnabled).count(),
                allUsers.stream().filter(u -> !u.isAccountNonLocked()).count());

        return ResponseEntity.ok(stats);
    }

    public record UserStats(
            long totalUsers,
            long adminCount,
            long clientCount,
            long influenceurCount,
            long activeUsers,
            long lockedUsers) {
    }
}
