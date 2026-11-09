package dev.brice.parking.core.entity;

import dev.brice.parking.core.constant.SlotStatus;
import dev.brice.parking.core.constant.VehicleType;

import java.util.UUID;

public record ParkingSlot(
        UUID id,
        String code,
        VehicleType vehicleType,
        SlotStatus status
) {
    public ParkingSlot {
        if (id == null) {
            throw new IllegalArgumentException("El id del cupo no puede ser nulo");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El codigo del cupo no puede estar vacio");
        }
        code = code.trim().toUpperCase();
        if (code.length() > 10) {
            throw new IllegalArgumentException("El codigo del cupo no puede superar 10 caracteres");
        }
        if (vehicleType == null) {
            throw new IllegalArgumentException("El tipo de vehiculo del cupo no puede ser nulo");
        }
        if (status == null) {
            throw new IllegalArgumentException("El estado del cupo no puede ser nulo");
        }
    }

    public boolean isFree() {
        return status == SlotStatus.FREE;
    }

    public ParkingSlot occupy() {
        if (status == SlotStatus.OCCUPIED) {
            throw new IllegalArgumentException("El cupo " + code + " ya esta ocupado");
        }
        return new ParkingSlot(id, code, vehicleType, SlotStatus.OCCUPIED);
    }

    public ParkingSlot release() {
        if (status == SlotStatus.FREE) {
            throw new IllegalArgumentException("El cupo " + code + " ya esta libre");
        }
        return new ParkingSlot(id, code, vehicleType, SlotStatus.FREE);
    }
}
