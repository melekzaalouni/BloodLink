
package tn.bloodlink.hospital.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tn.bloodlink.hospital.domain.BloodRequest;
import tn.bloodlink.hospital.domain.RequestState;
import tn.bloodlink.hospital.repository.BloodRequestRepository;
import tn.bloodlink.hospital.exception.InvalidBloodRequestException;
import tn.bloodlink.hospital.exception.RequestNotFoundException;

import java.util.Set;

@Service
public class BloodRequestService {

    private final BloodRequestRepository repository;

    // Les 8 groupes sanguins valides
    private static final Set<String> VALID_BLOOD_TYPES = Set.of(
            "A+", "A-", "B+", "B-",
            "AB+", "AB-", "O+", "O-"
    );

    // Limites des demandes
    private static final int MAX_UNITS = 50;
    private static final int APPROVAL_LIMIT = 20;

    // Injection par constructeur
    public BloodRequestService(BloodRequestRepository repository) {
        this.repository = repository;
    }

    // Créer une demande de sang
    @Transactional
    public BloodRequest create(String hospitalName, String bloodType, int units) {

        // R1 : vérifier le groupe sanguin
        if (!VALID_BLOOD_TYPES.contains(bloodType)) {
            throw new InvalidBloodRequestException(
                    "Invalid blood type: " + bloodType
            );
        }

        // R5 : vérifier le nombre d'unités
        if (units < 1 || units > MAX_UNITS) {
            throw new InvalidBloodRequestException(
                    "Units must be between 1 and 50"
            );
        }

        // Déterminer le statut
        RequestState status = units <= APPROVAL_LIMIT
                ? RequestState.APPROVED
                : RequestState.REJECTED;

        // Créer et sauvegarder la demande
        BloodRequest request = new BloodRequest(
                hospitalName, bloodType, units, status
        );

        return repository.save(request);
    }

    // Consulter une demande par son identifiant
    @Transactional(readOnly = true)
    public BloodRequest get(long id) {

        return repository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException(id));
    }
}
