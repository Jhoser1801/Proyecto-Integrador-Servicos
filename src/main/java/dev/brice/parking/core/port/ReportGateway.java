package dev.brice.parking.core.port;

import dev.brice.parking.core.dto.ReceiptDTO;
import dev.brice.parking.core.dto.ReportDataDTO;

// Los dos devuelven el PDF ya armado (bytes) para que cualquier adapter web lo pueda descargar
public interface ReportGateway {

    byte[] generateRevenueReport(ReportDataDTO data);
    byte[] generateReceipt(ReceiptDTO data);

}
