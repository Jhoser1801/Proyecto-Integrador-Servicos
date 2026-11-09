package dev.brice.parking.core.port.in;

import dev.brice.parking.core.dto.TariffDTO;
import dev.brice.parking.core.entity.Tariff;

import java.util.List;

public interface TariffUseCase {

    Tariff setTariff(TariffDTO request);
    List<Tariff> findAllTariffs();

}
