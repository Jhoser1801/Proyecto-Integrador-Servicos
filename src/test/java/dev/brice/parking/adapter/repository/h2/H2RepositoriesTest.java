package dev.brice.parking.adapter.repository.h2;

import dev.brice.parking.core.constant.SlotStatus;
import dev.brice.parking.core.constant.TicketStatus;
import dev.brice.parking.core.constant.UserRole;
import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.dto.CheckInDTO;
import dev.brice.parking.core.entity.ParkingSlot;
import dev.brice.parking.core.entity.ParkingTicket;
import dev.brice.parking.core.entity.Tariff;
import dev.brice.parking.core.entity.User;
import dev.brice.parking.core.service.ParkingService;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas de los adapters JDBI contra una H2 en memoria distinta para cada prueba
class H2RepositoriesTest {
    private static final LocalDateTime ENTRY = LocalDateTime.of(2026, 11, 10, 8, 0);

    private H2UserRepository users;
    private H2SlotRepository slots;
    private H2TariffRepository tariffs;
    private H2TicketRepository tickets;

    @BeforeEach
    void setUp() {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        jdbi.installPlugin(new SqlObjectPlugin());
        users = new H2UserRepository(jdbi);
        slots = new H2SlotRepository(jdbi);
        tariffs = new H2TariffRepository(jdbi);
        tickets = new H2TicketRepository(jdbi);
    }

    private ParkingSlot newSlot(String code, VehicleType type) {
        ParkingSlot slot = new ParkingSlot(UUID.randomUUID(), code, type, SlotStatus.FREE);
        slots.saveSlot(slot);
        return slot;
    }

    @Test
    void slotIsSavedFoundAndUpdated() {
        ParkingSlot slot = newSlot("A1", VehicleType.CAR);

        assertEquals(slot, slots.findSlotById(slot.id()).orElseThrow());
        assertEquals(slot, slots.findSlotByCode("a1").orElseThrow());

        slots.saveSlot(slot.occupy());
        assertEquals(SlotStatus.OCCUPIED, slots.findSlotById(slot.id()).orElseThrow().status());
        assertEquals(1, slots.findAllSlots().size());
    }

    @Test
    void freeSlotsAreFilteredByTypeAndStatus() {
        ParkingSlot car1 = newSlot("A1", VehicleType.CAR);
        newSlot("A2", VehicleType.CAR);
        newSlot("M1", VehicleType.MOTORCYCLE);
        slots.saveSlot(car1.occupy());

        var free = slots.findFreeSlotsByType(VehicleType.CAR);

        assertEquals(1, free.size());
        assertEquals("A2", free.get(0).code());
    }

    @Test
    void ticketRoundTripWithNullsAndClosing() {
        ParkingSlot slot = newSlot("A1", VehicleType.CAR);
        ParkingTicket open = ParkingTicket.open(UUID.randomUUID(), "ABC123", VehicleType.CAR, slot.id(), ENTRY);
        tickets.saveTicket(open);

        ParkingTicket loaded = tickets.findTicketById(open.id()).orElseThrow();
        assertEquals(open, loaded);
        assertNull(loaded.exitAt());
        assertNull(loaded.totalAmount());
        assertEquals(open, tickets.findOpenTicketByPlate("abc123").orElseThrow());

        ParkingTicket closed = open.close(ENTRY.plusHours(2), new BigDecimal("6000"));
        tickets.saveTicket(closed);

        ParkingTicket reloaded = tickets.findTicketById(open.id()).orElseThrow();
        assertEquals(TicketStatus.CLOSED, reloaded.status());
        assertEquals(0, new BigDecimal("6000").compareTo(reloaded.totalAmount()));
        assertEquals(ENTRY.plusHours(2), reloaded.exitAt());
        assertTrue(tickets.findOpenTicketByPlate("ABC123").isEmpty());
        assertEquals(1, tickets.findAllTickets().size());
    }

    @Test
    void closedTicketsAreFilteredByExitDate() {
        ParkingSlot slot = newSlot("A1", VehicleType.CAR);
        ParkingTicket inside = ParkingTicket.open(UUID.randomUUID(), "AAA111", VehicleType.CAR, slot.id(), ENTRY)
                .close(ENTRY.plusHours(1), new BigDecimal("3000"));
        ParkingTicket outside = ParkingTicket.open(UUID.randomUUID(), "BBB222", VehicleType.CAR, slot.id(), ENTRY.plusDays(5))
                .close(ENTRY.plusDays(5).plusHours(1), new BigDecimal("3000"));
        ParkingTicket stillOpen = ParkingTicket.open(UUID.randomUUID(), "CCC333", VehicleType.CAR, slot.id(), ENTRY);
        tickets.saveTicket(inside);
        tickets.saveTicket(outside);
        tickets.saveTicket(stillOpen);

        var found = tickets.findClosedTicketsBetween(ENTRY.toLocalDate().atStartOfDay(), ENTRY.toLocalDate().plusDays(1).atStartOfDay());

        assertEquals(1, found.size());
        assertEquals("AAA111", found.get(0).plate());
    }

    @Test
    void ticketNeedsAnExistingSlot() {
        ParkingTicket orphan = ParkingTicket.open(UUID.randomUUID(), "ABC123", VehicleType.CAR, UUID.randomUUID(), ENTRY);
        assertThrows(RuntimeException.class, () -> tickets.saveTicket(orphan));
    }

    @Test
    void tariffIsReplacedKeepingOnePerVehicleType() {
        UUID id = UUID.randomUUID();
        tariffs.saveTariff(new Tariff(id, VehicleType.CAR, new BigDecimal("3000")));
        tariffs.saveTariff(new Tariff(id, VehicleType.CAR, new BigDecimal("3500")));

        assertEquals(1, tariffs.findAllTariffs().size());
        assertEquals(0, new BigDecimal("3500").compareTo(tariffs.findTariffByVehicleType(VehicleType.CAR).orElseThrow().hourlyRate()));
        assertTrue(tariffs.findTariffByVehicleType(VehicleType.MOTORCYCLE).isEmpty());
    }

    @Test
    void userIsFoundByUsernameIgnoringCase() {
        User user = new User(UUID.randomUUID(), "Admin", "hash", UserRole.ADMIN);
        users.saveUser(user);

        assertEquals(user, users.findUserByUsername("admin").orElseThrow());
        assertEquals(user, users.findUserByUsername(" ADMIN ").orElseThrow());
        assertTrue(users.findUserByUsername("nobody").isEmpty());
        assertTrue(users.findUserByUsername(null).isEmpty());
        assertEquals(1, users.findAllUsers().size());
    }

    @Test
    void parkingFlowWorksOnTopOfH2() {
        newSlot("A1", VehicleType.CAR);
        tariffs.saveTariff(new Tariff(UUID.randomUUID(), VehicleType.CAR, new BigDecimal("3000")));
        ParkingService parking = new ParkingService(tickets, slots, tariffs, Clock.systemDefaultZone());

        ParkingTicket ticket = parking.checkIn(new CheckInDTO("abc123", VehicleType.CAR));
        assertEquals(SlotStatus.OCCUPIED, slots.findSlotById(ticket.slotId()).orElseThrow().status());
        assertThrows(IllegalArgumentException.class, () -> parking.checkIn(new CheckInDTO("ABC123", VehicleType.CAR)));

        ParkingTicket closed = parking.checkOut(ticket.id());
        assertEquals(0, new BigDecimal("3000").compareTo(closed.totalAmount()));
        assertEquals(SlotStatus.FREE, slots.findSlotById(ticket.slotId()).orElseThrow().status());
    }
}
