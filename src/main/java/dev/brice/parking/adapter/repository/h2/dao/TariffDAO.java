package dev.brice.parking.adapter.repository.h2.dao;

import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.entity.Tariff;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RegisterRowMapper(TariffDAO.Mapper.class)
public interface TariffDAO {

    @SqlUpdate("merge into tariff (tariff_id, vehicle_type, hourly_rate) key (tariff_id) values (?,?,?)")
    void upsertTariff(UUID id, VehicleType vehicleType, BigDecimal hourlyRate);

    @SqlQuery("select * from tariff where vehicle_type = ?")
    Optional<Tariff> findTariffByVehicleType(VehicleType vehicleType);

    @SqlQuery("select * from tariff order by vehicle_type")
    List<Tariff> findAllTariffs();

    class Mapper implements RowMapper<Tariff> {
        @Override
        public Tariff map(ResultSet rs, StatementContext ctx) throws SQLException {
            return new Tariff(
                    rs.getObject("tariff_id", UUID.class),
                    VehicleType.valueOf(rs.getString("vehicle_type")),
                    rs.getBigDecimal("hourly_rate")
            );
        }
    }
}
