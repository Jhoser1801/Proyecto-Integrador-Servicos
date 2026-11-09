package dev.brice.parking.adapter.repository.inmemory;

import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.entity.ParkingSlot;
import dev.brice.parking.core.port.out.SlotRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySlotRepository implements SlotRepository {
    private final Map<UUID, ParkingSlot> slots = new ConcurrentHashMap<>();

    @Override
    public void saveSlot(ParkingSlot slot) {
        slots.put(slot.id(), slot);
    }

    @Override
    public Optional<ParkingSlot> findSlotById(UUID id) {
        return Optional.ofNullable(slots.get(id));
    }

    @Override
    public Optional<ParkingSlot> findSlotByCode(String code) {
        return slots.values().stream().filter(slot -> slot.code().equalsIgnoreCase(code)).findFirst();
    }

    @Override
    public List<ParkingSlot> findAllSlots() {
        return List.copyOf(slots.values());
    }

    @Override
    public List<ParkingSlot> findFreeSlotsByType(VehicleType vehicleType) {
        return slots.values().stream()
                .filter(slot -> slot.vehicleType() == vehicleType && slot.isFree())
                .toList();
    }
}
