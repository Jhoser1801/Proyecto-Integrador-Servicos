package dev.brice.parking.adapter.ui.spring.controller;

import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.dto.SlotDTO;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/slots")
public class SlotController {
    private final SlotService slots;

    public SlotController(SlotService slots) {
        this.slots = slots;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("slots", slots.findAllSlots());
        model.addAttribute("vehicleTypes", VehicleType.values());
        model.addAttribute("form", new SlotDTO("", VehicleType.CAR));
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
