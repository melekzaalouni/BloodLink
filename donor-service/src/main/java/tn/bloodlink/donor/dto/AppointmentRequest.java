package tn.bloodlink.donor.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentRequest(
        @NotBlank String bankName,
        @NotNull @Future LocalDateTime scheduledAt) {
}