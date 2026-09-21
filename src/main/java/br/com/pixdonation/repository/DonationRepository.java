package br.com.pixdonation.repository;

import br.com.pixdonation.donation.Donation;
import br.com.pixdonation.donation.DonationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonationRepository extends JpaRepository<Donation, UUID> {

    List<Donation> findByStatusAndCreatedAtBefore(DonationStatus status, Instant cutoffTime);

    Optional<Donation> findByGatewayChargeId(String gatewayChargeId);
}