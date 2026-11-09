package dev.brice.parking.core.dto;

import dev.brice.parking.core.constant.SlotStatus;
import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.entity.ParkingSlot;

import java.util.UUID;

// Los mismos atributos de la entidad sin ID ni estado: un cupo nuevo siempre nace libre
public record SlotDTO(String code, VehicleType vehicleType) {
    public ParkingSlot toEntity(UUID id) {
        return new ParkingSlot(id, code, vehicleType, SlotStatus.FREE);
    }
}
