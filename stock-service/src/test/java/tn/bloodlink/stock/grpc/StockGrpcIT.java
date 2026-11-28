package tn.bloodlink.stock.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

// Un vrai serveur et un vrai client gRPC dans la même JVM (en mémoire, sans port réseau)
@SpringBootTest(properties = {
        "grpc.server.in-process-name=test",
        "grpc.server.port=-1",
        "grpc.client.inProcess.address=in-process:test"
})
@ActiveProfiles("test")
@DirtiesContext
class StockGrpcIT {

    @GrpcClient("inProcess")
    private StockServiceGrpc.StockServiceBlockingStub stub;

    @Test
    void getStock_returns_seeded_level() {
        StockLevel level = stub.getStock(GetStockRequest.newBuilder().setBloodType("O-").build());
        assertEquals(4, level.getUnits());
        assertTrue(level.getUrgent());
    }

    @Test
    void unknown_type_gives_invalid_argument() {
        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.getStock(GetStockRequest.newBuilder().setBloodType("ZZ").build()));
        assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
    }

    @Test
    void streaming_sends_all_blood_types() {
        Iterator<StockLevel> it = stub.streamStockLevels(
                StreamStockRequest.newBuilder().setIntervalSeconds(1).setMaxUpdates(1).build());
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        assertEquals(8, count);
    }

    @Test
    void updateStock_with_too_large_withdrawal_gives_failed_precondition() {
        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.updateStock(UpdateStockRequest.newBuilder()
                        .setBloodType("O-").setDelta(-100).build()));
        assertEquals(Status.Code.FAILED_PRECONDITION, ex.getStatus().getCode());
    }

    @Test
    void updateStock_with_invalid_type_gives_invalid_argument() {
        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.updateStock(UpdateStockRequest.newBuilder()
                        .setBloodType("ZZ").setDelta(1).build()));
        assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
    }

    @Test
    void updateStock_adds_units() {
        // A+ : seul test qui le modifie (30 au départ)
        StockLevel level = stub.updateStock(UpdateStockRequest.newBuilder()
                .setBloodType("A+").setDelta(5).build());
        assertEquals(35, level.getUnits());
    }
}
