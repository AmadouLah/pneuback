package com.pneumaliback.www.controller;

import com.pneumaliback.www.entity.Address;
import com.pneumaliback.www.entity.Order;
import com.pneumaliback.www.entity.User;
import com.pneumaliback.www.repository.AddressRepository;
import com.pneumaliback.www.repository.OrderRepository;
import com.pneumaliback.www.repository.UserRepository;
import com.pneumaliback.www.service.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@Tag(name = "Checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;

    public record CheckoutRequest(Long userId, Long addressId, String zone, String promoCode) {}

    @PostMapping
    @Operation(summary = "Créer une commande à partir du panier")
    public ResponseEntity<Order> createOrder(@RequestBody CheckoutRequest req) {
        if (req == null || req.userId() == null || req.addressId() == null) {
            throw new IllegalArgumentException("Paramètres de checkout invalides");
        }
        User user = userRepository.findById(req.userId()).orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
        Address address = addressRepository.findById(req.addressId()).orElseThrow(() -> new IllegalArgumentException("Adresse non trouvée"));
        Order order = checkoutService.createOrder(user, address, req.zone(), req.promoCode());
        // Order is already saved inside service; ensure managed state is returned
        return ResponseEntity.ok(order);
    }
}
