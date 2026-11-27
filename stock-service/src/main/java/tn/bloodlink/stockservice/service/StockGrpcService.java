package tn.bloodlink.stockservice.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;
import tn.bloodlink.stock.grpc.GetStockRequest;
import tn.bloodlink.stock.grpc.StockLevel;
import tn.bloodlink.stock.grpc.StockServiceGrpc;
import tn.bloodlink.stock.grpc.StreamStockRequest;
import tn.bloodlink.stock.grpc.UpdateStockRequest;
import tn.bloodlink.stockservice.config.DataInitializer;
import tn.bloodlink.stockservice.entity.BloodStock;
import tn.bloodlink.stockservice.repository.BloodStockRepository;

@GrpcService
public class StockGrpcService extends StockServiceGrpc.StockServiceImplBase {

    private static final int DEFAULT_INTERVAL_SECONDS = 5;

    private final BloodStockRepository repository;

    public StockGrpcService(BloodStockRepository repository) {
        this.repository = repository;
    }

    // Appel unaire : niveau de stock d'un groupe sanguin
    @Override
    @Transactional(readOnly = true)
    public void getStock(GetStockRequest request, StreamObserver<StockLevel> responseObserver) {
        String type = normalize(request.getBloodType());
        if (!isValid(type)) {
            responseObserver.onError(invalidType(type));
            return;
        }
        BloodStock stock = repository.findByBloodType(type).orElse(null);
        if (stock == null) {
            responseObserver.onError(notFound(type));
            return;
        }
        responseObserver.onNext(toLevel(stock));
        responseObserver.onCompleted();
    }

    // Server streaming : envoie les 8 niveaux à chaque tour, toutes les interval_seconds secondes.
    // max_updates = nombre de tours (0 = sans fin, jusqu'à l'annulation par le client).
    @Override
    public void streamStockLevels(StreamStockRequest request, StreamObserver<StockLevel> responseObserver) {
        ServerCallStreamObserver<StockLevel> call = (ServerCallStreamObserver<StockLevel>) responseObserver;

        if (request.getMaxUpdates() < 0) {
            call.onError(Status.INVALID_ARGUMENT
                    .withDescription("max_updates ne peut pas être négatif").asRuntimeException());
            return;
        }
        int interval = request.getIntervalSeconds() > 0
                ? request.getIntervalSeconds() : DEFAULT_INTERVAL_SECONDS;
        int maxUpdates = request.getMaxUpdates();
        int rounds = 0;

        try {
            while (!call.isCancelled()) {
                for (BloodStock stock : repository.findAllByOrderByIdAsc()) {
                    if (call.isCancelled()) {
                        return;
                    }
                    call.onNext(toLevel(stock));
                }
                rounds++;
                if (maxUpdates > 0 && rounds >= maxUpdates) {
                    break;
                }
                Thread.sleep(interval * 1000L);
            }
            if (!call.isCancelled()) {
                call.onCompleted();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            call.onError(Status.CANCELLED.withDescription("Flux interrompu").asRuntimeException());
        }
    }

    // Appel unaire : applique un delta (positif = don, négatif = prélèvement)
    @Override
    @Transactional
    public void updateStock(UpdateStockRequest request, StreamObserver<StockLevel> responseObserver) {
        String type = normalize(request.getBloodType());
        if (!isValid(type)) {
            responseObserver.onError(invalidType(type));
            return;
        }
        if (request.getDelta() == 0) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("delta ne peut pas être égal à 0").asRuntimeException());
            return;
        }
        BloodStock stock = repository.findForUpdate(type).orElse(null);
        if (stock == null) {
            responseObserver.onError(notFound(type));
            return;
        }
        int newUnits = stock.getUnits() + request.getDelta();
        if (newUnits < 0) {
            responseObserver.onError(Status.FAILED_PRECONDITION
                    .withDescription("Stock insuffisant pour " + type + " : disponible "
                            + stock.getUnits() + ", delta demandé " + request.getDelta())
                    .asRuntimeException());
            return;
        }
        stock.setUnits(newUnits);
        repository.save(stock);
        responseObserver.onNext(toLevel(stock));
        responseObserver.onCompleted();
    }

    private static StockLevel toLevel(BloodStock stock) {
        return StockLevel.newBuilder()
                .setBloodType(stock.getBloodType())
                .setUnits(stock.getUnits())
                .setThreshold(stock.getThreshold())
                .setUrgent(stock.isUrgent())
                .setUpdatedAtEpoch(stock.getUpdatedAt().getEpochSecond())
                .build();
    }

    private static String normalize(String type) {
        return type == null ? "" : type.trim().toUpperCase();
    }

    private static boolean isValid(String type) {
        return DataInitializer.BLOOD_TYPES.contains(type);
    }

    private static StatusRuntimeException invalidType(String type) {
        return Status.INVALID_ARGUMENT
                .withDescription("Groupe sanguin invalide : '" + type + "'")
                .asRuntimeException();
    }

    private static StatusRuntimeException notFound(String type) {
        return Status.NOT_FOUND
                .withDescription("Groupe introuvable : " + type)
                .asRuntimeException();
    }
}