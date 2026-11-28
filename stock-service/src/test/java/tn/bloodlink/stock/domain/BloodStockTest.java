package tn.bloodlink.stock.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BloodStockTest {

    @Test
    void stock_below_threshold_is_urgent() {
        assertTrue(new BloodStock("O-", 4, 10).isUrgent());
    }

    @Test
    void stock_at_threshold_is_not_urgent() {
        assertFalse(new BloodStock("O-", 10, 10).isUrgent());
    }

    @Test
    void removing_too_many_units_throws() {
        BloodStock s = new BloodStock("O-", 4, 10);
        assertThrows(InsufficientStockException.class, () -> s.changeUnits(-5));
        assertEquals(4, s.getUnits());   // le stock n'a pas changé
    }

    @Test
    void adding_units_increases_stock() {
        BloodStock s = new BloodStock("O-", 4, 10);
        s.changeUnits(6);
        assertEquals(10, s.getUnits());
        assertFalse(s.isUrgent());
    }

    @Test
    void removing_all_units_is_allowed() {
        BloodStock s = new BloodStock("O-", 4, 10);
        s.changeUnits(-4);
        assertEquals(0, s.getUnits());
    }
}
