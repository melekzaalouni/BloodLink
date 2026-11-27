package tn.bloodlink.donor.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.bloodlink.donor.domain.Donor;
import tn.bloodlink.donor.dto.DonorRequest;
import tn.bloodlink.donor.dto.DonorResponse;
import tn.bloodlink.donor.exception.ConflictException;
import tn.bloodlink.donor.exception.NotFoundException;
import tn.bloodlink.donor.repository.DonorRepository;

@Service
public class DonorService {

    private final DonorRepository donors;

    public DonorService(DonorRepository donors) {
        this.donors = donors;
    }

    @Transactional
    public DonorResponse create(DonorRequest req) {
        // R2 : e-mail unique
        if (donors.existsByEmail(req.email())) {
            throw new ConflictException("Email already used: " + req.email());
        }
        Donor donor = new Donor(req.fullName(), req.email(), req.phone(), req.bloodType(), req.lastDonation());
        return DonorResponse.from(donors.save(donor));
    }

    @Transactional(readOnly = true)
    public DonorResponse get(Long id) {
        return DonorResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public Page<DonorResponse> list(Pageable pageable) {
        return donors.findAll(pageable).map(DonorResponse::from);
    }

    @Transactional
    public DonorResponse update(Long id, DonorRequest req) {
        Donor donor = find(id);
        if (donors.existsByEmailAndIdNot(req.email(), id)) {
            throw new ConflictException("Email already used: " + req.email());
        }
        donor.setFullName(req.fullName());
        donor.setEmail(req.email());
        donor.setPhone(req.phone());
        donor.setBloodType(req.bloodType());
        donor.setLastDonation(req.lastDonation());
        return DonorResponse.from(donors.save(donor));
    }

    @Transactional
    public void delete(Long id) {
        Donor donor = find(id);
        donors.delete(donor);
    }

    /** Utilisé aussi par AppointmentService. */
    Donor find(Long id) {
        return donors.findById(id)
                .orElseThrow(() -> new NotFoundException("Donor " + id + " not found"));
    }
}