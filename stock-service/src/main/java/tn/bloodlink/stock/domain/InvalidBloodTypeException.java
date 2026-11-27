package tn.bloodlink.stock.domain;

public class InvalidBloodTypeException extends RuntimeException {
    public InvalidBloodTypeException(String type) {
        super("Groupe sanguin invalide : '" + type + "'");
    }
}
