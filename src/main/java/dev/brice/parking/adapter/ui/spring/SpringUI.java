package dev.brice.parking.adapter.ui.spring;

import dev.brice.parking.core.port.PasswordHasher;
import dev.brice.parking.core.service.ParkingService;
import dev.brice.parking.core.service.ReportService;
import dev.brice.parking.core.service.SlotService;
import dev.brice.parking.core.service.TariffService;
import dev.brice.parking.core.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Adaptador de interfaz web (Spring MVC + Thymeleaf)
@SpringBootApplication
public class SpringUI {
    public SpringUI(UserService userService, SlotService slotService, TariffService tariffService,
                    ParkingService parkingService, ReportService reportService, PasswordHasher passwordHasher) {
        SpringConfig.userService = userService;
        SpringConfig.slotService = slotService;
        SpringConfig.tariffService = tariffService;
        SpringConfig.parkingService = parkingService;
        SpringConfig.reportService = reportService;
        SpringConfig.passwordHasher = passwordHasher;
    }

    public void runServer(String[] args) {
        SpringApplication.run(SpringUI.class, args);
    }
}
