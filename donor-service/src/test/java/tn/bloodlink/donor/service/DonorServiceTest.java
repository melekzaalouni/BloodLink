package tn.bloodlink.donor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.bloodlink.donor.dto.DonorRequest;
import tn.bloodlink.donor.exception.ConflictException;
import tn.bloodlink.donor.exception.NotFoundException;
import tn.bloodlink.donor.repository.DonorRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DonorServiceTest {

    @Mock
    DonorRepository repository;

    @InjectMocks
    DonorService service;

    private final DonorRequest request =
            new DonorRequest("Awa Diallo", "awa@mail.com", "22912345678", "O+", null);

    @Test
    void creating_a_donor_with_existing_email_throws_conflict_and_never_saves() {
        when(repository.existsByEmail("awa@mail.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(request));

        verify(repository, never()).save(any());
    }

    @Test
    void getting_an_unknown_donor_throws_not_found() {
        when(repository.findById(9999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.get(9999L));
    }

    @Test
    void updating_with_an_email_used_by_another_donor_throws_conflict() {
        when(repository.findById(1L)).thenReturn(Optional.of(
                new tn.bloodlink.donor.domain.Donor("Awa", "old@mail.com", null, "O+", null)));
        when(repository.existsByEmailAndIdNot("awa@mail.com", 1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.update(1L, request));

        verify(repository, never()).save(any());
    }
}