package dev.brice.parking.core.entity;

import dev.brice.parking.core.constant.VehicleType;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

// Regla de cobro: cada hora o fraccion se cobra como hora completa y el minimo es una hora.
public record Tariff(
        UUID id,
        VehicleType vehicleType,
        BigDecimal hourlyRate
) {
    public Tariff {
        if (id == null) {
            throw new IllegalArgumentException("El id de la tarifa no puede ser nulo");
        }
        if (vehicleType == null) {
            throw new IllegalArgumentException("El tipo de vehiculo de la tarifa no puede ser nulo");
        }
        if (hourlyRate == null || hourlyRate.signum() <= 0) {
            throw new IllegalArgumentException("El valor por hora tiene que ser mayor a 0");
        }
    }

    public BigDecimal calculate(LocalDateTime entryAt, LocalDateTime exitAt) {
        if (entryAt == null || exitAt == null) {
            throw new IllegalArgumentException("Las fechas de entrada y salida no pueden ser nulas");
        }
        if (exitAt.isBefore(entryAt)) {
            throw new IllegalArgumentException("La salida no puede ser anterior a la entrada");
        }
        long minutes = Duration.between(entryAt, exitAt).toMinutes();
        long hours = Math.max(1, (minutes + 59) / 60);
        return hourlyRate.multiply(BigDecimal.valueOf(hours));
    }
}
