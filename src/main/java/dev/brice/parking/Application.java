package dev.brice.parking;

import dev.brice.parking.adapter.repository.inmemory.InMemorySlotRepository;
import dev.brice.parking.adapter.repository.inmemory.InMemoryTariffRepository;
import dev.brice.parking.adapter.repository.inmemory.InMemoryTicketRepository;
import dev.brice.parking.adapter.repository.inmemory.InMemoryUserRepository;
import dev.brice.parking.adapter.report.PendingReportGateway;
import dev.brice.parking.adapter.security.BCryptPasswordHasher;
import dev.brice.parking.adapter.web.rest.RestApi;
import dev.brice.parking.adapter.web.view.ViewApplication;
import dev.brice.parking.core.service.ParkingService;
import dev.brice.parking.core.service.ReportService;
import dev.brice.parking.core.service.SlotService;
import dev.brice.parking.core.service.TariffService;
import dev.brice.parking.core.service.UserService;
import dev.brice.parking.core.port.PasswordHasher;
import dev.brice.parking.core.port.ReportGateway;
import dev.brice.parking.core.port.SlotRepository;
import dev.brice.parking.core.port.TariffRepository;
import dev.brice.parking.core.port.TicketRepository;
import dev.brice.parking.core.port.UserRepository;
import org.springframework.boot.SpringApplication;

import java.time.Clock;

// Raiz de composicion: aqui (y solo aqui) se eligen los adapters y se conectan con el core.
public class Application {
    private static final int REST_PORT = 7070;

    public static void main(String[] args) {
        Clock clock = Clock.systemDefaultZone();

        // Adaptadores de salida. Persistencia en memoria por ahora; luego se cambia por JDBI en estas lineas.
        SlotRepository slotRepository = new InMemorySlotRepository();
        TicketRepository ticketRepository = new InMemoryTicketRepository();
        TariffRepository tariffRepository = new InMemoryTariffRepository();
        UserRepository userRepository = new InMemoryUserRepository();
        PasswordHasher passwordHasher = new BCryptPasswordHasher();
        ReportGateway reportGateway = new PendingReportGateway(); // luego: OpenPdfReportGateway

        // Casos de uso (services del core)
        UserService userService = new UserService(userRepository, passwordHasher);
        SlotService slotService = new SlotService(slotRepository);
        TariffService tariffService = new TariffService(tariffRepository);
        ParkingService parkingService = new ParkingService(ticketRepository, slotRepository, tariffRepository, clock);
        ReportService reportService = new ReportService(ticketRepository, slotRepository, reportGateway);

        SeedData.load(userService, slotService, tariffService, ticketRepository, slotRepository, tariffRepository);

        // Adaptadores de entrada: REST (Javalin) y vistas (Spring + Thymeleaf)
        new RestApi(parkingService, slotService, tariffService, reportService, userService).start(REST_PORT);

        SpringApplication views = new SpringApplication(ViewApplication.class);
        views.addInitializers(context -> {
            var beans = context.getBeanFactory();
            beans.registerSingleton("userService", userService);
            beans.registerSingleton("slotService", slotService);
            beans.registerSingleton("tariffService", tariffService);
            beans.registerSingleton("parkingService", parkingService);
            beans.registerSingleton("reportService", reportService);
            beans.registerSingleton("passwordHasher", passwordHasher);
        });
        views.run(args);
    }
}
