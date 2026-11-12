package dev.brice.parking.core.service;

import dev.brice.parking.core.dto.CheckInDTO;
import dev.brice.parking.core.entity.ParkingSlot;
import dev.brice.parking.core.entity.ParkingTicket;
import dev.brice.parking.core.entity.Tariff;
import dev.brice.parking.core.port.SlotRepository;
import dev.brice.parking.core.port.TariffRepository;
import dev.brice.parking.core.port.TicketRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Entrada: busca un cupo libre del tipo de vehiculo y lo ocupa. Salida: cobra, cierra el ticket y libera el cupo.
public class ParkingService {
    private final TicketRepository ticketRepository;
    private final SlotRepository slotRepository;
    private final TariffRepository tariffRepository;
    private final Clock clock;

    public ParkingService(TicketRepository ticketRepository,
                          SlotRepository slotRepository,
                          TariffRepository tariffRepository,
                          Clock clock) {
        this.ticketRepository = ticketRepository;
        this.slotRepository = slotRepository;
        this.tariffRepository = tariffRepository;
        this.clock = clock;
    }

    public ParkingTicket checkIn(CheckInDTO request) {
        if (request == null || request.vehicleType() == null) {
            throw new IllegalArgumentException("El tipo de vehiculo es obligatorio");
        }

        ParkingTicket draft = ParkingTicket.open(UUID.randomUUID(), request.plate(), request.vehicleType(),
                UUID.randomUUID(), LocalDateTime.now(clock)); // valida la placa antes de tocar el cupo

        if (ticketRepository.findOpenTicketByPlate(draft.plate()).isPresent()) {
            throw new IllegalArgumentException("El vehiculo " + draft.plate() + " ya esta dentro del parqueadero");
        }
        if (tariffRepository.findTariffByVehicleType(draft.vehicleType()).isEmpty()) {
            throw new IllegalArgumentException("No hay tarifa configurada para ese tipo de vehiculo");
        }

        ParkingSlot slot = slotRepository.findFreeSlotsByType(draft.vehicleType()).stream()
                .min(Comparator.comparing(ParkingSlot::code))
                .orElseThrow(() -> new IllegalArgumentException("No hay cupos libres para ese tipo de vehiculo"));

        ParkingTicket ticket = ParkingTicket.open(draft.id(), draft.plate(), draft.vehicleType(), slot.id(), draft.entryAt());
        slotRepository.saveSlot(slot.occupy());
        ticketRepository.saveTicket(ticket);
        return ticket;
    }

    public ParkingTicket checkOut(UUID ticketId) {
        ParkingTicket ticket = ticketRepository.findTicketById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("El ticket no existe"));
        if (!ticket.isOpen()) {
            throw new IllegalArgumentException("El ticket ya fue cerrado");
        }

        Tariff tariff = tariffRepository.findTariffByVehicleType(ticket.vehicleType())
                .orElseThrow(() -> new IllegalArgumentException("No hay tarifa configurada para ese tipo de vehiculo"));
        ParkingSlot slot = slotRepository.findSlotById(ticket.slotId())
                .orElseThrow(() -> new IllegalArgumentException("El cupo del ticket no existe"));

        LocalDateTime exitAt = LocalDateTime.now(clock);
        BigDecimal amount = tariff.calculate(ticket.entryAt(), exitAt);
        ParkingTicket closed = ticket.close(exitAt, amount);

        ticketRepository.saveTicket(closed);
        slotRepository.saveSlot(slot.release());
        return closed;
    }

    public Optional<ParkingTicket> findTicketById(UUID ticketId) {
        return ticketRepository.findTicketById(ticketId);
    }

    public List<ParkingTicket> findAllTickets() {
        return ticketRepository.findAllTickets().stream()
                .sorted(Comparator.comparing(ParkingTicket::entryAt).reversed())
                .toList();
    }

    public List<ParkingTicket> findOpenTickets() {
        return findAllTickets().stream().filter(ParkingTicket::isOpen).toList();
    }
}
