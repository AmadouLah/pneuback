package com.pneumaliback.www.controller;

import com.pneumaliback.www.entity.Address;
import com.pneumaliback.www.entity.Delivery;
import com.pneumaliback.www.enums.DeliveryStatus;
import com.pneumaliback.www.repository.DeliveryRepository;
import com.pneumaliback.www.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@Tag(name = "Livraisons")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final DeliveryRepository deliveryRepository;

    @GetMapping("/quote")
    @Operation(summary = "Devis des frais de livraison pour une zone")
    public ResponseEntity<BigDecimal> quote(@RequestParam String zone) {
        return ResponseEntity.ok(deliveryService.quoteShippingFee(zone));
    }

    @PutMapping("/{deliveryId}/status")
    @Operation(summary = "Mettre à jour le statut d'une livraison")
    public ResponseEntity<Delivery> updateStatus(@PathVariable Long deliveryId, @RequestParam DeliveryStatus status) {
        Delivery d = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Livraison introuvable"));
        return ResponseEntity.ok(deliveryService.updateStatus(d, status));
    }
}
