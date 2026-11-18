package dev.brice.parking.core.service;

import dev.brice.parking.core.dto.ReceiptDTO;
import dev.brice.parking.core.dto.ReportDataDTO;
import dev.brice.parking.core.entity.ParkingSlot;
import dev.brice.parking.core.entity.ParkingTicket;
import dev.brice.parking.core.port.SlotRepository;
import dev.brice.parking.core.port.TicketRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

// Arma los datos de los reportes (que se consulta, que se suma, que se valida).
// Convertirlos en PDF se hara despues, a traves del puerto ReportGateway.
public class ReportService {
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
