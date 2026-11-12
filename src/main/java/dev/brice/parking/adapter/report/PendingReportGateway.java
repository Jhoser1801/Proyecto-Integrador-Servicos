package dev.brice.parking.adapter.report;

import dev.brice.parking.core.dto.ReceiptDTO;
import dev.brice.parking.core.dto.ReportDataDTO;
import dev.brice.parking.core.port.ReportGateway;

// Adapter temporal: el PDF real (OpenPDF) se implementa despues. Se reemplaza en Application sin tocar el core.
public class PendingReportGateway implements ReportGateway {

    @Override
    public byte[] generateRevenueReport(ReportDataDTO data) {
        throw new UnsupportedOperationException("El reporte PDF todavia no esta disponible");
    }

    @Override
    public byte[] generateReceipt(ReceiptDTO data) {
        throw new UnsupportedOperationException("El reporte PDF todavia no esta disponible");
    }
}
