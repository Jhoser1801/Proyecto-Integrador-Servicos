package dev.brice.parking.adapter.repository.h2;

import dev.brice.parking.adapter.repository.h2.dao.SlotDAO;
import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.entity.ParkingSlot;
import dev.brice.parking.core.port.SlotRepository;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class H2SlotRepository implements SlotRepository {

    private final Jdbi jdbi;
    private final SlotDAO slotDAO;

    public H2SlotRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
        this.slotDAO = jdbi.onDemand(SlotDAO.class);

        setupTables();
    }

    @Override
    public void saveSlot(ParkingSlot slot) {
        slotDAO.upsertSlot(slot.id(), slot.code(), slot.vehicleType(), slot.status());
    }

    @Override
    public Optional<ParkingSlot> findSlotById(UUID id) {
        return slotDAO.findSlotById(id);
    }

    @Override
    public Optional<ParkingSlot> findSlotByCode(String code) {
        return slotDAO.findSlotByCode(code);
    }

    @Override
    public List<ParkingSlot> findAllSlots() {
        return slotDAO.findAllSlots();
    }

    @Override
    public List<ParkingSlot> findFreeSlotsByType(VehicleType vehicleType) {
        return slotDAO.findFreeSlotsByType(vehicleType);
    }

    private void setupTables() {
        jdbi.useHandle(handle -> handle.execute("""
                create table if not exists parking_slot (
                    slot_id uuid primary key,
                    code varchar(10) not null unique,
                    vehicle_type varchar(16) not null,
                    status varchar(16) not null
                )"""));
    }
}
