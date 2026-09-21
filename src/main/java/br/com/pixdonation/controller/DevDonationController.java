package br.com.pixdonation.controller;

import br.com.pixdonation.service.DonationService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/dev/donations")
@Profile("dev")
public class DevDonationController {

    private final DonationService donationService;

    public DevDonationController(DonationService donationService) {
        this.donationService = donationService;
    }

    @PostMapping("/{id}/simulate-paid")
    public ResponseEntity<Void> simulatePaid(@PathVariable UUID id) {
        boolean success = donationService.simulatePaid(id);

        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}