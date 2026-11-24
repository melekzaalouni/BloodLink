package tn.bloodlink.donor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.bloodlink.donor.domain.Appointment;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDonorId(Long donorId);
}