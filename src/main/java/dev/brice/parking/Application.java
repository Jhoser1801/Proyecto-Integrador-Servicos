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
import org.jdbi.v3.core.ConnectionException;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.util.Properties;

// Raiz de composicion: aqui (y solo aqui) se eligen los adapters y se conectan con el core.
public class Application {
    private static final int REST_PORT = 7070;

    public static void main(String[] args) {
        Clock clock = Clock.systemDefaultZone();

        // Base de datos H2 en un archivo (carpeta data/ del proyecto): los datos se conservan entre arranques.
        // La URL, el usuario y la contrasena salen de application.properties (app.db.*), igual que en
        // business-sales-project, y se pueden reemplazar con las variables de entorno DB_URL, DB_USER y DB_PASSWORD.
        // La primera vez, H2 crea la base con ese usuario y contrasena; despues hay que usar los mismos.
        Jdbi jdbi = Jdbi.create(
                setting("DB_URL", "app.db.url", "jdbc:h2:file:./data/parking"),
                setting("DB_USER", "app.db.username", "parking"),
                setting("DB_PASSWORD", "app.db.password", "parking2026"));
        jdbi.installPlugin(new SqlObjectPlugin());
        checkDatabaseAccess(jdbi);

        // Adaptadores de salida (puertos). El orden importa: ticket tiene llave foranea a cupo.
        // Para volver a memoria: new InMemory...Repository() en lugar de new H2...Repository(jdbi)
        UserRepository userRepository = new H2UserRepository(jdbi);
        SlotRepository slotRepository = new H2SlotRepository(jdbi);
        TariffRepository tariffRepository = new H2TariffRepository(jdbi);
        TicketRepository ticketRepository = new H2TicketRepository(jdbi);
        PasswordHasher passwordHasher = new BCryptPasswordHasher();

        // Casos de uso (services del core)
        UserService userService = new UserService(userRepository, passwordHasher);
        SlotService slotService = new SlotService(slotRepository, ticketRepository);
        TariffService tariffService = new TariffService(tariffRepository);
        ReportService reportService = new ReportService(ticketRepository, slotRepository);
        ParkingService parkingService = new ParkingService(ticketRepository, slotRepository, tariffRepository, clock);

        // datos de prueba: DataSeed solo siembra la primera vez, cuando la base esta vacia
        new DataSeed(userRepository, userService, slotService, tariffService, ticketRepository, slotRepository, tariffRepository,
                env("ADMIN_USER", "admin"), env("ADMIN_PASSWORD", "admin123")).run();

        // Adaptadores de interfaz: REST (Javalin) y vistas (Spring + Thymeleaf)
        JavalinUI javalinUI = new JavalinUI(parkingService, slotService, tariffService, reportService, userService);
        javalinUI.runServer(REST_PORT);

        SpringUI springUI = new SpringUI(userService, slotService, tariffService, parkingService, reportService, passwordHasher);
        springUI.runServer(args);
    }

    // Orden de prioridad: variable de entorno, luego application.properties, luego el valor por defecto
    private static String setting(String envName, String propertyName, String defaultValue) {
        String fromEnv = env(envName, null);
        if (fromEnv != null) {
            return fromEnv;
        }
        String fromFile = loadProperties().getProperty(propertyName);
        return fromFile == null || fromFile.isBlank() ? defaultValue : fromFile.trim();
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream in = Application.class.getResourceAsStream("/application.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            // sin archivo de propiedades se usan los valores por defecto
        }
        return properties;
    }

    // Si la base ya existia con otro usuario o contrasena, avisa claro en vez de mostrar un error largo de H2
    private static void checkDatabaseAccess(Jdbi jdbi) {
        try {
            jdbi.useHandle(handle -> handle.execute("select 1"));
        } catch (ConnectionException e) {
            throw new IllegalStateException("No se pudo abrir la base de datos H2. Si la carpeta data/ ya existia, "
                    + "fue creada con otro usuario o contrasena: usa los mismos (app.db.username y app.db.password en "
                    + "application.properties) o borra la carpeta data/ para crear la base de nuevo. Causa: "
                    + e.getCause().getMessage(), e);
        }
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
