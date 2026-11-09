package dev.brice.parking.core.dto;

import dev.brice.parking.core.constant.VehicleType;

public record CheckInDTO(String plate, VehicleType vehicleType) {
}
