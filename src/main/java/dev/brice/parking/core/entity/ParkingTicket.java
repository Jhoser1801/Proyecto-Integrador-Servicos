package dev.brice.parking.core.entity;

import dev.brice.parking.core.constant.TicketStatus;
import dev.brice.parking.core.constant.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ParkingTicket(
        UUID id,
        String plate,
        VehicleType vehicleType,
        UUID slotId,
        LocalDateTime entryAt,
        LocalDateTime exitAt,
        BigDecimal totalAmount,
        TicketStatus status
) {
    public ParkingTicket {
        if (id == null) {
            throw new IllegalArgumentException("El id del ticket no puede ser nulo");
        }
        if (vehicleType == null) {
            throw new IllegalArgumentException("El tipo de vehiculo no puede ser nulo");
        }
        if (plate == null || plate.isBlank()) {
            throw new IllegalArgumentException("La placa no puede estar vacia");
        }
        plate = plate.trim().toUpperCase();
        if (!vehicleType.isValidPlate(plate)) {
            throw new IllegalArgumentException("La placa " + plate + " no es valida para " + vehicleType.getVisualName().toLowerCase()
                    + ": debe tener " + vehicleType.getPlateDescription() + " (ejemplo " + vehicleType.getPlateExample() + ")");
        }
        if (slotId == null) {
            throw new IllegalArgumentException("El cupo del ticket no puede ser nulo");
        }
        if (entryAt == null) {
            throw new IllegalArgumentException("La fecha de entrada no puede ser nula");
        }
        if (status == null) {
            throw new IllegalArgumentException("El estado del ticket no puede ser nulo");
        }
        if (status == TicketStatus.OPEN && (exitAt != null || totalAmount != null)) {
            throw new IllegalArgumentException("Un ticket abierto no puede tener salida ni monto");
        }
        if (status == TicketStatus.CLOSED) {
            if (exitAt == null || totalAmount == null) {
                throw new IllegalArgumentException("Un ticket cerrado necesita fecha de salida y monto");
            }
            if (exitAt.isBefore(entryAt)) {
                throw new IllegalArgumentException("La salida no puede ser anterior a la entrada");
            }
            if (totalAmount.signum() < 0) {
                throw new IllegalArgumentException("El monto no puede ser negativo");
            }
        }
    }

    public static ParkingTicket open(UUID id, String plate, VehicleType vehicleType, UUID slotId, LocalDateTime entryAt) {
        return new ParkingTicket(id, plate, vehicleType, slotId, entryAt, null, null, TicketStatus.OPEN);
    }

    public boolean isOpen() {
        return status == TicketStatus.OPEN;
    }

    public ParkingTicket close(LocalDateTime exitAt, BigDecimal amount) {
        if (status == TicketStatus.CLOSED) {
            throw new IllegalArgumentException("El ticket ya fue cerrado");
        }
        return new ParkingTicket(id, plate, vehicleType, slotId, entryAt, exitAt, amount, TicketStatus.CLOSED);
    }
}
