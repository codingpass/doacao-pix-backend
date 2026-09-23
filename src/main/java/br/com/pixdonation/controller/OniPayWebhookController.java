package br.com.pixdonation.controller;

import br.com.pixdonation.service.DonationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/webhooks/onipay")
@CrossOrigin(origins = "*")
public class OniPayWebhookController {

    private static final Logger log = LoggerFactory.getLogger(OniPayWebhookController.class);

    private final DonationService donationService;

    public OniPayWebhookController(DonationService donationService) {
        this.donationService = donationService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> handleWebhook(@RequestBody Map<String, Object> payload) {
        log.info("[WEBHOOK ONIPAY] Notificacao de pagamento recebida: {}", payload);

        String status = extractString(payload, "status", "event", "payment_status");
        String externalIdStr = extractString(payload, "external_id", "donation_id", "reference_id");
        String chargeId = extractString(payload, "charge_id", "id");

        boolean isPaid = false;

        if (status != null) {
            String lowerStatus = status.toLowerCase();
            if (lowerStatus.contains("paid") || lowerStatus.contains("pago") || lowerStatus.contains("success") || lowerStatus.equals("approved")) {
                isPaid = true;
            }
        }

        if (isPaid) {
            boolean updated = false;

            if (externalIdStr != null && !externalIdStr.trim().isEmpty()) {
                try {
                    UUID id = UUID.fromString(externalIdStr.trim());
                    updated = donationService.simulatePaid(id);
                } catch (IllegalArgumentException e) {
                    log.warn("[WEBHOOK ONIPAY] external_id invalido como UUID: {}", externalIdStr);
                }
            }

            if (!updated && chargeId != null && !chargeId.trim().isEmpty()) {
                updated = donationService.markAsPaidByGatewayChargeId(chargeId.trim());
            }

            if (updated) {
                log.info("[WEBHOOK ONIPAY] Doacao atualizada com sucesso para PAID!");
            } else {
                log.warn("[WEBHOOK ONIPAY] Nenhuma doacao encontrada para o webhook recebido. externalId={}, chargeId={}", externalIdStr, chargeId);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("received", true);
        response.put("status", "processed");
        return ResponseEntity.ok(response);
    }

    private String extractString(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object val = map.get(key);
            if (val != null) {
                return val.toString();
            }
        }
        return null;
    }
}