package br.com.pixdonation.controller;

import br.com.pixdonation.dto.CreateDonationRequest;
import br.com.pixdonation.dto.DonationResponse;
import br.com.pixdonation.service.DonationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/donations")
public class DonationController {

    private final DonationService donationService;

    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }

    @PostMapping
    public ResponseEntity<DonationResponse> create(
            @Valid @RequestBody CreateDonationRequest request) {

        DonationResponse response = donationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonationResponse> findById(@PathVariable UUID id) {
        Optional<DonationResponse> found = donationService.findById(id);

        if (found.isPresent()) {
            return ResponseEntity.ok(found.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}