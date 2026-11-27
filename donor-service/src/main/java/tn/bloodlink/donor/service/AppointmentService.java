package tn.bloodlink.donor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.bloodlink.donor.domain.Appointment;
import tn.bloodlink.donor.domain.Donor;
import tn.bloodlink.donor.dto.AppointmentRequest;
import tn.bloodlink.donor.dto.AppointmentResponse;
import tn.bloodlink.donor.exception.BusinessRuleException;
import tn.bloodlink.donor.repository.AppointmentRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AppointmentService {

    /** R3 : minimum 56 jours entre deux dons. */
    static final int MIN_DAYS_BETWEEN_DONATIONS = 56;

    private final AppointmentRepository appointments;
    private final DonorService donorService;

    public AppointmentService(AppointmentRepository appointments, DonorService donorService) {
        this.appointments = appointments;
        this.donorService = donorService;
    }

    @Transactional
    public AppointmentResponse book(Long donorId, AppointmentRequest req) {
        Donor donor = donorService.find(donorId); // 404 si inconnu
        checkEligibility(donor.getLastDonation(), req.scheduledAt().toLocalDate());
        Appointment a = new Appointment(donorId, req.bankName(), req.scheduledAt());
        return AppointmentResponse.from(appointments.save(a));
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> list(Long donorId) {
        donorService.find(donorId); // 404 si inconnu
        return appointments.findByDonorId(donorId).stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    /** Règle pure (sans base, sans Spring) : facile à tester. */
    static void checkEligibility(LocalDate lastDonation, LocalDate appointmentDate) {
        if (lastDonation != null
                && ChronoUnit.DAYS.between(lastDonation, appointmentDate) < MIN_DAYS_BETWEEN_DONATIONS) {
            throw new BusinessRuleException("A donor must wait 56 days between two donations");
        }
    }
}