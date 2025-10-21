package com.pneumaliback.www.controller;

import com.pneumaliback.www.entity.Address;
import com.pneumaliback.www.entity.Delivery;
import com.pneumaliback.www.enums.DeliveryStatus;
import com.pneumaliback.www.repository.DeliveryRepository;
import com.pneumaliback.www.repository.AddressRepository;
import com.pneumaliback.www.repository.OrderRepository;
import com.pneumaliback.www.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import com.pneumaliback.www.dto.DeliveryCreateDTO;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@Tag(name = "Livraisons")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final DeliveryRepository deliveryRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;

    @GetMapping("/quote")
    @Operation(summary = "Devis des frais de livraison pour une zone")
    public ResponseEntity<BigDecimal> quote(@RequestParam String zone) {
        return ResponseEntity.ok(deliveryService.quoteShippingFee(zone));
    }

    @PostMapping
    @Operation(summary = "Créer une livraison pour une commande")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Delivery> create(@RequestBody DeliveryCreateDTO dto) {
        if (dto == null || dto.orderId() == null || dto.addressId() == null) {
            throw new IllegalArgumentException("Paramètres invalides");
        }
        var order = orderRepository.findById(dto.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable"));
        var address = addressRepository.findById(dto.addressId())
                .orElseThrow(() -> new IllegalArgumentException("Adresse introuvable"));
        BigDecimal fee = dto.shippingFee() != null ? dto.shippingFee() : deliveryService.quoteShippingFee(dto.zone());
        Delivery d = deliveryService.attachDelivery(order, address, dto.zone(), fee);
        return ResponseEntity.ok(d);
    }

    @PutMapping("/{deliveryId}/status")
    @Operation(summary = "Mettre à jour le statut d'une livraison")
    public ResponseEntity<Delivery> updateStatus(@PathVariable Long deliveryId, @RequestParam DeliveryStatus status) {
        Delivery d = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Livraison introuvable"));
        return ResponseEntity.ok(deliveryService.updateStatus(d, status));
    }
}
