package br.com.pixdonation.dto;

import br.com.pixdonation.donation.Donation;
import br.com.pixdonation.donation.DonationStatus;

import java.time.Instant;
import java.util.UUID;

public class DonationResponse {

    private UUID id;
    private Long amountCents;
    private DonationStatus status;
    private String pixCopyPaste;
    private String qrCodeBase64;
    private String gatewayChargeId;
    private Instant createdAt;

    public DonationResponse() {
    }

    public static DonationResponse from(Donation donation, String qrCodeBase64) {
        DonationResponse dto = new DonationResponse();
        dto.setId(donation.getId());
        dto.setAmountCents(donation.getAmountCents());
        dto.setStatus(donation.getStatus());
        dto.setPixCopyPaste(donation.getPixCopyPaste());
        dto.setQrCodeBase64(qrCodeBase64);
        dto.setGatewayChargeId(donation.getGatewayChargeId());
        dto.setCreatedAt(donation.getCreatedAt());
        return dto;
    }

    public static DonationResponse summary(Donation donation) {
        DonationResponse dto = new DonationResponse();
        dto.setId(donation.getId());
        dto.setAmountCents(donation.getAmountCents());
        dto.setStatus(donation.getStatus());
        return dto;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(Long amountCents) {
        this.amountCents = amountCents;
    }

    public DonationStatus getStatus() {
        return status;
    }

    public void setStatus(DonationStatus status) {
        this.status = status;
    }

    public String getPixCopyPaste() {
        return pixCopyPaste;
    }

    public void setPixCopyPaste(String pixCopyPaste) {
        this.pixCopyPaste = pixCopyPaste;
    }

    public String getQrCodeBase64() {
        return qrCodeBase64;
    }

    public void setQrCodeBase64(String qrCodeBase64) {
        this.qrCodeBase64 = qrCodeBase64;
    }

    public String getGatewayChargeId() {
        return gatewayChargeId;
    }

    public void setGatewayChargeId(String gatewayChargeId) {
        this.gatewayChargeId = gatewayChargeId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}