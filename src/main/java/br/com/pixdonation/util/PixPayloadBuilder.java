package br.com.pixdonation.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

public class PixPayloadBuilder {

    private String pixKey;
    private String merchantName;
    private String merchantCity;
    private String txId;
    private Long amountCents;

    public PixPayloadBuilder() {
        this.pixKey = "30a7a114dd09563d659f03e995ca0b5e";
        this.merchantName = "PETVIDA RESGATE ANIMAL";
        this.merchantCity = "SAO PAULO";
        this.txId = "***";
    }

    public PixPayloadBuilder setPixKey(String pixKey) {
        if (pixKey != null && !pixKey.trim().isEmpty()) {
            this.pixKey = pixKey.trim();
        }
        return this;
    }

    public PixPayloadBuilder setMerchantName(String merchantName) {
        if (merchantName != null && !merchantName.trim().isEmpty()) {
            this.merchantName = merchantName.trim();
        }
        return this;
    }

    public PixPayloadBuilder setMerchantCity(String merchantCity) {
        if (merchantCity != null && !merchantCity.trim().isEmpty()) {
            this.merchantCity = merchantCity.trim();
        }
        return this;
    }

    public PixPayloadBuilder setTxId(String txId) {
        if (txId != null && !txId.trim().isEmpty()) {
            this.txId = txId.trim();
        }
        return this;
    }

    public PixPayloadBuilder setAmountCents(Long amountCents) {
        this.amountCents = amountCents;
        return this;
    }

    public String buildPayload() {
        // Tag 00: Payload Format Indicator (000201)
        StringBuilder sb = new StringBuilder();
        sb.append("000201");

        // Tag 26: Merchant Account Information
        // Sub-tag 00: GUI (0014br.gov.bcb.pix)
        // Sub-tag 01: Chave Pix
        String sub00 = formatEMVField("00", "br.gov.bcb.pix");
        String sub01 = formatEMVField("01", pixKey);
        String tag26Value = sub00 + sub01;
        sb.append(formatEMVField("26", tag26Value));

        // Tag 52: Merchant Category Code (52040000)
        sb.append("52040000");

        // Tag 53: Transaction Currency (5303986)
        sb.append("5303986");

        // Tag 54: Transaction Amount (optional if amountCents is null or <= 0)
        if (amountCents != null && amountCents > 0) {
            double amountReais = amountCents / 100.0;
            String amountStr = String.format(Locale.US, "%.2f", amountReais);
            sb.append(formatEMVField("54", amountStr));
        }

        // Tag 58: Country Code (5802BR)
        sb.append("5802BR");

        // Tag 59: Merchant Name (max 25 chars)
        String cleanName = sanitizeASCII(merchantName);
        if (cleanName.length() > 25) {
            cleanName = cleanName.substring(0, 25);
        }
        sb.append(formatEMVField("59", cleanName));

        // Tag 60: Merchant City (max 15 chars)
        String cleanCity = sanitizeASCII(merchantCity);
        if (cleanCity.length() > 15) {
            cleanCity = cleanCity.substring(0, 15);
        }
        sb.append(formatEMVField("60", cleanCity));

        // Tag 62: Additional Data Field Template (txid)
        String cleanTxId = sanitizeASCII(txId);
        if (cleanTxId.length() > 25) {
            cleanTxId = cleanTxId.substring(0, 25);
        }
        String sub05 = formatEMVField("05", cleanTxId);
        sb.append(formatEMVField("62", sub05));

        // Tag 63: CRC16 (6304 + 4 Hex digits)
        sb.append("6304");

        String crc = calculateCRC16(sb.toString());
        sb.append(crc);

        return sb.toString();
    }

    public static String generateQrCodeBase64(String text, int width, int height) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (WriterException | IOException e) {
            return "";
        }
    }

    private static String formatEMVField(String id, String value) {
        int length = value.getBytes(StandardCharsets.UTF_8).length;
        String lenStr = String.format("%02d", length);
        return id + lenStr + value;
    }

    private static String sanitizeASCII(String input) {
        if (input == null) return "";
        return input.toUpperCase()
                .replaceAll("[ÁÀÃÂÄ]", "A")
                .replaceAll("[ÉÈÊË]", "E")
                .replaceAll("[ÍÌÎÏ]", "I")
                .replaceAll("[ÓÒÕÔÖ]", "O")
                .replaceAll("[ÚÙÛÜ]", "U")
                .replaceAll("[Ç]", "C")
                .replaceAll("[^A-Z0-9 ]", "")
                .trim();
    }

    public static String calculateCRC16(String payload) {
        int crc = 0xFFFF;
        int polynomial = 0x1021;
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);

        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i)) & 1) == 1;
                boolean c15 = ((crc >> 15) & 1) == 1;
                crc = (crc << 1) & 0xFFFF;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }
        return String.format("%04X", crc & 0xFFFF);
    }
}
