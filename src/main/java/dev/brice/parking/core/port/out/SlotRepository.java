package dev.brice.parking.core.port.out;

import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.entity.ParkingSlot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SlotRepository {

    void saveSlot(ParkingSlot slot);
    Optional<ParkingSlot> findSlotById(UUID id);
    Optional<ParkingSlot> findSlotByCode(String code);
    List<ParkingSlot> findAllSlots();
    List<ParkingSlot> findFreeSlotsByType(VehicleType vehicleType);

}
