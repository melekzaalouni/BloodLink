package tn.bloodlink.donor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.bloodlink.donor.domain.BloodRequest;

public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
}