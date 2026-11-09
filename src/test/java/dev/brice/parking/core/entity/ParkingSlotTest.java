package dev.brice.parking.core.entity;

import dev.brice.parking.core.constant.SlotStatus;
import dev.brice.parking.core.constant.VehicleType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ParkingSlotTest {
    private ParkingSlot freeSlot() {
        return new ParkingSlot(UUID.randomUUID(), " a1 ", VehicleType.CAR, SlotStatus.FREE);
    }

    @Test
    void normalizesCode() {
        assertEquals("A1", freeSlot().code());
    }

    @Test
    void occupyAndReleaseChangeStatus() {
        ParkingSlot occupied = freeSlot().occupy();
        assertEquals(SlotStatus.OCCUPIED, occupied.status());
        assertEquals(SlotStatus.FREE, occupied.release().status());
    }

    @Test
    void cannotOccupyOccupiedSlot() {
        assertThrows(IllegalArgumentException.class, () -> freeSlot().occupy().occupy());
    }

    @Test
    void cannotReleaseFreeSlot() {
        assertThrows(IllegalArgumentException.class, () -> freeSlot().release());
    }

    @Test
    void rejectsInvalidData() {
        assertThrows(IllegalArgumentException.class, () -> new ParkingSlot(UUID.randomUUID(), " ", VehicleType.CAR, SlotStatus.FREE));
        assertThrows(IllegalArgumentException.class, () -> new ParkingSlot(UUID.randomUUID(), "ABCDEFGHIJK", VehicleType.CAR, SlotStatus.FREE));
        assertThrows(IllegalArgumentException.class, () -> new ParkingSlot(UUID.randomUUID(), "A1", null, SlotStatus.FREE));
        assertThrows(IllegalArgumentException.class, () -> new ParkingSlot(null, "A1", VehicleType.CAR, SlotStatus.FREE));
    }
}
