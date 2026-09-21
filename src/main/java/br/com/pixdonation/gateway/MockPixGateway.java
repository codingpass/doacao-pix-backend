package br.com.pixdonation.gateway;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

/**
 * Implementacao FICTICIA do PixGateway para uso exclusivo no perfil "dev".
 *
 * NUNCA usar em producao. Esta classe:
 *   - Gera um chargeId aleatorio prefixado com "MOCK-"
 *   - Gera um copia-e-cola falso com "TESTE-NAO-PAGAR-" + donationId
 *   - Gera um QR Code PNG (300x300) localmente via ZXing com esse texto
 *
 * Ativa apenas quando spring.profiles.active=dev.
 */
@Component
@Profile("dev")
public class MockPixGateway implements PixGateway {

    private static final Logger log = LoggerFactory.getLogger(MockPixGateway.class);

    private static final int QR_SIZE_PX = 300;

    @Override
    public PixChargeResult createCharge(UUID donationId, long amountCents) {
        log.warn("[DEV] MockPixGateway ativado - cobranca FICTICIA, NAO processar pagamento real");

        String chargeId = "MOCK-" + UUID.randomUUID().toString().toUpperCase();
        String pixCopyPaste = "TESTE-NAO-PAGAR-" + donationId.toString();
        String qrCodeBase64 = generateQrCodeBase64(pixCopyPaste);

        log.info("[DEV] Cobranca simulada criada: chargeId={}, donationId={}, amountCents={}",
                chargeId, donationId, amountCents);

        return new PixChargeResult(chargeId, pixCopyPaste, qrCodeBase64);
    }

    /**
     * Gera um QR Code PNG 300x300 em Base64 a partir do texto fornecido.
     * Usa a biblioteca ZXing localmente — nenhuma chamada de rede.
     */
    private String generateQrCodeBase64(String text) {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix;

        try {
            matrix = writer.encode(text, BarcodeFormat.QR_CODE, QR_SIZE_PX, QR_SIZE_PX);
        } catch (WriterException e) {
            throw new IllegalStateException(
                "[DEV] Falha ao codificar QR Code para o texto: " + text, e
            );
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);
        } catch (IOException e) {
            throw new IllegalStateException(
                "[DEV] Falha ao gravar imagem PNG do QR Code", e
            );
        }

        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
}