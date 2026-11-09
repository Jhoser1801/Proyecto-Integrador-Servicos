package dev.brice.parking.core.service;

import dev.brice.parking.core.dto.SlotDTO;
import dev.brice.parking.core.entity.ParkingSlot;
import dev.brice.parking.core.port.in.SlotUseCase;
import dev.brice.parking.core.port.out.SlotRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SlotService implements SlotUseCase {
    private final SlotRepository repository;

    public SlotService(SlotRepository repository) {
        this.repository = repository;
    }

    @Override
    public ParkingSlot createSlot(SlotDTO request) {
        ParkingSlot slot = request.toEntity(UUID.randomUUID());
        if (repository.findSlotByCode(slot.code()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un cupo con el codigo " + slot.code());
        }
        repository.saveSlot(slot);
        return slot;
    }

    @Override
    public List<ParkingSlot> findAllSlots() {
        return repository.findAllSlots().stream().sorted(Comparator.comparing(ParkingSlot::code)).toList();
    }

    @Override
    public Optional<ParkingSlot> findSlotById(UUID id) {
        return repository.findSlotById(id);
    }
}
