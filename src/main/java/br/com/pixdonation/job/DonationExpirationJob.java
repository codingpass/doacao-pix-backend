package br.com.pixdonation.job;

import br.com.pixdonation.service.DonationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DonationExpirationJob {

    private static final Logger log = LoggerFactory.getLogger(DonationExpirationJob.class);

    private final DonationService donationService;

    public DonationExpirationJob(DonationService donationService) {
        this.donationService = donationService;
    }

    @Scheduled(fixedRate = 60000)
    public void expireOldDonations() {
        log.debug("Executando job de expiracao de doacoes PENDING...");
        donationService.expirePendingDonations();
    }
}