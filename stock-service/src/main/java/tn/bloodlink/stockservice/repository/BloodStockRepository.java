package tn.bloodlink.stockservice.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import tn.bloodlink.stockservice.entity.BloodStock;

import java.util.List;
import java.util.Optional;

public interface BloodStockRepository extends JpaRepository<BloodStock, Long> {

    Optional<BloodStock> findByBloodType(String bloodType);

    List<BloodStock> findAllByOrderByIdAsc();

    // Verrou en écriture : deux mises à jour simultanées ne peuvent pas rendre le stock négatif
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BloodStock b where b.bloodType = :bloodType")
    Optional<BloodStock> findForUpdate(String bloodType);
}