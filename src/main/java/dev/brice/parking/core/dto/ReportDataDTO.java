package dev.brice.parking.core.dto;

import dev.brice.parking.core.entity.ParkingTicket;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Datos ya calculados del reporte de ingresos por rango de fechas; el adapter PDF solo los dibuja
public record ReportDataDTO(
        String title,
        LocalDate from,
        LocalDate to,
        List<ParkingTicket> tickets,
        BigDecimal totalRevenue
) {
}
