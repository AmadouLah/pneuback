package com.pneumaliback.www.controller;

import com.pneumaliback.www.entity.Promotion;
import com.pneumaliback.www.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotions")
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping("/validate")
    @Operation(summary = "Valider une promotion par code")
    public ResponseEntity<Promotion> validate(@RequestParam String code) {
        Optional<Promotion> promo = promotionService.findValidByCode(code);
        return promo.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/resolve-influencer")
    @Operation(summary = "Résoudre une promotion à partir d'un code influenceur")
    public ResponseEntity<Promotion> resolveFromInfluencer(@RequestParam String code) {
        Optional<Promotion> promo = promotionService.resolveFromInfluencerCode(code);
        return promo.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
