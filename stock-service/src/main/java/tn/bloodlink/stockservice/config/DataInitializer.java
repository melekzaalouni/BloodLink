package tn.bloodlink.stockservice.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tn.bloodlink.stockservice.entity.BloodStock;
import tn.bloodlink.stockservice.repository.BloodStockRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class DataInitializer {

    public static final int DEFAULT_THRESHOLD = 10;

    private static final Map<String, Integer> INITIAL_UNITS = new LinkedHashMap<>();

    static {
        INITIAL_UNITS.put("A+", 30);
        INITIAL_UNITS.put("A-", 12);
        INITIAL_UNITS.put("B+", 25);
        INITIAL_UNITS.put("B-", 8);
        INITIAL_UNITS.put("AB+", 15);
        INITIAL_UNITS.put("AB-", 5);
        INITIAL_UNITS.put("O+", 40);
        INITIAL_UNITS.put("O-", 9);
    }

    public static final List<String> BLOOD_TYPES = List.copyOf(INITIAL_UNITS.keySet());

    @Bean
    CommandLineRunner initStock(BloodStockRepository repository) {
        return args -> INITIAL_UNITS.forEach((type, units) -> {
            if (repository.findByBloodType(type).isEmpty()) {
                repository.save(new BloodStock(type, units, DEFAULT_THRESHOLD));
            }
        });
    }
}