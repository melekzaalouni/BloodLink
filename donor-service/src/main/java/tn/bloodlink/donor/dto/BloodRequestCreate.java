package tn.bloodlink.donor.dto;

import jakarta.validation.constraints.*;

public record BloodRequestCreate(
        @NotBlank String hospitalName,
        @NotBlank @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "bloodType must be A, B, AB or O followed by + or -") String bloodType,
        @Min(1) @Max(50) int units) {
}