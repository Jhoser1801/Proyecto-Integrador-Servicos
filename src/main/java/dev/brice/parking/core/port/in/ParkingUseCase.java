package dev.brice.parking.core.port.in;

import dev.brice.parking.core.dto.CheckInDTO;
import dev.brice.parking.core.entity.ParkingTicket;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParkingUseCase {

    ParkingTicket checkIn(CheckInDTO request);
    ParkingTicket checkOut(UUID ticketId);
    Optional<ParkingTicket> findTicketById(UUID ticketId);
    List<ParkingTicket> findAllTickets();
    List<ParkingTicket> findOpenTickets();

}
