package dev.brice.parking.core.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// Ingresos de un dia; percentOfMax (0 a 100) es la altura de su barra respecto al mejor dia del rango
public record DailyRevenueDTO(LocalDate day, BigDecimal amount, int percentOfMax) {
}
