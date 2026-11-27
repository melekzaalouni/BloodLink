package tn.bloodlink.stock.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.bloodlink.stock.domain.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    StockRepository repository;

    @InjectMocks
    StockService service;

    @Test
    void invalid_blood_type_is_refused() {                       // règle R1
        assertThrows(InvalidBloodTypeException.class, () -> service.get("ZZ"));
    }

    @Test
    void missing_row_gives_not_found() {
        when(repository.findByBloodType("O-")).thenReturn(Optional.empty());
        assertThrows(StockNotFoundException.class, () -> service.get("O-"));
    }

    @Test
    void update_applies_the_delta_and_saves() {
        BloodStock stock = new BloodStock("A+", 30, 10);
        when(repository.findByBloodType("A+")).thenReturn(Optional.of(stock));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        BloodStock result = service.update("A+", 5);

        assertEquals(35, result.getUnits());
    }

    @Test
    void update_with_too_large_withdrawal_does_not_save() {
        when(repository.findByBloodType("O-")).thenReturn(Optional.of(new BloodStock("O-", 4, 10)));

        assertThrows(InsufficientStockException.class, () -> service.update("O-", -100));
        verify(repository, never()).save(any());
    }
}
