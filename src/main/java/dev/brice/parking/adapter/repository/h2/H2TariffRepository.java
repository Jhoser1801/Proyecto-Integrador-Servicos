package dev.brice.parking.adapter.repository.h2;

import dev.brice.parking.adapter.repository.h2.dao.TariffDAO;
import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.entity.Tariff;
import dev.brice.parking.core.port.TariffRepository;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Optional;

public class H2TariffRepository implements TariffRepository {

    private final Jdbi jdbi;
    private final TariffDAO tariffDAO;

    public H2TariffRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
        this.tariffDAO = jdbi.onDemand(TariffDAO.class);

        setupTables();
    }

    @Override
    public void saveTariff(Tariff tariff) {
        tariffDAO.upsertTariff(tariff.id(), tariff.vehicleType(), tariff.hourlyRate());
    }

    @Override
    public Optional<Tariff> findTariffByVehicleType(VehicleType vehicleType) {
        return tariffDAO.findTariffByVehicleType(vehicleType);
    }

    @Override
    public List<Tariff> findAllTariffs() {
        return tariffDAO.findAllTariffs();
    }

    private void setupTables() {
        jdbi.useHandle(handle -> handle.execute("""
                create table if not exists tariff (
                    tariff_id uuid primary key,
                    vehicle_type varchar(16) not null unique,
                    hourly_rate decimal(12,2) not null
                )"""));
    }
}
