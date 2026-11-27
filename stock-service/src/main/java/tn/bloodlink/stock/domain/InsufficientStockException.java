package tn.bloodlink.stock.domain;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String type, int available, int requested) {
        super("Stock insuffisant pour " + type + " : disponible " + available
                + ", demandé " + requested);
    }
}
