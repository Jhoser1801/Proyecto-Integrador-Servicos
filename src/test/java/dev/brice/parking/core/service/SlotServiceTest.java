package dev.brice.parking.core.service;

import dev.brice.parking.adapter.repository.inmemory.InMemorySlotRepository;
import dev.brice.parking.adapter.repository.inmemory.InMemoryTariffRepository;
import dev.brice.parking.adapter.repository.inmemory.InMemoryTicketRepository;
import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.dto.CheckInDTO;
import dev.brice.parking.core.dto.SlotDTO;
import dev.brice.parking.core.dto.TariffDTO;
import dev.brice.parking.core.entity.ParkingSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SlotServiceTest {
    private SlotService service;
    private ParkingService parking;

    @BeforeEach
    void setUp() {
        InMemorySlotRepository slots = new InMemorySlotRepository();
        InMemoryTicketRepository tickets = new InMemoryTicketRepository();
        InMemoryTariffRepository tariffs = new InMemoryTariffRepository();
        service = new SlotService(slots, tickets);
        parking = new ParkingService(tickets, slots, tariffs, Clock.systemDefaultZone());
        new TariffService(tariffs).setTariff(new TariffDTO(VehicleType.CAR, new BigDecimal("3000")));
    }

    @Test
    void updatesCodeAndTypeOfFreeSlotWithoutHistory() {
        ParkingSlot slot = service.createSlot(new SlotDTO("A1", VehicleType.CAR));

        ParkingSlot updated = service.updateSlot(slot.id(), new SlotDTO("b7", VehicleType.MOTORCYCLE));

        assertEquals("B7", updated.code());
        assertEquals(VehicleType.MOTORCYCLE, updated.vehicleType());
        assertEquals(slot.id(), updated.id());
        assertEquals("B7", service.findSlotById(slot.id()).orElseThrow().code());
    }

    @Test
    void cannotUseCodeOfAnotherSlotButCanKeepItsOwn() {
        ParkingSlot a1 = service.createSlot(new SlotDTO("A1", VehicleType.CAR));
        service.createSlot(new SlotDTO("A2", VehicleType.CAR));

        assertThrows(IllegalArgumentException.class, () -> service.updateSlot(a1.id(), new SlotDTO("A2", VehicleType.CAR)));
        assertDoesNotThrow(() -> service.updateSlot(a1.id(), new SlotDTO("a1", VehicleType.CAR)));
    }

    @Test
    void occupiedSlotCannotBeEditedNorDeleted() {
        ParkingSlot slot = service.createSlot(new SlotDTO("A1", VehicleType.CAR));
        parking.checkIn(new CheckInDTO("ABC123", VehicleType.CAR));

        assertThrows(IllegalArgumentException.class, () -> service.updateSlot(slot.id(), new SlotDTO("A9", VehicleType.CAR)));
        assertThrows(IllegalArgumentException.class, () -> service.deleteSlot(slot.id()));
    }

    @Test
    void slotWithHistoryKeepsItsTypeAndIsNotDeleted() {
        ParkingSlot slot = service.createSlot(new SlotDTO("A1", VehicleType.CAR));
        var ticket = parking.checkIn(new CheckInDTO("ABC123", VehicleType.CAR));
        parking.checkOut(ticket.id());

        assertThrows(IllegalArgumentException.class, () -> service.updateSlot(slot.id(), new SlotDTO("A1", VehicleType.MOTORCYCLE)));
        assertDoesNotThrow(() -> service.updateSlot(slot.id(), new SlotDTO("A5", VehicleType.CAR)));
        assertThrows(IllegalArgumentException.class, () -> service.deleteSlot(slot.id()));
    }

    @Test
    void deletesFreeSlotThatWasNeverUsed() {
        ParkingSlot slot = service.createSlot(new SlotDTO("A1", VehicleType.CAR));

        service.deleteSlot(slot.id());

        assertTrue(service.findSlotById(slot.id()).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> service.deleteSlot(slot.id()));
        assertThrows(IllegalArgumentException.class, () -> service.updateSlot(UUID.randomUUID(), new SlotDTO("X1", VehicleType.CAR)));
    }
}
