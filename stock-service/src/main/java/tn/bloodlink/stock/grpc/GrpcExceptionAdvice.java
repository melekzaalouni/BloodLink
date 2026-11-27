package tn.bloodlink.stock.grpc;

import io.grpc.Status;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import tn.bloodlink.stock.domain.InsufficientStockException;
import tn.bloodlink.stock.domain.InvalidBloodTypeException;
import tn.bloodlink.stock.domain.StockNotFoundException;

/**
 * Jumeau gRPC de @RestControllerAdvice : convertit les exceptions métier
 * (qui ne connaissent pas gRPC) en statuts gRPC, jamais UNKNOWN.
 */
@GrpcAdvice
public class GrpcExceptionAdvice {

    @GrpcExceptionHandler(StockNotFoundException.class)
    public Status notFound(StockNotFoundException e) {
        return Status.NOT_FOUND.withDescription(e.getMessage()).withCause(e);
    }

    @GrpcExceptionHandler(InvalidBloodTypeException.class)
    public Status invalid(InvalidBloodTypeException e) {
        return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).withCause(e);
    }

    @GrpcExceptionHandler(InsufficientStockException.class)
    public Status insufficient(InsufficientStockException e) {
        return Status.FAILED_PRECONDITION.withDescription(e.getMessage()).withCause(e);
    }
}
