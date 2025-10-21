package com.pneumaliback.www.controller;

import com.pneumaliback.www.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Paiements - Callback", description = "Callbacks des prestataires de paiement")
public class PaymentCallbackController {

    private final PaymentService paymentService;

    @PostMapping("/callback/success")
    @Operation(summary = "Callback succès paiement", description = "Confirme le paiement et la commande via la référence transaction")
    public ResponseEntity<Void> handleSuccessCallback(@RequestParam("txRef") String transactionReference) {
        paymentService.confirmSuccessByTransaction(transactionReference);
        return ResponseEntity.noContent().build();
    }
}
