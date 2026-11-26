
package tn.bloodlink.hospital.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "blood_requests")
public class BloodRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hospitalName;

    @Column(length = 3)
    private String bloodType;

    private int units;

    @Enumerated(EnumType.STRING)
    private RequestState status;

    private Instant createdAt;

    // Constructeur sans argument exigé par JPA
    protected BloodRequest() {
    }

    // Constructeur avec les quatre champs métier
    public BloodRequest(String hospitalName, String bloodType,
                        int units, RequestState status) {
        this.hospitalName = hospitalName;
        this.bloodType = bloodType;
        this.units = units;
        this.status = status;
        this.createdAt = Instant.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public String getBloodType() {
        return bloodType;
    }

    public int getUnits() {
        return units;
    }

    public RequestState getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
