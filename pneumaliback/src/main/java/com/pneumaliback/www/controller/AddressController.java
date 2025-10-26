package com.pneumaliback.www.controller;

import com.pneumaliback.www.dto.AddressDTO;
import com.pneumaliback.www.dto.AddressSimpleDTO;
import com.pneumaliback.www.dto.CreerAddressDTO;
import com.pneumaliback.www.dto.ModifierAddressDTO;
import com.pneumaliback.www.enums.Country;
import com.pneumaliback.www.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Adresses", description = "Gestion des adresses utilisateur")
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    @Operation(summary = "Créer une adresse")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Adresse créée", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AddressDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> create(@RequestBody CreerAddressDTO dto) {
        try {
            AddressDTO created = addressService.creerAddress(dto);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur interne du serveur", "message", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Adresses d'un utilisateur (page)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée"),
            @ApiResponse(responseCode = "400", description = "Paramètres invalides", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> byUser(@PathVariable Long userId, Pageable pageable) {
        try {
            Page<AddressSimpleDTO> page = addressService.obtenirAddressesParUtilisateur(userId, pageable);
            return ResponseEntity.ok(page);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur interne du serveur", "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Adresse par id pour un utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Adresse trouvée"),
            @ApiResponse(responseCode = "404", description = "Adresse non trouvée", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Paramètres invalides", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> byId(@PathVariable Long id, @RequestParam Long userId) {
        try {
            return addressService.obtenirAddressParId(id, userId)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(404).body(Map.of("error", "Adresse non trouvée")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur interne du serveur", "message", e.getMessage()));
        }
    }

    @GetMapping("/country/{country}")
    @Operation(summary = "Adresses par pays")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée"),
            @ApiResponse(responseCode = "400", description = "Paramètres invalides", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> byCountry(@PathVariable Country country) {
        try {
            return ResponseEntity.ok(addressService.obtenirAddressesParPays(country));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur interne du serveur", "message", e.getMessage()));
        }
    }

    @GetMapping("/city/{city}")
    @Operation(summary = "Adresses par ville")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée"),
            @ApiResponse(responseCode = "400", description = "Paramètres invalides", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> byCity(@PathVariable String city) {
        try {
            return ResponseEntity.ok(addressService.obtenirAddressesParVille(city));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur interne du serveur", "message", e.getMessage()));
        }
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Modifier une adresse")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Adresse modifiée"),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Adresse non trouvée", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> update(@PathVariable Long id,
                                             @RequestBody ModifierAddressDTO dto,
                                             @RequestParam Long userId) {
        try {
            AddressDTO updated = addressService.modifierAddress(id, dto, userId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Paramètres invalides";
            if (msg.toLowerCase().contains("introuvable") || msg.toLowerCase().contains("non trouv")) {
                return ResponseEntity.status(404).body(Map.of("error", msg));
            }
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur interne du serveur", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une adresse")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Supprimée"),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Adresse non trouvée", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestParam Long userId) {
        try {
            addressService.supprimerAddress(id, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Paramètres invalides";
            if (msg.toLowerCase().contains("introuvable") || msg.toLowerCase().contains("non trouv")) {
                return ResponseEntity.status(404).body(Map.of("error", msg));
            }
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur interne du serveur", "message", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/count")
    @Operation(summary = "Compter adresses utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compteur récupéré"),
            @ApiResponse(responseCode = "400", description = "Paramètres invalides", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> countByUser(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(addressService.compterAddressesParUtilisateur(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur interne du serveur", "message", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/country/{country}")
    @Operation(summary = "Adresses utilisateur filtrées par pays")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée"),
            @ApiResponse(responseCode = "400", description = "Paramètres invalides", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> byUserAndCountry(@PathVariable Long userId, @PathVariable Country country) {
        try {
            return ResponseEntity.ok(addressService.obtenirAddressesParUtilisateurEtPays(userId, country));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur interne du serveur", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/user/{userId}")
    @Operation(summary = "Supprimer toutes les adresses d'un utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Supprimées"),
            @ApiResponse(responseCode = "400", description = "Paramètres invalides", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> deleteAllForUser(@PathVariable Long userId) {
        try {
            addressService.supprimerToutesAddressesUtilisateur(userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur interne du serveur", "message", e.getMessage()));
        }
    }
}

