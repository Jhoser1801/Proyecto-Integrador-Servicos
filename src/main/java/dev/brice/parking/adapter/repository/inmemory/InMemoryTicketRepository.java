package dev.brice.parking.adapter.repository.inmemory;

import dev.brice.parking.core.entity.ParkingTicket;
import dev.brice.parking.core.port.TicketRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTicketRepository implements TicketRepository {
    private final Map<UUID, ParkingTicket> tickets = new ConcurrentHashMap<>();

    @Override
    public void saveTicket(ParkingTicket ticket) {
        tickets.put(ticket.id(), ticket);
    }

    @Override
    public Optional<ParkingTicket> findTicketById(UUID id) {
        return Optional.ofNullable(tickets.get(id));
    }

    @Override
    public Optional<ParkingTicket> findOpenTicketByPlate(String plate) {
        return tickets.values().stream()
                .filter(ticket -> ticket.isOpen() && ticket.plate().equalsIgnoreCase(plate))
                .findFirst();
    }

    @Override
    public List<ParkingTicket> findAllTickets() {
        return List.copyOf(tickets.values());
    }

    @Override
    public List<ParkingTicket> findClosedTicketsBetween(LocalDateTime from, LocalDateTime toExclusive) {
        return tickets.values().stream()
                .filter(ticket -> !ticket.isOpen())
                .filter(ticket -> !ticket.exitAt().isBefore(from) && ticket.exitAt().isBefore(toExclusive))
                .toList();
    }
}
