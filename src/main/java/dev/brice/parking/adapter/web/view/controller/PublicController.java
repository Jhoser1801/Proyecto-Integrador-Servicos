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

// Pagina publica (sin login): solo entrega CANTIDADES de cupos libres y totales.
// A la vista nunca llegan placas, horas, tickets ni los codigos de los cupos.
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

        model.addAttribute("freeCars", countFree(all, VehicleType.CAR));
        model.addAttribute("totalCars", countAll(all, VehicleType.CAR));
        model.addAttribute("freeMotorcycles", countFree(all, VehicleType.MOTORCYCLE));
        model.addAttribute("totalMotorcycles", countAll(all, VehicleType.MOTORCYCLE));
        return "public/slots";
    }

    private long countFree(List<ParkingSlot> all, VehicleType type) {
        return all.stream().filter(s -> s.vehicleType() == type && s.status() == SlotStatus.FREE).count();
    }

    private long countAll(List<ParkingSlot> all, VehicleType type) {
        return all.stream().filter(s -> s.vehicleType() == type).count();
    }
}
