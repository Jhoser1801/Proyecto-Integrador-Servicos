package dev.brice.parking.core.service;

import dev.brice.parking.core.dto.TariffDTO;
import dev.brice.parking.core.entity.Tariff;
import dev.brice.parking.core.port.in.TariffUseCase;
import dev.brice.parking.core.port.out.TariffRepository;

import java.util.List;
import java.util.UUID;

public class TariffService implements TariffUseCase {
    private final TariffRepository repository;

    public TariffService(TariffRepository repository) {
        this.repository = repository;
    }

    // Una sola tarifa por tipo de vehiculo: si ya existe se reemplaza el valor
    @Override
    public Tariff setTariff(TariffDTO request) {
        UUID id = request.vehicleType() == null ? UUID.randomUUID()
                : repository.findTariffByVehicleType(request.vehicleType()).map(Tariff::id).orElse(UUID.randomUUID());
        Tariff tariff = request.toEntity(id);
        repository.saveTariff(tariff);
        return tariff;
    }

    @Override
    public List<Tariff> findAllTariffs() {
        return repository.findAllTariffs();
    }
}
