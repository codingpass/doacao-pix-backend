package br.com.pixdonation.gateway;

/**
 * Resultado retornado pelo gateway apos criar uma cobranca PIX.
 *
 * chargeId       - identificador unico da cobranca no gateway
 * pixCopyPaste   - payload PIX Copia e Cola (texto puro)
 * qrCodeBase64   - imagem PNG do QR Code codificada em Base64
 */
public class PixChargeResult {

    private final String chargeId;
    private final String pixCopyPaste;
    private final String qrCodeBase64;

    public PixChargeResult(String chargeId, String pixCopyPaste, String qrCodeBase64) {
        this.chargeId = chargeId;
        this.pixCopyPaste = pixCopyPaste;
        this.qrCodeBase64 = qrCodeBase64;
    }

    public String getChargeId() {
        return chargeId;
    }

    public String getPixCopyPaste() {
        return pixCopyPaste;
    }

    public String getQrCodeBase64() {
        return qrCodeBase64;
    }
}