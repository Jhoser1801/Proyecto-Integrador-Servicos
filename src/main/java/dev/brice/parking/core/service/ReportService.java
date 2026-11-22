package dev.brice.parking.core.service;

import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.dto.DailyRevenueDTO;
import dev.brice.parking.core.dto.ReceiptDTO;
import dev.brice.parking.core.dto.ReportDataDTO;
import dev.brice.parking.core.dto.RevenueSummaryDTO;
import dev.brice.parking.core.entity.ParkingSlot;
import dev.brice.parking.core.entity.ParkingTicket;
import dev.brice.parking.core.port.SlotRepository;
import dev.brice.parking.core.port.TicketRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

// Arma los datos de los reportes (que se consulta, que se suma, que se valida).
// Convertirlos en PDF se hara despues, a traves del puerto ReportGateway.
public class ReportService {
    private static final int MAX_RANGE_DAYS = 366;

    private final TicketRepository ticketRepository;
    private final SlotRepository slotRepository;

    public ReportService(TicketRepository ticketRepository, SlotRepository slotRepository) {
        this.ticketRepository = ticketRepository;
        this.slotRepository = slotRepository;
    }

    // Ingresos por rango de fechas: suma lo cobrado en los tickets cerrados cuya salida cae en el rango
    public ReportDataDTO buildRevenueReport(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Las fechas del reporte son obligatorias");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        }

        List<ParkingTicket> tickets = ticketRepository
                .findClosedTicketsBetween(from.atStartOfDay(), to.plusDays(1).atStartOfDay()).stream()
                .sorted(Comparator.comparing(ParkingTicket::exitAt))
                .toList();
        BigDecimal total = tickets.stream().map(ParkingTicket::totalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ReportDataDTO("Reporte de ingresos", from, to, tickets, total);
    }

    // Totales del reporte: ganancia por dia (incluye los dias sin ingresos) y por tipo de vehiculo
    public RevenueSummaryDTO buildRevenueSummary(LocalDate from, LocalDate to) {
        ReportDataDTO report = buildRevenueReport(from, to);
        if (from.plusDays(MAX_RANGE_DAYS).isBefore(to)) {
            throw new IllegalArgumentException("El rango del reporte no puede superar " + MAX_RANGE_DAYS + " dias");
        }

        Map<LocalDate, BigDecimal> perDay = new TreeMap<>();
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            perDay.put(day, BigDecimal.ZERO);
        }
        BigDecimal cars = BigDecimal.ZERO;
        BigDecimal motorcycles = BigDecimal.ZERO;
        for (ParkingTicket ticket : report.tickets()) {
            perDay.merge(ticket.exitAt().toLocalDate(), ticket.totalAmount(), BigDecimal::add);
            if (ticket.vehicleType() == VehicleType.CAR) {
                cars = cars.add(ticket.totalAmount());
            } else {
                motorcycles = motorcycles.add(ticket.totalAmount());
            }
        }

        BigDecimal best = perDay.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        List<DailyRevenueDTO> days = perDay.entrySet().stream()
                .map(entry -> new DailyRevenueDTO(entry.getKey(), entry.getValue(), best.signum() == 0 ? 0
                        : entry.getValue().multiply(BigDecimal.valueOf(100)).divide(best, 0, java.math.RoundingMode.HALF_UP).intValue()))
                .toList();
        return new RevenueSummaryDTO(from, to, report.totalRevenue(), report.tickets().size(), cars, motorcycles, days);
    }

    // Comprobante de salida: solo de tickets cerrados
    public ReceiptDTO buildReceipt(UUID ticketId) {
        ParkingTicket ticket = ticketRepository.findTicketById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("El ticket no existe"));
        if (ticket.isOpen()) {
            throw new IllegalArgumentException("Solo se genera comprobante de tickets cerrados");
        }
        String slotCode = slotRepository.findSlotById(ticket.slotId()).map(ParkingSlot::code).orElse("-");
        return new ReceiptDTO(ticket, slotCode);
    }
}
