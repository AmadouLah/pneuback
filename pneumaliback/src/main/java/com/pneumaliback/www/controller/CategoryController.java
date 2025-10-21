package com.pneumaliback.www.controller;

import com.pneumaliback.www.entity.Category;
import com.pneumaliback.www.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Catégories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/active")
    @Operation(summary = "Lister catégories actives")
    public ResponseEntity<List<Category>> listActive() {
        return ResponseEntity.ok(categoryService.listActive());
    }

    @PostMapping
    @Operation(summary = "Créer une catégorie")
    public ResponseEntity<Category> create(@RequestBody Category c) {
        return ResponseEntity.ok(categoryService.create(c));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une catégorie")
    public ResponseEntity<Category> update(@PathVariable Long id, @RequestBody Category payload) {
        return ResponseEntity.ok(categoryService.update(id, payload));
    }

    @PutMapping("/{id}/active")
    @Operation(summary = "Activer/Désactiver une catégorie")
    public ResponseEntity<Category> toggle(@PathVariable Long id, @RequestParam boolean active) {
        return ResponseEntity.ok(categoryService.toggleActive(id, active));
    }
}
