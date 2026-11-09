package dev.brice.parking.core.dto;

import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.entity.Tariff;

import java.math.BigDecimal;
import java.util.UUID;

public record TariffDTO(VehicleType vehicleType, BigDecimal hourlyRate) {
    public Tariff toEntity(UUID id) {
        return new Tariff(id, vehicleType, hourlyRate);
    }
}
