package br.com.pixdonation.donation;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Valor da doacao em centavos (ex: 1500 = R$ 15,00).
     * NUNCA use double para valores monetarios.
     */
    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DonationStatus status;

    /**
     * Payload PIX Copia e Cola (ate 2000 chars, gerado pelo gateway).
     */
    @Column(name = "pix_copy_paste", length = 2000)
    private String pixCopyPaste;

    /**
     * ID da cobranca no gateway de pagamento externo.
     */
    @Column(name = "gateway_charge_id")
    private String gatewayChargeId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = Instant.now();
        if (this.status == null) {
            this.status = DonationStatus.PENDING;
        }
    }

    public UUID getId() {
        return id;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(long amountCents) {
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

    public String getGatewayChargeId() {
        return gatewayChargeId;
    }

    public void setGatewayChargeId(String gatewayChargeId) {
        this.gatewayChargeId = gatewayChargeId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}