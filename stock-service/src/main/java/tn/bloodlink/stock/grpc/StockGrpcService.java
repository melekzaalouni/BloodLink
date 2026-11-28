package tn.bloodlink.stock.grpc;

import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import tn.bloodlink.stock.domain.BloodStock;
import tn.bloodlink.stock.service.StockService;

@GrpcService
public class StockGrpcService extends StockServiceGrpc.StockServiceImplBase {

    private final StockService service;

    public StockGrpcService(StockService service) {
        this.service = service;
    }

    // UNAIRE : le niveau de stock d'un groupe sanguin
    @Override
    public void getStock(GetStockRequest request, StreamObserver<StockLevel> observer) {
        observer.onNext(toProto(service.get(request.getBloodType())));
        observer.onCompleted();
    }

    // UNAIRE : applique un delta (positif = don, négatif = prélèvement)
    @Override
    public void updateStock(UpdateStockRequest request, StreamObserver<StockLevel> observer) {
        observer.onNext(toProto(service.update(request.getBloodType(), request.getDelta())));
        observer.onCompleted();
    }

    // SERVER STREAMING : envoie les 8 niveaux à chaque tour.
    // max_updates = nombre de tours (0 = sans fin, jusqu'à ce que le client parte).
    @Override
    public void streamStockLevels(StreamStockRequest request, StreamObserver<StockLevel> observer) {
        ServerCallStreamObserver<StockLevel> call = (ServerCallStreamObserver<StockLevel>) observer;
        long pauseMs = Math.max(1, request.getIntervalSeconds()) * 1000L;
        int rounds = 0;
        try {
            while (!call.isCancelled()) {                  // on s'arrête si le client est parti
                for (BloodStock stock : service.all()) {
                    if (call.isCancelled()) {
                        return;
                    }
                    observer.onNext(toProto(stock));
                }
                rounds++;
                if (request.getMaxUpdates() > 0 && rounds >= request.getMaxUpdates()) {
                    break;
                }
                Thread.sleep(pauseMs);
            }
            if (!call.isCancelled()) {
                observer.onCompleted();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            observer.onError(Status.CANCELLED.withDescription("interrupted").asRuntimeException());
        }
    }

    private StockLevel toProto(BloodStock s) {
        return StockLevel.newBuilder()
                .setBloodType(s.getBloodType())
                .setUnits(s.getUnits())
                .setThreshold(s.getThreshold())
                .setUrgent(s.isUrgent())
                .setUpdatedAtEpoch(s.getUpdatedAt().getEpochSecond())
                .build();
    }
}
