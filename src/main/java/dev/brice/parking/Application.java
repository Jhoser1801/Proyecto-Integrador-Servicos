package dev.brice.parking;

import dev.brice.parking.adapter.repository.h2.H2SlotRepository;
import dev.brice.parking.adapter.repository.h2.H2TariffRepository;
import dev.brice.parking.adapter.repository.h2.H2TicketRepository;
import dev.brice.parking.adapter.repository.h2.H2UserRepository;
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

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.time.Clock;

// Raiz de composicion: aqui (y solo aqui) se eligen los adapters y se conectan con el core.
public class Application {
    private static final int REST_PORT = 7070;

    public static void main(String[] args) {
        Clock clock = Clock.systemDefaultZone();

        // Base de datos H2 en un archivo (carpeta data/ del proyecto): los datos se conservan entre arranques.
        // Se puede cambiar sin tocar el codigo con las variables de entorno DB_URL, DB_USER y DB_PASSWORD.
        // Para una base temporal en memoria: DB_URL=jdbc:h2:mem:parkingdb;DB_CLOSE_DELAY=-1
        Jdbi jdbi = Jdbi.create(env("DB_URL", "jdbc:h2:file:./data/parking"), env("DB_USER", "sa"), env("DB_PASSWORD", ""));
        jdbi.installPlugin(new SqlObjectPlugin());

        // Adaptadores de salida (puertos). El orden importa: ticket tiene llave foranea a cupo.
        // Para volver a memoria: new InMemory...Repository() en lugar de new H2...Repository(jdbi)
        UserRepository userRepository = new H2UserRepository(jdbi);
        SlotRepository slotRepository = new H2SlotRepository(jdbi);
        TariffRepository tariffRepository = new H2TariffRepository(jdbi);
        TicketRepository ticketRepository = new H2TicketRepository(jdbi);
        PasswordHasher passwordHasher = new BCryptPasswordHasher();

        // Casos de uso (services del core)
        UserService userService = new UserService(userRepository, passwordHasher);
        SlotService slotService = new SlotService(slotRepository);
        TariffService tariffService = new TariffService(tariffRepository);
        ReportService reportService = new ReportService(ticketRepository, slotRepository);
        ParkingService parkingService = new ParkingService(ticketRepository, slotRepository, tariffRepository, clock);

        // datos de prueba: solo la primera vez, cuando la base esta vacia
        if (userRepository.findAllUsers().isEmpty()) {
            SeedData.load(userService, slotService, tariffService, ticketRepository, slotRepository, tariffRepository);
        }

        // Adaptadores de interfaz: REST (Javalin) y vistas (Spring + Thymeleaf)
        JavalinUI javalinUI = new JavalinUI(parkingService, slotService, tariffService, reportService, userService);
        javalinUI.runServer(REST_PORT);

        SpringUI springUI = new SpringUI(userService, slotService, tariffService, parkingService, reportService, passwordHasher);
        springUI.runServer(args);
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
