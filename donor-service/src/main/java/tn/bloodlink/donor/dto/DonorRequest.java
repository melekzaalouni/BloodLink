package tn.bloodlink.donor.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record DonorRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Email String email,
        @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "phone must contain 8 to 15 digits, optional leading +")
        String phone,
        @NotBlank @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "bloodType must be A, B, AB or O followed by + or -")
        String bloodType,
        @PastOrPresent LocalDate lastDonation) {
}