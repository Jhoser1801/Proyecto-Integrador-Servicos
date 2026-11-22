package dev.brice.parking;

import dev.brice.parking.core.constant.UserRole;
import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.dto.CheckInDTO;
import dev.brice.parking.core.dto.SlotDTO;
import dev.brice.parking.core.dto.TariffDTO;
import dev.brice.parking.core.dto.UserDTO;
import dev.brice.parking.core.port.SlotRepository;
import dev.brice.parking.core.port.TariffRepository;
import dev.brice.parking.core.port.TicketRepository;
import dev.brice.parking.core.port.UserRepository;
import dev.brice.parking.core.service.ParkingService;
import dev.brice.parking.core.service.SlotService;
import dev.brice.parking.core.service.TariffService;
import dev.brice.parking.core.service.UserService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

// Datos de prueba.
// Se crea con los servicios del core, asi cada dato pasa por las mismas reglas que en la aplicacion.
final class DataSeed {
    private final UserRepository userRepository;
    private final UserService users;
    private final SlotService slots;
    private final TariffService tariffs;
    private final TicketRepository ticketRepository;
    private final SlotRepository slotRepository;
    private final TariffRepository tariffRepository;
    private final String adminUser;
    private final String adminPassword;

    DataSeed(UserRepository userRepository, UserService users, SlotService slots, TariffService tariffs,
             TicketRepository ticketRepository, SlotRepository slotRepository, TariffRepository tariffRepository,
             String adminUser, String adminPassword) {
        this.userRepository = userRepository;
        this.users = users;
        this.slots = slots;
        this.tariffs = tariffs;
        this.ticketRepository = ticketRepository;
        this.slotRepository = slotRepository;
        this.tariffRepository = tariffRepository;
        this.adminUser = adminUser;
        this.adminPassword = adminPassword;
    }

    void run() {

        // Guarda: si ya hay datos en la base, para no volver a sembrar
        if (!userRepository.findAllUsers().isEmpty()) {
            return;
        }

        // 1. Crear los usuarios por defecto (el administrador sale de ADMIN_USER y ADMIN_PASSWORD)
        users.createUser(new UserDTO(adminUser, adminPassword, UserRole.ADMIN));
        users.createUser(new UserDTO("operator", "operator123", UserRole.OPERATOR));

        // 2. Tarifas por hora
        tariffs.setTariff(new TariffDTO(VehicleType.CAR, new BigDecimal("3000")));
        tariffs.setTariff(new TariffDTO(VehicleType.MOTORCYCLE, new BigDecimal("1500")));

        // 3. Cupos de carros
        slots.createSlot(new SlotDTO("A1", VehicleType.CAR));
        slots.createSlot(new SlotDTO("A2", VehicleType.CAR));
        slots.createSlot(new SlotDTO("A3", VehicleType.CAR));
        slots.createSlot(new SlotDTO("A4", VehicleType.CAR));
        slots.createSlot(new SlotDTO("A5", VehicleType.CAR));

        // 4. Cupos de motos
        slots.createSlot(new SlotDTO("M1", VehicleType.MOTORCYCLE));
        slots.createSlot(new SlotDTO("M2", VehicleType.MOTORCYCLE));
        slots.createSlot(new SlotDTO("M3", VehicleType.MOTORCYCLE));

        // 5. Tickets de dias anteriores, ya cerrados (con un reloj propio para entrar y salir "en el pasado")
        SeedClock clock = new SeedClock();
        ParkingService history = new ParkingService(ticketRepository, slotRepository, tariffRepository, clock);
        Instant now = Instant.now();
        closedVisit(history, clock, now, "ABC123", VehicleType.CAR, 3, 95);
        closedVisit(history, clock, now, "XYZ987", VehicleType.CAR, 2, 240);
        closedVisit(history, clock, now, "MOT45A", VehicleType.MOTORCYCLE, 2, 50);
        closedVisit(history, clock, now, "JKL456", VehicleType.CAR, 1, 130);
        closedVisit(history, clock, now, "QWE12B", VehicleType.MOTORCYCLE, 1, 310);

        // 6. Vehiculos que estan dentro ahora mismo
        openVisit(history, clock, now, "DEF789", VehicleType.CAR, 45);
        openVisit(history, clock, now, "MNO21C", VehicleType.MOTORCYCLE, 20);
    }

    // Entra hace (daysAgo dias + minutes) y sale hace daysAgo dias: ticket cerrado con su cobro
    private void closedVisit(ParkingService history, SeedClock clock, Instant now,
                             String plate, VehicleType type, int daysAgo, int minutes) {
        Instant exit = now.minusSeconds(daysAgo * 86_400L);
        clock.set(exit.minusSeconds(minutes * 60L));
        var ticket = history.checkIn(new CheckInDTO(plate, type));
        clock.set(exit);
        history.checkOut(ticket.id());
    }

    // Entro hace minutesAgo minutos y sigue dentro
    private void openVisit(ParkingService history, SeedClock clock, Instant now,
                           String plate, VehicleType type, int minutesAgo) {
        clock.set(now.minusSeconds(minutesAgo * 60L));
        history.checkIn(new CheckInDTO(plate, type));
    }

    private static final class SeedClock extends Clock {
        private Instant now = Instant.now();

        void set(Instant instant) {
            now = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.systemDefault();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
