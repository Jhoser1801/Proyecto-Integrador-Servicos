package dev.brice.parking;

import dev.brice.parking.adapter.repository.inmemory.InMemorySlotRepository;
import dev.brice.parking.adapter.repository.inmemory.InMemoryTariffRepository;
import dev.brice.parking.adapter.repository.inmemory.InMemoryTicketRepository;
import dev.brice.parking.adapter.repository.inmemory.InMemoryUserRepository;
import dev.brice.parking.adapter.security.BCryptPasswordHasher;
import dev.brice.parking.adapter.ui.javalin.JavalinUI;
import dev.brice.parking.adapter.ui.spring.SpringUI;
import dev.brice.parking.core.service.ParkingService;
import dev.brice.parking.core.service.ReportService;
import dev.brice.parking.core.service.SlotService;
import dev.brice.parking.core.service.TariffService;
import dev.brice.parking.core.service.UserService;
import dev.brice.parking.core.port.PasswordHasher;
import dev.brice.parking.core.port.SlotRepository;
import dev.brice.parking.core.port.TariffRepository;
import dev.brice.parking.core.port.TicketRepository;
import dev.brice.parking.core.port.UserRepository;

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

        // Casos de uso (services del core)
        UserService userService = new UserService(userRepository, passwordHasher);
        SlotService slotService = new SlotService(slotRepository);
        TariffService tariffService = new TariffService(tariffRepository);
        ReportService reportService = new ReportService(ticketRepository, slotRepository);
        ParkingService parkingService = new ParkingService(ticketRepository, slotRepository, tariffRepository, clock);

        SeedData.load(userService, slotService, tariffService, ticketRepository, slotRepository, tariffRepository);

        // Adaptadores de interfaz: REST (Javalin) y vistas (Spring + Thymeleaf)
        JavalinUI javalinUI = new JavalinUI(parkingService, slotService, tariffService, reportService, userService);
        javalinUI.runServer(REST_PORT);

        SpringUI springUI = new SpringUI(userService, slotService, tariffService, parkingService, reportService, passwordHasher);
        springUI.runServer(args);
    }
}
