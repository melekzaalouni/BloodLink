package tn.bloodlink.donor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.bloodlink.donor.domain.Donor;

public interface DonorRepository extends JpaRepository<Donor, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}