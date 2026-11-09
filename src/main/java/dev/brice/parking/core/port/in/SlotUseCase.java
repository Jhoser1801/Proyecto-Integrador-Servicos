package dev.brice.parking.core.port.in;

import dev.brice.parking.core.dto.SlotDTO;
import dev.brice.parking.core.entity.ParkingSlot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SlotUseCase {

    ParkingSlot createSlot(SlotDTO request);
    List<ParkingSlot> findAllSlots();
    Optional<ParkingSlot> findSlotById(UUID id);

}
