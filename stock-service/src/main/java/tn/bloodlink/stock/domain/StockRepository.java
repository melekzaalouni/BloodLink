package tn.bloodlink.stock.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockRepository extends JpaRepository<BloodStock, Long> {

    Optional<BloodStock> findByBloodType(String bloodType);
}
