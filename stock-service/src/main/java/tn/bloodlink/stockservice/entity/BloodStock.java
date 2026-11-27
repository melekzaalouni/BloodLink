package tn.bloodlink.stockservice.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "blood_stock")
public class BloodStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blood_type", nullable = false, unique = true, length = 3)
    private String bloodType;

    @Column(nullable = false)
    private int units;

    @Column(nullable = false)
    private int threshold;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public BloodStock() {}

    public BloodStock(String bloodType, int units, int threshold) {
        this.bloodType = bloodType;
        this.units = units;
        this.threshold = threshold;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getBloodType() { return bloodType; }
    public int getUnits() { return units; }
    public int getThreshold() { return threshold; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setUnits(int units) {
        this.units = units;
        this.updatedAt = Instant.now();
    }

    public boolean isUrgent() {
        return units < threshold;
    }
}