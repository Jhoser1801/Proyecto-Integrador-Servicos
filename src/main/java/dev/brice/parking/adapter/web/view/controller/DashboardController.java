package dev.brice.parking.adapter.web.view.controller;

import dev.brice.parking.core.constant.SlotStatus;
import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.entity.ParkingSlot;
import dev.brice.parking.core.port.in.ParkingUseCase;
import dev.brice.parking.core.port.in.SlotUseCase;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {
    private final SlotUseCase slots;
    private final ParkingUseCase parking;

    public DashboardController(SlotUseCase slots, ParkingUseCase parking) {
        this.slots = slots;
        this.parking = parking;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        List<ParkingSlot> allSlots = slots.findAllSlots();
        model.addAttribute("slots", allSlots);
        model.addAttribute("freeCars", count(allSlots, VehicleType.CAR));
        model.addAttribute("freeMotorcycles", count(allSlots, VehicleType.MOTORCYCLE));
        model.addAttribute("openTickets", parking.findOpenTickets());
        return "index";
    }

    private long count(List<ParkingSlot> allSlots, VehicleType type) {
        return allSlots.stream().filter(s -> s.vehicleType() == type && s.status() == SlotStatus.FREE).count();
    }
}
