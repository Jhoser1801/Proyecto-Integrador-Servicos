package dev.brice.parking.adapter.ui.spring;

import dev.brice.parking.core.port.PasswordHasher;
import dev.brice.parking.core.service.ParkingService;
import dev.brice.parking.core.service.ReportService;
import dev.brice.parking.core.service.SlotService;
import dev.brice.parking.core.service.TariffService;
import dev.brice.parking.core.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Entrega a Spring los objetos del core que ya se crearon en Application (los mismos que usa Javalin)
@Configuration
public class SpringConfig {
    public static UserService userService;
    public static SlotService slotService;
    public static TariffService tariffService;
    public static ParkingService parkingService;
    public static ReportService reportService;
    public static PasswordHasher passwordHasher;

    @Bean
    public UserService injectUserService() {
        return userService;
    }

    @Bean
    public SlotService injectSlotService() {
        return slotService;
    }

    @Bean
    public TariffService injectTariffService() {
        return tariffService;
    }

    @Bean
    public ParkingService injectParkingService() {
        return parkingService;
    }

    @Bean
    public ReportService injectReportService() {
        return reportService;
    }

    @Bean
    public PasswordHasher injectPasswordHasher() {
        return passwordHasher;
    }
}
