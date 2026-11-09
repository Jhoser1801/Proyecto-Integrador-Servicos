package dev.brice.parking.core.port.in;

import java.time.LocalDate;
import java.util.UUID;

public interface ReportUseCase {

    byte[] generateRevenueReport(LocalDate from, LocalDate to);
    byte[] generateReceipt(UUID ticketId);

}
