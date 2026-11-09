package dev.brice.parking.core.service;

import dev.brice.parking.adapter.repository.inmemory.InMemorySlotRepository;
import dev.brice.parking.adapter.repository.inmemory.InMemoryTariffRepository;
import dev.brice.parking.adapter.repository.inmemory.InMemoryTicketRepository;
import dev.brice.parking.core.constant.SlotStatus;
import dev.brice.parking.core.constant.TicketStatus;
import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.dto.CheckInDTO;
import dev.brice.parking.core.dto.SlotDTO;
import dev.brice.parking.core.dto.TariffDTO;
import dev.brice.parking.core.entity.ParkingTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ParkingServiceTest {
    private MutableClock clock;
    private SlotService slotService;
    private ParkingService parking;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-11-10T08:00:00Z"));
        InMemorySlotRepository slots = new InMemorySlotRepository();
        InMemoryTicketRepository tickets = new InMemoryTicketRepository();
        InMemoryTariffRepository tariffs = new InMemoryTariffRepository();
        slotService = new SlotService(slots);
        parking = new ParkingService(tickets, slots, tariffs, clock);

        new TariffService(tariffs).setTariff(new TariffDTO(VehicleType.CAR, new BigDecimal("3000")));
        slotService.createSlot(new SlotDTO("A1", VehicleType.CAR));
        slotService.createSlot(new SlotDTO("A2", VehicleType.CAR));
        slotService.createSlot(new SlotDTO("M1", VehicleType.MOTORCYCLE));
    }

    @Test
    void checkInAssignsFirstFreeSlotAndOccupiesIt() {
        ParkingTicket ticket = parking.checkIn(new CheckInDTO("abc123", VehicleType.CAR));

        assertEquals(TicketStatus.OPEN, ticket.status());
        assertEquals("A1", slotService.findSlotById(ticket.slotId()).orElseThrow().code());
        assertEquals(SlotStatus.OCCUPIED, slotService.findSlotById(ticket.slotId()).orElseThrow().status());
    }

    @Test
    void cannotCheckInSamePlateTwice() {
        parking.checkIn(new CheckInDTO("ABC123", VehicleType.CAR));
        assertThrows(IllegalArgumentException.class, () -> parking.checkIn(new CheckInDTO("abc123", VehicleType.CAR)));
    }

    @Test
    void failsWhenNoFreeSlots() {
        parking.checkIn(new CheckInDTO("AAA111", VehicleType.CAR));
        parking.checkIn(new CheckInDTO("BBB222", VehicleType.CAR));
        assertThrows(IllegalArgumentException.class, () -> parking.checkIn(new CheckInDTO("CCC333", VehicleType.CAR)));
    }

    @Test
    void failsWithoutTariffForVehicleType() {
        assertThrows(IllegalArgumentException.class, () -> parking.checkIn(new CheckInDTO("MMM11A", VehicleType.MOTORCYCLE)));
    }

    @Test
    void checkOutChargesClosesTicketAndFreesSlot() {
        ParkingTicket ticket = parking.checkIn(new CheckInDTO("ABC123", VehicleType.CAR));
        clock.advanceMinutes(150); // 2h30 -> 3 horas

        ParkingTicket closed = parking.checkOut(ticket.id());

        assertEquals(TicketStatus.CLOSED, closed.status());
        assertEquals(new BigDecimal("9000"), closed.totalAmount());
        assertEquals(SlotStatus.FREE, slotService.findSlotById(ticket.slotId()).orElseThrow().status());
        assertTrue(parking.findOpenTickets().isEmpty());
    }

    @Test
    void cannotCheckOutTwiceOrUnknownTicket() {
        ParkingTicket ticket = parking.checkIn(new CheckInDTO("ABC123", VehicleType.CAR));
        parking.checkOut(ticket.id());

        assertThrows(IllegalArgumentException.class, () -> parking.checkOut(ticket.id()));
        assertThrows(IllegalArgumentException.class, () -> parking.checkOut(UUID.randomUUID()));
    }

    @Test
    void plateCanReenterAfterCheckOut() {
        ParkingTicket ticket = parking.checkIn(new CheckInDTO("ABC123", VehicleType.CAR));
        parking.checkOut(ticket.id());
        assertDoesNotThrow(() -> parking.checkIn(new CheckInDTO("ABC123", VehicleType.CAR)));
    }

    @Test
    void duplicatedSlotCodeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> slotService.createSlot(new SlotDTO("a1", VehicleType.CAR)));
    }

    // Reloj controlable para probar el cobro sin esperar horas reales
    static class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant start) {
            this.now = start;
        }

        void advanceMinutes(long minutes) {
            now = now.plusSeconds(minutes * 60);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
