package br.com.pixdonation.gateway;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementacao REAL do PixGateway para a API OniPay.
 *
 * Ativa quando o profile NAO for "dev" (ex: prod, default, staging).
 * Le a chave de API de ONIPAY_API_KEY do ambiente e envia requisicoes HTTP.
 */
@Component
@Profile("!dev")
public class OniPayPixGateway implements PixGateway {

    private static final Logger log = LoggerFactory.getLogger(OniPayPixGateway.class);
    private static final int QR_SIZE_PX = 300;

    @Value("${onipay.api-key:}")
    private String apiKey;

    @Value("${onipay.api-url:https://api.onipay.com.br/v1}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public OniPayPixGateway() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public PixChargeResult createCharge(UUID donationId, long amountCents) {
        log.info("[ONIPAY] Criando cobranca PIX real na OniPay para doacaoId={}, valorCentavos={}",
                donationId, amountCents);

        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("[ONIPAY] Variavel de ambiente ONIPAY_API_KEY nao foi configurada!");
            throw new IllegalStateException(
                "Chave de API da OniPay (ONIPAY_API_KEY) nao foi configurada no ambiente."
            );
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey.trim());
        headers.set("X-Api-Key", apiKey.trim());

        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", amountCents);
        payload.put("payment_method", "pix");
        payload.put("description", "Doacao para animais necessitados");
        payload.put("external_id", donationId.toString());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        try {
            String endpoint = apiUrl + "/charges";
            ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();

                String chargeId = extractString(body, "id", "charge_id");
                String pixCopyPaste = extractString(body, "pix_copy_paste", "qrcode", "copy_paste");
                String qrCodeBase64 = extractString(body, "qr_code_base64", "qrcode_base64");

                if (chargeId == null || chargeId.isEmpty()) {
                    chargeId = "ONIPAY-" + donationId.toString();
                }

                if (pixCopyPaste == null || pixCopyPaste.isEmpty()) {
                    throw new IllegalStateException("Gateway OniPay nao retornou o payload PIX copia e cola.");
                }

                if (qrCodeBase64 == null || qrCodeBase64.isEmpty()) {
                    qrCodeBase64 = generateQrCodeBase64(pixCopyPaste);
                }

                log.info("[ONIPAY] Cobranca criada com sucesso na OniPay. ChargeId={}", chargeId);
                return new PixChargeResult(chargeId, pixCopyPaste, qrCodeBase64);
            } else {
                throw new IllegalStateException("Resposta invalida do gateway OniPay: HTTP " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("[ONIPAY] Falha ao comunicar com o gateway OniPay: {}", e.getMessage());
            throw new RuntimeException("Falha na integracao com a OniPay: " + e.getMessage(), e);
        }
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

    private String generateQrCodeBase64(String text) {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix;
        try {
            matrix = writer.encode(text, BarcodeFormat.QR_CODE, QR_SIZE_PX, QR_SIZE_PX);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (WriterException | IOException e) {
            log.error("[ONIPAY] Erro ao gerar imagem QR Code local para copia-e-cola", e);
            return "";
        }
    }
}