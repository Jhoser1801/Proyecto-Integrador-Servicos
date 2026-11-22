package dev.brice.parking.adapter.ui.spring.controller;

import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.constant.SlotStatus;
import dev.brice.parking.core.dto.SlotDTO;
import dev.brice.parking.core.entity.ParkingSlot;
import dev.brice.parking.core.service.SlotService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/slots")
public class SlotController {
    private final SlotService slots;

    public SlotController(SlotService slots) {
        this.slots = slots;
    }

    // ?crear abre la ventana de cupo nuevo y ?editar=<id> la de edicion (la dibuja el servidor, sin JavaScript)
    @GetMapping
    public String list(@RequestParam(required = false) String crear,
                       @RequestParam(required = false) UUID editar, Model model) {
        List<ParkingSlot> all = slots.findAllSlots();
        model.addAttribute("slots", all);
        model.addAttribute("vehicleTypes", VehicleType.values());
        model.addAttribute("totalSlots", all.size());
        model.addAttribute("carSlots", all.stream().filter(s -> s.vehicleType() == VehicleType.CAR).count());
        model.addAttribute("motorcycleSlots", all.stream().filter(s -> s.vehicleType() == VehicleType.MOTORCYCLE).count());
        model.addAttribute("freeSlots", all.stream().filter(s -> s.status() == SlotStatus.FREE).count());
        model.addAttribute("showCreate", crear != null);
        model.addAttribute("editing", editar == null ? null : slots.findSlotById(editar).orElse(null));
        return "slots/list";
    }

    @PostMapping
    public String create(@ModelAttribute("form") SlotDTO form, RedirectAttributes redirect) {
        try {
            slots.createSlot(form);
            redirect.addFlashAttribute("success", "Cupo creado");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/slots";
    }

    // PUT: actualiza codigo y tipo de vehiculo de un cupo
    @PutMapping("/{id}")
    public String update(@PathVariable UUID id, @ModelAttribute SlotDTO form, RedirectAttributes redirect) {
        try {
            slots.updateSlot(id, form);
            redirect.addFlashAttribute("success", "Cupo actualizado");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/slots";
    }

    // DELETE: elimina un cupo libre y sin historial
    @DeleteMapping("/{id}")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        try {
            slots.deleteSlot(id);
            redirect.addFlashAttribute("success", "Cupo eliminado");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/slots";
    }
}
