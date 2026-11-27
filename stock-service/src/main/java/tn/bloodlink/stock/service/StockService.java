package tn.bloodlink.stock.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.bloodlink.stock.domain.BloodStock;
import tn.bloodlink.stock.domain.InvalidBloodTypeException;
import tn.bloodlink.stock.domain.StockNotFoundException;
import tn.bloodlink.stock.domain.StockRepository;

import java.util.List;
import java.util.Set;

@Service
public class StockService {

    private static final Set<String> TYPES =
            Set.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");

    private final StockRepository repo;

    public StockService(StockRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public BloodStock get(String type) {
        validate(type);
        return repo.findByBloodType(type).orElseThrow(() -> new StockNotFoundException(type));
    }

    @Transactional(readOnly = true)
    public List<BloodStock> all() {
        return repo.findAll();
    }

    @Transactional
    public BloodStock update(String type, int delta) {
        BloodStock stock = get(type);
        stock.changeUnits(delta);   // peut lancer InsufficientStockException
        return repo.save(stock);
    }

    // Règle R1 : le groupe sanguin doit être l'un des 8 groupes valides
    private void validate(String type) {
        if (!TYPES.contains(type)) {
            throw new InvalidBloodTypeException(type);
        }
    }
}
