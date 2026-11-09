package dev.brice.parking;

import dev.brice.parking.core.constant.UserRole;
import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.dto.CheckInDTO;
import dev.brice.parking.core.dto.SlotDTO;
import dev.brice.parking.core.dto.TariffDTO;
import dev.brice.parking.core.dto.UserDTO;
import dev.brice.parking.core.port.in.SlotUseCase;
import dev.brice.parking.core.port.in.TariffUseCase;
import dev.brice.parking.core.port.in.UserUseCase;
import dev.brice.parking.core.port.out.SlotRepository;
import dev.brice.parking.core.port.out.TariffRepository;
import dev.brice.parking.core.port.out.TicketRepository;
import dev.brice.parking.core.service.ParkingService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

// Datos de prueba: usuarios, cupos, tarifas y algunos tickets historicos para que el reporte tenga datos.
final class SeedData {
    private SeedData() {
    }

    static void load(UserUseCase users, SlotUseCase slots, TariffUseCase tariffs,
                     TicketRepository ticketRepository, SlotRepository slotRepository, TariffRepository tariffRepository) {
        users.createUser(new UserDTO("admin", "admin123", UserRole.ADMIN));
        users.createUser(new UserDTO("operator", "operator123", UserRole.OPERATOR));

        tariffs.setTariff(new TariffDTO(VehicleType.CAR, new BigDecimal("3000")));
        tariffs.setTariff(new TariffDTO(VehicleType.MOTORCYCLE, new BigDecimal("1500")));

        for (String code : new String[]{"A1", "A2", "A3", "A4", "A5"}) {
            slots.createSlot(new SlotDTO(code, VehicleType.CAR));
        }
        for (String code : new String[]{"M1", "M2", "M3"}) {
            slots.createSlot(new SlotDTO(code, VehicleType.MOTORCYCLE));
        }

        // Tickets de dias anteriores: se usa un reloj propio para entrar y salir "en el pasado"
        SeedClock clock = new SeedClock();
        ParkingService history = new ParkingService(ticketRepository, slotRepository, tariffRepository, clock);
        Instant now = Instant.now();
        record Visit(String plate, VehicleType type, int daysAgo, int minutes) { }
        for (Visit v : new Visit[]{
                new Visit("ABC123", VehicleType.CAR, 3, 95),
                new Visit("XYZ987", VehicleType.CAR, 2, 240),
                new Visit("MOT45A", VehicleType.MOTORCYCLE, 2, 50),
                new Visit("JKL456", VehicleType.CAR, 1, 130),
                new Visit("QWE12B", VehicleType.MOTORCYCLE, 1, 310)}) {
            Instant exit = now.minusSeconds(v.daysAgo() * 86_400L);
            clock.set(exit.minusSeconds(v.minutes() * 60L));
            var ticket = history.checkIn(new CheckInDTO(v.plate(), v.type()));
            clock.set(exit);
            history.checkOut(ticket.id());
        }

        // Dos vehiculos dentro ahora mismo
        clock.set(now.minusSeconds(45 * 60));
        history.checkIn(new CheckInDTO("DEF789", VehicleType.CAR));
        clock.set(now.minusSeconds(20 * 60));
        history.checkIn(new CheckInDTO("MNO21C", VehicleType.MOTORCYCLE));
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
