package br.com.pixdonation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateDonationRequest {

    @NotNull(message = "O valor da doacao e obrigatorio.")
    @Positive(message = "O valor da doacao deve ser maior que zero.")
    private Long amountCents;

    public CreateDonationRequest() {
    }

    public CreateDonationRequest(Long amountCents) {
        this.amountCents = amountCents;
    }

    public Long getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(Long amountCents) {
        this.amountCents = amountCents;
    }
}