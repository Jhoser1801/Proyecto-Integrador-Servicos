package dev.brice.parking.core.port;

import dev.brice.parking.core.entity.ParkingTicket;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository {

    void saveTicket(ParkingTicket ticket);
    Optional<ParkingTicket> findTicketById(UUID id);
    Optional<ParkingTicket> findOpenTicketByPlate(String plate);
    List<ParkingTicket> findAllTickets();
    List<ParkingTicket> findClosedTicketsBetween(LocalDateTime from, LocalDateTime toExclusive);

}
