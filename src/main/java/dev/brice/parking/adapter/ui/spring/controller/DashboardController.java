package dev.brice.parking.adapter.ui.spring.controller;

import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.entity.ParkingSlot;
import dev.brice.parking.core.entity.ParkingTicket;
import dev.brice.parking.core.service.ParkingService;
import dev.brice.parking.core.service.SlotService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class DashboardController {
    private final SlotService slots;
    private final ParkingService parking;

    public DashboardController(SlotService slots, ParkingService parking) {
        this.slots = slots;
        this.parking = parking;
    }

    // Un cupo junto al ticket abierto que lo ocupa (si lo hay): es lo que dibuja el mapa de cupos
    public record Bay(ParkingSlot slot, ParkingTicket ticket) {
        public boolean busy() {
            return ticket != null;
        }
    }

    @GetMapping("/")
    public String dashboard(Model model, Authentication authentication) {
        // Sin sesion, la raiz muestra la pagina publica: el login no tiene boton, se entra escribiendo /login
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/cupos";
        }
        Map<UUID, ParkingTicket> openBySlot = parking.findOpenTickets().stream()
                .collect(Collectors.toMap(ParkingTicket::slotId, Function.identity()));
        List<Bay> bays = slots.findAllSlots().stream()
                .map(slot -> new Bay(slot, openBySlot.get(slot.id())))
                .toList();

        List<Bay> carBays = bays.stream().filter(b -> b.slot().vehicleType() == VehicleType.CAR).toList();
        List<Bay> motorcycleBays = bays.stream().filter(b -> b.slot().vehicleType() == VehicleType.MOTORCYCLE).toList();
        model.addAttribute("carBays", carBays);
        model.addAttribute("motorcycleBays", motorcycleBays);
        model.addAttribute("freeCars", carBays.stream().filter(b -> !b.busy()).count());
        model.addAttribute("freeMotorcycles", motorcycleBays.stream().filter(b -> !b.busy()).count());
        return "index";
    }
}
