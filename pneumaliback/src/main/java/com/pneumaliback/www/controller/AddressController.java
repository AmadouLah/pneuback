package com.pneumaliback.www.controller;

import com.pneumaliback.www.dto.AddressDTO;
import com.pneumaliback.www.dto.AddressSimpleDTO;
import com.pneumaliback.www.dto.CreerAddressDTO;
import com.pneumaliback.www.dto.ModifierAddressDTO;
import com.pneumaliback.www.enums.Country;
import com.pneumaliback.www.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Adresses")
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    @Operation(summary = "Créer une adresse")
    public ResponseEntity<AddressDTO> create(@RequestBody CreerAddressDTO dto) {
        return ResponseEntity.ok(addressService.creerAddress(dto));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Adresses d'un utilisateur (page)")
    public ResponseEntity<Page<AddressSimpleDTO>> byUser(@PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(addressService.obtenirAddressesParUtilisateur(userId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Adresse par id pour un utilisateur")
    public ResponseEntity<AddressDTO> byId(@PathVariable Long id, @RequestParam Long userId) {
        return addressService.obtenirAddressParId(id, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/country/{country}")
    @Operation(summary = "Adresses par pays")
    public ResponseEntity<List<AddressSimpleDTO>> byCountry(@PathVariable Country country) {
        return ResponseEntity.ok(addressService.obtenirAddressesParPays(country));
    }

    @GetMapping("/city/{city}")
    @Operation(summary = "Adresses par ville")
    public ResponseEntity<List<AddressSimpleDTO>> byCity(@PathVariable String city) {
        return ResponseEntity.ok(addressService.obtenirAddressesParVille(city));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Modifier une adresse")
    public ResponseEntity<AddressDTO> update(@PathVariable Long id,
                                             @RequestBody ModifierAddressDTO dto,
                                             @RequestParam Long userId) {
        return ResponseEntity.ok(addressService.modifierAddress(id, dto, userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une adresse")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Long userId) {
        addressService.supprimerAddress(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}/count")
    @Operation(summary = "Compter adresses utilisateur")
    public ResponseEntity<Long> countByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(addressService.compterAddressesParUtilisateur(userId));
    }

    @GetMapping("/user/{userId}/country/{country}")
    @Operation(summary = "Adresses utilisateur filtrées par pays")
    public ResponseEntity<List<AddressSimpleDTO>> byUserAndCountry(@PathVariable Long userId, @PathVariable Country country) {
        return ResponseEntity.ok(addressService.obtenirAddressesParUtilisateurEtPays(userId, country));
    }

    @DeleteMapping("/user/{userId}")
    @Operation(summary = "Supprimer toutes les adresses d'un utilisateur")
    public ResponseEntity<Void> deleteAllForUser(@PathVariable Long userId) {
        addressService.supprimerToutesAddressesUtilisateur(userId);
        return ResponseEntity.noContent().build();
    }
}
