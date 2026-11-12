package dev.brice.parking.adapter.repository.inmemory;

import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.entity.Tariff;
import dev.brice.parking.core.port.TariffRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTariffRepository implements TariffRepository {
    private final Map<VehicleType, Tariff> tariffs = new ConcurrentHashMap<>();

    @Override
    public void saveTariff(Tariff tariff) {
        tariffs.put(tariff.vehicleType(), tariff);
    }

    @Override
    public Optional<Tariff> findTariffByVehicleType(VehicleType vehicleType) {
        return Optional.ofNullable(tariffs.get(vehicleType));
    }

    @Override
    public List<Tariff> findAllTariffs() {
        return List.copyOf(tariffs.values());
    }
}
