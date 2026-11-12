package dev.brice.parking.adapter.web.view.controller;

import dev.brice.parking.core.constant.SlotStatus;
import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.entity.ParkingSlot;
import dev.brice.parking.core.service.SlotService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

// Pagina publica (sin login): solo dice que cupos estan libres. No expone placas ni horas de nadie.
@Controller
public class PublicController {
    private final SlotService slots;

    public PublicController(SlotService slots) {
        this.slots = slots;
    }

    @GetMapping("/cupos")
    public String availableSlots(Model model, HttpServletResponse response) {
        response.setHeader("Refresh", "30"); // el navegador recarga la pagina solo
        List<ParkingSlot> all = slots.findAllSlots();
        List<ParkingSlot> cars = all.stream().filter(s -> s.vehicleType() == VehicleType.CAR).toList();
        List<ParkingSlot> motorcycles = all.stream().filter(s -> s.vehicleType() == VehicleType.MOTORCYCLE).toList();
        model.addAttribute("carSlots", cars);
        model.addAttribute("motorcycleSlots", motorcycles);
        model.addAttribute("freeCars", cars.stream().filter(s -> s.status() == SlotStatus.FREE).count());
        model.addAttribute("freeMotorcycles", motorcycles.stream().filter(s -> s.status() == SlotStatus.FREE).count());
        return "public/slots";
    }
}
