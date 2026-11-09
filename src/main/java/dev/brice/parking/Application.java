package dev.brice.parking;

import dev.brice.parking.adapter.repository.inmemory.InMemorySlotRepository;
import dev.brice.parking.adapter.repository.inmemory.InMemoryTariffRepository;
import dev.brice.parking.adapter.repository.inmemory.InMemoryTicketRepository;
import dev.brice.parking.adapter.repository.inmemory.InMemoryUserRepository;
import dev.brice.parking.adapter.report.PendingReportGateway;
import dev.brice.parking.adapter.security.BCryptPasswordHasher;
import dev.brice.parking.adapter.web.rest.RestApi;
import dev.brice.parking.adapter.web.view.ViewApplication;
import dev.brice.parking.core.port.in.ParkingUseCase;
import dev.brice.parking.core.port.in.ReportUseCase;
import dev.brice.parking.core.port.in.SlotUseCase;
import dev.brice.parking.core.port.in.TariffUseCase;
import dev.brice.parking.core.port.in.UserUseCase;
import dev.brice.parking.core.port.out.PasswordHasher;
import dev.brice.parking.core.port.out.ReportGateway;
import dev.brice.parking.core.port.out.SlotRepository;
import dev.brice.parking.core.port.out.TariffRepository;
import dev.brice.parking.core.port.out.TicketRepository;
import dev.brice.parking.core.port.out.UserRepository;
import dev.brice.parking.core.service.ParkingService;
import dev.brice.parking.core.service.ReportService;
import dev.brice.parking.core.service.SlotService;
import dev.brice.parking.core.service.TariffService;
import dev.brice.parking.core.service.UserService;
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
        UserUseCase userUseCase = new UserService(userRepository, passwordHasher);
        SlotUseCase slotUseCase = new SlotService(slotRepository);
        TariffUseCase tariffUseCase = new TariffService(tariffRepository);
        ParkingUseCase parkingUseCase = new ParkingService(ticketRepository, slotRepository, tariffRepository, clock);
        ReportUseCase reportUseCase = new ReportService(ticketRepository, slotRepository, reportGateway);

        SeedData.load(userUseCase, slotUseCase, tariffUseCase, ticketRepository, slotRepository, tariffRepository);

        // Adaptadores de entrada: REST (Javalin) y vistas (Spring + Thymeleaf)
        new RestApi(parkingUseCase, slotUseCase, tariffUseCase, reportUseCase, userUseCase).start(REST_PORT);

        SpringApplication views = new SpringApplication(ViewApplication.class);
        views.addInitializers(context -> {
            var beans = context.getBeanFactory();
            beans.registerSingleton("userUseCase", userUseCase);
            beans.registerSingleton("slotUseCase", slotUseCase);
            beans.registerSingleton("tariffUseCase", tariffUseCase);
            beans.registerSingleton("parkingUseCase", parkingUseCase);
            beans.registerSingleton("reportUseCase", reportUseCase);
            beans.registerSingleton("passwordHasher", passwordHasher);
        });
        views.run(args);
    }
}
