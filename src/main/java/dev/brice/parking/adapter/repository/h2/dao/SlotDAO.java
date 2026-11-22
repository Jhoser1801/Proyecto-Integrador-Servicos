package dev.brice.parking.adapter.repository.h2.dao;

import dev.brice.parking.core.constant.SlotStatus;
import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.entity.ParkingSlot;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RegisterRowMapper(SlotDAO.Mapper.class)
public interface SlotDAO {

    // merge = inserta si el id no existe y actualiza si ya existe (sirve para crear, ocupar y liberar)
    @SqlUpdate("merge into parking_slot (slot_id, code, vehicle_type, status) key (slot_id) values (?,?,?,?)")
    void upsertSlot(UUID id, String code, VehicleType vehicleType, SlotStatus status);

    @SqlQuery("select * from parking_slot where slot_id = ?")
    Optional<ParkingSlot> findSlotById(UUID id);

    @SqlQuery("select * from parking_slot where upper(code) = upper(?)")
    Optional<ParkingSlot> findSlotByCode(String code);

    @SqlQuery("select * from parking_slot order by code")
    List<ParkingSlot> findAllSlots();

    @SqlQuery("select * from parking_slot where vehicle_type = ? and status = 'FREE' order by code")
    List<ParkingSlot> findFreeSlotsByType(VehicleType vehicleType);

    @SqlUpdate("delete from parking_slot where slot_id = ?")
    void deleteSlotById(UUID id);

    class Mapper implements RowMapper<ParkingSlot> {
        @Override
        public ParkingSlot map(ResultSet rs, StatementContext ctx) throws SQLException {
            return new ParkingSlot(
                    rs.getObject("slot_id", UUID.class),
                    rs.getString("code"),
                    VehicleType.valueOf(rs.getString("vehicle_type")),
                    SlotStatus.valueOf(rs.getString("status"))
            );
        }
    }
}
