package com.pneumaliback.www.controller;

import com.pneumaliback.www.dto.NotificationRechercheDTO;
import com.pneumaliback.www.entity.Notification;
import com.pneumaliback.www.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{userId}")
    @Operation(summary = "Liste des notifications d'un utilisateur")
    public ResponseEntity<Page<Notification>> list(@PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(notificationService.list(userId, pageable));
    }

    @GetMapping("/{userId}/non-lues")
    @Operation(summary = "Liste des notifications non lues d'un utilisateur")
    public ResponseEntity<Page<Notification>> nonLues(@PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(notificationService.unread(userId, pageable));
    }

    @PostMapping("/rechercher")
    @Operation(summary = "Recherche avancée de notifications")
    public ResponseEntity<Page<Notification>> rechercher(@RequestBody NotificationRechercheDTO criteres,
            Pageable pageable) {
        return ResponseEntity.ok(notificationService.search(criteres, pageable));
    }

    @PostMapping("/{id}/lu")
    @Operation(summary = "Marquer une notification comme lue")
    public ResponseEntity<Void> marquerLu(@PathVariable Long id, @RequestParam Long userId) {
        notificationService.markAsRead(id, userId);
        // Envoyer le nouveau count via WebSocket
        notificationService.sendUnreadCount(
                notificationService.findUserById(userId));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/marquer-toutes-lues")
    @Operation(summary = "Marquer toutes les notifications comme lues")
    public ResponseEntity<Integer> marquerToutesCommeLues(@PathVariable Long userId) {
        int count = notificationService.markAllAsRead(userId);
        // Envoyer le nouveau count via WebSocket (0 car toutes marquées comme lues)
        notificationService.sendUnreadCount(
                notificationService.findUserById(userId));
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{userId}/count/non-lues")
    @Operation(summary = "Compter les notifications non lues")
    public ResponseEntity<Long> countNonLues(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.countUnread(userId));
    }

    @GetMapping("/{userId}/count/non-lues/{type}")
    @Operation(summary = "Compter les notifications non lues par type")
    public ResponseEntity<Long> countNonLuesParType(@PathVariable Long userId, @PathVariable String type) {
        return ResponseEntity.ok(notificationService.countUnreadByType(userId, type));
    }

    @GetMapping("/{userId}/statistiques")
    @Operation(summary = "Statistiques des notifications par type")
    public ResponseEntity<Map<String, Long>> statistiquesParType(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.statisticsByType(userId));
    }

    @DeleteMapping("/{userId}/nettoyer")
    @Operation(summary = "Nettoyer les anciennes notifications")
    public ResponseEntity<Integer> nettoyerAnciennes(@PathVariable Long userId,
            @RequestParam(defaultValue = "30") int joursRetention) {
        int count = notificationService.cleanupOld(userId, joursRetention);
        return ResponseEntity.ok(count);
    }

    // ===== ENDPOINTS WEBSOCKET =====

    /**
     * Marque une notification comme lue via WebSocket
     * Endpoint: /app/notifications/{userId}/marquer-lue
     */
    @MessageMapping("/notifications/{userId}/marquer-lue")
    @PreAuthorize("isAuthenticated()")
    public void marquerLuWebSocket(@DestinationVariable Long userId, @Payload Map<String, Object> payload) {
        try {
            Long notificationId = Long.valueOf(payload.get("notificationId").toString());
            log.info("WebSocket: Marquer notification {} comme lue pour utilisateur {}", notificationId, userId);

            notificationService.markAsRead(notificationId, userId);

            // Envoyer le nouveau count via WebSocket
            notificationService.sendUnreadCount(
                    notificationService.findUserById(userId));
        } catch (Exception e) {
            log.error("Erreur lors du marquage de notification via WebSocket: {}", e.getMessage());
        }
    }

    /**
     * Marque toutes les notifications comme lues via WebSocket
     * Endpoint: /app/notifications/{userId}/marquer-toutes-lues
     */
    @MessageMapping("/notifications/{userId}/marquer-toutes-lues")
    @PreAuthorize("isAuthenticated()")
    public void marquerToutesLuesWebSocket(@DestinationVariable Long userId) {
        try {
            log.info("WebSocket: Marquer toutes les notifications comme lues pour utilisateur {}", userId);

            notificationService.markAllAsRead(userId);

            // Envoyer le nouveau count via WebSocket (0 car toutes marquées comme lues)
            notificationService.sendUnreadCount(
                    notificationService.findUserById(userId));
        } catch (Exception e) {
            log.error("Erreur lors du marquage de toutes les notifications via WebSocket: {}", e.getMessage());
        }
    }

    /**
     * Demande le count actuel des notifications non lues via WebSocket
     * Endpoint: /app/notifications/{userId}/demander-count
     */
    @MessageMapping("/notifications/{userId}/demander-count")
    @PreAuthorize("isAuthenticated()")
    public void demanderCountWebSocket(@DestinationVariable Long userId) {
        try {
            log.debug("WebSocket: Demande count notifications non lues pour utilisateur {}", userId);

            // Envoyer le count actuel via WebSocket
            notificationService.sendUnreadCount(
                    notificationService.findUserById(userId));
        } catch (Exception e) {
            log.error("Erreur lors de la demande de count via WebSocket: {}", e.getMessage());
        }
    }

    /**
     * Souscription aux notifications d'un utilisateur
     * Endpoint: /app/notifications/{userId}/souscrire
     */
    @MessageMapping("/notifications/{userId}/souscrire")
    @PreAuthorize("isAuthenticated()")
    public void souscrireNotifications(@DestinationVariable Long userId) {
        try {
            log.info("WebSocket: Souscription aux notifications pour utilisateur {}", userId);

            // Envoyer immédiatement le count actuel
            notificationService.sendUnreadCount(
                    notificationService.findUserById(userId));
        } catch (Exception e) {
            log.error("Erreur lors de la souscription aux notifications via WebSocket: {}", e.getMessage());
        }
    }

}
