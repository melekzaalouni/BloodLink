package tn.bloodlink.donor.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "blood_requests")
public class BloodRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String hospitalName;

    @Column(nullable = false, length = 3)
    private String bloodType;

    @Column(nullable = false)
    private int units;

    // Chaîne libre : la valeur vient du service hospitalier (SOAP)
    @Column(nullable = false)
    private String status;

    // Id donné par l'hôpital. Simple nombre, JAMAIS une clé étrangère (une base par
    // service)
    private Long externalRef;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected BloodRequest() {
    }

    public BloodRequest(String hospitalName, String bloodType, int units, String status) {
        this.hospitalName = hospitalName;
        this.bloodType = bloodType;
        this.units = units;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getExternalRef() {
        return externalRef;
    }

    public void setExternalRef(Long externalRef) {
        this.externalRef = externalRef;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}