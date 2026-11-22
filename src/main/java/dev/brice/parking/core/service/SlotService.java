package dev.brice.parking.core.service;

import dev.brice.parking.core.dto.SlotDTO;
import dev.brice.parking.core.entity.ParkingSlot;
import dev.brice.parking.core.port.SlotRepository;
import dev.brice.parking.core.port.TicketRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SlotService {
    private final SlotRepository repository;
    private final TicketRepository ticketRepository;

    public SlotService(SlotRepository repository, TicketRepository ticketRepository) {
        this.repository = repository;
        this.ticketRepository = ticketRepository;
    }

    public ParkingSlot createSlot(SlotDTO request) {
        ParkingSlot slot = request.toEntity(UUID.randomUUID());
        if (repository.findSlotByCode(slot.code()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un cupo con el codigo " + slot.code());
        }
        repository.saveSlot(slot);
        return slot;
    }

    // Actualiza codigo y tipo. Solo un cupo libre; el tipo no cambia si el cupo ya tiene tickets (el historial quedaria inconsistente)
    public ParkingSlot updateSlot(UUID id, SlotDTO request) {
        ParkingSlot current = findExistingSlot(id);
        if (!current.isFree()) {
            throw new IllegalArgumentException("El cupo " + current.code() + " esta ocupado: registre la salida antes de editarlo");
        }
        ParkingSlot updated = current.updateData(request.code(), request.vehicleType());
        repository.findSlotByCode(updated.code()).ifPresent(other -> {
            if (!other.id().equals(id)) {
                throw new IllegalArgumentException("Ya existe un cupo con el codigo " + updated.code());
            }
        });
        if (updated.vehicleType() != current.vehicleType() && ticketRepository.existsTicketBySlotId(id)) {
            throw new IllegalArgumentException("El cupo " + current.code() + " ya tiene tickets: no se puede cambiar su tipo de vehiculo");
        }
        repository.saveSlot(updated);
        return updated;
    }

    // Elimina un cupo libre que nunca se uso (con tickets se perderia el historial y los reportes)
    public void deleteSlot(UUID id) {
        ParkingSlot slot = findExistingSlot(id);
        if (!slot.isFree()) {
            throw new IllegalArgumentException("El cupo " + slot.code() + " esta ocupado: registre la salida antes de eliminarlo");
        }
        if (ticketRepository.existsTicketBySlotId(id)) {
            throw new IllegalArgumentException("El cupo " + slot.code() + " ya tiene tickets registrados: no se puede eliminar para conservar el historial");
        }
        repository.deleteSlotById(id);
    }

    public List<ParkingSlot> findAllSlots() {
        return repository.findAllSlots().stream().sorted(Comparator.comparing(ParkingSlot::code)).toList();
    }

    public Optional<ParkingSlot> findSlotById(UUID id) {
        return repository.findSlotById(id);
    }

    private ParkingSlot findExistingSlot(UUID id) {
        return repository.findSlotById(id).orElseThrow(() -> new IllegalArgumentException("El cupo no existe"));
    }
}
