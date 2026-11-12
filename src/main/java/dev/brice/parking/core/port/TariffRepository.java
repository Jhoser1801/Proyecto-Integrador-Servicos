package dev.brice.parking.core.port;

import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.entity.Tariff;

import java.util.List;
import java.util.Optional;

public interface TariffRepository {

    void saveTariff(Tariff tariff);
    Optional<Tariff> findTariffByVehicleType(VehicleType vehicleType);
    List<Tariff> findAllTariffs();

}
