
package tn.bloodlink.hospital.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tn.bloodlink.hospital.domain.BloodRequest;
import tn.bloodlink.hospital.domain.RequestState;
import tn.bloodlink.hospital.exception.InvalidBloodRequestException;
import tn.bloodlink.hospital.repository.BloodRequestRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BloodRequestServiceTest {

    @Mock
    private BloodRequestRepository repository;

    @InjectMocks
    private BloodRequestService service;

    @Test
    void shouldApproveRequestWith20Units() {
        when(repository.save(any(BloodRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BloodRequest result =
                service.create("Charles Nicolle", "O-", 20);

        assertEquals(RequestState.APPROVED, result.getStatus());
        verify(repository).save(any(BloodRequest.class));
    }

    @Test
    void shouldRejectUnknownBloodType() {
        assertThrows(
                InvalidBloodRequestException.class,
                () -> service.create("Charles Nicolle", "X+", 5)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectRequestWith21Units() {
        when(repository.save(any(BloodRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BloodRequest result =
                service.create("Charles Nicolle", "O-", 21);

        assertEquals(RequestState.REJECTED, result.getStatus());
    }

    @Test
    void shouldRejectRequestWithZeroUnits() {
        assertThrows(
                InvalidBloodRequestException.class,
                () -> service.create("Charles Nicolle", "O-", 0)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectRequestWith51Units() {
        assertThrows(
                InvalidBloodRequestException.class,
                () -> service.create("Charles Nicolle", "O-", 51)
        );

        verify(repository, never()).save(any());
    }
}
