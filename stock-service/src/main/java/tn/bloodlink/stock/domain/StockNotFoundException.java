package tn.bloodlink.stock.domain;

public class StockNotFoundException extends RuntimeException {
    public StockNotFoundException(String type) {
        super("Aucun stock pour le groupe sanguin : " + type);
    }
}
