package com.pneumaliback.www.service;

import com.pneumaliback.www.entity.Promotion;
import com.pneumaliback.www.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final InfluenceurService influenceurService;

    public Optional<Promotion> findValidByCode(String code) {
        if (code == null || code.isBlank()) return Optional.empty();
        String normalized = code.trim();
        LocalDate today = LocalDate.now();
        return promotionRepository.findAll().stream()
                .filter(p -> normalized.equalsIgnoreCase(p.getCode()))
                .filter(p -> (p.getStartDate() == null || !today.isBefore(p.getStartDate())))
                .filter(p -> (p.getEndDate() == null || !today.isAfter(p.getEndDate())))
                .findFirst();
    }

    public Optional<Promotion> resolveFromInfluencerCode(String code) {
        if (code == null || code.isBlank()) return Optional.empty();
        LocalDate today = LocalDate.now();
        return influenceurService.findByPromoCode(code)
                .flatMap(inf -> promotionRepository.findAll().stream()
                        .filter(p -> p.getInfluenceur() != null && p.getInfluenceur().getId().equals(inf.getId()))
                        .filter(p -> (p.getStartDate() == null || !today.isBefore(p.getStartDate())))
                        .filter(p -> (p.getEndDate() == null || !today.isAfter(p.getEndDate())))
                        .findFirst());
    }
}
