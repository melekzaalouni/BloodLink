
package tn.bloodlink.hospital.repository;

import tn.bloodlink.hospital.domain.BloodRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BloodRequestRepository
        extends JpaRepository<BloodRequest, Long> {
}
