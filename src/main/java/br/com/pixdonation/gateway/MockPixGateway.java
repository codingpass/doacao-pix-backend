package br.com.pixdonation.gateway;

import br.com.pixdonation.util.PixPayloadBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implementacao FICTICIA do PixGateway para uso exclusivo no perfil "dev".
 *
 * Gera um payload PIX EMV-Co valido com CRC16 correto para testes locais.
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

        String pixCopyPaste = new PixPayloadBuilder()
                .setPixKey("11111111-2222-3333-4444-555555555555")
                .setMerchantName("PETVIDA RESGATE ANIMAL")
                .setMerchantCity("SAO PAULO")
                .setAmountCents(amountCents)
                .setTxId(donationId.toString().substring(0, 8))
                .buildPayload();

        String qrCodeBase64 = PixPayloadBuilder.generateQrCodeBase64(pixCopyPaste, QR_SIZE_PX, QR_SIZE_PX);

        log.info("[DEV] Cobranca simulada criada: chargeId={}, donationId={}, amountCents={}",
                chargeId, donationId, amountCents);

        return new PixChargeResult(chargeId, pixCopyPaste, qrCodeBase64);
    }
}