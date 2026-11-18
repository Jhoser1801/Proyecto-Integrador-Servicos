package dev.brice.parking.core.service;

import dev.brice.parking.adapter.repository.inmemory.InMemorySlotRepository;
import dev.brice.parking.adapter.repository.inmemory.InMemoryTariffRepository;
import dev.brice.parking.adapter.repository.inmemory.InMemoryTicketRepository;
import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.dto.CheckInDTO;
import dev.brice.parking.core.dto.ReceiptDTO;
import dev.brice.parking.core.dto.ReportDataDTO;
import dev.brice.parking.core.dto.SlotDTO;
import dev.brice.parking.core.dto.TariffDTO;
import dev.brice.parking.core.entity.ParkingTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReportServiceTest {
    private ParkingServiceTest.MutableClock clock;
    private ParkingService parking;
    private ReportService reports;

    @BeforeEach
    void setUp() {
        clock = new ParkingServiceTest.MutableClock(Instant.parse("2026-11-10T08:00:00Z"));
        InMemorySlotRepository slots = new InMemorySlotRepository();
        InMemoryTicketRepository tickets = new InMemoryTicketRepository();
        InMemoryTariffRepository tariffs = new InMemoryTariffRepository();
        parking = new ParkingService(tickets, slots, tariffs, clock);
        reports = new ReportService(tickets, slots);

        new TariffService(tariffs).setTariff(new TariffDTO(VehicleType.CAR, new BigDecimal("3000")));
        new SlotService(slots).createSlot(new SlotDTO("A1", VehicleType.CAR));
        new SlotService(slots).createSlot(new SlotDTO("A2", VehicleType.CAR));
    }

    @Test
    void revenueReportSumsClosedTicketsInRange() {
        ParkingTicket first = parking.checkIn(new CheckInDTO("ABC123", VehicleType.CAR));
        clock.advanceMinutes(60);
        parking.checkOut(first.id());            // 3.000, sale el 10/11
        ParkingTicket second = parking.checkIn(new CheckInDTO("XYZ987", VehicleType.CAR));
        clock.advanceMinutes(150);
        parking.checkOut(second.id());           // 9.000, sale el 10/11
        parking.checkIn(new CheckInDTO("QWE456", VehicleType.CAR)); // sigue abierto: no cuenta

        ReportDataDTO report = reports.buildRevenueReport(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 10));

        assertEquals(2, report.tickets().size());
        assertEquals(new BigDecimal("12000"), report.totalRevenue());
    }

    @Test
    void revenueReportExcludesTicketsOutsideRange() {
        ParkingTicket ticket = parking.checkIn(new CheckInDTO("ABC123", VehicleType.CAR));
        clock.advanceMinutes(60);
        parking.checkOut(ticket.id());

        ReportDataDTO report = reports.buildRevenueReport(LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 12));

        assertTrue(report.tickets().isEmpty());
        assertEquals(BigDecimal.ZERO, report.totalRevenue());
    }

    @Test
    void revenueReportRejectsInvalidRange() {
        assertThrows(IllegalArgumentException.class,
                () -> reports.buildRevenueReport(LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 10)));
        assertThrows(IllegalArgumentException.class, () -> reports.buildRevenueReport(null, LocalDate.of(2026, 11, 10)));
    }

    @Test
    void receiptContainsClosedTicketAndSlotCode() {
        ParkingTicket ticket = parking.checkIn(new CheckInDTO("ABC123", VehicleType.CAR));
        clock.advanceMinutes(30);
        parking.checkOut(ticket.id());

        ReceiptDTO receipt = reports.buildReceipt(ticket.id());

        assertEquals("A1", receipt.slotCode());
        assertEquals(new BigDecimal("3000"), receipt.ticket().totalAmount());
    }

    @Test
    void receiptRejectsOpenOrUnknownTicket() {
        ParkingTicket open = parking.checkIn(new CheckInDTO("ABC123", VehicleType.CAR));

        assertThrows(IllegalArgumentException.class, () -> reports.buildReceipt(open.id()));
        assertThrows(IllegalArgumentException.class, () -> reports.buildReceipt(UUID.randomUUID()));
    }
}
