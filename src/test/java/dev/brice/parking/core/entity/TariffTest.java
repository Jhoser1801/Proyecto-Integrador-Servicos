package dev.brice.parking.core.entity;

import dev.brice.parking.core.constant.VehicleType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TariffTest {
    private static final LocalDateTime ENTRY = LocalDateTime.of(2026, 11, 10, 8, 0);
    private final Tariff tariff = new Tariff(UUID.randomUUID(), VehicleType.CAR, new BigDecimal("3000"));

    @Test
    void chargesMinimumOneHour() {
        assertEquals(new BigDecimal("3000"), tariff.calculate(ENTRY, ENTRY));
        assertEquals(new BigDecimal("3000"), tariff.calculate(ENTRY, ENTRY.plusMinutes(5)));
    }

    @Test
    void exactHoursAreNotRoundedUp() {
        assertEquals(new BigDecimal("6000"), tariff.calculate(ENTRY, ENTRY.plusHours(2)));
    }

    @Test
    void fractionIsChargedAsFullHour() {
        assertEquals(new BigDecimal("9000"), tariff.calculate(ENTRY, ENTRY.plusMinutes(121)));
    }

    @Test
    void rejectsExitBeforeEntry() {
        assertThrows(IllegalArgumentException.class, () -> tariff.calculate(ENTRY, ENTRY.minusMinutes(1)));
    }

    @Test
    void rejectsInvalidRate() {
        assertThrows(IllegalArgumentException.class, () -> new Tariff(UUID.randomUUID(), VehicleType.CAR, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new Tariff(UUID.randomUUID(), VehicleType.CAR, null));
        assertThrows(IllegalArgumentException.class, () -> new Tariff(UUID.randomUUID(), null, BigDecimal.ONE));
    }
}
