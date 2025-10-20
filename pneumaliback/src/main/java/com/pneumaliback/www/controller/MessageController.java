package com.pneumaliback.www.controller;

import com.pneumaliback.www.dto.MessageDTO;
import com.pneumaliback.www.entity.Message;
import com.pneumaliback.www.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Messages", description = "Messagerie entre utilisateurs")
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    @Operation(summary = "Envoyer un message")
    public ResponseEntity<Message> envoyer(@RequestBody MessageDTO dto) {
        return ResponseEntity.ok(messageService.send(dto.authorId(), dto.recipientId(), dto.content()));
    }

    @GetMapping("/inbox/{destinataireId}")
    @Operation(summary = "Boîte de réception")
    public ResponseEntity<Page<Message>> inbox(@PathVariable Long destinataireId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(messageService.inbox(destinataireId, pageable));
    }

    @GetMapping("/outbox/{auteurId}")
    @Operation(summary = "Boîte d'envoi")
    public ResponseEntity<Page<Message>> outbox(@PathVariable Long auteurId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(messageService.outbox(auteurId, pageable));
    }

    @GetMapping("/conversation")
    @Operation(summary = "Conversation entre deux utilisateurs")
    public ResponseEntity<Page<Message>> conversation(@RequestParam Long u1,
            @RequestParam Long u2,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(messageService.conversation(u1, u2, pageable));
    }

    @PostMapping("/conversation/lire")
    @Operation(summary = "Marquer comme lue la conversation reçue d'un interlocuteur")
    public ResponseEntity<Integer> marquerLue(@RequestParam Long destinataireId, @RequestParam Long auteurId) {
        int updated = messageService.markConversationRead(destinataireId, auteurId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/non-lus")
    @Operation(summary = "Compteur des non-lus par interlocuteur pour un destinataire")
    public ResponseEntity<Map<Long, Long>> nonLus(@RequestParam Long destinataireId) {
        return ResponseEntity.ok(messageService.unreadByInterlocutor(destinataireId));
    }

    @GetMapping("/threads")
    @Operation(summary = "Liste des threads: dernier message + compteur non lus")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> threads(@RequestParam Long userId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(messageService.threadList(userId, q, limit));
    }
}
