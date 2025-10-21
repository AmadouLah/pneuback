package com.pneumaliback.www.controller;

import com.pneumaliback.www.entity.Favori;
import com.pneumaliback.www.entity.User;
import com.pneumaliback.www.repository.UserRepository;
import com.pneumaliback.www.service.FavoriService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favoris")
@RequiredArgsConstructor
@Tag(name = "Favoris")
public class FavoriController {

    private final FavoriService favoriService;
    private final UserRepository userRepository;

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
    }

    @GetMapping
    @Operation(summary = "Lister favoris d'un utilisateur")
    public ResponseEntity<List<Favori>> list(@RequestParam Long userId) {
        return ResponseEntity.ok(favoriService.listByUser(getUser(userId)));
    }

    @PostMapping
    @Operation(summary = "Ajouter un favori")
    public ResponseEntity<Favori> add(@RequestParam Long userId, @RequestParam Long productId) {
        return ResponseEntity.ok(favoriService.add(getUser(userId), productId));
    }

    @DeleteMapping
    @Operation(summary = "Supprimer un favori")
    public ResponseEntity<Void> remove(@RequestParam Long userId, @RequestParam Long productId) {
        favoriService.remove(getUser(userId), productId);
        return ResponseEntity.noContent().build();
    }
}
