package tn.bloodlink.stock.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "blood_stock")
public class BloodStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 3)
    private String bloodType;

    @Column(nullable = false)
    private int units;

    @Column(nullable = false)
    private int threshold;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    protected BloodStock() { }

    public BloodStock(String bloodType, int units, int threshold) {
        this.bloodType = bloodType;
        this.units = units;
        this.threshold = threshold;
    }

    // Règle R4 : le stock est urgent quand les unités sont strictement sous le seuil
    public boolean isUrgent() {
        return units < threshold;
    }

    // delta positif = don, delta négatif = prélèvement
    public void changeUnits(int delta) {
        int result = units + delta;
        if (result < 0) {
            throw new InsufficientStockException(bloodType, units, -delta);
        }
        this.units = result;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getBloodType() { return bloodType; }
    public int getUnits() { return units; }
    public int getThreshold() { return threshold; }
    public Instant getUpdatedAt() { return updatedAt; }
}
