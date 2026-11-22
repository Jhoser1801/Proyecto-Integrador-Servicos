package dev.brice.parking.adapter.ui.spring.controller;

import dev.brice.parking.core.constant.SlotStatus;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDateTime;
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
    public String dashboard(@RequestParam(required = false) String slot,
                            @RequestParam(required = false) String confirmar,
                            Model model, Authentication authentication) {
        // Sin sesion, la raiz muestra la pagina publica: el login no tiene boton, se entra escribiendo /login
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/cupos";
        }
        Map<UUID, ParkingTicket> openBySlot = parking.findOpenTickets().stream()
                .collect(Collectors.toMap(ParkingTicket::slotId, Function.identity()));
        List<Bay> bays = slots.findAllSlots().stream()
                .map(s -> new Bay(s, openBySlot.get(s.id())))
                .toList();

        List<Bay> carBays = bays.stream().filter(b -> b.slot().vehicleType() == VehicleType.CAR).toList();
        List<Bay> motorcycleBays = bays.stream().filter(b -> b.slot().vehicleType() == VehicleType.MOTORCYCLE).toList();
        long free = bays.stream().filter(b -> b.slot().status() == SlotStatus.FREE).count();
        model.addAttribute("carBays", carBays);
        model.addAttribute("motorcycleBays", motorcycleBays);
        model.addAttribute("freeCars", carBays.stream().filter(b -> !b.busy()).count());
        model.addAttribute("freeMotorcycles", motorcycleBays.stream().filter(b -> !b.busy()).count());
        model.addAttribute("totalSlots", bays.size());
        model.addAttribute("freeSlots", free);
        model.addAttribute("busySlots", bays.size() - free);
        model.addAttribute("occupancyPercent", bays.isEmpty() ? 0 : (bays.size() - free) * 100 / bays.size());

        // El cupo elegido en el mapa llega como ?slot=A1 (sin JavaScript: cada cupo es un enlace)
        Bay selected = slot == null ? null : bays.stream()
                .filter(b -> b.slot().code().equalsIgnoreCase(slot.trim())).findFirst().orElse(null);
        model.addAttribute("selected", selected);
        if (selected != null && selected.busy()) {
            model.addAttribute("stay", StayFormat.format(Duration.between(selected.ticket().entryAt(), LocalDateTime.now())));
            model.addAttribute("confirmExit", confirmar != null);
        }
        return "index";
    }
}
