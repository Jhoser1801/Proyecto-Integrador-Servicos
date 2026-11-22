package dev.brice.parking.adapter.repository.h2;

import dev.brice.parking.adapter.repository.h2.dao.TicketDAO;
import dev.brice.parking.core.entity.ParkingTicket;
import dev.brice.parking.core.port.TicketRepository;
import org.jdbi.v3.core.Jdbi;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Requiere que la tabla parking_slot exista primero (llave foranea): crear H2SlotRepository antes
public class H2TicketRepository implements TicketRepository {

    private final Jdbi jdbi;
    private final TicketDAO ticketDAO;

    public H2TicketRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
        this.ticketDAO = jdbi.onDemand(TicketDAO.class);

        setupTables();
    }

    @Override
    public void saveTicket(ParkingTicket ticket) {
        ticketDAO.upsertTicket(ticket.id(), ticket.plate(), ticket.vehicleType(), ticket.slotId(),
                ticket.entryAt(), ticket.exitAt(), ticket.totalAmount(), ticket.status());
    }

    @Override
    public Optional<ParkingTicket> findTicketById(UUID id) {
        return ticketDAO.findTicketById(id);
    }

    @Override
    public Optional<ParkingTicket> findOpenTicketByPlate(String plate) {
        return ticketDAO.findOpenTicketByPlate(plate);
    }

    @Override
    public List<ParkingTicket> findAllTickets() {
        return ticketDAO.findAllTickets();
    }

    @Override
    public List<ParkingTicket> findClosedTicketsBetween(LocalDateTime from, LocalDateTime toExclusive) {
        return ticketDAO.findClosedTicketsBetween(from, toExclusive);
    }

    @Override
    public boolean existsTicketBySlotId(UUID slotId) {
        return ticketDAO.existsTicketBySlotId(slotId);
    }

    private void setupTables() {
        jdbi.useHandle(handle -> handle.execute("""
                create table if not exists parking_ticket (
                    ticket_id uuid primary key,
                    plate varchar(7) not null,
                    vehicle_type varchar(16) not null,
                    slot_id uuid not null,
                    entry_at timestamp not null,
                    exit_at timestamp,
                    total_amount decimal(12,2),
                    status varchar(16) not null,
                    constraint fk_ticket_slot foreign key (slot_id) references parking_slot(slot_id)
                )"""));
    }
}
