package tn.bloodlink.stock.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.bloodlink.stock.domain.BloodStock;
import tn.bloodlink.stock.domain.StockRepository;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Remplit la table au premier démarrage, seulement pour les groupes absents.
 * Le stock de O- est volontairement bas (urgent) pour la démo.
 */
@Component
public class StockSeeder implements CommandLineRunner {

    private static final int DEFAULT_THRESHOLD = 10;

    private final StockRepository repo;

    public StockSeeder(StockRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        Map<String, Integer> initial = new LinkedHashMap<>();
        initial.put("A+", 30);
        initial.put("A-", 12);
        initial.put("B+", 25);
        initial.put("B-", 9);
        initial.put("AB+", 15);
        initial.put("AB-", 6);
        initial.put("O+", 40);
        initial.put("O-", 4);

        initial.forEach((type, units) -> {
            if (repo.findByBloodType(type).isEmpty()) {
                repo.save(new BloodStock(type, units, DEFAULT_THRESHOLD));
            }
        });
    }
}
