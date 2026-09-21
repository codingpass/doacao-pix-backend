package br.com.pixdonation.service;

import br.com.pixdonation.donation.Donation;
import br.com.pixdonation.donation.DonationStatus;
import br.com.pixdonation.dto.CreateDonationRequest;
import br.com.pixdonation.dto.DonationResponse;
import br.com.pixdonation.gateway.PixChargeResult;
import br.com.pixdonation.gateway.PixGateway;
import br.com.pixdonation.repository.DonationRepository;
import br.com.pixdonation.validator.DonationValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DonationService {

    private static final Logger log = LoggerFactory.getLogger(DonationService.class);

    private final DonationRepository donationRepository;
    private final DonationValidator donationValidator;
    private final PixGateway pixGateway;

    public DonationService(DonationRepository donationRepository,
                           DonationValidator donationValidator,
                           PixGateway pixGateway) {
        this.donationRepository = donationRepository;
        this.donationValidator = donationValidator;
        this.pixGateway = pixGateway;
    }

    @Transactional
    public DonationResponse create(CreateDonationRequest request) {
        donationValidator.validate(request.getAmountCents());

        Donation donation = new Donation();
        donation.setAmountCents(request.getAmountCents());
        donation.setStatus(DonationStatus.PENDING);

        Donation saved = donationRepository.save(donation);

        PixChargeResult chargeResult = pixGateway.createCharge(saved.getId(), saved.getAmountCents());

        saved.setGatewayChargeId(chargeResult.getChargeId());
        saved.setPixCopyPaste(chargeResult.getPixCopyPaste());

        saved = donationRepository.save(saved);

        return DonationResponse.from(saved, chargeResult.getQrCodeBase64());
    }

    @Transactional(readOnly = true)
    public Optional<DonationResponse> findById(UUID id) {
        Optional<Donation> found = donationRepository.findById(id);

        if (found.isPresent()) {
            return Optional.of(DonationResponse.summary(found.get()));
        } else {
            return Optional.empty();
        }
    }

    @Transactional
    public boolean simulatePaid(UUID id) {
        Optional<Donation> found = donationRepository.findById(id);

        if (found.isPresent()) {
            Donation donation = found.get();
            donation.setStatus(DonationStatus.PAID);
            donationRepository.save(donation);
            log.info("Doacao {} marcada como PAID.", id);
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    public boolean markAsPaidByGatewayChargeId(String gatewayChargeId) {
        Optional<Donation> found = donationRepository.findByGatewayChargeId(gatewayChargeId);

        if (found.isPresent()) {
            Donation donation = found.get();
            donation.setStatus(DonationStatus.PAID);
            donationRepository.save(donation);
            log.info("Doacao com chargeId {} marcada como PAID via webhook.", gatewayChargeId);
            return true;
        } else {
            log.warn("Nenhuma doacao encontrada com gatewayChargeId={}", gatewayChargeId);
            return false;
        }
    }

    @Transactional
    public int expirePendingDonations() {
        Instant cutoff = Instant.now().minus(30, ChronoUnit.MINUTES);
        List<Donation> pendingDonations = donationRepository.findByStatusAndCreatedAtBefore(DonationStatus.PENDING, cutoff);

        int count = 0;
        for (Donation donation : pendingDonations) {
            donation.setStatus(DonationStatus.EXPIRED);
            donationRepository.save(donation);
            count = count + 1;
        }

        if (count > 0) {
            log.info("{} doacoes PENDING com mais de 30 minutos foram expiradas.", count);
        }

        return count;
    }
}