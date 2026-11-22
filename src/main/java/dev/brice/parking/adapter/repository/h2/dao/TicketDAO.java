package dev.brice.parking.adapter.repository.h2.dao;

import dev.brice.parking.core.constant.TicketStatus;
import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.entity.ParkingTicket;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RegisterRowMapper(TicketDAO.Mapper.class)
public interface TicketDAO {

    @SqlUpdate("merge into parking_ticket (ticket_id, plate, vehicle_type, slot_id, entry_at, exit_at, total_amount, status) "
            + "key (ticket_id) values (?,?,?,?,?,?,?,?)")
    void upsertTicket(UUID id, String plate, VehicleType vehicleType, UUID slotId, LocalDateTime entryAt,
                      LocalDateTime exitAt, BigDecimal totalAmount, TicketStatus status);

    @SqlQuery("select * from parking_ticket where ticket_id = ?")
    Optional<ParkingTicket> findTicketById(UUID id);

    @SqlQuery("select * from parking_ticket where status = 'OPEN' and upper(plate) = upper(?)")
    Optional<ParkingTicket> findOpenTicketByPlate(String plate);

    @SqlQuery("select * from parking_ticket order by entry_at desc")
    List<ParkingTicket> findAllTickets();

    @SqlQuery("select * from parking_ticket where status = 'CLOSED' and exit_at >= ? and exit_at < ? order by exit_at")
    List<ParkingTicket> findClosedTicketsBetween(LocalDateTime from, LocalDateTime toExclusive);

    @SqlQuery("select count(*) > 0 from parking_ticket where slot_id = ?")
    boolean existsTicketBySlotId(UUID slotId);

    class Mapper implements RowMapper<ParkingTicket> {
        @Override
        public ParkingTicket map(ResultSet rs, StatementContext ctx) throws SQLException {
            return new ParkingTicket(
                    rs.getObject("ticket_id", UUID.class),
                    rs.getString("plate"),
                    VehicleType.valueOf(rs.getString("vehicle_type")),
                    rs.getObject("slot_id", UUID.class),
                    rs.getObject("entry_at", LocalDateTime.class),
                    rs.getObject("exit_at", LocalDateTime.class),
                    rs.getBigDecimal("total_amount"),
                    TicketStatus.valueOf(rs.getString("status"))
            );
        }
    }
}
