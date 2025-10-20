package com.pneumaliback.www.controller;

import com.pneumaliback.www.entity.User;
import com.pneumaliback.www.enums.Role;
import com.pneumaliback.www.repository.UserRepository;
import com.pneumaliback.www.entity.Order;
import com.pneumaliback.www.entity.Commission;
import com.pneumaliback.www.enums.CommissionStatus;
import com.pneumaliback.www.repository.OrderRepository;
import com.pneumaliback.www.repository.CommissionRepository;
import com.pneumaliback.www.service.OrderService;
import com.pneumaliback.www.service.CommissionService;
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
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Administration", description = "API d'administration - Accès admin uniquement")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CommissionRepository commissionRepository;
    private final OrderService orderService;
    private final CommissionService commissionService;

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

    @PutMapping("/orders/{orderId}/confirm")
    @Operation(summary = "Confirmer une commande")
    public ResponseEntity<Order> confirmOrder(@PathVariable Long orderId) {
        Optional<Order> opt = orderRepository.findById(orderId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Order order = opt.get();
        orderService.confirm(order);
        Order saved = orderRepository.save(order);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/commissions")
    @Operation(summary = "Liste des commissions")
    public ResponseEntity<List<Commission>> listAllCommissions() {
        return ResponseEntity.ok(commissionRepository.findAll());
    }

    @GetMapping("/commissions/influenceur/{influenceurId}")
    @Operation(summary = "Commissions par influenceur")
    public ResponseEntity<List<Commission>> listCommissionsByInfluenceur(@PathVariable Long influenceurId) {
        return ResponseEntity.ok(commissionRepository.findByInfluenceurId(influenceurId));
    }

    @GetMapping("/commissions/influenceur/{influenceurId}/balance")
    @Operation(summary = "Solde commissions influenceur")
    public ResponseEntity<BalanceDTO> getInfluenceurBalance(@PathVariable Long influenceurId) {
        BigDecimal total = commissionRepository.sumByInfluenceur(influenceurId);
        BigDecimal paid = commissionRepository.sumByInfluenceurAndStatus(influenceurId, CommissionStatus.PAID);
        BigDecimal pending = commissionRepository.sumByInfluenceurAndStatus(influenceurId, CommissionStatus.PENDING);
        return ResponseEntity.ok(new BalanceDTO(total, paid, pending));
    }

    @PutMapping("/commissions/{commissionId}/pay")
    @Operation(summary = "Marquer une commission comme payée")
    public ResponseEntity<Commission> payCommission(@PathVariable Long commissionId) {
        Optional<Commission> opt = commissionRepository.findById(commissionId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Commission c = opt.get();
        commissionService.markPaid(c);
        return ResponseEntity.ok(c);
    }

    public record BalanceDTO(BigDecimal total, BigDecimal paid, BigDecimal pending) {}
}
