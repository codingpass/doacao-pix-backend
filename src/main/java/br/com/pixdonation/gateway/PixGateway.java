package br.com.pixdonation.gateway;

import java.util.UUID;

/**
 * Contrato para integracao com qualquer gateway de pagamento PIX.
 *
 * Cada perfil de execucao DEVE fornecer exatamente uma implementacao
 * desta interface. Se nenhuma for encontrada, a aplicacao recusa subir
 * com mensagem explicita (ver ProductionGatewayGuard).
 */
public interface PixGateway {

    /**
     * Cria uma cobranca PIX no gateway e retorna os dados de pagamento.
     *
     * @param donationId  UUID da doacao ja persistida
     * @param amountCents valor em centavos (long, nunca double)
     * @return resultado com chargeId, copia-e-cola e QR Code Base64
     */
    PixChargeResult createCharge(UUID donationId, long amountCents);
}