package dev.brice.parking.core.entity;

import dev.brice.parking.core.constant.TicketStatus;
import dev.brice.parking.core.constant.VehicleType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ParkingTicketTest {
    private static final LocalDateTime ENTRY = LocalDateTime.of(2026, 11, 10, 8, 0);

    private ParkingTicket openTicket() {
        return ParkingTicket.open(UUID.randomUUID(), "abc123", VehicleType.CAR, UUID.randomUUID(), ENTRY);
    }

    @Test
    void openTicketNormalizesPlateAndStartsOpen() {
        ParkingTicket ticket = openTicket();
        assertEquals("ABC123", ticket.plate());
        assertEquals(TicketStatus.OPEN, ticket.status());
        assertNull(ticket.exitAt());
    }

    @Test
    void rejectsInvalidPlate() {
        assertThrows(IllegalArgumentException.class,
                () -> ParkingTicket.open(UUID.randomUUID(), "A-1", VehicleType.CAR, UUID.randomUUID(), ENTRY));
        assertThrows(IllegalArgumentException.class,
                () -> ParkingTicket.open(UUID.randomUUID(), " ", VehicleType.CAR, UUID.randomUUID(), ENTRY));
    }

    @Test
    void rejectsNullFields() {
        assertThrows(IllegalArgumentException.class,
                () -> ParkingTicket.open(null, "ABC123", VehicleType.CAR, UUID.randomUUID(), ENTRY));
        assertThrows(IllegalArgumentException.class,
                () -> ParkingTicket.open(UUID.randomUUID(), "ABC123", null, UUID.randomUUID(), ENTRY));
        assertThrows(IllegalArgumentException.class,
                () -> ParkingTicket.open(UUID.randomUUID(), "ABC123", VehicleType.CAR, null, ENTRY));
        assertThrows(IllegalArgumentException.class,
                () -> ParkingTicket.open(UUID.randomUUID(), "ABC123", VehicleType.CAR, UUID.randomUUID(), null));
    }

    @Test
    void closeSetsExitAmountAndStatus() {
        ParkingTicket closed = openTicket().close(ENTRY.plusHours(2), new BigDecimal("6000"));
        assertEquals(TicketStatus.CLOSED, closed.status());
        assertEquals(ENTRY.plusHours(2), closed.exitAt());
        assertEquals(new BigDecimal("6000"), closed.totalAmount());
    }

    @Test
    void cannotCloseTwice() {
        ParkingTicket closed = openTicket().close(ENTRY.plusHours(1), BigDecimal.TEN);
        assertThrows(IllegalArgumentException.class, () -> closed.close(ENTRY.plusHours(2), BigDecimal.TEN));
    }

    @Test
    void cannotExitBeforeEntry() {
        assertThrows(IllegalArgumentException.class, () -> openTicket().close(ENTRY.minusMinutes(1), BigDecimal.TEN));
    }

    @Test
    void cannotHaveNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> openTicket().close(ENTRY.plusHours(1), new BigDecimal("-1")));
    }
}
