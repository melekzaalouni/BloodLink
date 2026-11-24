package tn.bloodlink.donor.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "donors")
public class Donor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @Column(nullable = false, length = 3)
    private String bloodType;

    private LocalDate lastDonation;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Donor() {
    }

    public Donor(String fullName, String email, String phone, String bloodType, LocalDate lastDonation) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.bloodType = bloodType;
        this.lastDonation = lastDonation;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public LocalDate getLastDonation() {
        return lastDonation;
    }

    public void setLastDonation(LocalDate lastDonation) {
        this.lastDonation = lastDonation;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}