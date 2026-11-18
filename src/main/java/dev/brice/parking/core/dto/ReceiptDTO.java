package dev.brice.parking.core.dto;

import dev.brice.parking.core.entity.ParkingTicket;

// Datos del comprobante de salida: el ticket cerrado y el codigo legible del cupo
public record ReceiptDTO(ParkingTicket ticket, String slotCode) {
}
