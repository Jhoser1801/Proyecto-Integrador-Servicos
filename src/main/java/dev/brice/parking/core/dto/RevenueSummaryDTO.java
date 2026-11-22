package dev.brice.parking.core.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

// Resumen del reporte de ingresos de un rango de fechas: totales, reparto por tipo de vehiculo y ganancia de cada dia
public record RevenueSummaryDTO(
        LocalDate from,
        LocalDate to,
        BigDecimal total,
        int vehicles,
        BigDecimal carsRevenue,
        BigDecimal motorcyclesRevenue,
        List<DailyRevenueDTO> days
) {
    public BigDecimal dailyAverage() {
        return days.isEmpty() ? BigDecimal.ZERO : total.divide(BigDecimal.valueOf(days.size()), 0, RoundingMode.HALF_UP);
    }

    public double carsPercent() {
        return percentOfTotal(carsRevenue);
    }

    public double motorcyclesPercent() {
        return percentOfTotal(motorcyclesRevenue);
    }

    private double percentOfTotal(BigDecimal part) {
        if (total.signum() == 0) {
            return 0;
        }
        return part.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP).doubleValue();
    }
}
