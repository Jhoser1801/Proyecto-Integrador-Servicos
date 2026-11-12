package dev.brice.parking.core.service;

import dev.brice.parking.core.dto.ReceiptDTO;
import dev.brice.parking.core.dto.ReportDataDTO;
import dev.brice.parking.core.entity.ParkingSlot;
import dev.brice.parking.core.entity.ParkingTicket;
import dev.brice.parking.core.port.ReportGateway;
import dev.brice.parking.core.port.SlotRepository;
import dev.brice.parking.core.port.TicketRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ReportService {
    private final TicketRepository ticketRepository;
    private final SlotRepository slotRepository;
    private final ReportGateway gateway;

    public ReportService(TicketRepository ticketRepository, SlotRepository slotRepository, ReportGateway gateway) {
        this.ticketRepository = ticketRepository;
        this.slotRepository = slotRepository;
        this.gateway = gateway;
    }

    public byte[] generateRevenueReport(LocalDate from, LocalDate to) {
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

        return gateway.generateRevenueReport(new ReportDataDTO("Reporte de ingresos", from, to, tickets, total));
    }

    public byte[] generateReceipt(UUID ticketId) {
        ParkingTicket ticket = ticketRepository.findTicketById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("El ticket no existe"));
        if (ticket.isOpen()) {
            throw new IllegalArgumentException("Solo se genera comprobante de tickets cerrados");
        }
        String slotCode = slotRepository.findSlotById(ticket.slotId()).map(ParkingSlot::code).orElse("-");
        return gateway.generateReceipt(new ReceiptDTO(ticket, slotCode));
    }
}
