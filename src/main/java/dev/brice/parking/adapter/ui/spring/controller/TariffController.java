package dev.brice.parking.adapter.ui.spring.controller;

import dev.brice.parking.core.constant.VehicleType;
import dev.brice.parking.core.dto.TariffDTO;
import dev.brice.parking.core.service.TariffService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/tariffs")
public class TariffController {
    private final TariffService tariffs;

    public TariffController(TariffService tariffs) {
        this.tariffs = tariffs;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("tariffs", tariffs.findAllTariffs());
        return "tariffs/list";
    }

    // PUT: actualiza la tarifa de un tipo de vehiculo
    @PutMapping("/{vehicleType}")
    public String update(@PathVariable VehicleType vehicleType,
                         @RequestParam BigDecimal hourlyRate,
                         RedirectAttributes redirect) {
        try {
            tariffs.setTariff(new TariffDTO(vehicleType, hourlyRate));
            redirect.addFlashAttribute("success", "Tarifa de " + vehicleType.getVisualName().toLowerCase() + " actualizada");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tariffs";
    }
}
