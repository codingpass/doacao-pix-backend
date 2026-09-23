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
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @RequestHeader(value = "X-OniPay-Event", required = false) String eventHeader,
            @RequestBody Map<String, Object> payload) {

        log.info("[WEBHOOK ONIPAY] EventHeader={}, Payload={}", eventHeader, payload);

        Map<String, Object> data = payload;
        if (payload.get("data") instanceof Map) {
            data = (Map<String, Object>) payload.get("data");
        }

        String eventType = extractString(payload, "type", "event");
        String status = extractString(data, "status", "payment_status");
        String externalIdStr = extractString(data, "externalId", "external_id", "donation_id", "reference_id");
        String chargeId = extractString(data, "depositId", "id", "charge_id");

        boolean isPaid = false;

        if ("deposit.paid".equalsIgnoreCase(eventHeader) || "deposit.paid".equalsIgnoreCase(eventType)) {
            isPaid = true;
        } else if (status != null) {
            String lower = status.toLowerCase();
            if (lower.contains("paid") || lower.contains("pago") || lower.contains("success") || lower.equals("approved")) {
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
                log.info("[WEBHOOK ONIPAY] Doacao confirmada com sucesso! externalId={}, chargeId={}", externalIdStr, chargeId);
            } else {
                log.warn("[WEBHOOK ONIPAY] Doacao nao encontrada para externalId={}, chargeId={}", externalIdStr, chargeId);
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