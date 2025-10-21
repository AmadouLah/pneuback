package com.pneumaliback.www.controller;

import com.pneumaliback.www.entity.Review;
import com.pneumaliback.www.entity.User;
import com.pneumaliback.www.repository.UserRepository;
import com.pneumaliback.www.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Avis")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Lister les avis d'un produit")
    public ResponseEntity<List<Review>> listByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.listByProduct(productId));
    }

    public record AddReviewRequest(Long userId, int rating, String comment) {}

    @PostMapping("/product/{productId}")
    @Operation(summary = "Ajouter un avis à un produit")
    public ResponseEntity<Review> add(@PathVariable Long productId, @RequestBody AddReviewRequest req) {
        if (req == null || req.userId() == null) throw new IllegalArgumentException("Paramètres invalides");
        return ResponseEntity.ok(reviewService.addReview(getUser(req.userId()), productId, req.rating(), req.comment()));
    }
}
