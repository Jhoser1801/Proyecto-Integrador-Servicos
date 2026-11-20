package dev.brice.parking.adapter.repository.h2;

import dev.brice.parking.core.constant.SlotStatus;
import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.entity.ParkingSlot;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// La base H2 en archivo conserva los datos aunque se cierre la conexion y se abra otra
class H2FilePersistenceTest {

    private Jdbi open(Path dir) {
        Jdbi jdbi = Jdbi.create("jdbc:h2:file:" + dir.resolve("parking").toAbsolutePath(), "sa", "");
        jdbi.installPlugin(new SqlObjectPlugin());
        return jdbi;
    }

    @Test
    void dataSurvivesReopeningTheDatabase(@TempDir Path dir) {
        ParkingSlot slot = new ParkingSlot(UUID.randomUUID(), "A1", VehicleType.CAR, SlotStatus.FREE);
        new H2SlotRepository(open(dir)).saveSlot(slot.occupy());

        // segunda "ejecucion": un Jdbi y repositorios nuevos sobre el mismo archivo
        H2SlotRepository reopened = new H2SlotRepository(open(dir));

        assertEquals(slot.occupy(), reopened.findSlotById(slot.id()).orElseThrow());
        assertEquals(1, reopened.findAllSlots().size());
        assertTrue(Files.exists(dir.resolve("parking.mv.db")));
    }
}
