package dev.brice.parking.core.service;

import dev.brice.parking.core.dto.TariffDTO;
import dev.brice.parking.core.entity.Tariff;
import dev.brice.parking.core.port.TariffRepository;

import java.util.List;
import java.util.UUID;

public class TariffService {
    private final TariffRepository repository;

    public TariffService(TariffRepository repository) {
        this.repository = repository;
    }

    // Una sola tarifa por tipo de vehiculo: si ya existe se reemplaza el valor
    public Tariff setTariff(TariffDTO request) {
        UUID id = request.vehicleType() == null ? UUID.randomUUID()
                : repository.findTariffByVehicleType(request.vehicleType()).map(Tariff::id).orElse(UUID.randomUUID());
        Tariff tariff = request.toEntity(id);
        repository.saveTariff(tariff);
        return tariff;
    }

    public List<Tariff> findAllTariffs() {
        return repository.findAllTariffs();
    }
}
