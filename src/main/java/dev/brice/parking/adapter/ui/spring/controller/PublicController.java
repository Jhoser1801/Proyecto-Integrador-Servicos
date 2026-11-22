package dev.brice.parking.adapter.ui.spring.controller;

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
    // Cantidad libre y total de un tipo de vehiculo, con el estado que se muestra a la gente
    public record Availability(String title, long free, long total) {
        public int percent() {
            return total == 0 ? 0 : (int) (free * 100 / total);
        }

        public String label() {
            return free == 0 ? "Lleno" : percent() <= 20 ? "Pocos cupos" : "Disponible";
        }

        public String tone() {
            return free == 0 ? "busy" : percent() <= 20 ? "warn" : "free";
        }
    }

    private final SlotService slots;

    public PublicController(SlotService slots) {
        this.slots = slots;
    }

    @GetMapping("/cupos")
    public String availableSlots(Model model, HttpServletResponse response) {
        response.setHeader("Refresh", "30"); // el navegador recarga la pagina solo
        List<ParkingSlot> all = slots.findAllSlots();

        Availability cars = new Availability("Carros", countFree(all, VehicleType.CAR), countAll(all, VehicleType.CAR));
        Availability motorcycles = new Availability("Motos", countFree(all, VehicleType.MOTORCYCLE), countAll(all, VehicleType.MOTORCYCLE));
        model.addAttribute("availabilities", List.of(cars, motorcycles));
        model.addAttribute("totalSlots", all.size());
        return "public/slots";
    }

    private long countFree(List<ParkingSlot> all, VehicleType type) {
        return all.stream().filter(s -> s.vehicleType() == type && s.status() == SlotStatus.FREE).count();
    }

    private long countAll(List<ParkingSlot> all, VehicleType type) {
        return all.stream().filter(s -> s.vehicleType() == type).count();
    }
}
