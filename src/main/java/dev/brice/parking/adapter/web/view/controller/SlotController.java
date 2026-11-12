package dev.brice.parking.adapter.web.view.controller;

import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.dto.SlotDTO;
import dev.brice.parking.core.service.SlotService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
}
