package dev.brice.parking;

import dev.brice.parking.adapter.repository.inmemory.InMemorySlotRepository;
import dev.brice.parking.adapter.repository.inmemory.InMemoryTariffRepository;
import dev.brice.parking.adapter.repository.inmemory.InMemoryTicketRepository;
import dev.brice.parking.adapter.repository.inmemory.InMemoryUserRepository;
import dev.brice.parking.adapter.security.BCryptPasswordHasher;
import dev.brice.parking.core.constant.SlotStatus;
import dev.brice.parking.core.constant.UserRole;
import dev.brice.parking.core.entity.ParkingSlot;
import dev.brice.parking.core.entity.ParkingTicket;
import dev.brice.parking.core.service.SlotService;
import dev.brice.parking.core.service.TariffService;
import dev.brice.parking.core.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataSeedTest {
    private InMemoryUserRepository users;
    private InMemorySlotRepository slots;
    private InMemoryTariffRepository tariffs;
    private InMemoryTicketRepository tickets;
    private UserService userService;

    @BeforeEach
    void setUp() {
        users = new InMemoryUserRepository();
        slots = new InMemorySlotRepository();
        tariffs = new InMemoryTariffRepository();
        tickets = new InMemoryTicketRepository();
        userService = new UserService(users, new BCryptPasswordHasher());
    }

    private DataSeed seed(String adminUser, String adminPassword) {
        return new DataSeed(users, userService, new SlotService(slots), new TariffService(tariffs),
                tickets, slots, tariffs, adminUser, adminPassword);
    }

    @Test
    void seedsUsersTariffsSlotsAndTickets() {
        seed("admin", "admin123").run();

        assertEquals(2, users.findAllUsers().size());
        assertEquals(2, tariffs.findAllTariffs().size());
        assertEquals(8, slots.findAllSlots().size());
        assertEquals(7, tickets.findAllTickets().size());
        assertEquals(2, tickets.findAllTickets().stream().filter(ParkingTicket::isOpen).count());
        // los dos vehiculos que estan dentro ocupan cupos
        assertEquals(2, slots.findAllSlots().stream().filter(s -> s.status() == SlotStatus.OCCUPIED).count());
        assertTrue(userService.authenticate("admin", "admin123").isPresent());
        assertTrue(userService.authenticate("operator", "operator123").isPresent());
    }

    @Test
    void doesNotSeedTwice() {
        DataSeed seed = seed("admin", "admin123");
        seed.run();
        seed.run(); // la guarda lo evita: sin errores de "usuario ya existe" ni datos duplicados

        assertEquals(2, users.findAllUsers().size());
        assertEquals(8, slots.findAllSlots().size());
        assertEquals(7, tickets.findAllTickets().size());
    }

    @Test
    void doesNotTouchADatabaseThatAlreadyHasUsers() {
        userService.createUser(new dev.brice.parking.core.dto.UserDTO("existing", "secret1", UserRole.ADMIN));

        seed("admin", "admin123").run();

        assertEquals(1, users.findAllUsers().size());
        assertTrue(slots.findAllSlots().isEmpty());
    }

    @Test
    void adminComesFromTheConfiguration() {
        seed("jefe", "clave123").run();

        var admin = userService.authenticate("jefe", "clave123").orElseThrow();
        assertEquals(UserRole.ADMIN, admin.role());
        assertTrue(userService.authenticate("admin", "admin123").isEmpty());
    }
}
