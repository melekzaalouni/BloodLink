package tn.bloodlink.donor.dto;

import tn.bloodlink.donor.domain.Appointment;
import tn.bloodlink.donor.domain.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long donorId,
        String bankName,
        LocalDateTime scheduledAt,
        AppointmentStatus status) {

    public static AppointmentResponse from(Appointment a) {
        return new AppointmentResponse(a.getId(), a.getDonorId(), a.getBankName(),
                a.getScheduledAt(), a.getStatus());
    }
}