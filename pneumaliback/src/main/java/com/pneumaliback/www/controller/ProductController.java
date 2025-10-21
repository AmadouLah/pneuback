package com.pneumaliback.www.controller;

import com.pneumaliback.www.entity.Category;
import com.pneumaliback.www.entity.Product;
import com.pneumaliback.www.repository.CategoryRepository;
import com.pneumaliback.www.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Produits")
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/active")
    @Operation(summary = "Lister produits actifs (page)")
    public ResponseEntity<Page<Product>> listActive(Pageable pageable) {
        return ResponseEntity.ok(productService.listActive(pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher dans les produits actifs")
    public ResponseEntity<Page<Product>> search(@RequestParam String term, Pageable pageable) {
        return ResponseEntity.ok(productService.searchActive(term, pageable));
    }

    @GetMapping("/filter")
    @Operation(summary = "Filtrer les produits actifs")
    public ResponseEntity<Page<Product>> filter(@RequestParam(required = false) Long categoryId,
                                                @RequestParam(required = false) String brand,
                                                @RequestParam(required = false) String size,
                                                @RequestParam(required = false) String season,
                                                @RequestParam(required = false) BigDecimal minPrice,
                                                @RequestParam(required = false) BigDecimal maxPrice,
                                                Pageable pageable) {
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable"));
        }
        return ResponseEntity.ok(productService.findWithFilters(category, brand, size, season, minPrice, maxPrice, pageable));
    }

    @PostMapping
    @Operation(summary = "Créer un produit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.ok(productService.save(product));
    }
}
