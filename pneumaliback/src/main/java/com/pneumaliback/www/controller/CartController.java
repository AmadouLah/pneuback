package com.pneumaliback.www.controller;

import com.pneumaliback.www.entity.Cart;
import com.pneumaliback.www.entity.User;
import com.pneumaliback.www.repository.UserRepository;
import com.pneumaliback.www.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Panier")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
    }

    @GetMapping
    @Operation(summary = "Obtenir le panier d'un utilisateur")
    public ResponseEntity<Cart> getOrCreate(@RequestParam Long userId) {
        return ResponseEntity.ok(cartService.getOrCreate(getUser(userId)));
    }

    @PostMapping("/items")
    @Operation(summary = "Ajouter un article au panier")
    public ResponseEntity<Cart> addItem(@RequestParam Long userId,
                                        @RequestParam Long productId,
                                        @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.addItem(getUser(userId), productId, quantity));
    }

    @PutMapping("/items")
    @Operation(summary = "Mettre à jour la quantité d'un article")
    public ResponseEntity<Cart> updateItem(@RequestParam Long userId,
                                           @RequestParam Long productId,
                                           @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.updateItem(getUser(userId), productId, quantity));
    }

    @DeleteMapping
    @Operation(summary = "Vider le panier")
    public ResponseEntity<Cart> clear(@RequestParam Long userId) {
        return ResponseEntity.ok(cartService.clear(getUser(userId)));
    }
}
