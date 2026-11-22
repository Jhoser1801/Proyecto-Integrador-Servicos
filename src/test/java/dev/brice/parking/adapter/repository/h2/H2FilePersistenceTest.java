package dev.brice.parking.adapter.repository.h2;

import dev.brice.parking.core.constant.SlotStatus;
import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.entity.ParkingSlot;
import org.jdbi.v3.core.ConnectionException;
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
        return open(dir, "sa", "");
    }

    private Jdbi open(Path dir, String user, String password) {
        Jdbi jdbi = Jdbi.create("jdbc:h2:file:" + dir.resolve("parking").toAbsolutePath(), user, password);
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

    @Test
    void databaseKeepsTheUserAndPasswordItWasCreatedWith(@TempDir Path dir) {
        new H2SlotRepository(open(dir, "parking", "clave-segura")); // la primera conexion crea la base con ese usuario

        // con el mismo usuario y contrasena abre
        assertDoesNotThrow(() -> open(dir, "parking", "clave-segura").useHandle(handle -> handle.execute("select 1")));
        // con otra contrasena, otro usuario o la clave vacia de "sa" no abre
        assertThrows(ConnectionException.class, () -> open(dir, "parking", "otra").useHandle(handle -> handle.execute("select 1")));
        assertThrows(ConnectionException.class, () -> open(dir, "sa", "").useHandle(handle -> handle.execute("select 1")));
    }
}
