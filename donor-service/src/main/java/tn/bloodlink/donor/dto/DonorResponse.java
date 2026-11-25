package tn.bloodlink.donor.dto;

import tn.bloodlink.donor.domain.Donor;

import java.time.LocalDate;

public record DonorResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String bloodType,
        LocalDate lastDonation) {

    public static DonorResponse from(Donor d) {
        return new DonorResponse(d.getId(), d.getFullName(), d.getEmail(), d.getPhone(),
                d.getBloodType(), d.getLastDonation());
    }
}