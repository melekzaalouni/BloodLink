package tn.bloodlink.donor.service;

import org.junit.jupiter.api.Test;
import tn.bloodlink.donor.exception.BusinessRuleException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppointmentServiceTest {

    private static final LocalDate LAST = LocalDate.of(2026, 1, 1);

    @Test
    void donating_after_59_days_is_allowed() {
        assertDoesNotThrow(() -> AppointmentService.checkEligibility(LAST, LAST.plusDays(59)));
    }

    @Test
    void donating_after_exactly_56_days_is_allowed() {
        assertDoesNotThrow(() -> AppointmentService.checkEligibility(LAST, LAST.plusDays(56)));
    }

    @Test
    void donating_after_30_days_is_refused() {
        assertThrows(BusinessRuleException.class,
                () -> AppointmentService.checkEligibility(LAST, LAST.plusDays(30)));
    }

    @Test
    void first_donation_is_always_allowed() {
        assertDoesNotThrow(() -> AppointmentService.checkEligibility(null, LocalDate.now()));
    }
}
